package ru.itmo.is.dto.response;

import lombok.*;

import java.io.Serializable;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationResponseDto implements Serializable {
    private Double x;
    private double y;
    private float z;
    private String name;
}
