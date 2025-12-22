package com.fastbite.service;

import com.fastbite.dto.CreateCommentRequest;
import com.fastbite.dto.OrderEvent;
import com.fastbite.model.Comment;
import com.fastbite.model.Role;
import com.fastbite.model.User;
import com.fastbite.repository.CommentRepository;
import com.fastbite.repository.UserRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventService eventService;

    public CommentService(CommentRepository commentRepository,
                          UserRepository userRepository,
                          EventService eventService) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.eventService = eventService;
    }

    public Mono<Comment> addComment(String orderId, String userId, CreateCommentRequest request) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new RuntimeException("Usuario no encontrado")))
                .flatMap(user -> {
                    Comment comment = Comment.builder()
                            .orderId(orderId)
                            .userId(userId)
                            .userName(user.getName())
                            .userRole(user.getRole())
                            .message(request.getMessage())
                            .createdAt(Instant.now())
                            .build();
                    
                    return commentRepository.save(comment);
                })
                .doOnNext(comment -> {
                    // Emitir comentario para SSE
                    eventService.emitComment(comment);
                    // También emitir evento de orden para notificar
                    eventService.emitOrderEvent(OrderEvent.commentAdded(orderId));
                });
    }

    public Flux<Comment> getByOrderId(String orderId) {
        return commentRepository.findByOrderIdOrderByCreatedAtAsc(orderId);
    }
}
