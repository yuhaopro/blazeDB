package ed.inf.adbs.blazedb;

import ed.inf.adbs.blazedb.entity.Tuple;
import ed.inf.adbs.blazedb.operator.ScanOperator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class ScanOperatorTest {

    @Mock
    private CSVParser parser;

    @Test
    public void getNextTuple() throws IOException {
        String databaseDirectory = "samples/db";
        DatabaseCatalog.getInstance().initialize(databaseDirectory);
        ScanOperator scanOperator = new ScanOperator("Student");
        scanOperator.setCsvParser(parser);
        Mockito.when(parser.hasNext()).thenReturn(true, false);
        List<String> parsedTuple = new ArrayList<String>();
        parsedTuple.add("10");
        parsedTuple.add("20");
        parsedTuple.add("30");
        parsedTuple.add("40");
        Mockito.when(parser.next()).thenReturn(parsedTuple);

        // mock parser hasNext function
        Tuple tuple = scanOperator.getNextTuple();
        int a_value = tuple.getLookup().get("Student.A");

        // check if tuple is created properly
        assertEquals(10, a_value);

        // no more tuple left
        Tuple tuple_2 = scanOperator.getNextTuple();
        assertNull(tuple_2);

    }

    @Test
    public void reset() throws IOException {
        // close off parser, and reinitialize it with a new parser.
        String databaseDirectory = "samples/db";
        DatabaseCatalog.getInstance().initialize(databaseDirectory);
        ScanOperator scanOperator = new ScanOperator("Student");
        scanOperator.setCsvParser(parser);
        scanOperator.reset();
        Mockito.verify(parser).close();
        assertNotEquals(scanOperator.getCsvParser(), parser);
    }
}
