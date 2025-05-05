package org.viacheslav.beans;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import org.viacheslav.Point;
import org.viacheslav.services.AreaChecker;

import javax.management.*;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

@Named("pointCounter")
@ApplicationScoped
public class PointCounter implements PointCounterMBean, NotificationBroadcaster, Serializable {
    private final AtomicInteger totalPoints = new AtomicInteger();
    private final AtomicInteger pointsInArea = new AtomicInteger();
//    private int totalPoints = 0;
//    private int pointsInArea = 0;
//    private long sequenceNumber = 1;

    private final NotificationBroadcasterSupport broadcaster = new NotificationBroadcasterSupport();


    @Override
    public synchronized void addPoint(Point point) {
        totalPoints.incrementAndGet();
        if (point.isResult()) {
            pointsInArea.incrementAndGet();
        }

        if (totalPoints.get() % 10 == 0) {
            System.out.println("Total points: " + totalPoints.get());
            broadcaster.sendNotification(new Notification(
                    "point.count.multiple10",
                    this,
                    System.currentTimeMillis(),
                    "Количество точек кратно 10: " + totalPoints.get()
            ));
        }
    }

    @Override
    public void setTotalPoints(int points){
        totalPoints.set(points);
    }

    @Override
    public int getTotalPoints() {
        return totalPoints.get();
    }

    @Override
    public int getPointsInArea() {
        return pointsInArea.get();
    }

    @Override
    public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object o) throws IllegalArgumentException {
        broadcaster.addNotificationListener(listener, filter, o);
    }

    @Override
    public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException {
        broadcaster.removeNotificationListener(listener);
    }

    @Override
    public MBeanNotificationInfo[] getNotificationInfo() {
        String[] types = new String[]{"point.count.multiple10"};
        String name = Notification.class.getName();
        String description = "Notification is sent when the number of points is multiples of 10";
        return new MBeanNotificationInfo[]{new MBeanNotificationInfo(types, name, description)};
    }
}
