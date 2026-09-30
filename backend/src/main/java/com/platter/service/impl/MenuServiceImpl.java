package com.platter.service.impl;

import com.platter.dto.MenuItemRequest;
import com.platter.dto.MenuItemResponse;
import com.platter.entity.Category;
import com.platter.entity.MenuItem;
import com.platter.entity.Restaurant;
import com.platter.exception.ResourceNotFoundException;
import com.platter.repository.CategoryRepository;
import com.platter.repository.MenuItemRepository;
import com.platter.repository.RestaurantRepository;
import com.platter.service.MenuService;
import com.platter.search.RestaurantSearchIndexer;
import com.platter.audit.AuditService;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MenuServiceImpl implements MenuService {
    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final CategoryRepository categoryRepository;
    private final RestaurantSearchIndexer searchIndexer;
    private final AuditService auditService;

    public MenuServiceImpl(MenuItemRepository menuItemRepository, RestaurantRepository restaurantRepository, CategoryRepository categoryRepository, RestaurantSearchIndexer searchIndexer, AuditService auditService) {
        this.menuItemRepository = menuItemRepository;
        this.restaurantRepository = restaurantRepository;
        this.categoryRepository = categoryRepository;
        this.searchIndexer = searchIndexer;
        this.auditService = auditService;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "restaurant-menu", key = "#restaurantId")
    public List<MenuItemResponse> findAvailableMenu(Long restaurantId) {
        return menuItemRepository.findByRestaurantIdAndAvailableTrueOrderByCategoryNameAscNameAsc(restaurantId).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MenuItemResponse findById(Long id) {
        return toResponse(findItem(id));
    }

    @Override
    @CacheEvict(cacheNames = "restaurant-menu", key = "#restaurantId")
    public MenuItemResponse create(Long restaurantId, MenuItemRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId).orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + restaurantId));
        Category category = findCategory(request.categoryId());
        MenuItem saved = menuItemRepository.save(new MenuItem(request.name().trim(), request.description(), request.price(), request.imageUrl(), restaurant, category));
        auditService.record(null, "MENU_CREATED", "MENU_ITEM", saved.getId(), null, "restaurantId=" + restaurantId);
        searchIndexer.reindexAfterCommit(restaurantId);
        return toResponse(saved);
    }

    @Override
    @CacheEvict(cacheNames = "restaurant-menu", allEntries = true)
    public MenuItemResponse update(Long id, MenuItemRequest request) {
        MenuItem item = findItem(id);
        item.update(request.name().trim(), request.description(), request.price(), request.imageUrl(), findCategory(request.categoryId()));
        auditService.record(null, "MENU_UPDATED", "MENU_ITEM", item.getId(), null, "restaurantId=" + item.getRestaurant().getId());
        searchIndexer.reindexAfterCommit(item.getRestaurant().getId());
        return toResponse(item);
    }

    @Override
    @CacheEvict(cacheNames = "restaurant-menu", allEntries = true)
    public void delete(Long id) {
        MenuItem item = findItem(id);
        Long restaurantId = item.getRestaurant().getId();
        menuItemRepository.delete(item);
        auditService.record(null, "MENU_DELETED", "MENU_ITEM", item.getId(), null, "restaurantId=" + restaurantId);
        searchIndexer.reindexAfterCommit(restaurantId);
    }

    @Override
    @CacheEvict(cacheNames = "restaurant-menu", allEntries = true)
    public MenuItemResponse updateAvailability(Long id, boolean available) {
        MenuItem item = findItem(id);
        item.setAvailable(available);
        searchIndexer.reindexAfterCommit(item.getRestaurant().getId());
        return toResponse(item);
    }

    private MenuItem findItem(Long id) {
        return menuItemRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + id));
    }

    private Category findCategory(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
    }

    private MenuItemResponse toResponse(MenuItem item) {
        return new MenuItemResponse(item.getId(), item.getName(), item.getDescription(), item.getPrice(), item.getImageUrl(), item.getCategory().getName(), item.isAvailable(), item.getRestaurant().getId());
    }
}
