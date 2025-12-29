package ru.itmo.is.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.time.LocalDateTime;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "movie", uniqueConstraints = {
    @UniqueConstraint(name = "uk_movie_name_staff", 
        columnNames = {"name", "director_id", "screenwriter_id", "operator_id"})
})
@SequenceGenerator(name = "movie_seq", sequenceName = "movie_id_seq", allocationSize = 1)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "Movie")
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "movie_seq")
    private int id; //Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
    
    @Column(name = "name", nullable = false)
    private String name; //Поле не может быть null, Строка не может быть пустой
    
    @Embedded
    private Coordinates coordinates; //Поле не может быть null
    
    @Column(name = "creationdate", nullable = false, updatable = false)
    private LocalDateTime creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически
    
    @Column(name = "oscarsCount", nullable = false)
    private long oscarsCount; //Значение поля должно быть больше 0
    
    @Column(name = "budget")
    private Integer budget; //Значение поля должно быть больше 0, Поле может быть null
    
    @Column(name = "totalBoxOffice")
    private Long totalBoxOffice; //Поле может быть null, Значение поля должно быть больше 0
    
    @Enumerated(EnumType.STRING)
    @Column(name = "mpaaRating", nullable = false)
    private MpaaRating mpaaRating; //Поле не может быть null
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "director_id", nullable = false)
    private Person director; //Поле не может быть null
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screenwriter_id")
    private Person screenwriter;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id")
    private Person operator; //Поле может быть null
    
    @Column(name = "length", nullable = false)
    private int length; //Значение поля должно быть больше 0
    
    @Column(name = "goldenPalmCount", nullable = false)
    private long goldenPalmCount; //Значение поля должно быть больше 0
    
    @Enumerated(EnumType.STRING)
    @Column(name = "genre")
    private MovieGenre genre; //Поле может быть null
}
