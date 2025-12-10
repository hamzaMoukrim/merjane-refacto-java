package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.services.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    OrderRepository orderRepository;

    @Mock
    ProductService productService;

    @InjectMocks
    OrderServiceImpl orderService;

    @Test
    @DisplayName("process Order: missing order -> throw IllegalArgumentException")
    void processMissingOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> orderService.processOrder(1L));
        verify(orderRepository).findById(1L);
    }

    @Test
    @DisplayName("processOrder: order with products")
    void processOrderwithProducts() {
        Order order = new Order();
        order.setId(100L);
        Product p1 = new Product(); p1.setId(11L);
        Product p2 = new Product(); p2.setId(22L);
        order.setItems(new HashSet<>(Arrays.asList(p1, p2)));

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        doNothing().when(productService).processProduct(any(Product.class));

        ProcessOrderResponse res = orderService.processOrder(100L);

        assertNotNull(res);
        assertEquals(100L, res.id());
        verify(productService, times(1)).processProduct(p1);
        verify(productService, times(1)).processProduct(p2);
    }
}
