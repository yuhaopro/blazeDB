package ed.inf.adbs.blazedb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import ed.inf.adbs.blazedb.entity.Tuple;
import ed.inf.adbs.blazedb.operator.DuplicateEliminationOperator;
import ed.inf.adbs.blazedb.operator.ScanOperator;

/**
 * Unit tests for the {@link DuplicateEliminationOperator} class. This class
 * contains tests to verify the functionality of duplicate elimination in the
 * database query processing.
 * 
 * <p>
 * The test cases verify the proper operation of retrieving the next tuple and
 * ensuring that duplicates are eliminated, as well as the proper resetting of
 * the operator's internal state.
 * </p>
 */
@RunWith(MockitoJUnitRunner.class)
public class DuplicateEliminationOperatorTest {

    @Mock
    ScanOperator scanOperator;

    /**
     * Test to ensure that the {@link DuplicateEliminationOperator} correctly
     * eliminates duplicate tuples and returns the expected next tuple in the
     * sequence.
     * 
     * <p>
     * This test verifies that:
     * <ul>
     * <li>The operator returns the expected tuples, eliminating duplicates.</li>
     * <li>Each tuple is checked to ensure it is processed correctly with respect to
     * its columns and lookup values.</li>
     * <li>The operator correctly terminates when no more tuples are available.</li>
     * </ul>
     * </p>
     * 
     * @throws Exception if any error occurs during the test
     */
    @Test
    public void getNextTuple() {
        // Define sorted columns for comparison
        List<String> sortedColumns = new ArrayList<>();
        sortedColumns.add("Student.B");
        sortedColumns.add("Student.A");

        // Create the first tuple
        Tuple firstTuple = new Tuple();
        List<String> firstTupleColumns = new ArrayList<String>();
        firstTupleColumns.add("Student.A");
        firstTupleColumns.add("Student.B");
        firstTuple.setColumns(firstTupleColumns);
        HashMap<String, Integer> firstTupleLookup = new HashMap<String, Integer>();
        firstTupleLookup.put("Student.A", 1);
        firstTupleLookup.put("Student.B", 2);
        firstTuple.setLookup(firstTupleLookup);

        // Create the second tuple (duplicate of the first)
        Tuple secondTuple = new Tuple();
        List<String> secondTupleColumns = new ArrayList<String>();
        secondTupleColumns.add("Student.A");
        secondTupleColumns.add("Student.B");
        secondTuple.setColumns(secondTupleColumns);
        HashMap<String, Integer> secondTupleLookup = new HashMap<String, Integer>();
        secondTupleLookup.put("Student.A", 1);
        secondTupleLookup.put("Student.B", 2);
        secondTuple.setLookup(secondTupleLookup);

        // Create the third tuple (different from the first two)
        Tuple thirdTuple = new Tuple();
        List<String> thirdTupleColumns = new ArrayList<String>();
        thirdTupleColumns.add("Student.A");
        thirdTupleColumns.add("Student.B");
        thirdTuple.setColumns(thirdTupleColumns);
        HashMap<String, Integer> thirdTupleLookup = new HashMap<String, Integer>();
        thirdTupleLookup.put("Student.A", 1);
        thirdTupleLookup.put("Student.B", 3);
        thirdTuple.setLookup(thirdTupleLookup);

        // Mock the behavior of the scan operator
        Mockito.when(scanOperator.getNextTuple()).thenReturn(firstTuple, secondTuple, thirdTuple, null);
        DuplicateEliminationOperator duplicateEliminationOperator = new DuplicateEliminationOperator();
        duplicateEliminationOperator.setChild(scanOperator);

        // Fetch the tuples and check if duplicates are eliminated
        Tuple expectedFirstTuple = duplicateEliminationOperator.getNextTuple();
        int expectedFirstTupleValueA = expectedFirstTuple.getLookup().get("Student.A");
        int expectedFirstTupleValueB = expectedFirstTuple.getLookup().get("Student.B");

        Tuple expectedSecondTuple = duplicateEliminationOperator.getNextTuple();
        int expectedSecondTupleValueA = expectedSecondTuple.getLookup().get("Student.A");
        int expectedSecondTupleValueB = expectedSecondTuple.getLookup().get("Student.B");

        Tuple expectedThirdTuple = duplicateEliminationOperator.getNextTuple();

        // Verify that the duplicate elimination works
        assertEquals(expectedFirstTupleValueA, 1);
        assertEquals(expectedFirstTupleValueB, 2);

        assertEquals(expectedSecondTupleValueA, 1);
        assertEquals(expectedSecondTupleValueB, 3);

        // Verify that no more tuples are left after the third
        assertEquals(expectedThirdTuple, null);
    }

    /**
     * Test to ensure that the {@link DuplicateEliminationOperator} correctly resets
     * its internal state.
     * 
     * <p>
     * This test verifies that:
     * <ul>
     * <li>The {@link DuplicateEliminationOperator} can reset its internal state
     * correctly.</li>
     * <li>The operator correctly invokes the reset method on its child
     * operator.</li>
     * <li>The operator's internal hash table is cleared upon reset.</li>
     * </ul>
     * </p>
     * 
     * @throws IOException if an error occurs during the reset operation
     */
    @Test
    public void reset() throws IOException {
        // Create a new DuplicateEliminationOperator instance
        DuplicateEliminationOperator duplicateEliminationOperator = new DuplicateEliminationOperator();
        duplicateEliminationOperator.setChild(scanOperator);

        // Add some initial state to the operator's hash table
        HashMap<String, HashMap<Integer, Boolean>> hashTable = duplicateEliminationOperator.getHashTable();
        hashTable.put("Student.A", new HashMap<>());

        // Perform the reset
        duplicateEliminationOperator.reset();

        // Verify that the child operator's reset method was invoked
        Mockito.verify(scanOperator).reset();

        // Verify that the hash table was cleared during reset
        assertNotEquals(duplicateEliminationOperator.getHashTable(), hashTable);
    }
}
