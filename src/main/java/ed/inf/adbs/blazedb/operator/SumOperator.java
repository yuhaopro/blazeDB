package ed.inf.adbs.blazedb.operator;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import ed.inf.adbs.blazedb.SumExpressionEvaluator;
import ed.inf.adbs.blazedb.entity.Tuple;
import net.sf.jsqlparser.expression.Expression;


// SUM() can contain multiplication within it's expression
// groupBy should not have duplicates.
// should ignore the column without aggregation, simply return tuple.
public class SumOperator extends Operator {
	Operator child;
	List<String> groupBys;
	List<Expression> sumExpressions;
	HashMap<String, List<Integer>> groupByHashMap = new HashMap<String, List<Integer>>();

	public SumOperator() {}


	/**
	 * build the hash table for grouping
	 */
	public void initialize() {
		// Sum can exist without groupby
		// groupBy can exist without sum
		Tuple tuple;
		while ((tuple = child.getNextTuple()) != null) {
			SumExpressionEvaluator sumExpressionEvaluator = new SumExpressionEvaluator();
			sumExpressionEvaluator.setTuple(tuple);
			for (Expression sumExpression : sumExpressions) {
				sumExpression.accept(sumExpressionEvaluator);

				// get the value after evaluating expression
				// store the tuple in the hashmap by it's 
				int expressionValue = sumExpressionEvaluator.getOutputStack().pop();
				// List<String> columnOrder = tuple.getColumns().indexOf(sumExpressionEvaluator)
				// groupByHashMap.put(null, );
			}
		}
		
		
	}


	public void setGroupBys(List<String> groupBys) {
		this.groupBys = groupBys;
	}

	public void setSumExpressions(List<Expression> sumExpressions) {
		this.sumExpressions = sumExpressions;
	}

	public void setChild(Operator child) {
		this.child = child;
	}

	@Override
	public Tuple getNextTuple() {
		Tuple tuple = new Tuple();
		return tuple;
	}

	@Override
	public void reset() throws IOException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'reset'");
	}

}
