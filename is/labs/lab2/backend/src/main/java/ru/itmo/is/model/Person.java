package ru.itmo.is.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "person", uniqueConstraints = {
    @UniqueConstraint(name = "uk_person_name_location",
        columnNames = {"name", "location_x", "location_y", "location_z", "location_name"}),
    @UniqueConstraint(name = "uk_person_passportID",
        columnNames = {"passportID"})
})
@SequenceGenerator(name = "person_seq", sequenceName = "person_id_seq", allocationSize = 1)
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "person_seq")
    private Long id;
    
    @Column(name = "name", nullable = false)
    private String name; //Поле не может быть null, Строка не может быть пустой
    
    @Enumerated(EnumType.STRING)
    @Column(name = "eyeColor")
    private Color eyeColor; //Поле может быть null
    
    @Enumerated(EnumType.STRING)
    @Column(name = "hairColor")
    private Color hairColor; //Поле может быть null
    
    @Embedded
    private Location location; //Поле не может быть null
    
    @Column(name = "passportID", nullable = false, unique = true)
    private String passportID; //Поле не может быть null
    
    @Enumerated(EnumType.STRING)
    @Column(name = "nationality", nullable = false)
    private Country nationality; //Поле не может быть null
}
