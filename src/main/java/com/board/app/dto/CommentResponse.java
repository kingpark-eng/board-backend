package com.board.app.dto;

import com.board.app.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

public class CommentResponse {

    @Getter @Builder
    public static class Info {
        private Long id;
        private String content;
        private String authorNickname;
        private Long authorId;
        private LocalDateTime createdAt;

        public static Info from(Comment comment) {
            return Info.builder()
                    .id(comment.getId())
                    .content(comment.getContent())
                    .authorNickname(comment.getUser().getNickname())
                    .authorId(comment.getUser().getId())
                    .createdAt(comment.getCreatedAt())
                    .build();
        }
    }
}