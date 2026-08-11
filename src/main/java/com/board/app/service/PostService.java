package com.board.app.service;

import com.board.app.dto.PostRequest;
import com.board.app.dto.PostResponse;
import com.board.app.entity.Post;
import com.board.app.entity.User; 
import com.board.app.repository.PostRepository;
import com.board.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository; 

    // 목록 조회 (페이징 + 검색)
    @Transactional(readOnly = true)
    public Page<PostResponse.Summary> getList(String keyword, int page, int size, String date) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> posts = (keyword != null && !keyword.isBlank())
                ? postRepository.searchByKeyword(keyword, pageable)
                : postRepository.findAllByOrderByCreatedAtDesc(pageable);

        return posts.map(post -> {
            return PostResponse.Summary.from(post);
        });
    }

    // 상세 조회 (조회수 증가)
    @Transactional
    public PostResponse.Detail getDetail(Long postId) {
        Post post = findPost(postId);
        post.increaseViewCount();
        return PostResponse.Detail.from(post);
    }

    // 작성
    @Transactional
    public PostResponse.Detail create(PostRequest.Create req, String email) {
        User user = findUser(email);
        Post post = Post.builder()
                .title(req.getTitle())
                .user(user)
                .build();
        return PostResponse.Detail.from(postRepository.save(post));
    }

    // 수정
    @Transactional
    public PostResponse.Detail update(Long postId, PostRequest.Update req, String email) {
        Post post = findPost(postId);
        checkAuthor(post, email);
        post.setTitle(req.getTitle());
        return PostResponse.Detail.from(post);
    }

    // 삭제
    @Transactional
    public void delete(Long postId, String email) {
        Post post = findPost(postId);
        checkAuthor(post, email);
        postRepository.delete(post);
    }

    // ── 내부 헬퍼 ──────────────────────────
    private Post findPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
    }

    private void checkAuthor(Post post, String email) {
        if (!post.getUser().getEmail().equals(email)) {
            throw new AccessDeniedException("본인의 게시글만 수정/삭제할 수 있습니다.");
        }
    }
}
