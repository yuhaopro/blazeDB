package ed.inf.adbs.blazedb.operator;

import ed.inf.adbs.blazedb.BlazeExpressionDeParser;
import ed.inf.adbs.blazedb.Tuple;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;

import java.io.FileNotFoundException;
import java.io.IOException;

public class SelectOperator extends Operator {
    private Operator child;
    private final Expression expression;
    private final BlazeExpressionDeParser expressionDeParser;
    public SelectOperator(Expression expression) {
        this.expression = expression;
        this.expressionDeParser = new BlazeExpressionDeParser();
    }

    public void setChild(Operator child) {
        this.child = child;
    }

    /**
     * @return 
     */
    @Override
    public Tuple getNextTuple() {
        Tuple tuple;

        // keeps fetching for a tuple until it matches the condition
        while ((tuple = child.getNextTuple()) != null) {
            // tuple.print();
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
