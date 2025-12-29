package ru.itmo.is.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.itmo.is.model.Color;
import ru.itmo.is.model.Country;

import java.io.Serializable;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonRequestDto implements Serializable {
    @NotNull
    @NotBlank
    private String name;
    @Nullable
    private Color eyeColor;
    @Nullable
    private Color hairColor;
    @NotNull
    private LocationRequestDto location;
    @NotNull
    private String passportID;
    @NotNull
    private Country nationality;
}
