package com.carrental.service;

import com.carrental.dto.response.CarListResponse;
import java.util.List;

public interface CarService {

    List<CarListResponse> getActiveCars();

    CarListResponse getCarById(Long carId);

    List<CarListResponse> getActiveCarsByRegion(Long regionId);
    
    List<CarListResponse> searchActiveCarsByCity(String city);
}