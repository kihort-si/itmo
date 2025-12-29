package ru.itmo.is.dto.response;

import lombok.*;
import ru.itmo.is.model.MovieGenre;
import ru.itmo.is.model.MpaaRating;

import java.io.Serializable;
import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieResponseDto implements Serializable {
    private int id;
    private String name;
    private CoordinatesResponseDto coordinates;
    private LocalDateTime creationDate;
    private long oscarsCount;
    private Integer budget;
    private Long totalBoxOffice;
    private MpaaRating mpaaRating;
    private PersonResponseDto director;
    private PersonResponseDto screenwriter;
    private PersonResponseDto operator;
    private int length;
    private long goldenPalmCount;
    private MovieGenre genre;
}
