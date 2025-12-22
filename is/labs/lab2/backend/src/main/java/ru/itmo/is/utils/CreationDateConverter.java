package ru.itmo.is.utils;

import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.converters.Converter;
import org.eclipse.persistence.sessions.Session;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class CreationDateConverter implements Converter {

    @Override
    public Object convertObjectValueToDataValue(Object objectValue, Session session) {
        if (objectValue == null) {
            return Timestamp.valueOf(LocalDateTime.now());
        }
        if (objectValue instanceof LocalDateTime) {
            return Timestamp.valueOf((LocalDateTime) objectValue);
        }
        return objectValue;
    }

    @Override
    public Object convertDataValueToObjectValue(Object dataValue, Session session) {
        if (dataValue == null) {
            return null;
        }
        if (dataValue instanceof Timestamp) {
            return ((Timestamp) dataValue).toLocalDateTime();
        }
        return dataValue;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public void initialize(DatabaseMapping mapping, Session session) {
    }
}