package com.board.app.service.routine;

import com.board.app.entity.routine.Routine;
import com.board.app.repository.routine.RoutineLogRepository;
import com.board.app.repository.routine.RoutineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class RoutineLogService {

    private RoutineRepository routineRepository;
    private RoutineLogRepository routineLogRepository;

    @Transactional
    public boolean toggleToday(Long routineId, String email){

        //본인 소유 루틴인지 검증
        //findBy + Id + And + User + Email
        //Id → routine 테이블의 id 컬럼 (이건 맞습니다)
        //UserEmail → routine.user.email, 즉 Routine이 참조하는 User의 email
        //routine 테이블에는 email 컬럼이 없습니다. 대신 user_id FK가 있고, 그게 users 테이블을 가리키죠. Spring이 UserEmail을 user.email로 해석해서, 내부적으로 routine과 users를 조인합니다.
        Routine routine = routineRepository.findByIdAndUserEmail(routineId, email).orElseThrow(() -> new IllegalArgumentException("루틴을 찾을 수 없습니다."));

        LocalDate today = LocalDate.now();
        var existing = routineLogRepository.findByRoutineIdAndLogDate(routineId, today);






        return true;
    }
}
