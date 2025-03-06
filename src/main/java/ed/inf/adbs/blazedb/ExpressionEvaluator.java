package ed.inf.adbs.blazedb;

import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;

import java.util.Stack;

public class ExpressionEvaluator extends ExpressionDeParser {
    private Tuple tuple;
    private final Stack<Integer> valueStack = new Stack<Integer>();
    private final Stack<Boolean> outputStack = new Stack<Boolean>();
    public ExpressionEvaluator() {
        super();
    }

    public void setTuple(Tuple tuple) {
        this.tuple = tuple;
    }

    public boolean getOutput() {
        if (outputStack.isEmpty()) {
            System.err.println("Output stack is empty");
            outputStack.push(false);
        }
        return outputStack.pop();
    }

    @Override
    public void visit(AndExpression andExpression) {
        super.visit(andExpression);
        Boolean right = outputStack.pop();
        Boolean left = outputStack.pop();

        outputStack.push(left && right);
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        super.visit(equalsTo);
        Integer right = valueStack.pop();
        Integer left = valueStack.pop();
        outputStack.push(left.equals(right));
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        super.visit(notEqualsTo);
        Integer right = valueStack.pop();
        Integer left = valueStack.pop();
        outputStack.push(!left.equals(right));
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        super.visit(greaterThan);
        Integer right = valueStack.pop();
        Integer left = valueStack.pop();
        outputStack.push((left.compareTo(right) > 0));
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        super.visit(greaterThanEquals);
        Integer right = valueStack.pop();
        Integer left = valueStack.pop();
        outputStack.push((left.compareTo(right) >= 0));
    }

    @Override
    public void visit(MinorThan minorThan) {
        super.visit(minorThan);
        Integer right = valueStack.pop();
        Integer left = valueStack.pop();
        outputStack.push((left.compareTo(right) < 0));
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        super.visit(minorThanEquals);
        Integer right = valueStack.pop();
        Integer left = valueStack.pop();
        outputStack.push((left.compareTo(right) <= 0));
    }

    @Override
    public void visit(Column tableColumn) {
        super.visit(tableColumn);
        String columnName = tableColumn.toString();
        Integer columnValue = tuple.getLookup().get(columnName);
        valueStack.push(columnValue);
    }

    @Override
    public void visit(LongValue longValue) {
        super.visit(longValue);
        Integer literal = Math.toIntExact(longValue.getValue());
        valueStack.push(literal);
    }
}
