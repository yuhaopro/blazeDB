package ed.inf.adbs.blazedb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import ed.inf.adbs.blazedb.entity.Tuple;
import ed.inf.adbs.blazedb.operator.ScanOperator;
import ed.inf.adbs.blazedb.operator.SumOperator;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.GroupByElement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;

@RunWith(MockitoJUnitRunner.class)
public class SumOperatorTest {
    @Mock
    ScanOperator scanOperator;

    @Test
    public void withGroupByQuery() throws JSQLParserException {
        Tuple firstTuple = new Tuple();
        List<String> firstTupleColumns = new ArrayList<String>();
        firstTupleColumns.add("Student.A");
        firstTupleColumns.add("Student.B");
        firstTuple.setColumns(firstTupleColumns);
        HashMap<String, Integer> firstTupleLookup = new HashMap<String, Integer>();
        firstTupleLookup.put("Student.A", 1);
        firstTupleLookup.put("Student.B", 1);
        firstTuple.setLookup(firstTupleLookup);

        Tuple secondTuple = new Tuple();
        List<String> secondTupleColumns = new ArrayList<String>();
        secondTupleColumns.add("Student.A");
        secondTupleColumns.add("Student.B");
        secondTuple.setColumns(secondTupleColumns);
        HashMap<String, Integer> secondTupleLookup = new HashMap<String, Integer>();
        secondTupleLookup.put("Student.A", 1);
        secondTupleLookup.put("Student.B", 5);
        secondTuple.setLookup(secondTupleLookup);

        Tuple thirdTuple = new Tuple();
        List<String> thirdTupleColumns = new ArrayList<String>();
        thirdTupleColumns.add("Student.A");
        thirdTupleColumns.add("Student.B");
        thirdTuple.setColumns(thirdTupleColumns);
        HashMap<String, Integer> thirdTupleLookup = new HashMap<String, Integer>();
        thirdTupleLookup.put("Student.A", 2);
        thirdTupleLookup.put("Student.B", 3);
        thirdTuple.setLookup(thirdTupleLookup);

        String query = "SELECT Student.A, SUM(Student.B * Student.B) FROM Student GROUP BY Student.A";
        Statement statement = CCJSqlParserUtil.parse(query);
        Select select = (Select) statement;

        // extract the SUM Expression
        GroupByElement groupByElement = select.getPlainSelect().getGroupBy();

        List<SelectItem<?>> selectItems = select.getPlainSelect().getSelectItems();
        List<Expression> selectExpressions = selectItems.stream().map(SelectItem::getExpression)
                .collect(Collectors.toList());
        ExpressionList<Expression> selectExpressionList = new ExpressionList<>(selectExpressions);

        Mockito.when(scanOperator.getNextTuple()).thenReturn(firstTuple, secondTuple, thirdTuple, null);

        SumOperator sumOperator = new SumOperator(groupByElement, selectExpressionList);
        sumOperator.setChild(scanOperator);

        // builds the hash table to group records based on group by clause, if no
        // group by, it will check for sum functions only
        sumOperator.initialize();


        // Student.A, SUM(Student.B * Student.B)
        Tuple tuple = sumOperator.getNextTuple();
        int tupleValueA = tuple.getLookup().get("Student.A");
        int tupleValueB = tuple.getLookup().get("SUM(Student.B * Student.B)");
        assertEquals(2, tuple.getColumns().size());
        assertEquals(1, tupleValueA);
        assertEquals(26, tupleValueB);

        Tuple expectedSecondTuple = sumOperator.getNextTuple();
        int secondTupleValueA = expectedSecondTuple.getLookup().get("Student.A");
        int secondTupleValueB = expectedSecondTuple.getLookup().get("SUM(Student.B * Student.B)");
        assertEquals(2, tuple.getColumns().size());
        assertEquals(2, secondTupleValueA);
        assertEquals(9, secondTupleValueB);

        Tuple expectedThirdTuple = sumOperator.getNextTuple();
        assertNull(expectedThirdTuple);

    }

    @Test
    public void noGroupByQuery() throws JSQLParserException {
        Tuple firstTuple = new Tuple();
        List<String> firstTupleColumns = new ArrayList<String>();
        firstTupleColumns.add("Student.A");
        firstTupleColumns.add("Student.B");
        firstTuple.setColumns(firstTupleColumns);
        HashMap<String, Integer> firstTupleLookup = new HashMap<String, Integer>();
        firstTupleLookup.put("Student.A", 1);
        firstTupleLookup.put("Student.B", 1);
        firstTuple.setLookup(firstTupleLookup);

        Tuple secondTuple = new Tuple();
        List<String> secondTupleColumns = new ArrayList<String>();
        secondTupleColumns.add("Student.A");
        secondTupleColumns.add("Student.B");
        secondTuple.setColumns(secondTupleColumns);
        HashMap<String, Integer> secondTupleLookup = new HashMap<String, Integer>();
        secondTupleLookup.put("Student.A", 1);
        secondTupleLookup.put("Student.B", 5);
        secondTuple.setLookup(secondTupleLookup);

        Tuple thirdTuple = new Tuple();
        List<String> thirdTupleColumns = new ArrayList<String>();
        thirdTupleColumns.add("Student.A");
        thirdTupleColumns.add("Student.B");
        thirdTuple.setColumns(thirdTupleColumns);
        HashMap<String, Integer> thirdTupleLookup = new HashMap<String, Integer>();
        thirdTupleLookup.put("Student.A", 2);
        thirdTupleLookup.put("Student.B", 3);
        thirdTuple.setLookup(thirdTupleLookup);

        String query = "SELECT SUM(Student.B), SUM(Student.A) FROM Student";
        Statement statement = CCJSqlParserUtil.parse(query);
        Select select = (Select) statement;

        // extract the SUM Expression
        GroupByElement groupByElement = select.getPlainSelect().getGroupBy(); 

        List<SelectItem<?>> selectItems = select.getPlainSelect().getSelectItems();
        List<Expression> selectExpressions = selectItems.stream().map(SelectItem::getExpression)
                .collect(Collectors.toList());
        ExpressionList<Expression> selectExpressionList = new ExpressionList<>(selectExpressions);

        Mockito.when(scanOperator.getNextTuple()).thenReturn(firstTuple, secondTuple, thirdTuple, null);

        SumOperator sumOperator = new SumOperator(groupByElement, selectExpressionList);
        sumOperator.setChild(scanOperator);
        sumOperator.initialize();
        Tuple tuple = sumOperator.getNextTuple();
        int tupleValueA = tuple.getLookup().get("SUM(Student.A)");
        int tupleValueB = tuple.getLookup().get("SUM(Student.B)");
        assertEquals(2, tuple.getColumns().size());
        assertEquals(4, tupleValueA);
        assertEquals(9, tupleValueB);

        Tuple expectedSecondTuple = sumOperator.getNextTuple();
        assertNull(expectedSecondTuple);
    }


