package ed.inf.adbs.blazedb.operator;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import ed.inf.adbs.blazedb.entity.Tuple;

/**
 * The {@code ProjectOperator} class represents a relational algebra projection
 * operation. It filters the columns of tuples from its child operator based on
 * a given list of column names. If the projection column is {@code AllColumns}
 * (*), then the {@code ProjectOperator} should not be created.
 */
public class ProjectOperator extends Operator {

    /**
     * The child operator from which tuples are retrieved.
     */
    private Operator child;

    /**
     * The list of column names to project.
     */
    private final List<String> columns;

    /**
     * Constructs a new {@code ProjectOperator} with the specified list of column
     * names.
     *
     * @param columns The list of column names to project.
     */
    public ProjectOperator(List<String> columns) {
        this.columns = columns;
    }

    /**
     * Sets the child operator for this {@code ProjectOperator}.
     *
     * @param child The child operator.
     */
    public void setChild(Operator child) {
        this.child = child;
    }

    /**
     * Retrieves the child operator.
     *
     * @return The child operator.
     */
    public Operator getChild() {
        return child;
    }

    /**
     * Retrieves the next tuple from the child operator and projects it based on the
     * specified columns.
     *
     * @return The projected tuple, or {@code null} if there are no more tuples.
     */
    @Override
    public Tuple getNextTuple() {
        Tuple tuple;
        if ((tuple = child.getNextTuple()) != null) {
            HashMap<String, Integer> map = new HashMap<>();
            for (String column : columns) {
                map.put(column, tuple.getLookup().get(column));
            }
            // only keep the new projected columns
            tuple.setColumns(columns);
            tuple.setLookup(map);

            return tuple;
        }
        return null;
    }

    /**
     * Resets the child operator.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void reset() throws IOException {
        child.reset();
    }
}