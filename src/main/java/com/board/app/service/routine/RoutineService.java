package com.board.app.service.routine;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.board.app.dto.routine.RoutineRequest;
import com.board.app.dto.routine.RoutineResponse;
import com.board.app.entity.User;
import com.board.app.entity.routine.Routine;
import com.board.app.repository.UserRepository;
import com.board.app.repository.routine.RoutineLogRepository;
import com.board.app.repository.routine.RoutineRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoutineService {

    private final RoutineRepository routineRepository;
    private final RoutineLogRepository routineLogRepository;
    private final UserRepository userRepository;

    //목록조회
    public List<RoutineResponse.Detail> getList(String userName) {

        User user = userRepository.findByEmail(userName).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<Routine> routines = routineRepository.findByUserId(user.getId());

        LocalDate today = LocalDate.now();
        
        //1번 방법
//        List<RoutineLog> routineLog = routines.stream().flatMap((data)-> routineLogRepository.findAllByRoutineIdAndLogDate(data.getId(), today).stream()).toList();
        
        //2번 방법 (기존 메서드 사용)
        List<Long> routineIdList = routines.stream().map(Routine::getId).toList();
        
        Set<Long> doneIds = new HashSet<>(
        		routineLogRepository.findDoneRoutines(routineIdList, today)
		);
        
        
        //람다 표현식
        //return routines.map((data) -> RoutineResponse.Detail.from(data));

        //메서드 레퍼런스 - from메서드 그대로 사용
        //page형태
        //return routines.map(RoutineResponse.Detail::from);

        //list형태
//        return routines.stream().map(RoutineResponse.Detail::from).toList();
//        return routines.stream().map(routine-> RoutineResponse.Detail.from(routine, doneIds.contains(routine.getId()))).toList();
        return routines.stream()
                .map(routine ->
                    RoutineResponse.Detail.from(
                        routine,
                        doneIds.contains(routine.getId())
                    )
                )
                .toList();
    }

    //단건조회
    //생성
    public RoutineResponse.Detail create(RoutineRequest.Create create, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        Routine routine = Routine.builder()
                .title(create.getTitle())
                .user(user).build();
        return RoutineResponse.Detail.from(routineRepository.save(routine), false);
    }

}
