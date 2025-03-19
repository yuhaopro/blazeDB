package ed.inf.adbs.blazedb.operator;

import java.io.IOException;

import ed.inf.adbs.blazedb.EvaluationDeparser;
import ed.inf.adbs.blazedb.entity.Tuple;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;

/** 
 * This operator filters tuples based on the conditions specified in the WHERE clause.
*/
public class SelectOperator extends Operator {
    private Operator child;
    private final Expression expression;
    private final String tableName;
    public SelectOperator(String tableName, Expression expression) {
        // combine list of expressions
        this.expression = expression;
        this.tableName = tableName;
    }

    public void setChild(Operator child) {
        this.child = child;
    }

    public String getTableName() {
        return tableName;
    }

    /**
     * @return 
     */
    @Override
    public Tuple getNextTuple() {
        Tuple tuple;

        // keeps fetching for a tuple until it matches the condition
        while ((tuple = child.getNextTuple()) != null) {

            if (expression == null) {
                return tuple;
            }
            EvaluationDeparser evaluationDeparser = new EvaluationDeparser();
            evaluationDeparser.addTuple(tuple);
            expression.accept(evaluationDeparser);
            if (evaluationDeparser.getOutput()) {
                return tuple;
            }

        }
        return null;
    }

    /**
     * 
     */
    @Override
    public void reset() throws IOException {
        this.child.reset();
    }

    public Operator getChild() {
        return child;
    }
}
