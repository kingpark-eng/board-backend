package com.board.app.entity.routine;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "routine_log", uniqueConstraints= @UniqueConstraint(columnNames={"routine_id", "log_date"}))
public class RoutineLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "routine_id", referencedColumnName = "id", nullable = false) ref를 사용해도 되고 안해도됨. 안하면 기본참조
    @JoinColumn(name = "routine_id", nullable = false)
    private Routine routine;

}
