package com.board.app.dto.routine;

import java.util.List;
import java.util.Set;

import com.board.app.dto.routine.RoutineResponse.Detail;
import com.board.app.entity.routine.Routine;

import lombok.Builder;
import lombok.Getter;

public class RoutineResponse {

    @Getter @Builder
    public static class Detail{
        private Long id;
        private String title;
        private Boolean done;

        public static Detail from(Routine routine, boolean done){
            return Detail.builder()
                    .id(routine.getId())
                    .title(routine.getTitle())
                    .done(done)
                    .build();
        }
        
        public static List<Detail> from(List<Routine> routines, Set<Long> doneIds) {
            return routines.stream()
                    .map(routine -> Detail.from(routine, doneIds.contains(routine.getId())))
                    .toList();
        }
    }
    
    

}
