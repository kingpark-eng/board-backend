package com.board.app.repository.routine;

import com.board.app.entity.routine.RoutineLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoutineLogRepository extends JpaRepository<RoutineLog, Long> {

    //Optional사용 하는 이유 : 값이 없을 수 있음을 명시 (.isPresent())
    Optional<RoutineLog> findByRoutineIdAndLogDate(Long routineId, LocalDate logDate);

    //오늘 완료된 routine_id 목록 (done 계산용)
    @Query("select r1.routine.id from RoutineLog r1 " +
            "where r1.routine.id in :routineIds and r1.logDate = :date")
    List<Long> findDoneRoutines(@Param("routineIds") List<Long> routineIds, @Param("date") LocalDate date);

    // 히트맵용: 특정 기간 날짜별 완료 개수
    @Query("select r1.logDate, count(r1) from RoutineLog r1 " +
           "where r1.routine.user.id = :userId and r1.logDate between :start and :end group by r1.logDate")
    List<Object[]> countByDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

}
