package com.board.app.service.routine;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.board.app.dto.routine.DailySummaryResponse.DailySummaryDto;
import com.board.app.dto.routine.RoutineResponse;
import com.board.app.entity.User;
import com.board.app.entity.routine.Routine;
import com.board.app.entity.routine.RoutineLog;
import com.board.app.repository.UserRepository;
import com.board.app.repository.routine.RoutineLogRepository;
import com.board.app.repository.routine.RoutineRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoutineLogService {

    private final RoutineRepository routineRepository;
    private final RoutineLogRepository routineLogRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public boolean toggleToday(Long routineId, String email){
    	System.out.println("여기오는거 맞나");
        //본인 소유 루틴인지 검증
        //findBy + Id + And + User + Email
        //Id → routine 테이블의 id 컬럼 (이건 맞습니다)
        //UserEmail → routine.user.email, 즉 Routine이 참조하는 User의 email
        //routine 테이블에는 email 컬럼이 없습니다. 대신 user_id FK가 있고, 그게 users 테이블을 가리키죠. Spring이 UserEmail을 user.email로 해석해서, 내부적으로 routine과 users를 조인합니다.
        Routine routine = routineRepository.findByIdAndUserEmail(routineId, email).orElseThrow(() -> new IllegalArgumentException("루틴을 찾을 수 없습니다."));

        LocalDate today = LocalDate.now();
        var existing = routineLogRepository.findByRoutineIdAndLogDate(routineId, today);

        
        RoutineLog routineEntity = RoutineLog.builder().routine(routine).logDate(today).build();
        
        if(existing.isPresent()) {
        	routineLogRepository.delete(existing.get());
        	return true;
        }else {
        	routineLogRepository.save(routineEntity);
        	return false;
        }
    }
    
    @Transactional
    public List<RoutineLog> findMonthlyLogs(int year, int month, UserDetails userDetails){
    	
    	User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow(()->new UsernameNotFoundException("User not found"));
    
    	List<Routine> routines = routineRepository.findByUserId(user.getId());
    	
    	List<Long> routineIds = routines.stream().map(Routine::getId).toList();
    	
    	
    	LocalDate start = LocalDate.of(year, month, 1);
    	LocalDate end   = start.withDayOfMonth(start.lengthOfMonth());
    	
    	List<RoutineLog> routineLog = routineLogRepository.findAllByRoutineIdAndLogDateBetween(routineIds, start, end);

    	return routineLog;
    }
    
    @Transactional
    public List<RoutineLog> findDayLogs(LocalDate logDate, UserDetails userDetails){
    	
    	User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow(()->new UsernameNotFoundException("User not found"));
    	
    	List<Routine> routines = routineRepository.findByUserId(user.getId());
		Set<Long> routineIds = new HashSet(routines.stream().map(Routine::getId).toList());
    	
    	List<RoutineLog> routineLog = routineLogRepository.findAllByRoutineIdAndLogDateOne(routineIds, logDate);
    	
    	   	    	   	
    	RoutineResponse.Detail.from(routines, routineIds);
    	    	
    	return routineLog;
    }
    
    @Transactional
    public List<DailySummaryDto> getMonthlySummary(int year, int month, UserDetails userDetails) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow(()->new UsernameNotFoundException("User not found"));
        
        long total = routineRepository.countByUserId(user.getId());
        
        return routineLogRepository.countByDate(user.getId(), start, end).stream().map(e-> new DailySummaryDto((LocalDate)e[0], (long)e[1] ,total)).toList();
    }
    
    
}
