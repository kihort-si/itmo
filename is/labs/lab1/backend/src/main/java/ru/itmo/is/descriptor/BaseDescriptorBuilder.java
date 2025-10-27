package ru.itmo.is.descriptor;

import org.eclipse.persistence.descriptors.RelationalDescriptor;
import org.eclipse.persistence.mappings.DirectToFieldMapping;
import org.eclipse.persistence.mappings.converters.EnumTypeConverter;

public abstract class BaseDescriptorBuilder {

    protected DirectToFieldMapping createIdMapping() {
        DirectToFieldMapping idMapping = new DirectToFieldMapping();
        idMapping.setAttributeName("id");
        idMapping.setFieldName("id");
        idMapping.setIsPrimaryKeyMapping(true);
        return idMapping;
    }

    protected DirectToFieldMapping createDirectMapping(String attributeName, String fieldName) {
        DirectToFieldMapping mapping = new DirectToFieldMapping();
        mapping.setAttributeName(attributeName);
        mapping.setFieldName(fieldName);
        return mapping;
    }

    protected DirectToFieldMapping createEnumMapping(String attributeName, String fieldName, Class<?> enumClass) {
        DirectToFieldMapping mapping = new DirectToFieldMapping();
        mapping.setAttributeName(attributeName);
        mapping.setFieldName(fieldName);
        EnumTypeConverter converter = new EnumTypeConverter(mapping, enumClass, false);
        mapping.setConverter(converter);
        return mapping;
    }

    public abstract RelationalDescriptor buildDescriptor();
}
