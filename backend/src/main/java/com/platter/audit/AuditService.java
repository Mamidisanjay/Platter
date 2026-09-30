package com.platter.audit;

import com.platter.entity.AuditLog;
import com.platter.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    private final AuditLogRepository auditLogRepository;
    public AuditService(AuditLogRepository auditLogRepository) { this.auditLogRepository = auditLogRepository; }
    public void record(Long userId, String action, String entityType, Long entityId, String ipAddress, String metadata) {
        auditLogRepository.save(new AuditLog(userId, action, entityType, entityId, ipAddress, metadata));
        log.info("audit action={} entityType={} entityId={} userId={}", action, entityType, entityId, userId);
    }
}
