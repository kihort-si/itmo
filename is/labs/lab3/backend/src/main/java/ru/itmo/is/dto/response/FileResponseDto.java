package ru.itmo.is.dto.response;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileResponseDto implements Serializable {
    private Long id;
    private String filename;
    private Long size;
    private boolean success;
    private LocalDateTime creationDate;
    private Integer objectsCount;
}
