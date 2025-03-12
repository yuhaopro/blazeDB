package ed.inf.adbs.blazedb;

import ed.inf.adbs.blazedb.entity.Tuple;
import ed.inf.adbs.blazedb.operator.ProjectOperator;
import ed.inf.adbs.blazedb.operator.ScanOperator;
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
public class ProjectOperatorTest {
    @Mock
    private ScanOperator scanOperator;

    @Test
    public void getNextTuple() throws IOException {
        List<String> columns = new ArrayList<String>();
        columns.add("A");
        ProjectOperator projectOperator = new ProjectOperator(columns);
        projectOperator.setChild(scanOperator);

        Tuple scanTuple = new Tuple();
        List<String> scanTupleColumns = new ArrayList<String>();
        scanTupleColumns.add("A");
        scanTupleColumns.add("B");
        scanTupleColumns.add("C");
        scanTupleColumns.add("D");
        scanTuple.setColumns(scanTupleColumns);
        HashMap<String, Integer> lookup = new HashMap<String, Integer>();
        lookup.put("A", 0);
        lookup.put("B", 1);
        lookup.put("C", 2);
        lookup.put("D", 3);
        scanTuple.setLookup(lookup);
        Mockito.when(scanOperator.getNextTuple()).thenReturn(scanTuple);
        Tuple tuple = projectOperator.getNextTuple();

        assertEquals(1, tuple.getColumns().size());
        assertEquals("A", tuple.getColumns().get(0));
        int a_lookup = tuple.getLookup().get("A");
        assertEquals(0, a_lookup);
        assertNull(tuple.getLookup().get("B"));
        assertNull(tuple.getLookup().get("C"));
        assertNull(tuple.getLookup().get("D"));
    }

    @Test
    public void reset() throws IOException {
        List<String> columns = new ArrayList<String>();
        columns.add("A");
        ProjectOperator projectOperator = new ProjectOperator(columns);
        projectOperator.setChild(scanOperator);
        projectOperator.reset();
        Mockito.verify(scanOperator).reset();
    }
}
