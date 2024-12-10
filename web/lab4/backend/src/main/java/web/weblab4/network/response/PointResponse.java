package web.weblab4.network.response;

import lombok.*;
import web.weblab4.models.Point;

import java.io.Serializable;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PointResponse implements Serializable {
    private int id;
    private double x;
    private double y;
    private double r;
    private boolean result;
    private String createdAt;
    private long executionTime;

    public PointResponse(Point point) {
        this.id = point.getId();
        this.x = point.getX();
        this.y = point.getY();
        this.r = point.getR();
        this.result = point.isResult();
        this.createdAt = point.getCreatedAt();
        this.executionTime = point.getExecutionTime();
    }
}
