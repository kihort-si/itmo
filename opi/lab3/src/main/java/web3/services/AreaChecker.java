package web3.services;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import web3.Result;

import java.io.Serializable;
import java.time.LocalTime;

import static java.lang.Math.abs;

@Named("areaChecker")
@SessionScoped
@AreaCheckQualifier
public class AreaChecker implements AreaCheck, Serializable {
    @Override
    public Result checkArea(Input input, boolean fromGraph) {
        Result resultBean = new Result();
        long startTime = System.nanoTime();
        if (!fromGraph) {
            resultBean.setX(input.getX());
            resultBean.setY(input.getY());
        } else {
            resultBean.setX(input.getXGraph());
            resultBean.setY(input.getYGraph());
        }
        resultBean.setR(input.getR());
        resultBean.setCreatedAt(LocalTime.now());
        boolean hit = check(resultBean);
        resultBean.setResult(hit);
        resultBean.setExecutionTime(System.nanoTime() - startTime);
        return resultBean;
    }

    private boolean check(Result resultBean) {
        var x = resultBean.getX();
        var y = resultBean.getY();
        var r = resultBean.getR();
        if (x >= 0 && y >= 0) return x <= r && (y <= r );
        if (x > 0 && y < 0) return (x <= r) && (y >= -(r / 2)) && (x + abs(2*y) <= r);
        if (x < 0 && y < 0) return ((x * x + y * y) <= (r / 2 * r / 2));
        return false;
    }

}
