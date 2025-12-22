package com.fastbite.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "orders")
public class Order {
    
    @Id
    private String id;
    
    @Indexed
    private String customerId;
    
    private String customerName;
    
    private String customerPhone;
    
    private String deliveryAddress;
    
    private String notes;
    
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();
    
    private BigDecimal total;
    
    @Indexed
    private OrderStatus status;
    
    private String assignedDeliveryId;
    
    private Instant createdAt;
    
    private Instant updatedAt;
    
    // Calcular total basado en items
    public void calculateTotal() {
        this.total = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
