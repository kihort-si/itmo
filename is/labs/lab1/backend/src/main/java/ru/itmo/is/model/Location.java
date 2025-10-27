package ru.itmo.is.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    private Double x; //Поле не может быть null
    private double y;
    private float z;
    private String name; //Поле не может быть null
}
