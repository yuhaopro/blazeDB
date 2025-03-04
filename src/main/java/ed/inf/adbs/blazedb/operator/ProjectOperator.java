package ed.inf.adbs.blazedb.operator;

import ed.inf.adbs.blazedb.Tuple;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SelectItem;

import java.io.IOException;
import java.util.List;

public class ProjectOperator extends Operator {
    private Operator child;
    private List<SelectItem<?>> columns;
    public ProjectOperator(List<SelectItem<?>> columns) {
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
        while ((tuple = child.getNextTuple()) != null) {
//            Tuple newTuple = new Tuple();
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
