package com.fastbite.order.repository;

import com.fastbite.order.model.Product;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ProductRepository extends ReactiveMongoRepository<Product, String> {
    
    Flux<Product> findByAvailableTrue();
    
    Flux<Product> findByCategory(String category);
}
