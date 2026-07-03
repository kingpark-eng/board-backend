package com.board.app.controller.routine;

import com.board.app.entity.routine.RoutineLog;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/routines")
public class RoutineLogController {

    @PostMapping(name = "/log")
    public ResponseEntity<RoutineLog> createRoutineLog(@RequestBody RoutineLog routineLog, @AuthenticationPrincipal UserDetails userDetails){


        return ResponseEntity.ok(null);
    }

}
