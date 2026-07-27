package com.board.app.entity.routine;

import java.time.LocalDateTime;

import com.board.app.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Routine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String title;

    @Column(nullable = false)
    private boolean done;

//    @ManyToOne은 **"많은(Many) Routine이 하나(One)의 User를 가리킨다"**는 뜻이에요. 애노테이션을 붙인 쪽(Routine)이 Many, 참조 대상(User)이 One
    @ManyToOne(fetch = FetchType.LAZY) //LAZY 필요할 때만 호출
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
    
    @Column
    private LocalDateTime deleteAt;
    
    public void softDelete() {
    	this.deleteAt = LocalDateTime.now();
    }
}
