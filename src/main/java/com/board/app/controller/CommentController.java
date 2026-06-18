package com.board.app.controller;

import com.board.app.dto.CommentRequest;
import com.board.app.dto.CommentResponse;
import com.board.app.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // GET /api/posts/{postId}/comments
    @GetMapping("/api/posts/{postId}/comments")
    public ResponseEntity<List<CommentResponse.Info>> getComments(
            @PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getComments(postId));
    }

    // POST /api/posts/{postId}/comments
    @PostMapping("/api/posts/{postId}/comments")
    public ResponseEntity<CommentResponse.Info> create(
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequest.Create request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                commentService.create(postId, request, userDetails.getUsername()));
    }

    // DELETE /api/comments/{commentId}
    @DeleteMapping("/api/comments/{commentId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        commentService.delete(commentId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
