package com.platter.dto;

import java.time.Instant;

public record ReviewResponse(Long id, Long userId, String userName, int rating, String content, Instant createdAt, Instant updatedAt, boolean ownReview) { }
