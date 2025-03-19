package ed.inf.adbs.blazedb.operator;

import ed.inf.adbs.blazedb.EvaluationDeparser;
import ed.inf.adbs.blazedb.entity.Tuple;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;

import java.io.IOException;
import java.util.List;

/**
 * This operator filters tuples based on the conditions specified in the WHERE clause.
*/
public class SelectOperator extends Operator {
    private Operator child;
    private final Expression expression;
    private final EvaluationDeparser expressionDeParser;
    private final String tableName;
    public SelectOperator(String tableName, List<Expression> expressions) {
        // combine list of expressions
        this.expression = combineListOfExpressions(expressions);
        this.expressionDeParser = new EvaluationDeparser();
        this.tableName = tableName;
    }

    public Expression combineListOfExpressions(List<Expression> expressions) {
        if (expressions.size() == 1) return expressions.getFirst();

        Expression firstExpression = expressions.get(0);
        Expression secondExpression = expressions.get(1);
        AndExpression andExpression = new AndExpression(firstExpression, secondExpression);
        for (int i = 2; i < expressions.size(); i++) {
            andExpression = new AndExpression(andExpression, expressions.get(i));
        }

        return andExpression;
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
