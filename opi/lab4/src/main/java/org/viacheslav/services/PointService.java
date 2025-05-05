package org.viacheslav.services;

import org.viacheslav.Point;

public interface PointService {
    Point createAndCheckPoint(double x, double y, double r, String session);
}
