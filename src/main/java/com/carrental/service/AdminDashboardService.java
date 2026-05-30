package com.carrental.service;

import com.carrental.dto.response.DashboardResponse;

public interface AdminDashboardService {
    DashboardResponse getStats(String period);
}