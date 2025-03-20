package ed.inf.adbs.blazedb.operator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import ed.inf.adbs.blazedb.entity.Tuple;

public class SortOperator extends Operator {

    private Operator child;
    private final List<String> sortColumns;
    private Iterator<Tuple> sortedBuffer;
    private List<Tuple> buffer = new ArrayList<>();
    private boolean removeDuplicate = false;

    public SortOperator(List<String> sortColumns) {
        this.sortColumns = sortColumns;
    }

    public SortOperator(List<String> sortColumns, boolean removeDuplicate) {
        this.sortColumns = sortColumns;
        this.removeDuplicate = removeDuplicate;
        initialize();
    }

    // called before set child
    public void initialize() {
        Tuple tuple;
        while ((tuple = child.getNextTuple()) != null) {
            buffer.add(tuple);
        }

        sortBuffer();
        if (removeDuplicate) {
            removeDuplicateInBuffer();
        }
        this.sortedBuffer = buffer.iterator();
    }

    public void removeDuplicateInBuffer() {
        this.buffer = buffer.stream().distinct().collect(Collectors.toList());
    }

    public void setChild(Operator child) {
        this.child = child;
    }

    private void sortBuffer() {
        if (buffer == null || sortColumns == null || buffer.isEmpty() || sortColumns.isEmpty()) {
            return;
        }

        Comparator<Tuple> comparator = null;

        // building the comparator.
        for (String column : sortColumns) {
            Comparator<Tuple> columnComparator = Comparator.comparing(tuple -> tuple.getLookup().get(column),
                    // places the nulls last
                    Comparator.nullsLast(Comparator.naturalOrder()));

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
        this.sortedBuffer = buffer.iterator();
    }
}
