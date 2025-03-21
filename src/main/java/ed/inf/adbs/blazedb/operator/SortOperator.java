package ed.inf.adbs.blazedb.operator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import ed.inf.adbs.blazedb.entity.Tuple;

/**
 * The {@code SortOperator} class represents a relational algebra sort
 * operation. It sorts tuples from its child operator based on a specified list
 * of columns. Optionally, it can also remove duplicate tuples after sorting.
 */
public class SortOperator extends Operator {

    /**
     * The child operator from which tuples are retrieved.
     */
    private Operator child;

    /**
     * The list of column names used for sorting.
     */
    private final List<String> sortColumns;

    /**
     * An iterator over the sorted tuples.
     */
    private Iterator<Tuple> sortedBuffer;

    /**
     * A buffer to store all tuples from the child operator before sorting.
     */
    private List<Tuple> buffer = new ArrayList<>();

    /**
     * A flag indicating whether to remove duplicate tuples after sorting.
     */
    private boolean removeDuplicate = false;

    /**
     * Constructs a new {@code SortOperator} with the specified list of sort
     * columns.
     *
     * @param sortColumns The list of column names to sort by.
     */
    public SortOperator(List<String> sortColumns) {
        this.sortColumns = sortColumns;
    }

    /**
     * Constructs a new {@code SortOperator} with the specified list of sort columns
     * and a flag indicating whether to remove duplicates.
     *
     * @param sortColumns     The list of column names to sort by.
     * @param removeDuplicate A flag indicating whether to remove duplicate tuples.
     */
    public SortOperator(List<String> sortColumns, boolean removeDuplicate) {
        this.sortColumns = sortColumns;
        this.removeDuplicate = removeDuplicate;
    }

    /**
     * Initializes the sort operation by retrieving all tuples from the child
     * operator, sorting them, and optionally removing duplicates. This method
     * should be called after setting the child operator.
     */
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

    /**
     * Removes duplicate tuples from the buffer.
     */
    public void removeDuplicateInBuffer() {
        this.buffer = buffer.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Sets the child operator for this {@code SortOperator}.
     *
     * @param child The child operator.
     */
    public void setChild(Operator child) {
        this.child = child;
    }

    /**
     * Sorts the tuples in the buffer based on the specified sort columns.
     */
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
     * Retrieves the next sorted tuple.
     *
     * @return The next sorted tuple, or {@code null} if there are no more tuples.
     */
    @Override
    public Tuple getNextTuple() {
        if (sortedBuffer.hasNext()) {
            return sortedBuffer.next();
        }
        return null;
    }

    /**
     * Resets the sort operation by resetting the iterator to the beginning of the
     * sorted buffer.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void reset() throws IOException {
        this.sortedBuffer = buffer.iterator();
    }
}