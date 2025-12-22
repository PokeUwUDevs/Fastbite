package com.fastbite.service;

import com.fastbite.dto.CartItemRequest;
import com.fastbite.dto.CreateOrderRequest;
import com.fastbite.dto.OrderEvent;
import com.fastbite.model.*;
import com.fastbite.repository.OrderRepository;
import com.fastbite.repository.ProductRepository;
import com.fastbite.repository.UserRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final EventService eventService;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        UserRepository userRepository,
                        EventService eventService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.eventService = eventService;
    }

    public Mono<Order> createOrder(String customerId, CreateOrderRequest request) {
        return userRepository.findById(customerId)
                .switchIfEmpty(Mono.error(new RuntimeException("Usuario no encontrado")))
                .flatMap(user -> buildOrderItems(request.getItems())
                        .collectList()
                        .map(items -> {
                            Order order = Order.builder()
                                    .customerId(customerId)
                                    .customerName(user.getName())
                                    .customerPhone(user.getPhone())
                                    .deliveryAddress(request.getDeliveryAddress())
                                    .notes(request.getNotes())
                                    .items(items)
                                    .status(OrderStatus.RECIBIDO)
                                    .createdAt(Instant.now())
                                    .updatedAt(Instant.now())
                                    .build();
                            order.calculateTotal();
                            return order;
                        }))
                .flatMap(orderRepository::save)
                .doOnNext(order -> {
                    // Emitir evento de nueva orden (para cocina)
                    eventService.emitOrderEvent(OrderEvent.created(order));
                });
    }

    private Flux<OrderItem> buildOrderItems(List<CartItemRequest> cartItems) {
        return Flux.fromIterable(cartItems)
                .flatMap(cartItem -> productRepository.findById(cartItem.getProductId())
                        .map(product -> OrderItem.builder()
                                .productId(product.getId())
                                .productName(product.getName())
                                .unitPrice(product.getPrice())
                                .quantity(cartItem.getQuantity())
                                .build())
                        .switchIfEmpty(Mono.error(
                                new RuntimeException("Producto no encontrado: " + cartItem.getProductId())
                        ))
                );
    }

    public Mono<Order> getById(String orderId) {
        return orderRepository.findById(orderId)
                .switchIfEmpty(Mono.error(new RuntimeException("Pedido no encontrado")));
    }

    public Flux<Order> getByCustomerId(String customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    public Flux<Order> getAll() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    public Flux<Order> getByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public Flux<Order> getForKitchen() {
        // Cocina ve: RECIBIDO, PREPARANDO, LISTO
        return orderRepository.findByStatusIn(
                List.of(OrderStatus.RECIBIDO, OrderStatus.PREPARANDO, OrderStatus.LISTO)
        );
    }

    public Flux<Order> getForDelivery() {
        // Repartidor ve: LISTO, EN_CAMINO
        return orderRepository.findByStatusIn(
                List.of(OrderStatus.LISTO, OrderStatus.EN_CAMINO)
        );
    }

    public Mono<Order> updateStatus(String orderId, OrderStatus newStatus, String userId, Role userRole) {
        return orderRepository.findById(orderId)
                .switchIfEmpty(Mono.error(new RuntimeException("Pedido no encontrado")))
                .flatMap(order -> {
                    // Validar transición de estado según rol
                    if (!isValidTransition(order.getStatus(), newStatus, userRole)) {
                        return Mono.error(new RuntimeException(
                                "Transición de estado no permitida: " + order.getStatus() + " -> " + newStatus
                        ));
                    }
                    
                    order.setStatus(newStatus);
                    order.setUpdatedAt(Instant.now());
                    
                    // Si el repartidor toma el pedido, asignarlo
                    if (newStatus == OrderStatus.EN_CAMINO && userRole == Role.REPARTIDOR) {
                        order.setAssignedDeliveryId(userId);
                    }
                    
                    return orderRepository.save(order);
                })
                .doOnNext(order -> {
                    // Emitir evento de cambio de estado
                    eventService.emitOrderEvent(OrderEvent.statusChanged(order));
                });
    }

    private boolean isValidTransition(OrderStatus current, OrderStatus next, Role role) {
        return switch (role) {
            case COCINA -> switch (current) {
                case RECIBIDO -> next == OrderStatus.PREPARANDO;
                case PREPARANDO -> next == OrderStatus.LISTO;
                default -> false;
            };
            case REPARTIDOR -> switch (current) {
                case LISTO -> next == OrderStatus.EN_CAMINO;
                case EN_CAMINO -> next == OrderStatus.ENTREGADO;
                default -> false;
            };
            case CLIENTE -> false; // Cliente no puede cambiar estados
        };
    }

    // Verificar si el usuario tiene acceso al pedido
    public Mono<Boolean> hasAccess(String orderId, String userId, Role role) {
        return orderRepository.findById(orderId)
                .map(order -> switch (role) {
                    case CLIENTE -> order.getCustomerId().equals(userId);
                    case COCINA, REPARTIDOR -> true; // Cocina y repartidor ven todos
                })
                .defaultIfEmpty(false);
    }
}
