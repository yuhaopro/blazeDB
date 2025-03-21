package ed.inf.adbs.blazedb.operator;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import ed.inf.adbs.blazedb.entity.Tuple;

/**
 * The {@link DuplicateEliminationOperator} class is an implementation of a
 * database operator that removes duplicate tuples based on certain columns.
 * This operator processes a child operator, checks for duplicate tuples, and
 * ensures that only unique tuples (based on column values) are passed to the
 * next stage in the query processing pipeline.
 * <p>
 * The operator uses a hash table to keep track of column values that have
 * already been seen to eliminate duplicates. It operates by examining each
 * tuple produced by the child operator and ensuring that only tuples with
 * unique values in specified columns are returned.
 * </p>
 */
public class DuplicateEliminationOperator extends Operator {

    /** The child operator that provides tuples for processing. */
    private Operator child;

    /** The hash table used to store seen column values to detect duplicates. */
    private HashMap<String, HashMap<Integer, Boolean>> hashTable = new HashMap<>();

    /**
     * Sets the child operator from which this operator will fetch tuples.
     * 
     * @param child The child operator that produces tuples to be processed.
     */
    public void setChild(Operator child) {
        this.child = child;
    }

    /**
     * Retrieves the current hash table used to track column values and detect
     * duplicates.
     * 
     * @return The hash table containing previously seen column values.
     */
    public HashMap<String, HashMap<Integer, Boolean>> getHashTable() {
        return hashTable;
    }

    /**
     * Fetches the next tuple from the child operator, checking for duplicates.
     * 
     * <p>
     * This method examines each tuple from the child operator and ensures that it
     * only returns unique tuples by checking the values of the columns against the
     * hash table. If a tuple is determined to be a duplicate, it is ignored. If a
     * tuple is unique, it is returned.
     * </p>
     * 
     * @return The next unique tuple from the child operator, or {@code null} if no
     *         more tuples are available or all tuples are duplicates.
     */
    @Override
    public Tuple getNextTuple() {
        Tuple tuple;
        // Process each tuple from the child operator
        while ((tuple = child.getNextTuple()) != null) {
            boolean hasDuplicate = true;
            List<String> columns = tuple.getColumns();

            // Check for duplication in each column
            for (String column : columns) {
                // Initialize hash table for the column if it doesn't exist
                hashTable.putIfAbsent(column, new HashMap<>());

                HashMap<Integer, Boolean> duplicateLookup = hashTable.get(column);
                int columnValue = tuple.getLookup().get(column);

                // Check if the column value is already in the hash table
                if (duplicateLookup.containsKey(columnValue)) {
                    continue; // Skip this column value if it has already been seen
                } else {
                    // Mark the column value as seen and set the tuple as unique
                    duplicateLookup.put(columnValue, true);
                    hasDuplicate = false;
                }
            }

            // Return the tuple if it is not a duplicate
            if (!hasDuplicate) {
                return tuple;
            }
        }
        return null; // No more unique tuples available
    }

    /**
     * Resets the operator, clearing the hash table and resetting the child
     * operator.
     * 
     * <p>
     * This method is typically called when restarting the query execution process
     * or clearing state between multiple invocations of the operator.
     * </p>
     * 
     * @throws IOException if there is an error resetting the child operator.
     */
    @Override
    public void reset() throws IOException {
        this.child.reset(); // Reset the child operator
        this.hashTable = new HashMap<>(); // Clear the hash table for duplicate tracking
    }
}
