package com.board.app.dto;

import com.board.app.entity.Post;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class PostResponse {

    // 목록용 (댓글·파일 제외, 가볍게)
    @Getter @Builder
    public static class Summary {
        private Long id;
        private String title;
        private String authorNickname;
        private int viewCount;
        private long commentCount;
        private LocalDateTime createdAt;

        public static Summary from(Post post, long commentCount) {
            return Summary.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .authorNickname(post.getUser().getNickname())
                    .viewCount(post.getViewCount())
                    .commentCount(commentCount)
                    .createdAt(post.getCreatedAt())
                    .build();
        }
    }

    // 상세용 (댓글·파일 포함)
    @Getter @Builder
    public static class Detail {
        private Long id;
        private String title;
        private String content;
        private String authorNickname;
        private Long authorId;
        private int viewCount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<CommentResponse.Info> comments;
        private List<FileResponse> files;

        public static Detail from(Post post) {
            return Detail.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .authorNickname(post.getUser().getNickname())
                    .authorId(post.getUser().getId())
                    .viewCount(post.getViewCount())
                    .createdAt(post.getCreatedAt())
                    .updatedAt(post.getUpdatedAt())
                    .comments(post.getComments().stream()
                            .map(CommentResponse.Info::from)
                            .collect(Collectors.toList()))
                    .files(post.getFiles().stream()
                            .map(FileResponse::from)
                            .collect(Collectors.toList()))
                    .build();
        }
    }
}