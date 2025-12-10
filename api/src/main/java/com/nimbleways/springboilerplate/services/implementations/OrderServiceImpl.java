package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;
import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.services.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@AllArgsConstructor
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;

    @Override
    public ProcessOrderResponse processOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order with id " + orderId + " not found "));
        Set<Product> products = order.getItems();
        // ProductService gere les produits
        for (Product p : products) {
            productService.processProduct(p);
        }
        return new ProcessOrderResponse(order.getId());
    }
}
