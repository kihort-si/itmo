package ru.itmo.is.descriptor;

import org.eclipse.persistence.descriptors.RelationalDescriptor;
import org.eclipse.persistence.mappings.DirectToFieldMapping;
import org.eclipse.persistence.sequencing.NativeSequence;
import ru.itmo.is.model.File;
import ru.itmo.is.utils.CreationDateConverter;

public class FileDescriptorBuilder extends BaseDescriptorBuilder {
    @Override
    public RelationalDescriptor buildDescriptor() {
        RelationalDescriptor fileDescriptor = new RelationalDescriptor();
        fileDescriptor.setJavaClass(File.class);
        fileDescriptor.setTableName("file");
        fileDescriptor.addPrimaryKeyFieldName("id");

        fileDescriptor.setSequenceNumberFieldName("id");
        fileDescriptor.setSequenceNumberName("file_id_seq");
        NativeSequence sequence = new NativeSequence("file_id_seq");
        sequence.setPreallocationSize(1);
        fileDescriptor.setSequence(sequence);

        fileDescriptor.addMapping(createIdMapping());
        fileDescriptor.addMapping(createDirectMapping("filename", "filename"));
        fileDescriptor.addMapping(createDirectMapping("size", "size"));
        fileDescriptor.addMapping(createDirectMapping("success", "success"));
        DirectToFieldMapping creationDateMapping = createDirectMapping("creationDate", "creationdate");
        creationDateMapping.setConverter(new CreationDateConverter());
        fileDescriptor.addMapping(creationDateMapping);
        fileDescriptor.addMapping(createDirectMapping("objectsCount", "objectscount"));

        return fileDescriptor;
    }
}
