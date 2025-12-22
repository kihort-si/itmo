package ru.itmo.is.descriptor;

import org.eclipse.persistence.descriptors.RelationalDescriptor;
import org.eclipse.persistence.mappings.AggregateObjectMapping;
import org.eclipse.persistence.sequencing.NativeSequence;
import ru.itmo.is.model.*;

public class PersonDescriptorBuilder extends BaseDescriptorBuilder {

    @Override
    public RelationalDescriptor buildDescriptor() {
        RelationalDescriptor personDescriptor = new RelationalDescriptor();
        personDescriptor.setJavaClass(Person.class);
        personDescriptor.setTableName("person");
        personDescriptor.addPrimaryKeyFieldName("id");

        personDescriptor.setSequenceNumberFieldName("id");
        personDescriptor.setSequenceNumberName("person_id_seq");
        NativeSequence sequence = new NativeSequence("person_id_seq");
        sequence.setPreallocationSize(1);
        personDescriptor.setSequence(sequence);

        personDescriptor.addMapping(createIdMapping());

        personDescriptor.addMapping(createDirectMapping("name", "name"));
        personDescriptor.addMapping(createDirectMapping("passportID", "passportID"));
        personDescriptor.addMapping(createEnumMapping("eyeColor", "eyeColor", Color.class));
        personDescriptor.addMapping(createEnumMapping("hairColor", "hairColor", Color.class));
        personDescriptor.addMapping(createEnumMapping("nationality", "nationality", Country.class));

        AggregateObjectMapping locationMapping = new AggregateObjectMapping();
        locationMapping.setAttributeName("location");
        locationMapping.setReferenceClass(Location.class);
        locationMapping.addFieldNameTranslation("location_x", "x");
        locationMapping.addFieldNameTranslation("location_y", "y");
        locationMapping.addFieldNameTranslation("location_z", "z");
        locationMapping.addFieldNameTranslation("location_name", "name");
        personDescriptor.addMapping(locationMapping);

        return personDescriptor;
    }
}
