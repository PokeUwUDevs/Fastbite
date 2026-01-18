package com.fastbite.order.service;

import com.fastbite.order.dto.OrderEvent;
import com.fastbite.order.model.Comment;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Service
public class EventService {

    // Sink para eventos de órdenes (nuevas, cambios de estado)
    private final Sinks.Many<OrderEvent> orderSink;
    
    // Sink para comentarios
    private final Sinks.Many<Comment> commentSink;

    public EventService() {
        // multicast().onBackpressureBuffer() permite múltiples suscriptores
        // y maneja backpressure con un buffer
        this.orderSink = Sinks.many().multicast().onBackpressureBuffer();
        this.commentSink = Sinks.many().multicast().onBackpressureBuffer();
    }

    // Emitir evento de orden
    public void emitOrderEvent(OrderEvent event) {
        orderSink.tryEmitNext(event);
    }

    // Emitir comentario
    public void emitComment(Comment comment) {
        commentSink.tryEmitNext(comment);
    }

    // Obtener stream de todos los eventos de órdenes
    public Flux<OrderEvent> getOrderEvents() {
        return orderSink.asFlux();
    }

    // Obtener stream de eventos filtrados por orderId
    public Flux<OrderEvent> getOrderEventsByOrderId(String orderId) {
        return orderSink.asFlux()
                .filter(event -> event.getOrderId().equals(orderId));
    }

    // Obtener stream de comentarios para una orden específica
    public Flux<Comment> getCommentsByOrderId(String orderId) {
        return commentSink.asFlux()
                .filter(comment -> comment.getOrderId().equals(orderId));
    }
}
