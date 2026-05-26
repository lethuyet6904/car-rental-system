package com.carrental.service;

import java.util.List;

import com.carrental.entity.Region;

public interface RegionService {

    /**
     * Lấy danh sách Region đang hoạt động (status = Active).
     * Dùng cho dropdown tìm kiếm trên trang Home và trang Car List.
     *
     * TẠI SAO chỉ lấy Active?
     * → Vì Region có field "status" (Active/Hidden).
     *   Admin có thể ẩn 1 region khi không muốn hiển thị nữa.
     *   Nếu lấy tất cả, dropdown sẽ hiện cả region đã bị ẩn → sai logic.
     */
    List<Region> getActiveRegions();
}
