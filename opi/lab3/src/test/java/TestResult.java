import org.junit.Before;
import web3.Result;
import web3.services.AreaChecker;
import web3.services.Input;

public class TestResult {
    private AreaChecker areaChecker;

    @Before
    public void setUp() {
        areaChecker = new AreaChecker();
    }

    @org.junit.Test
    public void testCheckArea() {
        // Test case 1: Point inside the area
        Input input1 = new Input();
        input1.setX(1.0);
        input1.setY(1.0);
        input1.setR(3.0);
        Result result1 = areaChecker.checkArea(input1, false);
        assert result1.isResult();

        // Test case 2: Point outside the area
        Input input2 = new Input();
        input2.setX(3.0);
        input2.setY(3.0);
        input2.setR(2.0);
        Result result2 = areaChecker.checkArea(input2, false);
        assert !result2.isResult();

        // Test case 3: Point on the boundary of the area
        Input input3 = new Input();
        input3.setX(0.0);
        input3.setY(0.0);
        input3.setR(2.0);
        Result result3 = areaChecker.checkArea(input3, false);
        assert result3.isResult();
    }
}
