package ed.inf.adbs.blazedb.operator;

import ed.inf.adbs.blazedb.Tuple;

import java.io.FileNotFoundException;
import java.io.IOException;

public class SelectOperator extends Operator {
    private final Operator child;
    public SelectOperator(Operator child) {
        this.child = child;
    }

    /**
     * @return 
     */
    @Override
    public Tuple getNextTuple() {
        return null;
    }

    /**
     * 
     */
    @Override
    public void reset() throws IOException {
        this.child.reset();
    }
}
