package com.board.app.repository.routine;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.board.app.entity.routine.Routine;

public interface RoutineRepository extends JpaRepository<Routine, Long> {
                                                      // ↑엔티티   ↑ID 타입
    List<Routine> findByUserId(Long userId);

    Optional<Routine> findByIdAndUserEmail(Long routineId, String email);
    
    Long countByUserId(Long userId);
    
    List<Routine> findByUserIdAndDeleteAtIsNull(Long userId);
}
