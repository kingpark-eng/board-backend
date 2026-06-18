package com.board.app.controller;

import com.board.app.dto.PostRequest;
import com.board.app.dto.PostResponse;
import com.board.app.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000") // Put your React URL here
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // GET /api/posts?page=0&size=10&keyword=검색어
    @GetMapping
    public ResponseEntity<Page<PostResponse.Summary>> getList(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(postService.getList(keyword, page, size));
    }

    // GET /api/posts/{id}
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse.Detail> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getDetail(id));
    }

    // POST /api/posts
    // @RequestBody PostRequest.Create request
    // PostRequest는 요청데이터를 담는 그릇
    @PostMapping
    public ResponseEntity<PostResponse.Detail> create(
            @Valid @RequestBody PostRequest.Create request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(postService.create(request, userDetails.getUsername()));
    }

    // PUT /api/posts/{id}
    @PutMapping("/{id}")
    public ResponseEntity<PostResponse.Detail> update(
            @PathVariable Long id,
            @Valid @RequestBody PostRequest.Update request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(postService.update(id, request, userDetails.getUsername()));
    }

    // DELETE /api/posts/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        postService.delete(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
