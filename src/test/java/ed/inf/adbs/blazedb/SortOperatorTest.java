package ed.inf.adbs.blazedb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import ed.inf.adbs.blazedb.entity.Tuple;
import ed.inf.adbs.blazedb.operator.ScanOperator;
import ed.inf.adbs.blazedb.operator.SortOperator;

/**
 * Unit tests for the {@link SortOperator} class. These tests verify the correct
 * functionality of the Sort Operator, which is responsible for sorting tuples
 * based on specified column names.
 * 
 * <p>
 * The tests focus on ensuring the correct sorting behavior for tuples based on
 * different criteria, including sorting on multiple columns and handling
 * distinct values.
 * </p>
 */
@RunWith(MockitoJUnitRunner.class)
public class SortOperatorTest {

    @Mock
    private ScanOperator scanOperator;

    /**
     * Test to verify that the {@link SortOperator} sorts tuples based on two
     * specified columns. The test ensures that tuples are correctly ordered
     * according to the specified columns in ascending order.
     * 
     * <p>
     * This test checks the sorting behavior with two columns ("Student.B",
     * "Student.A"). The expected result is the tuples sorted in the order (0,1),
     * (1,3), and (1,5).
     * </p>
     */
    @Test
    public void sortTuplesWithTwoColumns() {
        List<String> sortedColumns = new ArrayList<>();
        sortedColumns.add("Student.B");
        sortedColumns.add("Student.A");

        SortOperator sortOperator = new SortOperator(sortedColumns);
        sortOperator.setChild(scanOperator);

        // Creating the first tuple
        Tuple firstTuple = new Tuple();
        List<String> firstTupleColumns = new ArrayList<>();
        firstTupleColumns.add("Student.A");
        firstTupleColumns.add("Student.B");
        firstTuple.setColumns(firstTupleColumns);
        HashMap<String, Integer> firstTupleLookup = new HashMap<>();
        firstTupleLookup.put("Student.A", 0);
        firstTupleLookup.put("Student.B", 1);
        firstTuple.setLookup(firstTupleLookup);

        // Creating the second tuple
        Tuple secondTuple = new Tuple();
        List<String> secondTupleColumns = new ArrayList<>();
        secondTupleColumns.add("Student.A");
        secondTupleColumns.add("Student.B");
        secondTuple.setColumns(secondTupleColumns);
        HashMap<String, Integer> secondTupleLookup = new HashMap<>();
        secondTupleLookup.put("Student.A", 1);
        secondTupleLookup.put("Student.B", 5);
        secondTuple.setLookup(secondTupleLookup);

        // Creating the third tuple
        Tuple thirdTuple = new Tuple();
        List<String> thirdTupleColumns = new ArrayList<>();
        thirdTupleColumns.add("Student.A");
        thirdTupleColumns.add("Student.B");
        thirdTuple.setColumns(thirdTupleColumns);
        HashMap<String, Integer> thirdTupleLookup = new HashMap<>();
        thirdTupleLookup.put("Student.A", 1);
        thirdTupleLookup.put("Student.B", 3);
        thirdTuple.setLookup(thirdTupleLookup);

        // Mocking the scan operator to return the tuples in order
        Mockito.when(scanOperator.getNextTuple()).thenReturn(secondTuple, firstTuple, thirdTuple, null);
        sortOperator.initialize();

        // Testing the sorted order of the tuples
        Tuple expectedFirstTuple = sortOperator.getNextTuple();
        int firstTupleValueA = expectedFirstTuple.getLookup().get("Student.A");
        int firstTupleValueB = expectedFirstTuple.getLookup().get("Student.B");

        assertEquals(0, firstTupleValueA);
        assertEquals(1, firstTupleValueB);

        Tuple expectedSecondTuple = sortOperator.getNextTuple();
        int secondTupleValueA = expectedSecondTuple.getLookup().get("Student.A");
        int secondTupleValueB = expectedSecondTuple.getLookup().get("Student.B");

        assertEquals(1, secondTupleValueA);
        assertEquals(3, secondTupleValueB);

        Tuple expectedThirdTuple = sortOperator.getNextTuple();
        int thirdTupleValueA = expectedThirdTuple.getLookup().get("Student.A");
        int thirdTupleValueB = expectedThirdTuple.getLookup().get("Student.B");

        assertEquals(1, thirdTupleValueA);
        assertEquals(5, thirdTupleValueB);
    }

