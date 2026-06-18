package com.board.app.dto.routine;

import com.board.app.entity.routine.Routine;
import lombok.Builder;
import lombok.Getter;

public class RoutineResponse {

    @Getter @Builder
    public static class Detail{
        private Long id;
        private String title;
        private String done;

        public static Detail from(Routine routine){
            return Detail.builder()
                    .id(routine.getId())
                    .title(routine.getTitle())
                    .done(routine.getDone())
                    .build();
        }
    }

}
