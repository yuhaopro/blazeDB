package ed.inf.adbs.blazedb;

import ed.inf.adbs.blazedb.entity.Tuple;
import ed.inf.adbs.blazedb.operator.ScanOperator;
import ed.inf.adbs.blazedb.operator.SelectOperator;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Unit tests for the {@link SelectOperator} class. These tests verify the
 * correct functionality of the Select Operator, which is used to filter tuples
 * based on the specified conditions (expressions). The {@link SelectOperator}
 * processes a tuple and checks whether it satisfies the given conditions
 * (expressions).
 * 
 * <p>
 * The test cases validate the functionality of filtering tuples using single
 * and multiple expressions, checking both passing and failing conditions, and
 * verifying the correct reset behavior.
 * </p>
 */
@RunWith(MockitoJUnitRunner.class)
public class SelectOperatorTest {

    @Mock
    ScanOperator scanOperator;

    /**
     * Test to verify that the {@link SelectOperator} correctly fails the condition
     * and returns null if the tuple does not meet the specified filter expression.
     * 
     * <p>
     * This test ensures that:
     * <ul>
     * <li>If a tuple does not meet the filter condition, the operator returns
     * null.</li>
     * <li>The condition is correctly evaluated using the given expression.</li>
     * </ul>
     * </p>
     * 
     * @throws FileNotFoundException if the specified file for the tuple is not
     *                               found
     * @throws JSQLParserException   if the expression parsing fails
     */
    @Test
    public void getNextTupleFailsExpression() throws FileNotFoundException, JSQLParserException {
        String str_expression = "Student.A > 2";
        Expression expression = CCJSqlParserUtil.parseExpression(str_expression);
        List<Expression> expressions = new ArrayList<Expression>();
        expressions.add(expression);
        Expression combinedExpression = QueryPlanBuilder.combineListOfExpressions(expressions);

        SelectOperator selectOperator = new SelectOperator("Student", combinedExpression);

        // Mocking the tuple for select operator child
        Tuple mockTuple = new Tuple();
        List<String> columns = new ArrayList<String>();
        columns.add("Student.A");
        columns.add("Student.B");
        columns.add("Student.C");
        columns.add("Student.D");

        HashMap<String, Integer> map = new HashMap<>();
        map.put("Student.A", 1);
        map.put("Student.B", 2);
        map.put("Student.C", 3);
        map.put("Student.D", 4);
        mockTuple.setColumns(columns);
        mockTuple.setLookup(map);

        Mockito.when(scanOperator.getNextTuple()).thenReturn(mockTuple, (Tuple) null);
        selectOperator.setChild(scanOperator);

        Tuple tuple = selectOperator.getNextTuple();
        assertNull(tuple);
    }

    /**
     * Test to verify that the {@link SelectOperator} correctly passes the condition
     * and returns the tuple if it satisfies the filter expression.
     * 
     * <p>
     * This test ensures that:
     * <ul>
     * <li>If the tuple satisfies the filter condition, the operator returns the
     * tuple.</li>
     * <li>The condition is evaluated correctly based on the provided
     * expression.</li>
     * </ul>
     * </p>
     * 
     * @throws FileNotFoundException if the specified file for the tuple is not
     *                               found
     * @throws JSQLParserException   if the expression parsing fails
     */
    @Test
    public void getNextTuplePassesExpression() throws FileNotFoundException, JSQLParserException {
        String raw_expression = "Student.A > 2";
        Expression expression = CCJSqlParserUtil.parseExpression(raw_expression);
        List<Expression> expressions = new ArrayList<Expression>();
        expressions.add(expression);
        Expression combinedExpression = QueryPlanBuilder.combineListOfExpressions(expressions);

        SelectOperator selectOperator = new SelectOperator("Student", combinedExpression);

        // Mocking the tuple for select operator child
        Tuple mockTuple = new Tuple();
        List<String> columns = new ArrayList<String>();
        columns.add("Student.A");
        columns.add("Student.B");
        columns.add("Student.C");
        columns.add("Student.D");

        HashMap<String, Integer> map = new HashMap<>();
        map.put("Student.A", 3);
        map.put("Student.B", 2);
        map.put("Student.C", 3);
        map.put("Student.D", 4);
        mockTuple.setColumns(columns);
        mockTuple.setLookup(map);

        Mockito.when(scanOperator.getNextTuple()).thenReturn(mockTuple);
        selectOperator.setChild(scanOperator);

        Tuple tuple = selectOperator.getNextTuple();
        int valueA = tuple.getLookup().get("Student.A");
        assertEquals(mockTuple, tuple);
    }

