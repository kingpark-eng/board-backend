package com.board.app.service;

import com.board.app.dto.CommentRequest;
import com.board.app.dto.CommentResponse;
import com.board.app.entity.Comment;
import com.board.app.entity.Post;
import com.board.app.entity.User;
import com.board.app.repository.CommentRepository;
import com.board.app.repository.PostRepository;
import com.board.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 댓글 목록
    @Transactional(readOnly = true)
    public List<CommentResponse.Info> getComments(Long postId) {
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId)
                .stream()
                .map(CommentResponse.Info::from)
                .collect(Collectors.toList());
    }

    // 댓글 작성
    @Transactional
    public CommentResponse.Info create(Long postId, CommentRequest.Create req, String email) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        Comment comment = Comment.builder()
                .content(req.getContent())
                .post(post)
                .user(user)
                .build();

        return CommentResponse.Info.from(commentRepository.save(comment));
    }

    // 댓글 삭제
    @Transactional
    public void delete(Long commentId, String email) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        if (!comment.getUser().getEmail().equals(email)) {
            throw new AccessDeniedException("본인의 댓글만 삭제할 수 있습니다.");
        }

        commentRepository.delete(comment);
    }
}