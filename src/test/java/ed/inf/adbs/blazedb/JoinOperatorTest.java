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

import java.io.IOException;
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
    public void getNextTupleForOneJoinExpression() throws JSQLParserException {
        String raw_expression = "Student.A = Enrolled.A";
        Expression expression = CCJSqlParserUtil.parseExpression(raw_expression);
    
        JoinOperator joinOperator = new JoinOperator(expression);
     
        // mock the child operators
        Tuple leftTuple = new Tuple();
        List<String> leftTupleColumns = new ArrayList<>();
        leftTupleColumns.add("Student.A");
        leftTupleColumns.add("Student.B");
        leftTuple.setColumns(leftTupleColumns);

        HashMap<String, Integer> leftLookup = new HashMap<String, Integer>();
        leftLookup.put("Student.A", 1);
        leftLookup.put("Student.B", 5);
        leftTuple.setLookup(leftLookup);

        Tuple rightTuple = new Tuple();
        List<String> rightTupleColumns = new ArrayList<>();
        rightTupleColumns.add("Enrolled.A");
        rightTupleColumns.add("Enrolled.E");
        rightTuple.setColumns(rightTupleColumns);

        HashMap<String, Integer> rightLookup = new HashMap<String, Integer>();
        rightLookup.put("Enrolled.A", 1);
        rightLookup.put("Enrolled.E", 16);
        rightTuple.setLookup(rightLookup);

        Mockito.when(leftScanOperator.getNextTuple()).thenReturn(leftTuple, (Tuple) null);
        Mockito.when(rightScanOperator.getNextTuple()).thenReturn(rightTuple, (Tuple) null);

        joinOperator.setLeftChild(leftScanOperator);
        joinOperator.setRightChild(rightScanOperator);
        Tuple joinTuple = joinOperator.getNextTuple();

        assertEquals(4, joinTuple.getColumns().size());
        int studentA = joinTuple.getLookup().get("Student.A");
        assertEquals(1, studentA);
        int studentB = joinTuple.getLookup().get("Student.B");
        assertEquals(5, studentB);
        int enrolledA = joinTuple.getLookup().get("Enrolled.A");
        assertEquals(1, enrolledA);
        int enrolledE = joinTuple.getLookup().get("Enrolled.E");
        assertEquals(16, enrolledE);
    }

    @Test
    public void getNextTupleForTwoJoinExpressions() throws JSQLParserException {
        String raw_expression = "Student.A = Enrolled.A AND Student.B = Enrolled.E";
        Expression expression = CCJSqlParserUtil.parseExpression(raw_expression);
        JoinOperator joinOperator = new JoinOperator(expression);

        // mock the child operators
        Tuple leftTuple = new Tuple();
        List<String> leftTupleColumns = new ArrayList<>();
        leftTupleColumns.add("Student.A");
        leftTupleColumns.add("Student.B");
        leftTuple.setColumns(leftTupleColumns);

        HashMap<String, Integer> leftLookup = new HashMap<String, Integer>();
        leftLookup.put("Student.A", 1);
        leftLookup.put("Student.B", 5);
        leftTuple.setLookup(leftLookup);

        Tuple rightTuple = new Tuple();
        List<String> rightTupleColumns = new ArrayList<>();
        rightTupleColumns.add("Enrolled.A");
        rightTupleColumns.add("Enrolled.E");
        rightTuple.setColumns(rightTupleColumns);

        HashMap<String, Integer> rightLookup = new HashMap<String, Integer>();
        rightLookup.put("Enrolled.A", 1);
        rightLookup.put("Enrolled.E", 5);
        rightTuple.setLookup(rightLookup);

        Mockito.when(leftScanOperator.getNextTuple()).thenReturn(leftTuple, (Tuple) null);
        Mockito.when(rightScanOperator.getNextTuple()).thenReturn(rightTuple, (Tuple) null);

        joinOperator.setLeftChild(leftScanOperator);
        joinOperator.setRightChild(rightScanOperator);
        Tuple joinTuple = joinOperator.getNextTuple();

        assertEquals(4, joinTuple.getColumns().size());
        int studentA = joinTuple.getLookup().get("Student.A");
        assertEquals(1, studentA);
        int studentB = joinTuple.getLookup().get("Student.B");
        assertEquals(5, studentB);
        int enrolledA = joinTuple.getLookup().get("Enrolled.A");
        assertEquals(1, enrolledA);
        int enrolledB = joinTuple.getLookup().get("Enrolled.E");
        assertEquals(5, enrolledB);
    }

    @Test
    public void reset() throws JSQLParserException, IOException {
        String raw_expression = "Student.A = Enrolled.A";
        Expression expression = CCJSqlParserUtil.parseExpression(raw_expression);
        JoinOperator joinOperator = new JoinOperator(expression);
        joinOperator.setLeftChild(leftScanOperator);
        joinOperator.setRightChild(rightScanOperator);
        joinOperator.reset();
        Mockito.verify(leftScanOperator).reset();
        Mockito.verify(rightScanOperator).reset();
    }
}
