package com.platter.lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class RedisInventoryLockServiceTest {
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    @AfterEach
    void tearDown() { executor.shutdownNow(); }

    @Test
    void onlyOneConcurrentReservationCanAcquireTheSameInventoryLock() throws Exception {
        AtomicBoolean occupied = new AtomicBoolean();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> occupied.compareAndSet(false, true));

        RedisInventoryLockService service = new RedisInventoryLockService(redisTemplate);
        List<Callable<String>> attempts = List.of(() -> service.tryLock(7L), () -> service.tryLock(7L));
        List<Future<String>> results = executor.invokeAll(attempts);
        List<String> acquired = new ArrayList<>();
        for (Future<String> result : results) if (result.get() != null) acquired.add(result.get());

        assertThat(acquired).hasSize(1);
    }
}
