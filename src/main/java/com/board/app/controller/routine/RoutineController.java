package com.board.app.controller.routine;

import com.board.app.dto.routine.RoutineRequest;
import com.board.app.dto.routine.RoutineResponse;
import com.board.app.repository.routine.RoutineRepository;
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
    private final RoutineRepository routineRepository;

/*  @GetMapping     → 조회 (GET)
    @PostMapping    → 생성 (POST)
    @PutMapping     → 전체 수정 (PUT)
    @DeleteMapping  → 삭제 (DELETE)
    @PatchMapping   → 부분 수정 (PATCH)   ← 이거*/

    //requestParam과 param의 차이
    //HTTP 요청과 쿼리에 사용하는 값.
    @GetMapping
    public ResponseEntity<List<RoutineResponse.Detail>> getList(@AuthenticationPrincipal UserDetails userDetails){
        System.out.println("여기123 => " + userDetails.getUsername());
        return ResponseEntity.ok(routineService.getList(userDetails.getUsername()));
    }

    @PostMapping
    public ResponseEntity<RoutineResponse.Detail> create(@RequestBody RoutineRequest.Create create, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(routineService.create(create, userDetails));
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
