package com.platter.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "idempotency_records")
public class IdempotencyRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String idempotencyKey;
    private Long userId;
    private String requestHash;
    private String resourceType;
    private Long resourceId;
    private String status;
    private Instant createdAt;

    protected IdempotencyRecord() { }
    public IdempotencyRecord(String idempotencyKey, Long userId, String requestHash, String resourceType, Long resourceId, String status) { this.idempotencyKey = idempotencyKey; this.userId = userId; this.requestHash = requestHash; this.resourceType = resourceType; this.resourceId = resourceId; this.status = status; this.createdAt = Instant.now(); }
    public Long getResourceId() { return resourceId; }
    public String getRequestHash() { return requestHash; }
}
