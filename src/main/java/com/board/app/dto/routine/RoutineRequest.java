package com.board.app.dto.routine;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

public class RoutineRequest {

    @Getter @Setter
    public static class Create{

        @NotBlank(message = "제목을 입력해주세요.")
        private static String title;
    }

    @Getter @Setter
    public static class Update{

        @NotBlank(message = "제목을 입력해주세요.")
        private static String title;
    }

}
