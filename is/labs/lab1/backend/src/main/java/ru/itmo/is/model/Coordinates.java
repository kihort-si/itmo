package ru.itmo.is.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Coordinates {
    private double x; //Максимальное значение поля: 347
    private Integer y; //Поле не может быть null
}
