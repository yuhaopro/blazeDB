package ed.inf.adbs.blazedb;

import ed.inf.adbs.blazedb.entity.Tuple;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

// Expression is always column to column comparison
public class JoinExpressionEvaluator extends ExpressionDeParser {
    private Tuple leftTuple;
    private Tuple rightTuple;
    private List<String> rightJoinColumns = new ArrayList<String>();
    private final Stack<String> valueStack = new Stack<String>();
    private final Stack<Boolean> outputStack = new Stack<Boolean>();

    public JoinExpressionEvaluator(
    ) {
        super();

    }

    public void setLeftTuple(Tuple tuple) {
        this.leftTuple = tuple;
    }

    public void setRightTuple(Tuple tuple) {
        this.rightTuple = tuple;
    }


    public boolean getOutput() {
        if (outputStack.isEmpty()) {
            System.err.println("Output stack is empty");
            outputStack.push(false);
        }
        return outputStack.pop();
    }

    public List<String> getRightJoinColumns() {
        return this.rightJoinColumns;
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
        String right = valueStack.pop(); // right column
        String left = valueStack.pop(); // left column
        rightJoinColumns.add(right);

        Integer leftValue = leftTuple.getLookup().get(left);
        Integer rightValue = rightTuple.getLookup().get(right);

        outputStack.push(leftValue.equals(rightValue));
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        super.visit(notEqualsTo);
        String right = valueStack.pop(); // right column
        String left = valueStack.pop(); // left column
        rightJoinColumns.add(right);

        Integer leftValue = leftTuple.getLookup().get(left);
        Integer rightValue = rightTuple.getLookup().get(right);
        outputStack.push(!leftValue.equals(rightValue));
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        super.visit(greaterThan);
        String right = valueStack.pop(); // right column
        String left = valueStack.pop(); // left column
        rightJoinColumns.add(right);

        Integer leftValue = leftTuple.getLookup().get(left);
        Integer rightValue = rightTuple.getLookup().get(right);
        outputStack.push((leftValue.compareTo(rightValue) > 0));
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        super.visit(greaterThanEquals);
        String right = valueStack.pop(); // right column
        String left = valueStack.pop(); // left column
        rightJoinColumns.add(right);

        Integer leftValue = leftTuple.getLookup().get(left);
        Integer rightValue = rightTuple.getLookup().get(right);
        outputStack.push((leftValue.compareTo(rightValue) >= 0));
    }

    @Override
    public void visit(MinorThan minorThan) {
        super.visit(minorThan);
        String right = valueStack.pop(); // right column
        String left = valueStack.pop(); // left column
        rightJoinColumns.add(right);

        Integer leftValue = leftTuple.getLookup().get(left);
        Integer rightValue = rightTuple.getLookup().get(right);
        outputStack.push((leftValue.compareTo(rightValue) < 0));
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        super.visit(minorThanEquals);
        String right = valueStack.pop(); // right column
        String left = valueStack.pop(); // left column
        rightJoinColumns.add(right);

        Integer leftValue = leftTuple.getLookup().get(left);
        Integer rightValue = rightTuple.getLookup().get(right);
        outputStack.push((leftValue.compareTo(rightValue) <= 0));
    }

    @Override
    public void visit(Column tableColumn) {
        super.visit(tableColumn);
        String columnName = tableColumn.toString();
        valueStack.push(columnName);
    }

}