package com.board.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

//게시판 DTO
public class PostRequest {

    @Getter @Setter
    public static class Create {

        @NotBlank(message = "제목을 입력해주세요.")
        @Size(max = 200, message = "제목은 200자 이내여야 합니다.")
        private String title;

        @NotBlank(message = "내용을 입력해주세요.")
        private String content;
    }

    @Getter @Setter
    public static class Update {

        @NotBlank(message = "제목을 입력해주세요.")
        @Size(max = 200, message = "제목은 200자 이내여야 합니다.")
        private String title;

        @NotBlank(message = "내용을 입력해주세요.")
        private String content;
    }
}