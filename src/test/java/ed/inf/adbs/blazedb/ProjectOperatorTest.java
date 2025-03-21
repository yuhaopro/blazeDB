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

/**
 * Unit tests for the {@link ProjectOperator} class. These tests validate the
 * functionality of the project operation in a database query processing system.
 * The project operator is responsible for selecting specific columns from a
 * tuple.
 * 
 * <p>
 * The test cases focus on verifying that the project operator correctly
 * retrieves the selected columns from the child operator's tuple and that it
 * correctly resets its state.
 * </p>
 */
@RunWith(MockitoJUnitRunner.class)
public class ProjectOperatorTest {

    @Mock
    private ScanOperator scanOperator;

    /**
     * Test to ensure that the {@link ProjectOperator} correctly retrieves the
     * projected columns from a tuple.
     * 
     * <p>
     * This test verifies that:
     * <ul>
     * <li>The project operator selects the correct columns from the input
     * tuple.</li>
     * <li>The resulting tuple contains only the columns specified in the
     * projection.</li>
     * <li>Other columns from the input tuple are not present in the resulting
     * tuple.</li>
     * </ul>
     * </p>
     * 
     * @throws IOException if there is an error while processing the tuple
     */
    @Test
    public void getNextTuple() throws IOException {
        List<String> columns = new ArrayList<String>();
        columns.add("A");
        ProjectOperator projectOperator = new ProjectOperator(columns);
        projectOperator.setChild(scanOperator);

        // Mock the tuple returned by the scan operator
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

        // Mock the behavior of the scan operator
        Mockito.when(scanOperator.getNextTuple()).thenReturn(scanTuple);

        // Get the projected tuple
        Tuple tuple = projectOperator.getNextTuple();

        // Verify that only the selected column ("A") is present
        assertEquals(1, tuple.getColumns().size());
        assertEquals("A", tuple.getColumns().get(0));
        int a_lookup = tuple.getLookup().get("A");
        assertEquals(0, a_lookup);
        assertNull(tuple.getLookup().get("B"));
        assertNull(tuple.getLookup().get("C"));
        assertNull(tuple.getLookup().get("D"));
    }

    /**
     * Test to ensure that the {@link ProjectOperator} correctly resets its state.
     * 
     * <p>
     * This test verifies that:
     * <ul>
     * <li>The reset method of the project operator calls the reset method of its
     * child operator.</li>
     * </ul>
     * </p>
     * 
     * @throws IOException if there is an error during the reset operation
     */
    @Test
    public void reset() throws IOException {
        List<String> columns = new ArrayList<String>();
        columns.add("A");
        ProjectOperator projectOperator = new ProjectOperator(columns);
        projectOperator.setChild(scanOperator);

        // Reset the project operator
        projectOperator.reset();

        // Verify that the reset method is called on the child operator
        Mockito.verify(scanOperator).reset();
    }
}
