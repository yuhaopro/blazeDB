package ed.inf.adbs.blazedb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.ComparisonOperator;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;

/**
 * {@code SplitExpressionDeparser} is a subclass of {@link ExpressionDeParser}
 * responsible for processing SQL comparison expressions (e.g., equality,
 * greater than, less than) and organizing them into single table and join
 * expressions. It handles the analysis of comparison expressions between
 * columns or literals and categorizes them based on whether they involve
 * columns from the same table or different tables.
 * 
 * <p>
 * It divides the comparison expressions into two types:
 * </p>
 * <ul>
 * <li>Single expressions: Comparisons where both operands belong to the same
 * table.</li>
 * <li>Join expressions: Comparisons where the operands belong to different
 * tables.</li>
 * </ul>
 * 
 * <p>
 * Additionally, it provides functionality to check if an expression always
 * evaluates to false (e.g., a comparison like 4 = 5).
 * </p>
 */
public class SplitExpressionDeparser extends ExpressionDeParser {

    private boolean expressionIsAlwaysFalse = false;
    private final HashMap<String, List<Expression>> singleExpressions = new HashMap<>();
    private final HashMap<String, List<Expression>> joinExpressions = new HashMap<>();

    /**
     * Default constructor for {@code SplitExpressionDeparser}.
     */
    public SplitExpressionDeparser() {
    }

    /**
     * Returns whether the expression always evaluates to false.
     * 
     * <p>
     * This is determined when both operands are {@link LongValue} literals and they
     * are not equal.
     * </p>
     * 
     * @return {@code true} if the expression is always false, {@code false}
     *         otherwise
     */
    public boolean getExpressionIsAlwaysFalse() {
        return expressionIsAlwaysFalse;
    }

    /**
     * Returns a map of single expressions, where the key is the table name and the
     * value is a list of expressions for that table.
     * 
     * @return a map of single table expressions
     */
    public HashMap<String, List<Expression>> getSingleExpressions() {
        return singleExpressions;
    }

    /**
     * Returns a map of join expressions, where the key is the table name and the
     * value is a list of expressions involving that table.
     * 
     * @return a map of join expressions
     */
    public HashMap<String, List<Expression>> getJoinExpressions() {
        return joinExpressions;
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        super.visit(equalsTo);
        processExpression(equalsTo);
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        super.visit(notEqualsTo);
        processExpression(notEqualsTo);
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        super.visit(greaterThan);
        processExpression(greaterThan);
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        super.visit(greaterThanEquals);
        processExpression(greaterThanEquals);
    }

    @Override
    public void visit(MinorThan minorThan) {
        super.visit(minorThan);
        processExpression(minorThan);
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        super.visit(minorThanEquals);
        processExpression(minorThanEquals);
    }

    /**
     * Processes a comparison expression and categorizes it into either a single
     * expression or a join expression.
     * 
     * <p>
     * This method checks if the operands of the comparison operator are columns,
     * literals, or a combination thereof, and stores the expression in either the
     * {@code singleExpressions} or {@code joinExpressions} map accordingly.
     * </p>
     * 
     * @param operator the comparison operator to process (e.g., {@link EqualsTo},
     *                 {@link GreaterThan})
     */
    public void processExpression(ComparisonOperator operator) {
        Expression left = operator.getLeftExpression();
        Expression right = operator.getRightExpression();

        // Check if both expressions are columns
        if (left instanceof Column && right instanceof Column) {
            Column leftColumn = (Column) left;
            Column rightColumn = (Column) right;

            String leftTableName = leftColumn.getTable().getName();
            String rightTableName = rightColumn.getTable().getName();
            // If left and right tables are different, it's a join expression
            if (!leftTableName.equals(rightTableName)) {
                joinExpressions.putIfAbsent(leftTableName, new ArrayList<>());
                joinExpressions.get(leftTableName).add(operator);
                joinExpressions.putIfAbsent(rightTableName, new ArrayList<>());
                joinExpressions.get(rightTableName).add(operator);
                return;
            }

            // If both columns are from the same table, it's a single expression
            singleExpressions.putIfAbsent(leftTableName, new ArrayList<>());
            singleExpressions.get(leftTableName).add(operator);
        }

        // If the left or right expression is a column and the other is a literal value
        if (left instanceof Column) {
            Column column = (Column) left;
            String tableName = column.getTable().getName();
            singleExpressions.putIfAbsent(tableName, new ArrayList<>());
            singleExpressions.get(tableName).add(operator);
            return;
        }

        if (right instanceof Column) {
            Column column = (Column) right;
            String tableName = column.getTable().getName();
            singleExpressions.putIfAbsent(tableName, new ArrayList<>());
            singleExpressions.get(tableName).add(operator);
            return;
        }

        // If both operands are literals, check if they are equal or not
        if (left instanceof LongValue && right instanceof LongValue) {
            expressionIsAlwaysFalse = !left.equals(right);
        }
    }
}
