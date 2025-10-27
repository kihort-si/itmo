package ru.itmo.is.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Person {
    private Long id;
    private String name; //Поле не может быть null, Строка не может быть пустой
    private Color eyeColor; //Поле может быть null
    private Color hairColor; //Поле может быть null
    private Location location; //Поле не может быть null
    private String passportID; //Поле не может быть null
    private Country nationality; //Поле не может быть null
}
