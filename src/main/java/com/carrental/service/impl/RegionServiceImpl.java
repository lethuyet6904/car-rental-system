package com.carrental.service.impl;

import com.carrental.entity.Region;
import com.carrental.enums.CategoryStatus;
import com.carrental.repository.RegionRepository;
import com.carrental.service.RegionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * RegionServiceImpl — Triển khai RegionService.
 *
 * GIẢI THÍCH CÁC ANNOTATION:
 *
 * @Service → Đánh dấu class này là một Spring Bean thuộc tầng Service.
 *            Spring sẽ tự động tạo 1 instance duy nhất (Singleton) và quản lý.
 *            Khi ai đó @Autowired RegionService, Spring sẽ inject class này vào.
 *
 * @RequiredArgsConstructor (Lombok) → Tự động tạo constructor cho tất cả field "final".
 *            Thay vì viết:
 *              public RegionServiceImpl(RegionRepository regionRepository) {
 *                  this.regionRepository = regionRepository;
 *              }
 *            Lombok sẽ tạo hộ, giúp code ngắn gọn hơn.
 *            Spring sẽ inject RegionRepository qua constructor này (Constructor Injection).
 */
@Service
@RequiredArgsConstructor
public class RegionServiceImpl implements RegionService {

    // "final" nghĩa là field này PHẢI được gán giá trị khi tạo object (qua constructor).
    // @RequiredArgsConstructor sẽ tạo constructor nhận RegionRepository làm tham số.
    // Spring Boot nhìn thấy constructor cần RegionRepository → tự động inject vào.
    private final RegionRepository regionRepository;

    /**
     * Lấy danh sách Region đang Active.
     *
     * LUỒNG HOẠT ĐỘNG:
     * 1. Controller gọi: regionService.getActiveRegions()
     * 2. Method này gọi xuống: regionRepository.findByStatus(CategoryStatus.Active)
     * 3. RegionRepository kế thừa JpaRepository, Spring Data JPA sẽ tự tạo SQL query:
     *    → SELECT * FROM Region WHERE status = 'Active'
     * 4. Kết quả trả về List<Region> cho Controller
     *
     * TẠI SAO dùng findByStatus() mà không tự viết @Query?
     * → Spring Data JPA có tính năng "Derived Query Methods":
     *   Chỉ cần đặt tên method theo convention (findBy + TenField),
     *   JPA sẽ tự động sinh ra câu SQL tương ứng. Rất tiện lợi!
     */
    @Override
    public List<Region> getActiveRegions() {
        return regionRepository.findByStatus(CategoryStatus.Active);
    }
}
