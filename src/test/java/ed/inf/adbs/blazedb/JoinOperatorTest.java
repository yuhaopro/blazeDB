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

/**
 * Unit tests for the {@link JoinOperator} class. These tests validate the
 * functionality of the join operation in a database query processing system,
 * including the handling of different join conditions and resetting the
 * operator's state.
 * 
 * <p>
 * The test cases focus on verifying that the join operator correctly combines
 * tuples from the left and right child operators based on one or more
 * conditions, and that the operator resets correctly.
 * </p>
 */
@RunWith(MockitoJUnitRunner.class)
public class JoinOperatorTest {

    @Mock
    private ScanOperator leftScanOperator;

    @Mock
    private ScanOperator rightScanOperator;

    /**
     * Test to ensure that the {@link JoinOperator} correctly joins two tuples based
     * on a single join expression.
     * 
     * <p>
     * This test verifies the following:
     * <ul>
     * <li>The join operator correctly joins two tuples based on a single
     * condition.</li>
     * <li>The resulting tuple contains columns from both the left and right tuples,
     * with correct values.</li>
     * </ul>
     * </p>
     * 
     * @throws JSQLParserException if there is an error parsing the join expression
     */
    @Test
    public void getNextTupleForOneJoinExpression() throws JSQLParserException {
        String raw_expression = "Student.A = Enrolled.A";
        Expression expression = CCJSqlParserUtil.parseExpression(raw_expression);

        JoinOperator joinOperator = new JoinOperator(expression);

        // Mock the child operators
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

        // Validate that the join operator returns the correct result
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

    /**
     * Test to ensure that the {@link JoinOperator} correctly joins two tuples based
     * on two join expressions.
     * 
     * <p>
     * This test verifies the following:
     * <ul>
     * <li>The join operator correctly handles multiple join conditions.</li>
     * <li>The resulting tuple contains columns from both the left and right tuples,
     * with correct values matching both join conditions.</li>
     * </ul>
     * </p>
     * 
     * @throws JSQLParserException if there is an error parsing the join expression
     */
    @Test
    public void getNextTupleForTwoJoinExpressions() throws JSQLParserException {
        String raw_expression = "Student.A = Enrolled.A AND Student.B = Enrolled.E";
        Expression expression = CCJSqlParserUtil.parseExpression(raw_expression);
        JoinOperator joinOperator = new JoinOperator(expression);

        // Mock the child operators
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

        // Validate that the join operator returns the correct result based on both join
        // conditions
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

    /**
     * Test to ensure that the {@link JoinOperator} correctly resets its internal
     * state.
     * 
     * <p>
     * This test verifies that:
     * <ul>
     * <li>The join operator correctly calls the reset method on its left and right
     * child operators.</li>
     * <li>The internal state of the join operator is reset, allowing it to be
     * reused for new queries.</li>
     * </ul>
     * </p>
     * 
     * @throws JSQLParserException if there is an error parsing the join expression
     * @throws IOException         if an error occurs during the reset operation
     */
    @Test
    public void reset() throws JSQLParserException, IOException {
        String raw_expression = "Student.A = Enrolled.A";
        Expression expression = CCJSqlParserUtil.parseExpression(raw_expression);
        JoinOperator joinOperator = new JoinOperator(expression);
        joinOperator.setLeftChild(leftScanOperator);
        joinOperator.setRightChild(rightScanOperator);
        joinOperator.reset();

        // Verify that the reset method is called on both child operators
        Mockito.verify(leftScanOperator).reset();
        Mockito.verify(rightScanOperator).reset();
    }
}
