package com.carrental.service;

import com.carrental.dto.response.AdminBrandResponse;
import com.carrental.dto.response.AdminCarTypeResponse;
import com.carrental.dto.response.AdminRegionResponse;
import java.util.List;

public interface AdminCategoryService {

    // CarType
    List<AdminCarTypeResponse> getTypeList(String keyword);
    void addCarType(String name, String description);
    void editCarType(Long id, String name, String description, String status);
    void deleteCarType(Long id);

    // Brand
    List<AdminBrandResponse> getBrandList(String keyword);
    void addBrand(String name, String logoUrl);
    void editBrand(Long id, String name, String status, String logoUrl);
    void deleteBrand(Long id);

    // Region
    List<AdminRegionResponse> getRegionList(String keyword);
    void addRegion(String name);
    void editRegion(Long id, String name, String status);
    void deleteRegion(Long id);
}