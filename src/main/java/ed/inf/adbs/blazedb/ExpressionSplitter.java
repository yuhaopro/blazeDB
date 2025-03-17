package ed.inf.adbs.blazedb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;

public class ExpressionSplitter extends ExpressionDeParser {
    private List<SplitExpression> joinExpressions = new ArrayList<SplitExpression>();
    // TableName, SplitExpression
    private final HashMap<String, List<SplitExpression>> singleExpressions = new HashMap<String, List<SplitExpression>>();
    private Stack<String> inputStack = new Stack<String>();
    private Stack<SplitExpression> outputStack = new Stack<SplitExpression>();
    private Expression expression;
    public ExpressionSplitter(Expression expression) {
        super();
        this.expression = expression;
    }
    public List<SplitExpression> getJoinExpressions() {
        return joinExpressions;
    }
    public HashMap<String, List<SplitExpression>> getSingleExpressions() {
        return singleExpressions;
    }

    public Stack<SplitExpression> getOutputStack() {
        return outputStack;
    }

    public void split() {
        expression.accept(this);
    }

    @Override
    public void visit(AndExpression andExpression) {
        super.visit(andExpression);
        SplitExpression leftExpression = outputStack.pop();
        SplitExpression rightExpression = outputStack.pop();

        // the last stack value will refer to the first value
        if (leftExpression.isJoinExpression()) {
            outputStack.push(leftExpression);
        }
        if (rightExpression.isJoinExpression()) {
            rightExpression.setConditionBeforeExpression("AND");
            joinExpressions.add(rightExpression);
        } else {
            singleExpressions.computeIfAbsent(rightExpression.getTableName(), k -> new ArrayList<>()).add(rightExpression);
        }
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        super.visit(equalsTo);
        processExpression("=");
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        super.visit(notEqualsTo);
        processExpression("!=");
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        super.visit(greaterThan);
        processExpression(">");
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        super.visit(greaterThanEquals);
        processExpression(">=");
    }

    @Override
    public void visit(MinorThan minorThan) {
        super.visit(minorThan);
        processExpression("<");

    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        super.visit(minorThanEquals);
        processExpression("<=");
    }

    @Override
    public void visit(Column tableColumn) {
        super.visit(tableColumn);
        String columnName = tableColumn.toString();
        inputStack.push(columnName);
    }

    @Override
    public void visit(LongValue longValue) {
        super.visit(longValue);
        int literal = Math.toIntExact(longValue.getValue());
        inputStack.push(Integer.toString(literal));
    }

    public void processExpression(String operator) {
        String right = inputStack.pop();
        String left = inputStack.pop();
        SplitExpression splitExpression = new SplitExpression(left, right, operator);
        outputStack.push(splitExpression);
    }
}
