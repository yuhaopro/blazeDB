package ed.inf.adbs.blazedb.operator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import ed.inf.adbs.blazedb.SumExpressionEvaluator;
import ed.inf.adbs.blazedb.entity.Tuple;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.GroupByElement;

// This operator should always come before project operator
// SUM() can contain multiplication within it's expression
// groupBy should not have duplicates
// should ignore the column without aggregation, simply return tuple
public class SumOperator extends Operator {
	Operator child;
	ExpressionList<Expression> groupByExpressionList;
	ExpressionList<Expression> selectExpressionList;

	LinkedHashMap<String, HashMap<String, Integer>> groupByHashMap;
	Iterator<Tuple> outputTuplesIterator;
	List<Tuple> outputTuples;

	public SumOperator(GroupByElement groupByElement,
			ExpressionList<Expression> selectExpressionList) {

		if (groupByElement != null) {
			this.groupByExpressionList = groupByElement.getGroupByExpressionList();
		}
		this.selectExpressionList = selectExpressionList;
	}

	/**
	 * Creates a new tuple, which will sum up all the child tuples based on select
	 * expression
	 */
	public Tuple computeSumOfTuples() {
		Tuple tuple;
		Tuple outputTuple = new Tuple();
		boolean firstTupleIteration = false;
		while ((tuple = child.getNextTuple()) != null) {
			if (outputTuple.getColumns().isEmpty()) {
				firstTupleIteration = true;
			} else {
				firstTupleIteration = false;
			}
			for (Expression selectExpression : selectExpressionList) {
				if (selectExpression instanceof Function) {
					Function sumFunction = (Function) selectExpression;
					Expression sumInnerExpression = (Expression) sumFunction.getParameters().getFirst();

					SumExpressionEvaluator sumExpressionEvaluator = new SumExpressionEvaluator(tuple);
					sumInnerExpression.accept(sumExpressionEvaluator);
					Integer output = sumExpressionEvaluator.getOutputStack().pop();

					if (firstTupleIteration) {
						outputTuple.getColumns().add(sumFunction.toString());
						outputTuple.getLookup().putIfAbsent(sumFunction.toString(), output);
						continue;
					}
					// add onto the previous values for summing up
					Integer prevOutput = outputTuple.getLookup().get(sumFunction.toString());
					outputTuple.getLookup().put(sumFunction.toString(), prevOutput + output);
				}
			}
		}

		return outputTuple;
	}

	public LinkedHashMap<String, HashMap<String, Integer>> buildGroupByHashMap() {
		Tuple tuple;
		LinkedHashMap<String, HashMap<String, Integer>> groupByLookup = new LinkedHashMap<>();
		while ((tuple = child.getNextTuple()) != null) {

			String keyString = buildKey(tuple);
			groupByLookup.putIfAbsent(keyString, new HashMap<>());

			for (Expression selectExpression : selectExpressionList) {

				if (selectExpression instanceof Column) {
					Column column = (Column) selectExpression;
					Integer value = tuple.getLookup().get(column.toString());
					groupByLookup.get(keyString).put(column.toString(), value);
				}
				// sum function
				if (selectExpression instanceof Function) {
					Function sumFunction = (Function) selectExpression;
					Expression sumInnerExpression = (Expression) sumFunction.getParameters().getFirst();
					SumExpressionEvaluator sumExpressionEvaluator = new SumExpressionEvaluator(tuple);
					sumInnerExpression.accept(sumExpressionEvaluator);
					Integer output = sumExpressionEvaluator.getOutputStack().pop();
					Integer prevOutput = groupByLookup.get(keyString).get(sumFunction.toString());

					// this is the first tuple Itera
					if (prevOutput == null) {
						groupByLookup.get(keyString).putIfAbsent(sumFunction.toString(), output);
						continue;
					}

					groupByLookup.get(keyString).put(sumFunction.toString(), prevOutput + output);
				}

			}
		}
		return groupByLookup;
	}

	public List<Tuple> buildOutputTuples() {
		List<Tuple> tupleList = new ArrayList<>();
		for (Entry<String, HashMap<String, Integer>> entry : groupByHashMap.entrySet()) {
			List<String> columns = new ArrayList<>();
			HashMap<String, Integer> lookup = new HashMap<>();
			for (Expression selectExpression : selectExpressionList) {

				HashMap<String, Integer> value = entry.getValue();
				if (selectExpression instanceof Column) {
					Column column = (Column) selectExpression;
					columns.add(column.toString());
					Integer columnValue = value.get(column.toString());
					lookup.put(column.toString(), columnValue);
					continue;
				}

				if (selectExpression instanceof Function) {
					Function sumFunction = (Function) selectExpression;
					Integer sumValue = value.get(sumFunction.toString());
					columns.add(sumFunction.toString());
					lookup.put(sumFunction.toString(), sumValue);
				}

			}
			Tuple outputTuple = new Tuple(columns, lookup);
			tupleList.add(outputTuple);
		}
		return tupleList;
	}

	public void initialize() {
		List<Tuple> tupleList = new ArrayList<>();

		// no group by
		if (groupByExpressionList == null) {
			tupleList.add(computeSumOfTuples());
			this.outputTuples = tupleList;
			this.outputTuplesIterator = this.outputTuples.iterator();
			return;
		}

		// with group by
		this.groupByHashMap = buildGroupByHashMap();
		tupleList = buildOutputTuples();
		this.outputTuples = tupleList;
		this.outputTuplesIterator = this.outputTuples.iterator();

	}

	/**
	 * 
	 * @param tuple
	 * @return String key
	 * @implNote Key is equals to the column int value, where the column is part of
	 *           a group by clause. Key should have the same length as the number of
	 *           columns in the group by clause. "1,2" <=> GROUP BY S.A, S.B
	 */
	public String buildKey(Tuple tuple) {
		StringBuilder key = new StringBuilder();
		for (int i = 0; i < groupByExpressionList.size(); i++) {
			Expression groupByExpression = groupByExpressionList.get(i);
			Column column = (Column) groupByExpression;
			Integer columnValue = tuple.getLookup().get(column.toString());
			key.append(columnValue);

			// if not last column to iterate, separate the key string with comma.
			if (i != groupByExpressionList.size() - 1) {
				key.append(",");
			}
		}
		return key.toString();
	}

	public void setChild(Operator child) {
		this.child = child;
	}

	@Override
	public Tuple getNextTuple() {
		if (outputTuplesIterator.hasNext()) {
			return outputTuplesIterator.next();
		}
		return null;
	}

	@Override
	public void reset() {
		this.outputTuplesIterator = outputTuples.iterator();
	}

}
