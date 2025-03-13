package ed.inf.adbs.blazedb.operator;

import ed.inf.adbs.blazedb.entity.Tuple;

import java.io.IOException;
import java.util.List;

public class SortOperator extends Operator{
    private final List<String> sortColumns;
    public SortOperator(List<String> sortColumns) {
        this.sortColumns = sortColumns;
    }

    /**
     * @return 
     */
    @Override
    public Tuple getNextTuple() {

        return null;
    }

    /**
     * @throws IOException 
     */
    @Override
    public void reset() throws IOException {

    }
}