    /**
     * Test to verify that the {@link SortOperator} handles sorting of distinct
     * tuples correctly. This test ensures that the sorting operation returns tuples
     * in the correct order and removes any duplicate tuples based on the sorting
     * criteria.
     * 
     * <p>
     * This test uses the "Student.A" and "Student.B" columns for sorting and
     * verifies that the expected sorted order is maintained (i.e., distinct values
     * are handled correctly).
     * </p>
     */
    @Test
    public void sortTuplesWithTwoColumnsDistinct() {
        // Creating the first tuple
        Tuple firstTuple = new Tuple();
        List<String> firstTupleColumns = new ArrayList<>();
        firstTupleColumns.add("Student.A");
        firstTupleColumns.add("Student.B");
        firstTuple.setColumns(firstTupleColumns);
        HashMap<String, Integer> firstTupleLookup = new HashMap<>();
        firstTupleLookup.put("Student.A", 1);
        firstTupleLookup.put("Student.B", 3);
        firstTuple.setLookup(firstTupleLookup);

        // Creating the second tuple
        Tuple secondTuple = new Tuple();
        List<String> secondTupleColumns = new ArrayList<>();
        secondTupleColumns.add("Student.A");
        secondTupleColumns.add("Student.B");
        secondTuple.setColumns(secondTupleColumns);
        HashMap<String, Integer> secondTupleLookup = new HashMap<>();
        secondTupleLookup.put("Student.A", 0);
        secondTupleLookup.put("Student.B", 5);
        secondTuple.setLookup(secondTupleLookup);

        // Creating the third tuple
        Tuple thirdTuple = new Tuple();
        List<String> thirdTupleColumns = new ArrayList<>();
        thirdTupleColumns.add("Student.A");
        thirdTupleColumns.add("Student.B");
        thirdTuple.setColumns(thirdTupleColumns);
        HashMap<String, Integer> thirdTupleLookup = new HashMap<>();
        thirdTupleLookup.put("Student.A", 1);
        thirdTupleLookup.put("Student.B", 3);
        thirdTuple.setLookup(thirdTupleLookup);

        List<String> sortedColumns = new ArrayList<>();
        sortedColumns.add("Student.A");
        sortedColumns.add("Student.B");

        SortOperator sortOperator = new SortOperator(sortedColumns, true);
        sortOperator.setChild(scanOperator);

        // Mocking the scan operator to return the tuples in order
        Mockito.when(scanOperator.getNextTuple()).thenReturn(secondTuple, firstTuple, thirdTuple, null);

        sortOperator.initialize();

        // Testing the sorted order of the tuples
        Tuple expectedFirstTuple = sortOperator.getNextTuple();
        int firstTupleValueA = expectedFirstTuple.getLookup().get("Student.A");
        int firstTupleValueB = expectedFirstTuple.getLookup().get("Student.B");

        assertEquals(0, firstTupleValueA);
        assertEquals(5, firstTupleValueB);

        Tuple expectedSecondTuple = sortOperator.getNextTuple();
        int secondTupleValueA = expectedSecondTuple.getLookup().get("Student.A");
        int secondTupleValueB = expectedSecondTuple.getLookup().get("Student.B");

        assertEquals(1, secondTupleValueA);
        assertEquals(3, secondTupleValueB);

        Tuple expectedThirdTuple = sortOperator.getNextTuple();
        assertNull(expectedThirdTuple);
    }
}
