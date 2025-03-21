package ed.inf.adbs.blazedb.operator;

import java.io.IOException;

import ed.inf.adbs.blazedb.EvaluationDeparser;
import ed.inf.adbs.blazedb.entity.Tuple;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;

/**
 * * The {@code SelectOperator} class represents a relational algebra selection
 * operation. It filters tuples from its child operator based on a specified
 * boolean expression. This operator corresponds to the WHERE clause in SQL
 * queries.
 */
public class SelectOperator extends Operator {

    /**
     * The child operator from which tuples are retrieved.
     */
    private Operator child;

    /**
     * The boolean expression used to filter tuples.
     */
    private final Expression expression;

    /**
     * The name of the table being operated on.
     */
    private final String tableName;

    /**
     * Constructs a new {@code SelectOperator} with the specified table name and
     * filter expression.
     *
     * @param tableName  The name of the table.
     * @param expression The boolean expression used for filtering.
     */
    public SelectOperator(String tableName, Expression expression) {
        this.expression = expression;
        this.tableName = tableName;
    }

    /**
     * Sets the child operator for this {@code SelectOperator}.
     *
     * @param child The child operator.
     */
    public void setChild(Operator child) {
        this.child = child;
    }

    /**
     * Retrieves the name of the table being operated on.
     *
     * @return The table name.
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Retrieves the next tuple that satisfies the filter expression.
     *
     * @return The next tuple that satisfies the filter expression, or {@code null}
     *         if there are no more matching tuples.
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
     * Resets the child operator.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void reset() throws IOException {
        this.child.reset();
    }

    /**
     * Retrieves the child operator.
     *
     * @return The child operator.
     */
    public Operator getChild() {
        return child;
    }
}