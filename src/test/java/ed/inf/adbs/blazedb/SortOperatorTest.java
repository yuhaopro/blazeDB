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

@RunWith(MockitoJUnitRunner.class)
public class SortOperatorTest {
    @Mock
    private ScanOperator scanOperator;
    
    @Test
    public void sortTuplesWithTwoColumns() {
        List<String> sortedColumns = new ArrayList<>();
        sortedColumns.add("Student.B");
        sortedColumns.add("Student.A");

        SortOperator sortOperator = new SortOperator(sortedColumns);
        sortOperator.setChild(scanOperator);

        Tuple firstTuple = new Tuple();
        List<String> firstTupleColumns = new ArrayList<String>();
        firstTupleColumns.add("Student.A");
        firstTupleColumns.add("Student.B");
        firstTuple.setColumns(firstTupleColumns);
        HashMap<String, Integer> firstTupleLookup = new HashMap<String, Integer>();
        firstTupleLookup.put("Student.A", 0);
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
        thirdTupleLookup.put("Student.A", 1);
        thirdTupleLookup.put("Student.B", 3);
        thirdTuple.setLookup(thirdTupleLookup);


        // returns (1,5) (0,1), (1,3)
        Mockito.when(scanOperator.getNextTuple()).thenReturn(secondTuple, firstTuple, thirdTuple, null);
        sortOperator.initialize();

        // expected new order (0,1), (1,3), (1,5)
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
    @Test
    public void sortTuplesWithTwoColumnsDistinct() {

        Tuple firstTuple = new Tuple();
        List<String> firstTupleColumns = new ArrayList<String>();
        firstTupleColumns.add("Student.A");
        firstTupleColumns.add("Student.B");
        firstTuple.setColumns(firstTupleColumns);
        HashMap<String, Integer> firstTupleLookup = new HashMap<String, Integer>();
        firstTupleLookup.put("Student.A", 1);
        firstTupleLookup.put("Student.B", 3);
        firstTuple.setLookup(firstTupleLookup);

        Tuple secondTuple = new Tuple();
        List<String> secondTupleColumns = new ArrayList<String>();
        secondTupleColumns.add("Student.A");
        secondTupleColumns.add("Student.B");
        secondTuple.setColumns(secondTupleColumns);
        HashMap<String, Integer> secondTupleLookup = new HashMap<String, Integer>();
        secondTupleLookup.put("Student.A", 0);
        secondTupleLookup.put("Student.B", 5);
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

        List<String> sortedColumns = new ArrayList<>();
        sortedColumns.add("Student.A");
        sortedColumns.add("Student.B");

        SortOperator sortOperator = new SortOperator(sortedColumns, true);
        sortOperator.setChild(scanOperator);
        // returns (1,3) (0,5), (1,3)
        Mockito.when(scanOperator.getNextTuple()).thenReturn(secondTuple, firstTuple, thirdTuple, null);
        
        sortOperator.initialize();

        // expected new order (0,1), (1,3), (1,5)
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
