package com.platter.service;

import com.platter.dto.MenuItemRequest;
import com.platter.dto.MenuItemResponse;
import java.util.List;

public interface MenuService {
    List<MenuItemResponse> findAvailableMenu(Long restaurantId);
    MenuItemResponse findById(Long id);
    MenuItemResponse create(Long restaurantId, MenuItemRequest request);
    MenuItemResponse update(Long id, MenuItemRequest request);
    void delete(Long id);
    MenuItemResponse updateAvailability(Long id, boolean available);
}
