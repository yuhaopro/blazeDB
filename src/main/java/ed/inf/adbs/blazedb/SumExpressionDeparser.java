package ed.inf.adbs.blazedb;

import java.util.Stack;

import ed.inf.adbs.blazedb.entity.Tuple;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;

/**
 * {@code SumExpressionDeparser} is a subclass of {@link ExpressionDeParser}
 * responsible for processing arithmetic expressions, particularly focusing on
 * evaluating multiplication operations and extracting column or literal values
 * from a {@link Tuple}.
 * 
 * <p>
 * This class handles arithmetic expressions involving multiplication, and it
 * can evaluate the product of columns or literals based on the data in a given
 * {@link Tuple}. It uses a stack to temporarily store intermediate results
 * during expression evaluation.
 * </p>
 */
public class SumExpressionDeparser extends ExpressionDeParser {

	private Tuple tuple;
	private Stack<Integer> outputStack = new Stack<>();

	/**
	 * Constructs a {@code SumExpressionDeparser} with the given {@link Tuple} to
	 * evaluate expressions using its data.
	 * 
	 * @param tuple the {@link Tuple} that contains column values for evaluation
	 */
	public SumExpressionDeparser(Tuple tuple) {
		this.tuple = tuple;
	}

	/**
	 * Returns the final output of the evaluated arithmetic expression. This value
	 * is popped from the stack and is the result of the expression evaluation.
	 * 
	 * <p>
	 * If the output is {@code null}, an error message will be printed and a default
	 * value of {@code 0} is returned.
	 * </p>
	 * 
	 * @return the result of the evaluated expression
	 */
	public Integer getOutput() {
		Integer output = outputStack.pop();
		if (output == null) {
			System.err.println("Multiplication output cannot be none!");
			return 0;
		}
		return output;
	}

	/**
	 * Visits a multiplication expression and evaluates it by popping two values
	 * from the stack, multiplying them, and pushing the result back onto the stack.
	 * 
	 * @param multiplication the multiplication expression to process
	 */
	@Override
	public void visit(Multiplication multiplication) {
		super.visit(multiplication);
		int right = outputStack.pop();
		int left = outputStack.pop();
		outputStack.push(right * left);
	}

	/**
	 * Visits a {@link Column} expression and retrieves the value of the column from
	 * the {@link Tuple}. The value is then pushed onto the stack.
	 * 
	 * @param tableColumn the column expression to process
	 */
	@Override
	public void visit(Column tableColumn) {
		super.visit(tableColumn);
		String columnName = tableColumn.toString();
		Integer columnValue = tuple.getLookup().get(columnName);
		outputStack.push(columnValue);
	}

	/**
	 * Visits a {@link LongValue} expression and pushes its integer value onto the
	 * stack.
	 * 
	 * @param longValue the long literal expression to process
	 */
	@Override
	public void visit(LongValue longValue) {
		super.visit(longValue);
		int literal = Math.toIntExact(longValue.getValue());
		outputStack.push(literal);
	}
}
