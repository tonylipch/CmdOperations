import com.anton.simplecalc.Params;
import com.anton.simplecalc.SimpleCalc;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.anton.cmdlineproc.engine.CmdLineEngine;

public class SimpleCalcTest {

    @Test
    void testAddition() {
        Params params = new Params();
        params.setLeft(5);
        params.setRight(3);
        params.setOperation(Params.Operation.plus);

        int result = SimpleCalc.calc(params);
        Assertions.assertEquals(8, result);
    }

    @Test
    void testSubtraction() {
        Params params = new Params();
        params.setLeft(10);
        params.setRight(5);
        params.setOperation(Params.Operation.minus);

        int result = SimpleCalc.calc(params);
        Assertions.assertEquals(5, result);
    }

    @Test
    void testMultiplication() {
        Params params = new Params();
        params.setLeft(4);
        params.setRight(3);
        params.setOperation(Params.Operation.mul);

        int result = SimpleCalc.calc(params);
        Assertions.assertEquals(12, result);
    }

    @Test
    void testDivision() {
        Params params = new Params();
        params.setLeft(10);
        params.setRight(2);
        params.setOperation(Params.Operation.div);

        int result = SimpleCalc.calc(params);
        Assertions.assertEquals(5, result);
    }

    @Test
    void testDivisionByZero() {
        Params params = new Params();
        params.setLeft(10);
        params.setRight(0);
        params.setOperation(Params.Operation.div);

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            SimpleCalc.calc(params);
        });
    }

    @Test
    void testUnknownOperation() {
        Params params = new Params();
        params.setLeft(5);
        params.setRight(3);
        params.setOperation(null);

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            SimpleCalc.calc(params);
        });
    }
}
