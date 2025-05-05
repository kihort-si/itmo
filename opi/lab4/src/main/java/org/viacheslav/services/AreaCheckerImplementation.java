package org.viacheslav.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@Named("areaChecker")
@ApplicationScoped
public class AreaCheckerImplementation implements AreaChecker {
    @Override
    public boolean check(double x, double y, double r) {
        return (x >= 0 && y >= 0 && (y <= r / 2 - 0.5 * x)) ||
                (x < 0 && y <= 0 && (Math.sqrt(x * x + y * y) <= r)) ||
                (x < 0 && y >= 0 && (x >= -r) && (y <= r));
    }
}
