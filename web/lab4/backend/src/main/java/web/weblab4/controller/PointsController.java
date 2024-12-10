package web.weblab4.controller;

import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import web.weblab4.database.PointsDB;
import web.weblab4.models.Point;
import web.weblab4.models.User;
import web.weblab4.network.request.PointRequest;
import web.weblab4.network.response.PointResponse;
import web.weblab4.utils.PointChecker;
import web.weblab4.utils.SecurityManager;

import java.time.LocalTime;
import java.util.List;

@Path("/results")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PointsController {
    @EJB
    private PointsDB pointsDB;
    @Inject
    private SecurityManager securityManager;

    @POST
    public Response savePoint(@Valid PointRequest pointRequest) {
        long startTime = System.nanoTime();
        User user = securityManager.getCurrentUser();
        Point point = new Point();
        point.setX(pointRequest.getX());
        point.setY(pointRequest.getY());
        point.setR(pointRequest.getR());
        point.setCreatedAt(LocalTime.now().toString());
        point.setResult(PointChecker.checkPoint(pointRequest));
        point.setCreatedBy(user.getId());
        point.setExecutionTime(System.nanoTime() - startTime);
        pointsDB.addPoint(point);
        return Response.ok().build();
    }

    @GET
    public Response getPoints() {
        User user = securityManager.getCurrentUser();
        List<Point> points = pointsDB.getResultsByUser(user.getId());
        List<PointResponse> pointResponses = points.stream()
                .map(PointResponse::new)
                .toList();
        return Response.ok(pointResponses).build();
    }

    @DELETE
    public Response deleteAllPointsByCurrentUser() {
        User user = securityManager.getCurrentUser();
        pointsDB.removePoints(user.getId());
        return Response.ok().build();
    }
}
