package ed.inf.adbs.blazedb.operator;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import ed.inf.adbs.blazedb.entity.Tuple;

// If projection column is AllColumns -> *, then the Project Operator should not be created.
public class ProjectOperator extends Operator {
    private Operator child;
    private final List<String> columns;

    public ProjectOperator(List<String> columns) {
        this.columns = columns;
    }

    public void setChild(Operator child) {
        this.child = child;
    }

    public Operator getChild() {
        return child;
    }

    /**
     * @return
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
     * @throws IOException
     */
    @Override
    public void reset() throws IOException {
        child.reset();
    }
}
