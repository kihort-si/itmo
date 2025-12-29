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
public class Location {
    @Column(name = "location_x", nullable = false)
    private Double x; //Поле не может быть null
    
    @Column(name = "location_y", nullable = false)
    private double y;
    
    @Column(name = "location_z", nullable = false)
    private float z;
    
    @Column(name = "location_name", nullable = false)
    private String name; //Поле не может быть null
}
