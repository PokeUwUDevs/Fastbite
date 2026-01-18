package com.fastbite.order.controller;

import com.fastbite.order.dto.CreateOrderRequest;
import com.fastbite.order.dto.OrderEvent;
import com.fastbite.order.dto.UpdateStatusRequest;
import com.fastbite.order.model.Order;
import com.fastbite.order.model.Role;
import com.fastbite.order.repository.UserRepository;
import com.fastbite.order.service.EventService;
import com.fastbite.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final EventService eventService;
    private final UserRepository userRepository;

    public OrderController(OrderService orderService, 
                          EventService eventService,
                          UserRepository userRepository) {
        this.orderService = orderService;
        this.eventService = eventService;
        this.userRepository = userRepository;
    }

    // Crear pedido (CLIENTE)
    @PostMapping
    public Mono<ResponseEntity<Order>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication) {
        
        if (authentication == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        
        String userId = (String) authentication.getPrincipal();
        
        return orderService.createOrder(userId, request)
                .map(order -> ResponseEntity.status(HttpStatus.CREATED).body(order))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().build()));
    }

    // Obtener pedido por ID
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Order>> getById(
            @PathVariable String id,
            Authentication authentication) {
        
        if (authentication == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        
        String userId = (String) authentication.getPrincipal();
        
        return getRoleFromAuth(authentication)
                .flatMap(role -> orderService.hasAccess(id, userId, role))
                .flatMap(hasAccess -> {
                    if (!hasAccess) {
                        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).<Order>build());
                    }
                    return orderService.getById(id)
                            .map(ResponseEntity::ok)
                            .onErrorResume(e -> Mono.just(ResponseEntity.notFound().build()));
                })
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    // Obtener mis pedidos (CLIENTE)
    @GetMapping("/my")
    public Mono<ResponseEntity<Object>> getMyOrders(Authentication authentication) {
        if (authentication == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        String userId = (String) authentication.getPrincipal();
        return orderService.getByCustomerId(userId)
                .collectList()
                .map(orders -> ResponseEntity.ok((Object) orders));
    }

    // Obtener todos los pedidos (COCINA)
    @GetMapping("/kitchen")
    public Mono<ResponseEntity<Object>> getForKitchen(Authentication authentication) {
        if (authentication == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        return orderService.getForKitchen()
                .collectList()
                .map(orders -> ResponseEntity.ok((Object) orders));
    }

    // Obtener pedidos para repartidor
    @GetMapping("/delivery")
    public Mono<ResponseEntity<Object>> getForDelivery(Authentication authentication) {
        if (authentication == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        return orderService.getForDelivery()
                .collectList()
                .map(orders -> ResponseEntity.ok((Object) orders));
    }

    // Actualizar estado del pedido
    @PatchMapping("/{id}/status")
    public Mono<ResponseEntity<Order>> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateStatusRequest request,
            Authentication authentication) {
        
        if (authentication == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        
        String userId = (String) authentication.getPrincipal();
        
        return getRoleFromAuth(authentication)
                .flatMap(role -> orderService.updateStatus(id, request.getStatus(), userId, role))
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    if (e.getMessage() != null && e.getMessage().contains("no permitida")) {
                        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
                    }
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }

    // ==================== SSE ENDPOINTS ====================

    // Stream de todos los eventos de órdenes (para COCINA)
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<OrderEvent> streamAllOrders() {
        return eventService.getOrderEvents()
                .onBackpressureBuffer();
    }

    // Stream de eventos para una orden específica (para CLIENTE viendo su pedido)
    @GetMapping(value = "/{id}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<OrderEvent> streamOrder(@PathVariable String id) {
        return eventService.getOrderEventsByOrderId(id)
                .onBackpressureBuffer();
    }

    // Helper para obtener rol del usuario
    private Mono<Role> getRoleFromAuth(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        return userRepository.findById(userId)
                .map(user -> user.getRole())
                .switchIfEmpty(Mono.error(new RuntimeException("Usuario no encontrado")));
    }
}