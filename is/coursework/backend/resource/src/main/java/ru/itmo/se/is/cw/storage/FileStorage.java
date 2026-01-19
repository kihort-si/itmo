package ru.itmo.se.is.cw.storage;

import java.io.InputStream;

public interface FileStorage {

    void save(String bucket, String objectKey, InputStream content, long size, String contentType) throws Exception;

    InputStream get(String bucket, String objectKey) throws Exception;

    void delete(String bucket, String objectKey) throws Exception;
}
