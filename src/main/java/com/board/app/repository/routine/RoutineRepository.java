package com.board.app.repository.routine;

import com.board.app.entity.routine.Routine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoutineRepository extends JpaRepository<Routine, Long> {
                                                      // ↑엔티티   ↑ID 타입
    List<Routine> findByUserId(Long userId);

}
