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

/**
 * Unit tests for the {@link ScanOperator} class. These tests verify the correct
 * functionality of the scan operator, which is responsible for reading and
 * processing data tuples from a specified table (such as "Student"). The scan
 * operator retrieves and processes each tuple from a CSV source.
 * 
 * <p>
 * The test cases validate that the scan operator correctly retrieves tuples,
 * processes the parsed data, and handles resetting.
 * </p>
 */
@RunWith(MockitoJUnitRunner.class)
public class ScanOperatorTest {

    @Mock
    private CSVParser parser;

    /**
     * Test to ensure that the {@link ScanOperator} correctly retrieves and
     * processes the next tuple from the CSV parser.
     * 
     * <p>
     * This test verifies that:
     * <ul>
     * <li>The {@link ScanOperator} correctly calls the parser's next method to
     * retrieve data.</li>
     * <li>The resulting tuple is properly constructed with the parsed values from
     * the CSV.</li>
     * <li>If there are no more tuples, the {@link ScanOperator} correctly returns
     * null.</li>
     * </ul>
     * </p>
     * 
     * @throws IOException if an I/O error occurs while reading the CSV data
     */
    @Test
    public void getNextTuple() throws IOException {
        String databaseDirectory = "samples/db";
        DatabaseCatalog.getInstance().initialize(databaseDirectory);
        ScanOperator scanOperator = new ScanOperator("Student");
        scanOperator.setCsvParser(parser);

        // Mock the parser to simulate retrieving the next tuple
        Mockito.when(parser.hasNext()).thenReturn(true, false);

        List<String> parsedTuple = new ArrayList<String>();
        parsedTuple.add("10");
        parsedTuple.add("20");
        parsedTuple.add("30");
        parsedTuple.add("40");
        Mockito.when(parser.next()).thenReturn(parsedTuple);

        // Get the next tuple from the scan operator
        Tuple tuple = scanOperator.getNextTuple();
        int valueA = tuple.getLookup().get("Student.A");

        // Verify that the tuple is correctly created and values are set as expected
        assertEquals(10, valueA);

        // Verify that no more tuples are left
        Tuple secondTuple = scanOperator.getNextTuple();
        assertNull(secondTuple);
    }

    /**
     * Test to ensure that the {@link ScanOperator} correctly resets its state,
     * including closing and reinitializing the CSV parser.
     * 
     * <p>
     * This test verifies that:
     * <ul>
     * <li>The {@link ScanOperator} correctly calls the close method on the parser
     * when reset is invoked.</li>
     * <li>The parser is reinitialized after reset, ensuring it is in a clean state
     * for subsequent operations.</li>
     * </ul>
     * </p>
     * 
     * @throws IOException if an error occurs during the reset operation
     */
    @Test
    public void reset() throws IOException {
        String databaseDirectory = "samples/db";
        DatabaseCatalog.getInstance().initialize(databaseDirectory);
        ScanOperator scanOperator = new ScanOperator("Student");
        scanOperator.setCsvParser(parser);

        // Reset the scan operator, which should close the parser and reinitialize it
        scanOperator.reset();

        // Verify that the parser's close method was called
        Mockito.verify(parser).close();

        // Ensure that the parser is reinitialized and no longer references the old
        // parser
        assertNotEquals(scanOperator.getCsvParser(), parser);
    }
}
