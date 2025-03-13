package ed.inf.adbs.blazedb.operator;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import ed.inf.adbs.blazedb.entity.Tuple;

public class DuplicateEliminationOperator extends Operator {
    private Operator child;

    private HashMap<String, HashMap<Integer, Boolean>> hashTable = new HashMap<>();

    public DuplicateEliminationOperator() {

    }

    public void setChild(Operator child) {
        this.child = child;
    }

    @Override
    public Tuple getNextTuple() {
        // build the hash table
        Tuple tuple;
        while ((tuple = child.getNextTuple()) != null) {
            // check for duplication in each of the column in the tuple
        
            List<String> columns = tuple.getColumns();
            for (String column: columns) {
                HashMap<Integer, Boolean> duplicateLookup = hashTable.get(column);
                int columnValue = tuple.getLookup().get(column);
                if (duplicateLookup.containsKey(columnValue)) {
                    continue;
                } else {
                    // at least 1 column is not a duplicate
                    duplicateLookup.put(columnValue, true);
                    return tuple;
                }                
            }
        }
        return null;
    }

    @Override
    public void reset() throws IOException {
        child.reset();
        hashTable = new HashMap<>();
    }

}
