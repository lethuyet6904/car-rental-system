package com.carrental.service.impl;

import com.carrental.dto.response.AdminBrandResponse;
import com.carrental.dto.response.AdminCarTypeResponse;
import com.carrental.dto.response.AdminRegionResponse;
import com.carrental.entity.Brand;
import com.carrental.entity.CarType;
import com.carrental.entity.Region;
import com.carrental.enums.CategoryStatus;
import com.carrental.repository.BrandRepository;
import com.carrental.repository.CarTypeRepository;
import com.carrental.repository.RegionRepository;
import com.carrental.service.AdminCategoryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCategoryServiceImpl implements AdminCategoryService {

    private final CarTypeRepository carTypeRepository;
    private final BrandRepository   brandRepository;
    private final RegionRepository  regionRepository;

    // ── CarType ──────────────────────────────────────────────────

    @Override
    public List<AdminCarTypeResponse> getTypeList(String keyword) {
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return carTypeRepository.findByKeyword(kw)
                .stream()
                .map(t -> AdminCarTypeResponse.from(t,
                        carTypeRepository.countCarsByTypeId(t.getCarTypeId())))
                .toList();
    }

    @Override
    @Transactional
    public void addCarType(String name, String description) {
        if (carTypeRepository.existsByTypeName(name.trim())) {
            throw new IllegalArgumentException("Loại xe \"" + name.trim() + "\" đã tồn tại");
        }
        CarType type = CarType.builder()
                .typeName(name.trim())
                .description(description != null ? description.trim() : null)
                .status(CategoryStatus.Active)
                .build();
        carTypeRepository.save(type);
    }

    @Override
    @Transactional
    public void editCarType(Long id, String name, String description, String status) {
        CarType type = carTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy loại xe id=" + id));
        type.setTypeName(name.trim());
        type.setDescription(description != null ? description.trim() : null);
        type.setStatus(CategoryStatus.valueOf(status));
        carTypeRepository.save(type);
    }

    @Override
    @Transactional
    public void deleteCarType(Long id) {
        if (!carTypeRepository.existsById(id)) {
            throw new EntityNotFoundException("Không tìm thấy loại xe id=" + id);
        }
        carTypeRepository.deleteById(id);
    }

    // ── Brand ────────────────────────────────────────────────────

    @Override
    public List<AdminBrandResponse> getBrandList(String keyword) {
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return brandRepository.findByKeyword(kw)
                .stream()
                .map(b -> AdminBrandResponse.from(b,
                        brandRepository.countCarsByBrandId(b.getBrandId())))
                .toList();
    }

    @Override
    @Transactional
    public void addBrand(String name) {
        if (brandRepository.existsByBrandName(name.trim())) {
            throw new IllegalArgumentException("Hãng xe \"" + name.trim() + "\" đã tồn tại");
        }
        Brand brand = Brand.builder()
                .brandName(name.trim())
                .status(CategoryStatus.Active)
                .build();
        brandRepository.save(brand);
    }

    @Override
    @Transactional
    public void editBrand(Long id, String name, String status) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hãng xe id=" + id));
        brand.setBrandName(name.trim());
        brand.setStatus(CategoryStatus.valueOf(status));
        brandRepository.save(brand);
    }

    @Override
    @Transactional
    public void deleteBrand(Long id) {
        if (!brandRepository.existsById(id)) {
            throw new EntityNotFoundException("Không tìm thấy hãng xe id=" + id);
        }
        brandRepository.deleteById(id);
    }

    // ── Region ───────────────────────────────────────────────────

    @Override
    public List<AdminRegionResponse> getRegionList(String keyword) {
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return regionRepository.findByKeyword(kw)
                .stream()
                .map(r -> AdminRegionResponse.from(r,
                        regionRepository.countActiveCarsByRegionId(r.getRegionId())))
                .toList();
    }

    @Override
    @Transactional
    public void addRegion(String name) {
        if (regionRepository.existsByRegionName(name.trim())) {
            throw new IllegalArgumentException("Khu vực \"" + name.trim() + "\" đã tồn tại");
        }
        Region region = Region.builder()
                .regionName(name.trim())
                .status(CategoryStatus.Active)
                .build();
        regionRepository.save(region);
    }

    @Override
    @Transactional
    public void editRegion(Long id, String name, String status) {
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khu vực id=" + id));
        region.setRegionName(name.trim());
        region.setStatus(CategoryStatus.valueOf(status));
        regionRepository.save(region);
    }

    @Override
    @Transactional
    public void deleteRegion(Long id) {
        if (!regionRepository.existsById(id)) {
            throw new EntityNotFoundException("Không tìm thấy khu vực id=" + id);
        }
        regionRepository.deleteById(id);
    }
}