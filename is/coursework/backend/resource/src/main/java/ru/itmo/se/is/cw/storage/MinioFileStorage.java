package ru.itmo.se.is.cw.storage;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class MinioFileStorage implements FileStorage {

    private final MinioClient minio;

    @Override
    public void save(String bucket, String objectKey, InputStream content, long size, String contentType) throws Exception {
        minio.putObject(
                PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectKey)
                        .stream(content, size, -1)
                        .contentType(contentType)
                        .build()
        );
    }

    @Override
    public InputStream get(String bucket, String objectKey) throws Exception {
        return minio.getObject(
                GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectKey)
                        .build()
        );
    }

    @Override
    public void delete(String bucket, String objectKey) throws Exception {
        minio.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectKey)
                        .build()
        );
    }
}
