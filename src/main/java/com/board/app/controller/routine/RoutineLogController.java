package com.board.app.controller.routine;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.board.app.entity.routine.RoutineLog;
import com.board.app.service.routine.RoutineLogService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/routinelog")
public class RoutineLogController {

	private final RoutineLogService routineLogService;
	
    @PostMapping
    public ResponseEntity<Boolean> createRoutineLog(@RequestBody RoutineLog routineLog, @AuthenticationPrincipal UserDetails userDetails){
    	
    	Boolean toggleToday = routineLogService.toggleToday(routineLog.getId(), userDetails.getUsername());
    	
        return ResponseEntity.ok(toggleToday);
    }
    
    //매월 로그조회
    @GetMapping("/monthlyLogs")
    public ResponseEntity<?> monthlyLogs(@RequestParam Integer year, @RequestParam Integer month, @AuthenticationPrincipal UserDetails userDetails){
    	return ResponseEntity.ok(routineLogService.findMonthlyLogs(year, month, userDetails));
    }
    
    //매월 로그조회
    @GetMapping("/dayLogs")
    public ResponseEntity<?> dayLogs(@RequestParam LocalDate logDate, @AuthenticationPrincipal UserDetails userDetails){
    	
    	System.out.println("logDate ===> " + logDate);
    	
    	return ResponseEntity.ok(routineLogService.findDayLogs(logDate, userDetails));
    }
    
    @GetMapping("/monthlySummary")
    public ResponseEntity<?> monthlySummary(@RequestParam Integer year, @RequestParam Integer month, @AuthenticationPrincipal UserDetails userDetails){
    	return ResponseEntity.ok(routineLogService.getMonthlySummary(year, month, userDetails));
    }

}
