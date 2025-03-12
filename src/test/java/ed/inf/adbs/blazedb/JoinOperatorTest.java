package ed.inf.adbs.blazedb;

import ed.inf.adbs.blazedb.entity.Tuple;
import ed.inf.adbs.blazedb.operator.JoinOperator;
import ed.inf.adbs.blazedb.operator.ScanOperator;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class JoinOperatorTest {
    @Mock
    private ScanOperator leftScanOperator;
    @Mock
    private ScanOperator rightScanOperator;

    @Test
    public void getNextTuple() throws JSQLParserException {
        String raw_expression = "Student.A = Enrolled.A";
        Expression expression = CCJSqlParserUtil.parseExpression(raw_expression);
        String leftTable = "Student";
        String rightTable = "Enrolled";
        JoinExpressionEvaluator joinExpressionEvaluator = new JoinExpressionEvaluator();
        JoinOperator joinOperator = new JoinOperator(leftTable, rightTable, expression, joinExpressionEvaluator);

        // mock the child operators
        Tuple leftTuple = new Tuple();
        List<String> leftTupleColumns = new ArrayList<>();
        leftTupleColumns.add("Student.A");
        leftTupleColumns.add("Student.B");
        HashMap<String, Integer> leftLookup = new HashMap<String, Integer>();
        leftLookup.put("Student.A", 1);
        leftLookup.put("Student.B", 5);

        Tuple rightTuple = new Tuple();
        List<String> rightTupleColumns = new ArrayList<>();
        rightTupleColumns.add("Enrolled.A");
        rightTupleColumns.add("Enrolled.E");
        HashMap<String, Integer> rightLookup = new HashMap<String, Integer>();
        rightLookup.put("Enrolled.A", 1);
        rightLookup.put("Enrolled.E", 16);


        Mockito.when(leftScanOperator.getNextTuple()).thenReturn(leftTuple, (Tuple) null);
        Mockito.when(rightScanOperator.getNextTuple()).thenReturn(rightTuple, (Tuple) null);


        Tuple joinTuple = joinOperator.getNextTuple();

        // A, B, E
        assertEquals(3, joinTuple.getColumns().size());
        int A = joinTuple.getLookup().get("Student.A");
        assertEquals(1, A);
        int B = joinTuple.getLookup().get("Student.B");
        assertEquals(5, B);
        int E = joinTuple.getLookup().get("Enrolled.E");
        assertEquals(16, E);
    }
}
