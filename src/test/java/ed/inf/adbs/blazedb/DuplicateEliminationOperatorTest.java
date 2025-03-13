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

@RunWith(MockitoJUnitRunner.class)
public class DuplicateEliminationOperatorTest {
	@Mock
	ScanOperator scanOperator;

	@Test
	public void getNextTuple() {
		List<String> sortedColumns = new ArrayList<>();
        sortedColumns.add("Student.B");
        sortedColumns.add("Student.A");

        Tuple firstTuple = new Tuple();
        List<String> firstTupleColumns = new ArrayList<String>();
        firstTupleColumns.add("Student.A");
        firstTupleColumns.add("Student.B");
        firstTuple.setColumns(firstTupleColumns);
        HashMap<String, Integer> firstTupleLookup = new HashMap<String, Integer>();
        firstTupleLookup.put("Student.A", 1);
        firstTupleLookup.put("Student.B", 2);
        firstTuple.setLookup(firstTupleLookup);

        Tuple secondTuple = new Tuple();
        List<String> secondTupleColumns = new ArrayList<String>();
        secondTupleColumns.add("Student.A");
        secondTupleColumns.add("Student.B");
        secondTuple.setColumns(secondTupleColumns);
        HashMap<String, Integer> secondTupleLookup = new HashMap<String, Integer>();
        secondTupleLookup.put("Student.A", 1);
        secondTupleLookup.put("Student.B", 2);
        secondTuple.setLookup(secondTupleLookup);

        Tuple thirdTuple = new Tuple();
        List<String> thirdTupleColumns = new ArrayList<String>();
        thirdTupleColumns.add("Student.A");
        thirdTupleColumns.add("Student.B");
        thirdTuple.setColumns(thirdTupleColumns);
        HashMap<String, Integer> thirdTupleLookup = new HashMap<String, Integer>();
        thirdTupleLookup.put("Student.A", 1);
        thirdTupleLookup.put("Student.B", 3);
        thirdTuple.setLookup(thirdTupleLookup);

		Mockito.when(scanOperator.getNextTuple()).thenReturn(firstTuple, secondTuple, thirdTuple, null);
		DuplicateEliminationOperator duplicateEliminationOperator = new DuplicateEliminationOperator();
		duplicateEliminationOperator.setChild(scanOperator);
		
		Tuple expectedFirstTuple = duplicateEliminationOperator.getNextTuple();
		int expectedFirstTupleValueA = expectedFirstTuple.getLookup().get("Student.A");
		int expectedFirstTupleValueB = expectedFirstTuple.getLookup().get("Student.B");

		Tuple expectedSecondTuple = duplicateEliminationOperator.getNextTuple();
		int expectedSecondTupleValueA = expectedSecondTuple.getLookup().get("Student.A");
		int expectedSecondTupleValueB = expectedSecondTuple.getLookup().get("Student.B");

		Tuple expectedThirdTuple = duplicateEliminationOperator.getNextTuple();

		assertEquals(expectedFirstTupleValueA, 1);
		assertEquals(expectedFirstTupleValueB, 2);

		assertEquals(expectedSecondTupleValueA, 1);
		assertEquals(expectedSecondTupleValueB, 3);

		assertEquals(expectedThirdTuple, null);
	}

    @Test
    public void reset() throws IOException {
        DuplicateEliminationOperator duplicateEliminationOperator = new DuplicateEliminationOperator();
		duplicateEliminationOperator.setChild(scanOperator);
        HashMap<String, HashMap<Integer, Boolean>> hashTable = duplicateEliminationOperator.getHashTable();
        hashTable.put("Student.A", new HashMap<>());
        duplicateEliminationOperator.reset();
        Mockito.verify(scanOperator).reset();
        assertNotEquals(duplicateEliminationOperator.getHashTable(), hashTable);

    }
}
