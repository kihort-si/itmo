package org.viacheslav;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Destroyed;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.event.Observes;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.*;
import org.viacheslav.beans.PointCounter;
import org.viacheslav.beans.ShapeArea;
import org.viacheslav.services.PointServiceImplementation;
import org.viacheslav.utils.MBeanRegistry;

@Named("utilBean")
@ApplicationScoped
public class UtilBean implements Serializable {
    public static final Logger logger = Logger.getLogger("UtilBean");
    @Getter
    @Setter
    private double x;
    @Getter
    @Setter
    private double y;

    @Getter
    private double r;

    @Setter
    @Getter
    private ArrayList<Point> pointsList;

    @Getter
    private double area;

    private DBController dbController;

    @Inject
    private PointServiceImplementation pointService;

    private final PointCounter pointCounter = new PointCounter();
    private final ShapeArea shapeArea = new ShapeArea();


    @PostConstruct
    public void init() {
        x = 0;
        y = 0;
        r = 1;
        dbController = DBController.getInstance();
        pointsList = dbController.getAll();
        if (pointsList == null) pointsList = new ArrayList<>();
        shapeArea.setRadius(r);
        area = shapeArea.getArea();
        pointCounter.setTotalPoints(pointsList.size());
    }

    public void init(@Observes @Initialized(SessionScoped.class) Object unused) {
        MBeanRegistry.registerBean(pointCounter, "pointCounter");
        MBeanRegistry.registerBean(shapeArea, "shapeArea");
    }

    public void destroy(@Observes @Destroyed(SessionScoped.class) Object unused) {
        MBeanRegistry.unregisterBean(pointCounter);
        MBeanRegistry.unregisterBean(shapeArea);
    }

    public String clear() {
        dbController.clear(getSessionId());
        pointsList.clear();
        pointsList = dbController.getAll();
        pointCounter.setTotalPoints(pointsList.size());
        return "goToMain?faces-redirect=true";
    }

    public void setR(double r) {
        this.r = r;
        shapeArea.setRadius(r);
        this.area = shapeArea.getArea();
    }

    private String getSessionId() {
        // Получаем текущий контекст
        FacesContext context = FacesContext.getCurrentInstance();
        // Получаем HttpServletRequest из контекста
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        // Получаем HttpSession
        HttpSession session = request.getSession(false); // false - не создавать новую сессию
        if (session != null) {
            return session.getId(); // Возвращаем ID сессии
        }
        return null; // Или обработайте случай, когда сессия отсутствует
    }

    public String checkAndAdd() {
        logger.info("Пришёл запрос на добавление точки: x = " + x + ", y = " + y + ", r = " + r);
        logger.info("SessionID=" + getSessionId());
        Point point = pointService.createAndCheckPoint(x, y, r, getSessionId());
        dbController.addPoint(point);
        pointsList.add(point);
        pointCounter.addPoint(point);
        return "goToMain?faces-redirect=true";
    }

    public String pointsToString() {
        String pointsStr = "";
        for (Point point : pointsList) {
            pointsStr += point.getX() + "," + point.getY() + "," + point.getR() + "," + point.isResult() + ";";
        }
        if (pointsStr.isEmpty()) return pointsStr;
        return pointsStr.substring(0, pointsStr.length() - 1);
    }

    public boolean checkSession(String sessionId) {
        return sessionId.equals(getSessionId());
    }

}