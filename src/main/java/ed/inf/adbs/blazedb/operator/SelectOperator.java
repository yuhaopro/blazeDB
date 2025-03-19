package ed.inf.adbs.blazedb.operator;

import ed.inf.adbs.blazedb.EvaluationDeparser;
import ed.inf.adbs.blazedb.entity.Tuple;
import net.sf.jsqlparser.expression.Expression;

import java.io.IOException;

public class SelectOperator extends Operator {
    private Operator child;
    private final Expression expression;
    private final EvaluationDeparser expressionDeParser;
    private final String tableName;
    public SelectOperator(String tableName, Expression expression) {
        this.expression = expression;
        this.expressionDeParser = new EvaluationDeparser();
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
            expressionDeParser.setTuple(tuple);
            expression.accept(expressionDeParser);
            if (expressionDeParser.getOutput()) {
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
