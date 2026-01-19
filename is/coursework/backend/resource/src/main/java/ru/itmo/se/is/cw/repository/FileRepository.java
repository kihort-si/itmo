package ru.itmo.se.is.cw.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.itmo.se.is.cw.model.FileEntity;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {

    @Modifying
    @Query(value = """
            call p_update_file_version_and_set_current(
                :fileId, :bucket, :objectKey, :sizeBytes, :contentType, :creatorId
            )
            """, nativeQuery = true)
    void updateFileVersionAndSetCurrent(
            @Param("fileId") Long fileId,
            @Param("bucket") String bucket,
            @Param("objectKey") String objectKey,
            @Param("sizeBytes") Long sizeBytes,
            @Param("contentType") String contentType,
            @Param("creatorId") Long creatorId
    );

}
