package org.viacheslav;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Entity
@Setter
@Getter
@NoArgsConstructor
@Named("pointBean")
@ApplicationScoped
@Table(name = "points")
public class Point implements Serializable {
    @Id
    @GeneratedValue
    @Column(name="id", nullable=false, unique=true)
    private int id;

    @Column(name="x", nullable=false)
    private double x;

    @Column(name="y", nullable=false)
    private double y;

    @Column(name="r", nullable=false)
    private double r;

    @Column(name="date", nullable=false)
    private Date date;

    @Column(name="result", nullable=false)
    private boolean result;

    @Column(name="session")
    private String session;

    public Point(double x, double y, double r) {
        this.x = x;
        this.y = y;
        this.r = r;
    }

    @Override
    public String toString() {
        return "x = " + x + ", y = " + y + ", r = " + r + ", date = " + date + ", isHit = " + result;
    }
}
