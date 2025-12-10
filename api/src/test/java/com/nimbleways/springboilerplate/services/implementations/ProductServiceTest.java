package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    ProductRepository productRepository;

    @Mock
    NotificationService notificationService;

    @InjectMocks
    ProductServiceImpl productService;

    @Captor
    ArgumentCaptor<Product> productCaptor;

    /* -----------------------
    NORMAL products
     ----------------------- */
    @Test
    @DisplayName("NORMAL: available -> decrement and save")
    void normalAvailable() {
        Product p = new Product();
        p.setName("p1");
        p.setType("NORMAL");
        p.setAvailable(3);
        p.setLeadTime(5);
        productService.processProduct(p);
        verify(productRepository, times(1)).save(productCaptor.capture());
        Product saved = productCaptor.getValue();
        assertEquals(2, saved.getAvailable());
        verify(notificationService, never()).sendDelayNotification(anyInt(), anyString());
    }


    /* -----------------------
    SEASONAL products
     ----------------------- */

    @Test
    @DisplayName("SEASONAL out of stock -> mark unavailable")
    void seasonalOutOfStock() {
        Product p = new Product();
        p.setName("P2");
        p.setType("SEASONAL");
        p.setAvailable(0);
        p.setLeadTime(20);
        p.setSeasonEndDate(LocalDate.now().plusDays(5));
        productService.processProduct(p);
        verify(notificationService, times(1)).sendOutOfStockNotification(eq("P2"));
        verify(productRepository, times(1)).save(productCaptor.capture());
        assertEquals(0, productCaptor.getValue().getAvailable());
    }

    @Test
    @DisplayName("SEASONAL not in season")
    void seasonalNotYetInSeason() {
        Product p = new Product();
        p.setName("P3");
        p.setType("SEASONAL");
        p.setAvailable(0);
        p.setLeadTime(5);
        p.setSeasonStartDate(LocalDate.now().plusDays(10));

        productService.processProduct(p);

        verify(notificationService, times(1)).sendOutOfStockNotification(eq("P3"));
        verify(productRepository, times(1)).save(productCaptor.capture());
    }

    // -----------------------
    // EXPIRABLE tests
    // -----------------------

    @Test
    @DisplayName("EXPIRABLE not expired and available")
    void expirableNotExpired() {
        Product p = new Product();
        p.setName("p4");
        p.setType("EXPIRABLE");
        p.setAvailable(2);
        p.setExpiryDate(LocalDate.now().plusDays(3));

        productService.processProduct(p);

        verify(productRepository, times(1)).save(productCaptor.capture());
        assertEquals(1, productCaptor.getValue().getAvailable());
        verify(notificationService, never()).sendExpirationNotification(anyString(), any());
    }

}
