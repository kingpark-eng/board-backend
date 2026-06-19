package com.board.app.controller.routine;

import com.board.app.dto.routine.RoutineResponse;
import com.board.app.service.routine.RoutineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/routines")
public class RoutineController {

    private final RoutineService routineService;

/*  @GetMapping     → 조회 (GET)
    @PostMapping    → 생성 (POST)
    @PutMapping     → 전체 수정 (PUT)
    @DeleteMapping  → 삭제 (DELETE)
    @PatchMapping   → 부분 수정 (PATCH)   ← 이거*/

    //requestParam과 param의 차이
    //HTTP 요청과 쿼리에 사용하는 값.
    @GetMapping
    public ResponseEntity<List<RoutineResponse.Detail>> getList(@RequestParam Long userId){
        return ResponseEntity.ok(routineService.getList(userId));
    }

    @PostMapping
    public ResponseEntity<List<RoutineResponse.Detail>> create(@RequestParam Long userId){
        return ResponseEntity.ok();
    }

    //부분 수정
    //완료여부, 활성 비활성 토글
    @PatchMapping("/{id}/done")
    public ResponseEntity<List<RoutineResponse.Detail>> patch(@PathVariable Long id){
        return null;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<List<RoutineResponse.Detail>> delete(@AuthenticationPrincipal UserDetails userDetails){
        return null;
    }
}
