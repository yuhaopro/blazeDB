package ed.inf.adbs.blazedb;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import ed.inf.adbs.blazedb.entity.Tuple;
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

/**
 * {@code EvaluationDeparser} is a custom implementation of
 * {@code ExpressionDeParser} used to evaluate SQL expressions in the context of
 * tuples. This class is primarily responsible for handling logical and
 * relational expressions (e.g., AND, equals, greater-than). It processes these
 * expressions and computes their corresponding results based on the values in
 * the input {@code Tuple}s.
 * 
 * <p>
 * The {@code EvaluationDeparser} operates by visiting different types of
 * expressions (such as logical and comparison expressions) and evaluating them
 * by pushing values onto a stack and performing operations like comparison or
 * logical conjunction. The results of the expressions are also pushed onto a
 * stack, where the final result can be accessed.
 * </p>
 * 
 * <p>
 * It works by visiting expressions in the AST (abstract syntax tree)
 * representation of SQL statements and using the values from a list of
 * {@code Tuple}s to compute the result of those expressions.
 * </p>
 */
public class EvaluationDeparser extends ExpressionDeParser {
    private List<Tuple> tuples = new ArrayList<>();
    private final Stack<Integer> valueStack = new Stack<>();
    private final Stack<Boolean> outputStack = new Stack<>();

    /**
     * Default constructor for {@code EvaluationDeparser}.
     */
    public EvaluationDeparser() {
        super();
    }

    /**
     * Gets the final evaluation result of the expression.
     * 
     * <p>
     * This method returns the result of the evaluated expression as a boolean. The
     * boolean result is popped from the output stack after processing the
     * expression.
     * </p>
     * 
     * @return the evaluation result of the expression
     */
    public boolean getOutput() {
        if (outputStack.isEmpty()) {
            System.err.println("Output stack is empty");
            outputStack.push(false); // Default to false if the output stack is empty
        }
        return outputStack.pop();
    }

    /**
     * Adds a tuple to the list of tuples to be used for expression evaluation.
     * 
     * @param tuple the tuple to add
     */
    public void addTuple(Tuple tuple) {
        tuples.add(tuple);
    }

    /**
     * Visits an {@code AndExpression} and evaluates it by performing a logical AND
     * operation.
     * 
     * @param andExpression the AND expression to visit
     */
    @Override
    public void visit(AndExpression andExpression) {
        super.visit(andExpression);
        Boolean right = outputStack.pop();
        Boolean left = outputStack.pop();
        outputStack.push(left && right);
    }

    /**
     * Visits an {@code EqualsTo} expression and evaluates whether the left and
     * right values are equal.
     * 
     * @param equalsTo the equals expression to visit
     */
    @Override
    public void visit(EqualsTo equalsTo) {
        super.visit(equalsTo);
        Integer right = valueStack.pop();
        Integer left = valueStack.pop();
        outputStack.push(left.equals(right));
    }

    /**
     * Visits a {@code NotEqualsTo} expression and evaluates whether the left and
     * right values are not equal.
     * 
     * @param notEqualsTo the not-equals expression to visit
     */
    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        super.visit(notEqualsTo);
        Integer right = valueStack.pop();
        Integer left = valueStack.pop();
        outputStack.push(!left.equals(right));
    }

    /**
     * Visits a {@code GreaterThan} expression and evaluates whether the left value
     * is greater than the right value.
     * 
     * @param greaterThan the greater-than expression to visit
     */
    @Override
    public void visit(GreaterThan greaterThan) {
        super.visit(greaterThan);
        Integer right = valueStack.pop();
        Integer left = valueStack.pop();
        outputStack.push((left.compareTo(right) > 0));
    }

    /**
     * Visits a {@code GreaterThanEquals} expression and evaluates whether the left
     * value is greater than or equal to the right value.
     * 
     * @param greaterThanEquals the greater-than-or-equal expression to visit
     */
    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        super.visit(greaterThanEquals);
        Integer right = valueStack.pop();
        Integer left = valueStack.pop();
        outputStack.push((left.compareTo(right) >= 0));
    }

    /**
     * Visits a {@code MinorThan} expression and evaluates whether the left value is
     * less than the right value.
     * 
     * @param minorThan the less-than expression to visit
     */
    @Override
    public void visit(MinorThan minorThan) {
        super.visit(minorThan);
        Integer right = valueStack.pop();
        Integer left = valueStack.pop();
        outputStack.push((left.compareTo(right) < 0));
    }

    /**
     * Visits a {@code MinorThanEquals} expression and evaluates whether the left
     * value is less than or equal to the right value.
     * 
     * @param minorThanEquals the less-than-or-equal expression to visit
     */
    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        super.visit(minorThanEquals);
        Integer right = valueStack.pop();
        Integer left = valueStack.pop();
        outputStack.push((left.compareTo(right) <= 0));
    }

    /**
     * Visits a {@code Column} expression and pushes the value of the column from
     * the tuples onto the value stack.
     * 
     * @param tableColumn the column expression to visit
     */
    @Override
    public void visit(Column tableColumn) {
        super.visit(tableColumn);
        String columnName = tableColumn.toString();

        for (Tuple tuple : tuples) {
            Integer columnValue = tuple.getLookup().get(columnName);
            if (columnValue != null) {
                valueStack.push(columnValue);
                break;
            }
        }
    }

    /**
     * Visits a {@code LongValue} expression and pushes the long value as an integer
     * onto the value stack.
     * 
     * @param longValue the long value expression to visit
     */
    @Override
    public void visit(LongValue longValue) {
        super.visit(longValue);
        Integer literal = Math.toIntExact(longValue.getValue());
        valueStack.push(literal);
    }
}
