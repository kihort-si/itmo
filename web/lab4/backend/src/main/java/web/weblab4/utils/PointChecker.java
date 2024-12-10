package web.weblab4.utils;

import web.weblab4.network.request.PointRequest;

public class PointChecker {
    public static boolean checkPoint(PointRequest pointRequest) {
        double x = pointRequest.getX();
        double y = pointRequest.getY();
        double r = pointRequest.getR();

        if (x >= 0 && y >= 0) return (y <= r) && (x <= r / 2) && (2*x + y <= r);
        if (x > 0 && y < 0) return (-y <= r) && (x <= r / 2);
        if (x < 0 && y > 0) return ((x * x + y * y) <= (r / 2 * r / 2));
        return false;
    }
}
