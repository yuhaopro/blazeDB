package ed.inf.adbs.blazedb.operator;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import ed.inf.adbs.blazedb.entity.Tuple;

public class DuplicateEliminationOperator extends Operator {
    private Operator child;

    private HashMap<String, HashMap<Integer, Boolean>> hashTable = new HashMap<String, HashMap<Integer, Boolean>>();

    public DuplicateEliminationOperator() {
    }

    public void setChild(Operator child) {
        this.child = child;
    }

    public HashMap<String, HashMap<Integer, Boolean>> getHashTable() {
        return hashTable;
    }

    @Override
    public Tuple getNextTuple() {
        // build the hash table
        Tuple tuple;
        while ((tuple = child.getNextTuple()) != null) {
            // check for duplication in each of the column in the tuple
            boolean hasDuplicate = true;
            List<String> columns = tuple.getColumns();
            for (String column : columns) {

                if (hashTable.get(column) == null) {
                    hashTable.put(column, new HashMap<>());
                }

                HashMap<Integer, Boolean> duplicateLookup = hashTable.get(column);
                int columnValue = tuple.getLookup().get(column);
                if (duplicateLookup.containsKey(columnValue)) {
                    continue;
                } else {
                    // at least 1 column is not a duplicate
                    duplicateLookup.put(columnValue, true);
                    hasDuplicate = false;
                }

            }
            if (!hasDuplicate) {
                return tuple;
            }
        }
        return null;
    }

    @Override
    public void reset() throws IOException {
        this.child.reset();
        this.hashTable = new HashMap<String, HashMap<Integer, Boolean>>();
    }

}
