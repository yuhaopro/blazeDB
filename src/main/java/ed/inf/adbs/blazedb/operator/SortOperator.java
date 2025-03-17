package ed.inf.adbs.blazedb.operator;

import ed.inf.adbs.blazedb.entity.Tuple;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class SortOperator extends Operator{

    private Operator child;
    private final List<String> sortColumns;
    private Iterator<Tuple> sortedBuffer;
    private List<Tuple> buffer = new ArrayList<>();
    public SortOperator(List<String> sortColumns) {
        this.sortColumns = sortColumns;
    }

    // called before set child
    public void initialize() {
        Tuple tuple;
        while ((tuple = child.getNextTuple()) != null) {
            buffer.add(tuple);
        }

        sortBuffer();
        this.sortedBuffer = buffer.iterator();
    }
    
    public void setChild(Operator child) {
        this.child = child;
    }

    private void sortBuffer() {
        if (buffer == null || sortColumns == null || buffer.isEmpty() || sortColumns.isEmpty()) {
            return;
        }

        Comparator<Tuple> comparator = null;

        for (String column : sortColumns) {
            Comparator<Tuple> columnComparator = Comparator.comparing(
                tuple -> tuple.getLookup().get(column),
                Comparator.nullsLast(Comparator.naturalOrder())
            );

            if (comparator == null) {
                comparator = columnComparator;
            } else {
                comparator = comparator.thenComparing(columnComparator);
            }
        }

        if (comparator != null) {
            Collections.sort(buffer, comparator);
        }
    }

    /**
     * @return 
     */
    @Override
    public Tuple getNextTuple() {
        if (sortedBuffer.hasNext()) {
            return sortedBuffer.next();
        }
        return null;
    }

    /**
     * @throws IOException 
     */
    @Override
    public void reset() throws IOException {
        child.reset();
        initialize();
    }
}
