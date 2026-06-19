package com.board.app.service.routine;

import com.board.app.dto.routine.RoutineRequest;
import com.board.app.dto.routine.RoutineResponse;
import com.board.app.entity.User;
import com.board.app.entity.routine.Routine;
import com.board.app.repository.UserRepository;
import com.board.app.repository.routine.RoutineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoutineService {

    private final RoutineRepository routineRepository;
    private final UserRepository userRepository;

    //목록조회
    public List<RoutineResponse.Detail> getList(Long userId) {
        List<Routine> routines = routineRepository.findByUserId(userId);

        //람다 표현식
        //return routines.map((data) -> RoutineResponse.Detail.from(data));

        //메서드 레퍼런스 - from메서드 그대로 사용
        //page형태
        //return routines.map(RoutineResponse.Detail::from);

        //list형태
        return routines.stream().map(RoutineResponse.Detail::from).toList();
    }

    //단건조회
    //생성
    public RoutineResponse.Detail create(RoutineRequest.Create create, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername());

        return null;
    }


    //수정
    //삭제

}
