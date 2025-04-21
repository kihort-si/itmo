package web3;

import jakarta.inject.Named;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Named("resultBean")
@Table(name="results")
public class AltResult implements Serializable {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id", nullable=false, unique=true)
    private int id;

    @NotNull
    @Column(name="x", nullable=false)
    private double x;

    @NotNull
    @Column(name="y", nullable=false)
    private double y;

    @NotNull
    @Column(name="r", nullable=false)
    private double r;

    @NotNull
    @Column(name="result", nullable=false)
    private boolean result;

    @NotNull
    @Column(name="created_at", nullable=false)
    private LocalTime createdAt;

    @NotNull
    @Column(name="execution_time", nullable=false)
    private Long executionTime;
}