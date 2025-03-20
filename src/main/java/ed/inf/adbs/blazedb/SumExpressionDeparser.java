package ed.inf.adbs.blazedb;

import java.util.Stack;

import ed.inf.adbs.blazedb.entity.Tuple;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;

public class SumExpressionDeparser extends ExpressionDeParser {
	
	private Tuple tuple;
	private Stack<Integer> outputStack = new Stack<>();
	
	public SumExpressionDeparser(Tuple tuple) {
		this.tuple = tuple;
	}

	public Integer getOutput() {
		Integer output = outputStack.pop();
		if (output == null) {
			System.err.println("Multiplication output cannot be none!");
			return 0;
		}
		return output;
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
