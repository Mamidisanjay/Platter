package com.platter.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long userId;
    private String action;
    private String entityType;
    private Long entityId;
    private Instant timestamp;
    private String ipAddress;
    private String metadata;
    protected AuditLog() { }
    public AuditLog(Long userId, String action, String entityType, Long entityId, String ipAddress, String metadata) { this.userId=userId; this.action=action; this.entityType=entityType; this.entityId=entityId; this.timestamp=Instant.now(); this.ipAddress=ipAddress; this.metadata=metadata; }
    public Long getId(){return id;} public Long getUserId(){return userId;} public String getAction(){return action;} public String getEntityType(){return entityType;} public Long getEntityId(){return entityId;} public Instant getTimestamp(){return timestamp;} public String getIpAddress(){return ipAddress;} public String getMetadata(){return metadata;}
}
