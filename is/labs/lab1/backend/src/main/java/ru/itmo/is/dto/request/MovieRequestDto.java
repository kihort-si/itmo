package ru.itmo.is.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.itmo.is.model.MovieGenre;
import ru.itmo.is.model.MpaaRating;

import java.io.Serializable;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieRequestDto implements Serializable {
    @NotNull
    @NotBlank
    private String name;
    @NotNull
    private CoordinatesRequestDto coordinates;
    @Min(0)
    private long oscarsCount;
    @Min(1)
    @Nullable
    private Integer budget;
    @Min(1)
    @Nullable
    private Long totalBoxOffice;
    @NotNull
    private MpaaRating mpaaRating;
    @NotNull
    private Long directorId;
    @Nullable
    private Long screenwriterId;
    @Nullable
    private Long operatorId;
    @Min(1)
    private int length;
    @Min(1)
    private long goldenPalmCount;
    @Nullable
    private MovieGenre genre;
}
