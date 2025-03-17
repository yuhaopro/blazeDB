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
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;

@RunWith(MockitoJUnitRunner.class)
public class SumOperatorTest {
	@Mock
	ScanOperator scanOperator;

	@Test
	public  void getNextTuple() throws JSQLParserException {
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
        SelectItem<?> selectItem = select.getPlainSelect().getSelectItems().get(1);
        Expression selectExpression = selectItem.getExpression();

        List<Expression> sumExpressions = new ArrayList<>();
        if (selectExpression instanceof Function) {
            Function sumFunction =  (Function) selectExpression;

            // this gets "Student.B * Student.B"
            Expression innerExpression = (Expression) sumFunction.getParameters().getFirst();
            sumExpressions.add(innerExpression);
        }   
        

        ExpressionList<?> groupByExpressions = select.getPlainSelect().getGroupBy().getGroupByExpressionList();
        List<String> groupByExpressionStrings = groupByExpressions.stream().map((expr) -> expr.toString()).collect(Collectors.toList());


        SumOperator sumOperator = new SumOperator();
        sumOperator.setChild(scanOperator);
        sumOperator.setGroupBys(groupByExpressionStrings);
        sumOperator.setSumExpressions(sumExpressions);

        // builds the hash table to group records based on group by clause, if no group by, it will check for sumExpressions
        sumOperator.initialize();

        Mockito.when(scanOperator.getNextTuple()).thenReturn(firstTuple, secondTuple, thirdTuple, null);

        // Student.A, SUM(Student.B * Student.B)
        Tuple tuple = sumOperator.getNextTuple();
        int tupleValueA = tuple.getLookup().get("Student.A");
        int tupleValueB = tuple.getLookup().get("Student.B");
        assertEquals(2, tuple.getColumns().size());
        assertEquals(1, tupleValueA);
        assertEquals(6, tupleValueB);

        Tuple expectedSecondTuple = sumOperator.getNextTuple();
        int secondTupleValueA = expectedSecondTuple.getLookup().get("Student.A");
        int secondTupleValueB = expectedSecondTuple.getLookup().get("Student.B");
        assertEquals(2, tuple.getColumns().size());
        assertEquals(2, secondTupleValueA);
        assertEquals(3, secondTupleValueB);

        Tuple expectedThirdTuple = sumOperator.getNextTuple();
        assertNull(expectedThirdTuple);

	}

}