    /**
     * Test to verify that the {@link SelectOperator} correctly handles multiple
     * filter expressions. The tuple must satisfy all the specified conditions (AND
     * operation between multiple expressions).
     * 
     * <p>
     * This test ensures that:
     * <ul>
     * <li>The tuple satisfies all filter conditions (AND operation between multiple
     * conditions).</li>
     * <li>If any of the conditions fail, the tuple will not be returned.</li>
     * </ul>
     * </p>
     * 
     * @throws FileNotFoundException if the specified file for the tuple is not
     *                               found
     * @throws JSQLParserException   if the expression parsing fails
     */
    @Test
    public void multipleExpressions() throws FileNotFoundException, JSQLParserException {
        String firstExpressionStr = "Student.A > 2";
        String secondExpressionStr = "Student.B = 10";
        Expression firstExpression = CCJSqlParserUtil.parseExpression(firstExpressionStr);
        Expression secondExpression = CCJSqlParserUtil.parseExpression(secondExpressionStr);

        List<Expression> expressions = new ArrayList<Expression>();
        expressions.add(firstExpression);
        expressions.add(secondExpression);
        Expression combinedExpression = QueryPlanBuilder.combineListOfExpressions(expressions);
        SelectOperator selectOperator = new SelectOperator("Student", combinedExpression);

        // Mocking the tuple for select operator child
        Tuple mockTuple = new Tuple();
        List<String> columns = new ArrayList<String>();
        columns.add("Student.A");
        columns.add("Student.B");
        columns.add("Student.C");
        columns.add("Student.D");

        HashMap<String, Integer> map = new HashMap<>();
        map.put("Student.A", 3);
        map.put("Student.B", 1);
        map.put("Student.C", 3);
        map.put("Student.D", 4);
        mockTuple.setColumns(columns);
        mockTuple.setLookup(map);

        Tuple secondMockTuple = new Tuple();
        List<String> secondTupleColumns = new ArrayList<String>();
        secondTupleColumns.add("Student.A");
        secondTupleColumns.add("Student.B");
        secondTupleColumns.add("Student.C");
        secondTupleColumns.add("Student.D");

        HashMap<String, Integer> secondTupleLookup = new HashMap<>();
        secondTupleLookup.put("Student.A", 4);
        secondTupleLookup.put("Student.B", 10);
        secondTupleLookup.put("Student.C", 3);
        secondTupleLookup.put("Student.D", 4);
        secondMockTuple.setColumns(secondTupleColumns);
        secondMockTuple.setLookup(secondTupleLookup);

        Mockito.when(scanOperator.getNextTuple()).thenReturn(mockTuple, secondMockTuple, null);
        selectOperator.setChild(scanOperator);

        Tuple tuple = selectOperator.getNextTuple();
        int valueA = tuple.getLookup().get("Student.A");
        int valueB = tuple.getLookup().get("Student.B");
        assertEquals(4, valueA);
        assertEquals(10, valueB);

        Tuple expectedSecondTuple = selectOperator.getNextTuple();
        assertNull(expectedSecondTuple);
    }

    /**
     * Test to verify that the {@link SelectOperator} correctly resets its state.
     * This ensures that the child operator is also reset during the reset
     * operation.
     * 
     * @throws JSQLParserException if the expression parsing fails
     * @throws IOException         if an error occurs during the reset operation
     */
    @Test
    public void reset() throws JSQLParserException, IOException {
        String str_expression = "Student.A > 2";
        Expression expression = CCJSqlParserUtil.parseExpression(str_expression);
        List<Expression> expressions = new ArrayList<Expression>();
        expressions.add(expression);
        Expression combinedExpression = QueryPlanBuilder.combineListOfExpressions(expressions);
        SelectOperator selectOperator = new SelectOperator("Student", combinedExpression);
        selectOperator.setChild(scanOperator);

        selectOperator.reset();
        Mockito.verify(scanOperator).reset();
    }
}
