package com.fastbite.order.dto;

import com.fastbite.order.model.Order;
import com.fastbite.order.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {
    
    public enum EventType {
        CREATED,
        STATUS_CHANGED,
        COMMENT_ADDED
    }
    
    private EventType eventType;
    
    private String orderId;
    
    private OrderStatus status;
    
    private Order order;
    
    private Instant timestamp;
    
    public static OrderEvent created(Order order) {
        return OrderEvent.builder()
                .eventType(EventType.CREATED)
                .orderId(order.getId())
                .status(order.getStatus())
                .order(order)
                .timestamp(Instant.now())
                .build();
    }
    
    public static OrderEvent statusChanged(Order order) {
        return OrderEvent.builder()
                .eventType(EventType.STATUS_CHANGED)
                .orderId(order.getId())
                .status(order.getStatus())
                .order(order)
                .timestamp(Instant.now())
                .build();
    }
    
    public static OrderEvent commentAdded(String orderId) {
        return OrderEvent.builder()
                .eventType(EventType.COMMENT_ADDED)
                .orderId(orderId)
                .timestamp(Instant.now())
                .build();
    }
}
