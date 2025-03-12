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

@RunWith(MockitoJUnitRunner.class)
public class SelectOperatorTest {
    @Mock
    ScanOperator scanOperator;

    @Test
    public void getNextTupleFailsExpression() throws FileNotFoundException, JSQLParserException {
        String str_expression = "Student.A > 2";
        Expression expression = CCJSqlParserUtil.parseExpression(str_expression);
        SelectOperator selectOperator = new SelectOperator("Student", expression);


        // mocking the tuple in get next tuple for select operator child
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

    @Test
    public void getNextTuplePassesExpression() throws FileNotFoundException, JSQLParserException {
        String raw_expression = "Student.A > 2";
        Expression expression = CCJSqlParserUtil.parseExpression(raw_expression);
        SelectOperator selectOperator = new SelectOperator("Student", expression);


        // mocking the tuple in get next tuple for select operator child
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
        int value_A = tuple.getLookup().get("Student.A");
        assertEquals(mockTuple, tuple);
    }

    @Test
    public void reset() throws JSQLParserException, IOException {
        String str_expression = "Student.A > 2";
        Expression expression = CCJSqlParserUtil.parseExpression(str_expression);
        SelectOperator selectOperator = new SelectOperator("Student", expression);
        selectOperator.setChild(scanOperator);

        selectOperator.reset();
        Mockito.verify(scanOperator).reset();
    }
}
