package com.carrental.service.impl;

import com.carrental.dto.request.CarRequest;
import com.carrental.entity.*;
import com.carrental.enums.CarStatus;
import com.carrental.enums.OrderStatus;
import com.carrental.repository.*;
import com.carrental.service.OwnerService;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerServiceImpl implements OwnerService {

    private final CarRepository          carRepository;
    private final CarImageRepository     carImageRepository;
    private final RentalOrderRepository  rentalOrderRepository;
    private final BrandRepository        brandRepository;
    private final CarTypeRepository      carTypeRepository;
    private final RegionRepository       regionRepository;
    private final UserRepository         userRepository;

    @Value("${upload.path:uploads}")
    private String uploadPath;

    // ====================== THỐNG KÊ ======================

    @Override
    public long countCarsByOwner(Long ownerId) {
        User owner = userRepository.findById(ownerId).orElse(null);
        if (owner == null) return 0;
        return carRepository.findByOwner(owner).size();
    }

    @Override
    public long countCarsByOwnerAndStatus(Long ownerId, CarStatus status) {
        User owner = userRepository.findById(ownerId).orElse(null);
        if (owner == null) return 0;
        return carRepository.findByOwner(owner).stream()
                .filter(car -> car.getStatus() == status)
                .count();
    }

    @Override
    public long countOrdersByOwnerAndStatus(Long ownerId, OrderStatus status) {
        User owner = userRepository.findById(ownerId).orElse(null);
        if (owner == null) return 0;
        return rentalOrderRepository.findByCarOwner(owner).stream()
                .filter(order -> order.getStatus() == status)
                .count();
    }

    @Override
    public long getTotalRevenueByOwner(Long ownerId) {
        User owner = userRepository.findById(ownerId).orElse(null);
        if (owner == null) return 0;
        return rentalOrderRepository.findByCarOwner(owner).stream()
                .filter(order -> order.getStatus() == OrderStatus.Completed)
                .mapToLong(order -> order.getTotalAmount().longValue())
                .sum();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RentalOrder> getRecentOrdersByOwner(Long ownerId, int limit) {
        User owner = userRepository.findById(ownerId).orElse(null);
        if (owner == null) return new ArrayList<>();

        List<RentalOrder> orders = rentalOrderRepository
                .findByCarOwnerUserIdOrderByCreatedAtDesc(ownerId);

        if (orders.size() > limit) {
            orders = orders.subList(0, limit);
        }

        for (RentalOrder order : orders) {
            Hibernate.initialize(order.getCustomer());
            Hibernate.initialize(order.getCar());
            if (order.getCar() != null) {
                Hibernate.initialize(order.getCar().getBrand());
                Hibernate.initialize(order.getCar().getCarType());
            }
        }

        return orders;
    }

    // ====================== QUẢN LÝ XE ======================

    @Override
    @Transactional(readOnly = true)
    public Page<Car> getCarsByOwner(Long ownerId, String status, Pageable pageable) {
        User owner = userRepository.findById(ownerId).orElse(null);
        if (owner == null) return Page.empty(pageable);

        List<Car> allCars = carRepository.findByOwner(owner);

        if (status != null && !status.isEmpty()) {
            try {
                CarStatus carStatus = CarStatus.valueOf(status);
                allCars = allCars.stream()
                        .filter(car -> car.getStatus() == carStatus)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException ignored) {}
        }

        allCars.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        int start = (int) pageable.getOffset();
        int end   = Math.min(start + pageable.getPageSize(), allCars.size());

        if (start > allCars.size()) {
            return new PageImpl<>(new ArrayList<>(), pageable, allCars.size());
        }

        List<Car> pageContent = allCars.subList(start, end);

        for (Car car : pageContent) {
            Hibernate.initialize(car.getBrand());
            Hibernate.initialize(car.getCarType());
            Hibernate.initialize(car.getRegion());
        }

        return new PageImpl<>(pageContent, pageable, allCars.size());
    }

    @Override
    public Car getCarByIdAndOwner(Long carId, Long ownerId) {
        return carRepository.findById(carId)
                .filter(car -> car.getOwner().getUserId().equals(ownerId))
                .orElse(null);
    }

    @Override
    @Transactional
    public Car createCar(Long ownerId, CarRequest request) {
        // Kiểm tra owner tồn tại
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chủ xe"));

        // Kiểm tra biển số trùng
        if (carRepository.existsByLicensePlate(request.getLicensePlate().trim())) {
            throw new RuntimeException("Biển số xe \"" + request.getLicensePlate() + "\" đã tồn tại trong hệ thống");
        }

        Brand   brand   = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hãng xe"));
        CarType carType = carTypeRepository.findById(request.getCarTypeId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy loại xe"));
        Region  region  = regionRepository.findById(request.getRegionId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khu vực"));

        Car car = Car.builder()
                .owner(owner)
                .brand(brand)
                .carType(carType)
                .region(region)
                .modelName(request.getModelName().trim())
                .licensePlate(request.getLicensePlate().trim().toUpperCase())
                .seats(request.getSeats())
                .yearOfManufacture(request.getYearOfManufacture())
                .fuel(request.getFuel())
                .transmission(request.getTransmission())
                .pricePerDay(request.getPricePerDay())
                .pickupLocation(request.getPickupLocation().trim())
                .features(request.getFeatures() != null ? request.getFeatures().trim() : null)
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .status(CarStatus.Pending)
                .avgRating(BigDecimal.ZERO)
                .build();

        Car savedCar = carRepository.save(car);

        // Lưu ảnh xe nếu có
        saveCarImages(savedCar, request.getImages());

        return savedCar;
    }

    @Override
    @Transactional
    public Car updateCar(Long carId, Long ownerId, CarRequest request) {
        Car car = getCarByIdAndOwner(carId, ownerId);
        if (car == null) {
            throw new RuntimeException("Không tìm thấy xe hoặc bạn không có quyền sửa");
        }

        // Kiểm tra biển số trùng (trừ chính xe đang sửa)
        if (!car.getLicensePlate().equalsIgnoreCase(request.getLicensePlate().trim())) {
            if (carRepository.existsByLicensePlate(request.getLicensePlate().trim())) {
                throw new RuntimeException("Biển số xe \"" + request.getLicensePlate() + "\" đã tồn tại trong hệ thống");
            }
        }

        Brand   brand   = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hãng xe"));
        CarType carType = carTypeRepository.findById(request.getCarTypeId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy loại xe"));
        Region  region  = regionRepository.findById(request.getRegionId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khu vực"));

        car.setBrand(brand);
        car.setCarType(carType);
        car.setRegion(region);
        car.setModelName(request.getModelName().trim());
        car.setLicensePlate(request.getLicensePlate().trim().toUpperCase());
        car.setSeats(request.getSeats());
        car.setYearOfManufacture(request.getYearOfManufacture());
        car.setFuel(request.getFuel());
        car.setTransmission(request.getTransmission());
        car.setPricePerDay(request.getPricePerDay());
        car.setPickupLocation(request.getPickupLocation().trim());
        car.setFeatures(request.getFeatures() != null ? request.getFeatures().trim() : null);
        car.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);

        // Nếu xe đang Active thì khi sửa cần duyệt lại
        if (car.getStatus() == CarStatus.Active) {
            car.setStatus(CarStatus.Pending);
        }
        // Nếu xe bị Rejected, khi sửa xong reset về Pending để admin duyệt lại
        if (car.getStatus() == CarStatus.Rejected) {
            car.setStatus(CarStatus.Pending);
            car.setRejectReason(null);
        }

        Car savedCar = carRepository.save(car);

        // Nếu có ảnh mới thì xóa ảnh cũ và lưu ảnh mới
        boolean hasNewImages = request.getImages() != null &&
                request.getImages().stream().anyMatch(f -> f != null && !f.isEmpty());
        if (hasNewImages) {
            carImageRepository.deleteByCarCarId(carId);
            saveCarImages(savedCar, request.getImages());
        }

        return savedCar;
    }

    @Override
    @Transactional
    public void toggleCarStatus(Long carId, Long ownerId) {
        Car car = getCarByIdAndOwner(carId, ownerId);
        if (car == null) {
            throw new RuntimeException("Không tìm thấy xe hoặc bạn không có quyền");
        }
        switch (car.getStatus()) {
            case Active   -> car.setStatus(CarStatus.Inactive);
            case Inactive -> car.setStatus(CarStatus.Active);
            case Pending  -> throw new RuntimeException("Xe đang chờ duyệt, không thể thay đổi trạng thái");
            case Rejected -> throw new RuntimeException("Xe bị từ chối, vui lòng chỉnh sửa và gửi lại");
        }
        carRepository.save(car);
    }

    @Override
    @Transactional
    public void deleteCar(Long carId, Long ownerId) {
        Car car = getCarByIdAndOwner(carId, ownerId);
        if (car == null) {
            throw new RuntimeException("Không tìm thấy xe hoặc bạn không có quyền xóa");
        }
        if (car.getStatus() == CarStatus.Pending || car.getStatus() == CarStatus.Rejected) {
            carImageRepository.deleteByCarCarId(carId);
            carRepository.delete(car);
        } else {
            throw new RuntimeException("Chỉ có thể xóa xe đang ở trạng thái chờ duyệt hoặc bị từ chối");
        }
    }

    // ====================== QUẢN LÝ ĐƠN HÀNG ======================

    @Override
    @Transactional(readOnly = true)
    public Page<RentalOrder> getOrdersByOwner(Long ownerId, OrderStatus status, Pageable pageable) {
        User owner = userRepository.findById(ownerId).orElse(null);
        if (owner == null) return Page.empty(pageable);

        List<RentalOrder> allOrders = rentalOrderRepository.findByCarOwner(owner);

        if (status != null) {
            allOrders = allOrders.stream()
                    .filter(o -> o.getStatus() == status)
                    .collect(Collectors.toList());
        }

        allOrders.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        int start = (int) pageable.getOffset();
        int end   = Math.min(start + pageable.getPageSize(), allOrders.size());

        List<RentalOrder> pageContent = (start > allOrders.size())
                ? new ArrayList<>()
                : allOrders.subList(start, end);

        for (RentalOrder order : pageContent) {
            Hibernate.initialize(order.getCustomer());
            Hibernate.initialize(order.getCar());
            if (order.getCar() != null) {
                Hibernate.initialize(order.getCar().getBrand());
            }
        }

        return new PageImpl<>(pageContent, pageable, allOrders.size());
    }

    @Override
    public RentalOrder getOrderByIdAndOwner(Long orderId, Long ownerId) {
        return rentalOrderRepository.findById(orderId)
                .filter(order -> order.getCar().getOwner().getUserId().equals(ownerId))
                .orElse(null);
    }

    @Override
    @Transactional
    public void confirmOrder(Long orderId, Long ownerId) {
        RentalOrder order = getOrderByIdAndOwner(orderId, ownerId);
        if (order == null) throw new RuntimeException("Không tìm thấy đơn hàng");
        if (order.getStatus() != OrderStatus.Pending)
            throw new RuntimeException("Đơn hàng không ở trạng thái chờ xác nhận");
        order.setStatus(OrderStatus.Confirmed);
        rentalOrderRepository.save(order);
    }

    @Override
    @Transactional
    public void rejectOrder(Long orderId, Long ownerId, String reason) {
        RentalOrder order = getOrderByIdAndOwner(orderId, ownerId);
        if (order == null) throw new RuntimeException("Không tìm thấy đơn hàng");
        if (order.getStatus() != OrderStatus.Pending)
            throw new RuntimeException("Đơn hàng không ở trạng thái chờ xác nhận");
        order.setStatus(OrderStatus.Rejected);
        order.setCancelReason(reason);
        rentalOrderRepository.save(order);
    }

    @Override
    @Transactional
    public void completeOrder(Long orderId, Long ownerId) {
        RentalOrder order = getOrderByIdAndOwner(orderId, ownerId);
        if (order == null) throw new RuntimeException("Không tìm thấy đơn hàng");
        if (order.getStatus() != OrderStatus.InProgress)
            throw new RuntimeException("Đơn hàng không ở trạng thái đang thuê");
        order.setStatus(OrderStatus.Completed);
        order.setActualReturnTime(LocalDateTime.now());
        rentalOrderRepository.save(order);
    }

    // ====================== DỮ LIỆU DANH MỤC ======================

    @Override
    public List<Brand> getAllActiveBrands() {
        return brandRepository.findByStatus(com.carrental.enums.CategoryStatus.Active);
    }

    @Override
    public List<CarType> getAllActiveCarTypes() {
        return carTypeRepository.findByStatus(com.carrental.enums.CategoryStatus.Active);
    }

    @Override
    public List<Region> getAllActiveRegions() {
        return regionRepository.findByStatus(com.carrental.enums.CategoryStatus.Active);
    }

    // ====================== PRIVATE HELPERS ======================

    /**
     * Lưu danh sách ảnh xe vào thư mục uploads/cars/ và ghi vào DB.
     * sortOrder bắt đầu từ 0 — ảnh đầu tiên là ảnh bìa (thumbnail).
     */
    private void saveCarImages(Car car, List<MultipartFile> images) {
        if (images == null || images.isEmpty()) return;

        int order = 0;
        for (MultipartFile file : images) {
            if (file == null || file.isEmpty()) continue;

            String imageUrl = saveImageFile(file, "cars");

            CarImage carImage = CarImage.builder()
                    .car(car)
                    .imageUrl(imageUrl)
                    .sortOrder(order++)
                    .build();
            carImageRepository.save(carImage);
        }
    }

    /**
     * Lưu một file ảnh vào đường dẫn uploads/{subDir}/ trên server.
     * Trả về URL tương đối để lưu vào DB, ví dụ: /uploads/cars/abc123.jpg
     *
     * @param file   file upload từ form
     * @param subDir thư mục con bên trong uploads (ví dụ: "cars")
     * @return đường dẫn URL lưu vào DB
     */
    private String saveImageFile(MultipartFile file, String subDir) {
        // Validate content-type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Chỉ chấp nhận file ảnh (JPG, PNG, WEBP...)");
        }

        // Validate kích thước tối đa 5MB
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("Ảnh xe không được vượt quá 5MB");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = (originalFilename != null && originalFilename.contains("."))
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase()
                    : ".jpg";

            String filename  = UUID.randomUUID() + extension;
            Path   uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize().resolve(subDir);

            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            Path filePath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), filePath,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + subDir + "/" + filename;

        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi lưu ảnh xe: " + e.getMessage());
        }
    }
}