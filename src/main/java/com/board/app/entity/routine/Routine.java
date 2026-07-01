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

    @Column
    private String email;

    @ManyToOne(fetch = FetchType.LAZY) //LAZY 필요할 때만 호출
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
}
