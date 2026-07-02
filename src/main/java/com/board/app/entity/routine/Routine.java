package com.board.app.entity.routine;

import com.board.app.entity.User;
import jakarta.persistence.*;
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
}
