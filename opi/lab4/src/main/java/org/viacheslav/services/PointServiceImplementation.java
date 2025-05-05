package org.viacheslav.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.viacheslav.Point;

import java.util.Date;

@Named("pointService")
@ApplicationScoped
public class PointServiceImplementation implements PointService {
    @Inject
    AreaChecker areaChecker;

    @Override
    public Point createAndCheckPoint(double x, double y, double r, String session) {
        Point point = new Point(x, y, r);
        point.setDate(new Date(System.currentTimeMillis()));
        System.out.println(point.toString());
        point.setResult(areaChecker.check(x, y, r));
        point.setSession(session);
        return point;
    }
}
