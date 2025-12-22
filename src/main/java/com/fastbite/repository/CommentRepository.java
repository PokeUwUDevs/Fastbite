package com.fastbite.repository;

import com.fastbite.model.Comment;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface CommentRepository extends ReactiveMongoRepository<Comment, String> {
    
    Flux<Comment> findByOrderIdOrderByCreatedAtAsc(String orderId);
}
