package com.board.app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

public class CommentRequest {

    @Getter @Setter
    public static class Create {
        @NotBlank(message = "댓글 내용을 입력해주세요.")
        private String content;
    }
}