package ru.itmo.is.dto.response;

import lombok.*;
import ru.itmo.is.model.Color;
import ru.itmo.is.model.Country;

import java.io.Serializable;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonResponseDto implements Serializable {
    private Long id;
    private String name;
    private Color eyeColor;
    private Color hairColor;
    private LocationResponseDto location;
    private String passportID;
    private Country nationality;
}
