package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;
import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.OrderService;
import com.nimbleways.springboilerplate.services.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {


    private final ProductRepository productRepository;


    private final ProductService productService;

    private final OrderRepository orderRepository;

    public OrderServiceImpl(ProductRepository productRepository, ProductService productService, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.productService=productService;
        this.orderRepository = orderRepository;
    }


    @Transactional
    public void processOrder(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found " + productId)) ;

        switch (product.getType()) {
            case NORMAL:
                processNormalProduct(product, quantity);
                break;
            case SEASONAL:
                processSeasonalProduct(product, quantity);
                break;
            case EXPIRABLE:
                processExpirableProduct(product, quantity);
                break;
            default:
                throw new UnsupportedOperationException("Unknown product type");
        }
    }

    @Override
    public ProcessOrderResponse processOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        for (Product product : order.getItems()) {
            processProduct(product);
        }
        return new ProcessOrderResponse(order.getId());
    }

    private void processProduct(Product product) {
        switch (product.getType()) {
            case NORMAL -> processNormalProduct(product,1);
            case SEASONAL -> processSeasonalProduct(product,1);
            case EXPIRABLE -> processExpirableProduct(product,1);
            default -> log.warn("Unknown product type: {}" , product.getType());
        }
    }

    private void processNormalProduct(Product product, int quantity) {
        if (product.getAvailable() > 0) {
            updateStock(product, quantity);
        } else if (product.getLeadTime() > 0) {
            productService.notifyDelay(product.getLeadTime(), product);
        }
    }



    private void processSeasonalProduct(Product product, int quantity) {
        LocalDate today = LocalDate.now();
        if (isInSeason(product, today) && product.getAvailable() > 0) {
            updateStock(product, 1);
        } else {
            productService.handleSeasonalProduct(product);
        }
    }


    private void processExpirableProduct(Product product, int quantity) {
        if (isStillValid(product)) {
            updateStock(product,1);
        } else {
            productService.handleExpiredProduct(product);
        }
    }


    private boolean isStillValid(Product product) {
        return product.getAvailable() > 0 && product.getExpiryDate().isAfter(LocalDate.now());
    }

    private void updateStock(Product product, int quantity) {
        int newAvailable = product.getAvailable() - quantity;
        product.setAvailable(Math.max(newAvailable, 0));
        productRepository.save(product);
    }

    private boolean isInSeason(Product product, LocalDate today) {
        return today.isAfter(product.getSeasonStartDate()) && today.isBefore(product.getSeasonEndDate());
    }




}
