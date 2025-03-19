package ed.inf.adbs.blazedb;

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

public class SplitExpressionDeparser extends ExpressionDeParser {

    private boolean expressionIsAlwaysFalse = false;
    private final HashMap<String, List<Expression>> singleExpressions = new HashMap<>();
    private final HashMap<String, List<Expression>> joinExpressions = new HashMap<>();
    
    public SplitExpressionDeparser() {

    }

    /**
     * @ checks if expression contains literals eg. 4=4
     * @return boolean value
     */
    public boolean getExpressionIsAlwaysFalse() {
        return expressionIsAlwaysFalse;
    }

    public HashMap<String, List<Expression>> getSingleExpressions() {
        return singleExpressions;
    }

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

    public void processExpression(ComparisonOperator operator) {
        Expression left = operator.getLeftExpression();
        Expression right = operator.getRightExpression();

        if (left instanceof Column && right instanceof Column) {
            Column leftColumn = (Column) left;
            Column rightColumn = (Column) right;

            String leftTableName = leftColumn.getTable().getName();
            String rightTableName = rightColumn.getTable().getName();
            // left and right tables are different
            if (!leftTableName.equals(rightTableName)) {

                joinExpressions.get(leftTableName).add(operator);
                joinExpressions.get(rightTableName).add(operator);
                return;
            }

            singleExpressions.get(leftTableName).add(operator);
        }

        // one of the expression is a string
        if (left instanceof Column) {
            Column column = (Column) left;
            String tableName = column.getTable().getName();
            singleExpressions.get(tableName).add(operator);
            return;
        }

        if (right instanceof Column) {
            Column column = (Column) right;
            String tableName = column.getTable().getName();
            singleExpressions.get(tableName).add(operator);
            return;
        }

        if (left instanceof LongValue && right instanceof LongValue) {
            expressionIsAlwaysFalse = !left.equals(right);
        }


    }
}
