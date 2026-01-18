package com.fastbite.restaurant.service;

import com.fastbite.restaurant.model.Product;
import com.fastbite.restaurant.repository.ProductRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Flux<Product> getAllAvailable() {
        return productRepository.findByAvailableTrue();
    }

    public Flux<Product> getAll() {
        return productRepository.findAll();
    }

    public Mono<Product> getById(String id) {
        return productRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Producto no encontrado")));
    }

    public Mono<Product> create(Product product) {
        product.setAvailable(true);
        return productRepository.save(product);
    }
}
