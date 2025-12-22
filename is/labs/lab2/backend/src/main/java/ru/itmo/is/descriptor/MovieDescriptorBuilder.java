package ru.itmo.is.descriptor;

import org.eclipse.persistence.descriptors.RelationalDescriptor;
import org.eclipse.persistence.mappings.AggregateObjectMapping;
import org.eclipse.persistence.mappings.DirectToFieldMapping;
import org.eclipse.persistence.mappings.OneToOneMapping;
import org.eclipse.persistence.sequencing.NativeSequence;
import ru.itmo.is.model.*;
import ru.itmo.is.utils.CreationDateConverter;

public class MovieDescriptorBuilder extends BaseDescriptorBuilder {

    @Override
    public RelationalDescriptor buildDescriptor() {
        RelationalDescriptor movieDescriptor = new RelationalDescriptor();
        movieDescriptor.setJavaClass(Movie.class);
        movieDescriptor.setTableName("movie");
        movieDescriptor.addPrimaryKeyFieldName("id");

        movieDescriptor.setSequenceNumberFieldName("id");
        movieDescriptor.setSequenceNumberName("movie_id_seq");
        NativeSequence sequence = new NativeSequence("movie_id_seq");
        sequence.setPreallocationSize(1);
        movieDescriptor.setSequence(sequence);

        movieDescriptor.addMapping(createIdMapping());

        movieDescriptor.addMapping(createDirectMapping("name", "name"));

        DirectToFieldMapping creationDateMapping = createDirectMapping("creationDate", "creationdate");
        creationDateMapping.setConverter(new CreationDateConverter());
        movieDescriptor.addMapping(creationDateMapping);

        movieDescriptor.addMapping(createDirectMapping("oscarsCount", "oscarsCount"));
        movieDescriptor.addMapping(createDirectMapping("budget", "budget"));
        movieDescriptor.addMapping(createDirectMapping("totalBoxOffice", "totalBoxOffice"));
        movieDescriptor.addMapping(createDirectMapping("length", "length"));
        movieDescriptor.addMapping(createDirectMapping("goldenPalmCount", "goldenPalmCount"));

        movieDescriptor.addMapping(createEnumMapping("mpaaRating", "mpaaRating", MpaaRating.class));
        movieDescriptor.addMapping(createEnumMapping("genre", "genre", MovieGenre.class));

        movieDescriptor.addMapping(createPersonMapping("director", "director_id", false));
        movieDescriptor.addMapping(createPersonMapping("screenwriter", "screenwriter_id", true));
        movieDescriptor.addMapping(createPersonMapping("operator", "operator_id", true));

        AggregateObjectMapping coordinatesMapping = new AggregateObjectMapping();
        coordinatesMapping.setAttributeName("coordinates");
        coordinatesMapping.setReferenceClass(Coordinates.class);
        coordinatesMapping.addFieldNameTranslation("coordinates_x", "x");
        coordinatesMapping.addFieldNameTranslation("coordinates_y", "y");
        movieDescriptor.addMapping(coordinatesMapping);

        return movieDescriptor;
    }

    private OneToOneMapping createPersonMapping(String attributeName, String foreignKey, boolean isOptional) {
        OneToOneMapping mapping = new OneToOneMapping();
        mapping.setAttributeName(attributeName);
        mapping.setReferenceClass(Person.class);
        mapping.addForeignKeyFieldName(foreignKey, "id");
        mapping.setIsOptional(isOptional);
        mapping.setCascadeAll(true);
        mapping.dontUseIndirection();
        return mapping;
    }
}
