package ru.itmo.is.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Coordinates {
    @Column(name = "coordinates_x", nullable = false)
    private double x; //Максимальное значение поля: 347
    
    @Column(name = "coordinates_y", nullable = false)
    private Integer y; //Поле не может быть null
}
