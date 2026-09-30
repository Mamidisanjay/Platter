package com.platter.dto;

import java.time.Instant;

public record AuditLogResponse(Long id, Long userId, String action, String entityType, Long entityId, Instant timestamp, String ipAddress, String metadata) { }
