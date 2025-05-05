package org.viacheslav.beans;

import org.viacheslav.Point;

public interface PointCounterMBean {
    void addPoint(Point point);
    int getTotalPoints();
    int getPointsInArea();
    void setTotalPoints(int points);
}
