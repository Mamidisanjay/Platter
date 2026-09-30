package com.platter.lock;

public interface InventoryLockService {
    String tryLock(Long menuItemId);
    void release(Long menuItemId, String token);
}
