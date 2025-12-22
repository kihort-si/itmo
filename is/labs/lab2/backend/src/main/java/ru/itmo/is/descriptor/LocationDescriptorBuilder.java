package ru.itmo.is.descriptor;

import org.eclipse.persistence.descriptors.RelationalDescriptor;
import ru.itmo.is.model.Location;

public class LocationDescriptorBuilder extends BaseDescriptorBuilder {

    @Override
    public RelationalDescriptor buildDescriptor() {
        RelationalDescriptor locationDescriptor = new RelationalDescriptor();
        locationDescriptor.setJavaClass(Location.class);
        locationDescriptor.descriptorIsAggregate();

        locationDescriptor.addMapping(createDirectMapping("x", "x"));
        locationDescriptor.addMapping(createDirectMapping("y", "y"));
        locationDescriptor.addMapping(createDirectMapping("z", "z"));
        locationDescriptor.addMapping(createDirectMapping("name", "name"));

        return locationDescriptor;
    }
}
