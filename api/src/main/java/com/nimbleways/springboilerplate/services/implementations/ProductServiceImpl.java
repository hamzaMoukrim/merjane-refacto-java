package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
@AllArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    private final NotificationService notificationService;

    public void notifyDelay(int leadTime, Product product) {
        product.setLeadTime(leadTime);
        productRepository.save(product);
        notificationService.sendDelayNotification(leadTime, product.getName());
    }

    public void processProduct(Product product) {
        switch (product.getType()) {
            case "NORMAL" -> handleNormalProduct(product);
            case "SEASONAL" -> handleSeasonalProduct(product);
            case "EXPIRABLE" -> handleExpirableProduct(product);
            default -> System.out.println("Unknown product type: " + product.getType());
        }
    }

    private void handleNormalProduct(Product product) {
        if (product.getAvailable() > 0) {
            decrementStock(product);
        } else if (product.getLeadTime() > 0) {
            notifyDelay(product.getLeadTime(), product);
        }
    }

    private void handleExpirableProduct(Product product) {
        if (product.getAvailable() > 0 && product.getExpiryDate().isAfter(LocalDate.now())) {
            decrementStock(product);
        } else {
            handleExpiredProduct(product);
        }
    }

    public void handleSeasonalProduct(Product product) {
        if (isOutOfSeason(product)) {
            markAsUnavailable(product);
        } else if (isNotYetInSeason(product)) {
            notifyOutOfStock(product);
        } else {
            notifyDelay(product.getLeadTime(), product);
        }
    }

    public void handleExpiredProduct(Product product) {
        if (isNotExpired(product)) {
            decrementStock(product);
        } else {
            markAsExpired(product);
        }
    }

    private boolean isOutOfSeason(Product product) {
        LocalDate seasonEnd = product.getSeasonEndDate();
        if (seasonEnd == null) {
            return false; // ou true selon ta règle métier
        }
        return LocalDate.now().plusDays(product.getLeadTime()).isAfter(seasonEnd);
    }

    private boolean isNotYetInSeason(Product product) {
        return product.getSeasonStartDate().isAfter(LocalDate.now());
    }

    private void markAsExpired(Product product) {
        notificationService.sendExpirationNotification(product.getName(), product.getExpiryDate());
        product.setAvailable(0);
        productRepository.save(product);
    }

    private boolean isNotExpired(Product product) {
        return product.getAvailable() > 0 && product.getExpiryDate().isAfter(LocalDate.now());
    }
    private void decrementStock(Product product) {
        product.setAvailable(product.getAvailable() - 1);
        productRepository.save(product);
    }


    private void notifyOutOfStock(Product product) {
        notificationService.sendOutOfStockNotification(product.getName());
        productRepository.save(product);
    }


    private void markAsUnavailable(Product product) {
        notificationService.sendOutOfStockNotification(product.getName());
        product.setAvailable(0);
        productRepository.save(product);
    }
}
