package com.fastbite.order.repository;
import com.fastbite.order.model.Order;
import com.fastbite.order.model.OrderStatus;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface OrderRepository extends ReactiveMongoRepository<Order, String> {
    
    Flux<Order> findByCustomerId(String customerId);
    
    Flux<Order> findByStatus(OrderStatus status);
    
    Flux<Order> findByStatusIn(java.util.List<OrderStatus> statuses);
    
    Flux<Order> findByAssignedDeliveryId(String deliveryId);
    
    Flux<Order> findAllByOrderByCreatedAtDesc();
}
