package ed.inf.adbs.blazedb;

import java.util.Stack;

import ed.inf.adbs.blazedb.entity.Tuple;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;

public class SumExpressionEvaluator extends ExpressionDeParser {
	
	private Tuple tuple;
	private final Stack<Integer> outputStack = new Stack<>();
	
	public SumExpressionEvaluator() {

	}

	public Stack<Integer> getOutputStack() {
		return this.outputStack;
	}

	public void setTuple(Tuple tuple) {
		this.tuple = tuple;
	}

	@Override
	public void visit(Multiplication multiplication) {
		super.visit(multiplication);
		int right = outputStack.pop();
		int left = outputStack.pop();

		outputStack.push(right*left);
	}

    @Override
    public void visit(Column tableColumn) {
        super.visit(tableColumn);
        String columnName = tableColumn.toString();
		Integer columnValue = tuple.getLookup().get(columnName);
		outputStack.push(columnValue);
    }

    @Override
    public void visit(LongValue longValue) {
        super.visit(longValue);
        int literal = Math.toIntExact(longValue.getValue());
		outputStack.push(literal);
	}
}
