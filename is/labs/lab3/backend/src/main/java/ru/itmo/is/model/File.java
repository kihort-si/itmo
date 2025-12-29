package ru.itmo.is.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "file")
@SequenceGenerator(name = "file_seq", sequenceName = "file_id_seq", allocationSize = 1)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "File")
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "file_seq")
    private Long id;
    
    @Column(name = "filename", nullable = false)
    private String filename;
    
    @Column(name = "size", nullable = false)
    private Long size;
    
    @Column(name = "success", nullable = false)
    private boolean success;
    
    @Column(name = "creationdate")
    private LocalDateTime creationDate;
    
    @Column(name = "objectscount")
    private Integer objectsCount;
    
    @Column(name = "object_key")
    private String objectKey;
}
