package ru.itmo.se.is.cw.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.ZonedDateTime;

@Getter
@Setter
@Entity
@Table(name = "file_version")
public class FileVersionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    @Column(name = "bucket", nullable = false, length = 63)
    private String bucket;

    @Column(name = "object_key", nullable = false, length = Integer.MAX_VALUE)
    private String objectKey;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "uploaded_at", nullable = false)
    private ZonedDateTime uploadedAt;

    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "file_id", nullable = false)
    private FileEntity file;


}