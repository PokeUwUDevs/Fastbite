package com.fastbite.controller;

import com.fastbite.dto.CreateCommentRequest;
import com.fastbite.model.Comment;
import com.fastbite.service.CommentService;
import com.fastbite.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/orders/{orderId}/comments")
public class CommentController {

    private final CommentService commentService;
    private final EventService eventService;

    public CommentController(CommentService commentService, EventService eventService) {
        this.commentService = commentService;
        this.eventService = eventService;
    }

    // Agregar comentario
    @PostMapping
    public Mono<ResponseEntity<Comment>> addComment(
            @PathVariable String orderId,
            @Valid @RequestBody CreateCommentRequest request,
            Authentication authentication) {
        
        if (authentication == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        
        String userId = (String) authentication.getPrincipal();
        
        return commentService.addComment(orderId, userId, request)
                .map(comment -> ResponseEntity.status(HttpStatus.CREATED).body(comment))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().build()));
    }

    // Obtener comentarios de una orden
    @GetMapping
    public Flux<Comment> getComments(@PathVariable String orderId) {
        return commentService.getByOrderId(orderId);
    }

    // Stream de comentarios en tiempo real
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Comment> streamComments(@PathVariable String orderId) {
        return eventService.getCommentsByOrderId(orderId)
                .onBackpressureBuffer();
    }
}