    @Test
    public void groupByNoSumQueryAndReset() throws JSQLParserException {
        Tuple firstTuple = new Tuple();
        List<String> firstTupleColumns = new ArrayList<String>();
        firstTupleColumns.add("Student.A");
        firstTupleColumns.add("Student.B");
        firstTuple.setColumns(firstTupleColumns);
        HashMap<String, Integer> firstTupleLookup = new HashMap<String, Integer>();
        firstTupleLookup.put("Student.A", 1);
        firstTupleLookup.put("Student.B", 1);
        firstTuple.setLookup(firstTupleLookup);

        Tuple secondTuple = new Tuple();
        List<String> secondTupleColumns = new ArrayList<String>();
        secondTupleColumns.add("Student.A");
        secondTupleColumns.add("Student.B");
        secondTuple.setColumns(secondTupleColumns);
        HashMap<String, Integer> secondTupleLookup = new HashMap<String, Integer>();
        secondTupleLookup.put("Student.A", 1);
        secondTupleLookup.put("Student.B", 5);
        secondTuple.setLookup(secondTupleLookup);

        Tuple thirdTuple = new Tuple();
        List<String> thirdTupleColumns = new ArrayList<String>();
        thirdTupleColumns.add("Student.A");
        thirdTupleColumns.add("Student.B");
        thirdTuple.setColumns(thirdTupleColumns);
        HashMap<String, Integer> thirdTupleLookup = new HashMap<String, Integer>();
        thirdTupleLookup.put("Student.A", 2);
        thirdTupleLookup.put("Student.B", 3);
        thirdTuple.setLookup(thirdTupleLookup);

        String query = "SELECT Student.A, Student.B FROM Student GROUP BY Student.A, Student.B";
        Statement statement = CCJSqlParserUtil.parse(query);
        Select select = (Select) statement;

        // extract the SUM Expression
        GroupByElement groupByElement = select.getPlainSelect().getGroupBy(); 

        List<SelectItem<?>> selectItems = select.getPlainSelect().getSelectItems();
        List<Expression> selectExpressions = selectItems.stream().map(SelectItem::getExpression)
                .collect(Collectors.toList());
        ExpressionList<Expression> selectExpressionList = new ExpressionList<>(selectExpressions);

        Mockito.when(scanOperator.getNextTuple()).thenReturn(firstTuple, secondTuple, thirdTuple, null);

        SumOperator sumOperator = new SumOperator(groupByElement, selectExpressionList);
        sumOperator.setChild(scanOperator);

        sumOperator.initialize();


        Tuple tuple = sumOperator.getNextTuple();
        int tupleValueA = tuple.getLookup().get("Student.A");
        int tupleValueB = tuple.getLookup().get("Student.B");
        assertEquals(2, tuple.getColumns().size());
        assertEquals(1, tupleValueA);
        assertEquals(1, tupleValueB);

        Tuple expectedSecondTuple = sumOperator.getNextTuple();
        int secondTupleValueA = expectedSecondTuple.getLookup().get("Student.A");
        int secondTupleValueB = expectedSecondTuple.getLookup().get("Student.B");
        assertEquals(2, tuple.getColumns().size());
        assertEquals(1, secondTupleValueA);
        assertEquals(5, secondTupleValueB);


        Tuple expectedThirdTuple = sumOperator.getNextTuple();
        int thirdTupleValueA = expectedThirdTuple.getLookup().get("Student.A");
        int thirdTupleValueB = expectedThirdTuple.getLookup().get("Student.B");
        assertEquals(2, tuple.getColumns().size());
        assertEquals(2, thirdTupleValueA);
        assertEquals(3, thirdTupleValueB);

        sumOperator.reset();
        Tuple expectedFourthTuple = sumOperator.getNextTuple();
        int fourthTupleValueA = expectedFourthTuple.getLookup().get("Student.A");
        int fourthTupleValueB = expectedFourthTuple.getLookup().get("Student.B");
        assertEquals(2, tuple.getColumns().size());
        assertEquals(1, fourthTupleValueA);
        assertEquals(1, fourthTupleValueB); 
    }


}
