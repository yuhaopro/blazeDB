package ed.inf.adbs.blazedb.operator;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import ed.inf.adbs.blazedb.entity.Tuple;


// SUM() can contain multiplication within it's expression
public class SumOperator extends Operator {
	Operator child;
	List<String> groupBy;
	List<String> sumExpressions;
	HashMap<String, Integer> groupByHashMap = new HashMap<String, Integer>();

	public SumOperator() {}


	/**
	 * build the hash table for grouping
	 */
	public void initialize() {
		// Sum can exist without groupby
		// groupBy can exist without sum

		
		
	}


	public void setGroupBys(List<String> groupBy) {
		this.groupBy = groupBy;
	}

	public void setSumExpressions(List<String> sumExpressions) {
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
