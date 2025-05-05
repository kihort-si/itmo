package org.viacheslav.services;

import java.io.Serializable;

public interface AreaChecker extends Serializable {
    boolean check(double x, double y, double r);
}
