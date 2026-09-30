package com.platter.search;

import com.platter.entity.Restaurant;
import com.platter.repository.RestaurantRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class RestaurantSearchIndexer {
    private static final Logger log = LoggerFactory.getLogger(RestaurantSearchIndexer.class);
    private final RestaurantRepository restaurantRepository;
    private final RestaurantSearchRepository searchRepository;

    public RestaurantSearchIndexer(RestaurantRepository restaurantRepository, RestaurantSearchRepository searchRepository) { this.restaurantRepository = restaurantRepository; this.searchRepository = searchRepository; }

    public void reindexAfterCommit(Long restaurantId) {
        Runnable action = () -> reindex(restaurantId);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { action.run(); }
            });
        } else action.run();
    }

    public void deleteAfterCommit(Long restaurantId) {
        Runnable action = () -> safelyDelete(restaurantId);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { action.run(); }
            });
        } else action.run();
    }

    @Scheduled(fixedDelayString = "${platter.search.reconcile-delay-ms:300000}")
    public void reconcile() {
        try { restaurantRepository.findAll().forEach(restaurant -> searchRepository.save(toDocument(restaurant))); }
        catch (RuntimeException exception) { log.warn("Elasticsearch reconciliation unavailable; PostgreSQL remains authoritative"); }
    }

    private void reindex(Long restaurantId) {
        try { restaurantRepository.findById(restaurantId).map(this::toDocument).ifPresent(searchRepository::save); }
        catch (RuntimeException exception) { log.warn("Elasticsearch index update failed for restaurantId={}", restaurantId); }
    }

    private void safelyDelete(Long restaurantId) {
        try { searchRepository.deleteById(restaurantId); }
        catch (RuntimeException exception) { log.warn("Elasticsearch index delete failed for restaurantId={}", restaurantId); }
    }

    private RestaurantSearchDocument toDocument(Restaurant restaurant) { return new RestaurantSearchDocument(restaurant.getId(), restaurant.getName(), restaurant.getCuisine(), restaurant.getRating(), restaurant.getDeliveryTimeMinutes(), restaurant.getPriceTier(), restaurant.getTag(), restaurant.isAvailable()); }
}
