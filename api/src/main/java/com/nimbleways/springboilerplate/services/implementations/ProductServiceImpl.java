package com.nimbleways.springboilerplate.services.implementations;

import java.time.LocalDate;

import com.nimbleways.springboilerplate.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private NotificationService notificationService;

    @Override
    public void notifyDelay(int leadTime, Product product) {
        product.setLeadTime(leadTime);
        productRepository.save(product);
        notificationService.sendDelayNotification(leadTime, product.getName());
    }

    @Override
    public void handleSeasonalProduct(Product product) {
        LocalDate today = LocalDate.now();

        if (isAfterSeasonEnd(product, today)) {
            product.setAvailable(0);
            productRepository.save(product);
            notificationService.sendOutOfStockNotification(product.getName());
        } else if (isBeforeSeasonStart(product, today)) {
            notificationService.sendOutOfStockNotification(product.getName());
            productRepository.save(product);
        } else {
            notifyDelay(product.getLeadTime(), product);
        }
    }

    @Override
    public void handleExpiredProduct(Product product) {
        if (isProductStillValid(product)) {
            updateStock(product, 1);
        } else {
            markProductAsExpired(product);
        }
    }

    private boolean isAfterSeasonEnd(Product product, LocalDate today) {
        return today.plusDays(product.getLeadTime()).isAfter(product.getSeasonEndDate());
    }

    private boolean isBeforeSeasonStart(Product product, LocalDate today) {
        return product.getSeasonStartDate().isAfter(today);
    }

    private boolean isProductStillValid(Product product) {
        return product.getAvailable() > 0 && product.getExpiryDate().isAfter(LocalDate.now());
    }

    private void updateStock(Product product, int quantity) {
        int newAvailable = product.getAvailable() - quantity;
        product.setAvailable(Math.max(newAvailable, 0));
        productRepository.save(product);
    }

    private void markProductAsExpired(Product product) {
        notificationService.sendExpirationNotification(product.getName(), product.getExpiryDate());
        product.setAvailable(0);
        productRepository.save(product);
    }
}
