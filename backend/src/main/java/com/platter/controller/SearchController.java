package com.platter.controller;

import com.platter.dto.PageResponse;
import com.platter.dto.RestaurantResponse;
import com.platter.service.SearchService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
@Validated
public class SearchController {
    private final SearchService searchService;
    public SearchController(SearchService searchService) { this.searchService = searchService; }
    @GetMapping
    public PageResponse<RestaurantResponse> search(@RequestParam @NotBlank String q, @RequestParam(defaultValue = "0") @Min(0) int page, @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) { return searchService.search(q, page, size); }
}
