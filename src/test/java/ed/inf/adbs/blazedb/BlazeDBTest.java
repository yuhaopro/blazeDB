package ed.inf.adbs.blazedb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;

import ed.inf.adbs.blazedb.entity.Tuple;
import ed.inf.adbs.blazedb.operator.Operator;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.Distinct;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.GroupByElement;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;

/**
 * Unit tests for the BlazeDB system. These tests verify the functionality of
 * various components of the database system, including SQL parsing, query plan
 * construction, and the execution of sample queries.
 */
public class BlazeDBTest {

	/**
	 * Basic test to verify that the system returns true for simple assertions. This
	 * is a basic test used to confirm the testing framework is working correctly.
	 */
	@Test
	public void shouldAnswerWithTrue() {
		assertTrue(true);
	}

	/**
	 * Integration test for executing sample queries and comparing their results
	 * with expected outputs. It parses sample queries, executes them, and compares
	 * the resulting tuples with expected values from CSV files.
	 *
	 * @throws JSQLParserException   if there is an error parsing the SQL query
	 * @throws FileNotFoundException if the expected output file cannot be found
	 */
	@Test
	public void sampleQueries() throws JSQLParserException, FileNotFoundException {
		String queryDirectory = "samples/input/query{0}.sql";
		String expectedOutputDirectory = "samples/expected_output/query{0}.csv";
		String databaseDirectory = "samples/db";

		DatabaseCatalog.getInstance().initialize(databaseDirectory);

		for (int i = 1; i < 12; i++) {
			String filename = MessageFormat.format(queryDirectory, i);
			String expectedOutputPath = MessageFormat.format(expectedOutputDirectory, i);
			Statement statement = BlazeDB.parseSQLFromFilename(filename);
			Select select = (Select) statement;
			System.out.println("Statement: " + select.toString());
			Operator root = new QueryPlanBuilder(select).build();

			List<List<String>> expectedValueList = new ArrayList<>();
			List<List<String>> actualValueList = new ArrayList<>();
			CSVParser csvParser = new CSVParser(expectedOutputPath);
			while (csvParser.hasNext()) {
				List<String> expectedValues = csvParser.next();
				expectedValueList.add(expectedValues);
			}
			Tuple tuple;
			while ((tuple = root.getNextTuple()) != null) {
				List<String> actualValues = new ArrayList<>();
				for (String column : tuple.getColumns()) {
					Integer value = tuple.getLookup().get(column);
					actualValues.add(value.toString());
				}
				actualValueList.add(actualValues);
			}

			Collections.sort(actualValueList, listComparator());
			Collections.sort(expectedValueList, listComparator());
			System.out.println("Actual Values: " + actualValueList);
			System.out.println("Expected Values: " + expectedValueList);
			for (int j = 0; j < actualValueList.size(); j++) {
				assertEquals(expectedValueList.get(0), actualValueList.get(0));
			}
		}
	}

	/**
	 * Comparator for sorting lists of strings by their integer sum. This is used
	 * for sorting the actual and expected result lists for comparison.
	 * 
	 * @return a comparator for comparing lists of strings
	 */
	public static Comparator<List<String>> listComparator() {
		return Comparator.comparingInt(list -> list.stream().map(Integer::parseInt).mapToInt(Integer::intValue).sum());
	}

	/**
	 * Test for verifying the correct identification of tables in a SQL query. This
	 * test parses a SQL query containing multiple tables and checks if the correct
	 * table names are identified.
	 * 
	 * @throws JSQLParserException if there is an error parsing the SQL query
	 */
	@Test
	public void testFindTables() throws JSQLParserException {
		Statement statement = CCJSqlParserUtil.parse("SELECT * FROM A,B,C,D,E,F,G");
		Select select = (Select) statement;
		FromItem from = select.getPlainSelect().getFromItem();
		List<Join> joins = select.getPlainSelect().getJoins();
		assertEquals("A", from.toString());
		assertEquals("B", joins.get(0).toString());
		assertEquals("C", joins.get(1).toString());
		assertEquals("D", joins.get(2).toString());
		assertEquals("E", joins.get(3).toString());
		assertEquals("F", joins.get(4).toString());
		assertEquals("G", joins.get(5).toString());
	}

	/**
	 * Test for verifying the handling of SQL queries with the wildcard `*` symbol
	 * in the SELECT clause. This test checks if all columns are selected when
	 * `SELECT *` is used.
	 * 
	 * @throws JSQLParserException if there is an error parsing the SQL query
	 */
	@Test
	public void testSelectItemsWithAllColumns() throws JSQLParserException {
		Statement statement = CCJSqlParserUtil.parse("SELECT * FROM A");
		Select select = (Select) statement;
		List<SelectItem<?>> selectItems = select.getPlainSelect().getSelectItems();
		for (SelectItem<?> selectItem : selectItems) {
			assertTrue(selectItem.getExpression() instanceof AllColumns);
		}
	}

	/**
	 * Test for verifying the correct handling of SQL queries with a single column
	 * in the SELECT clause. This test checks if a column expression is correctly
	 * identified in a query like `SELECT A.B FROM A`.
	 * 
	 * @throws JSQLParserException if there is an error parsing the SQL query
	 */
	@Test
	public void testSelectItemsWithSingleColumn() throws JSQLParserException {
		Statement statement = CCJSqlParserUtil.parse("SELECT A.B FROM A");
		Select select = (Select) statement;
		List<SelectItem<?>> selectItems = select.getPlainSelect().getSelectItems();
		for (SelectItem<?> selectItem : selectItems) {
			assertTrue(selectItem.getExpression() instanceof Column);
		}
	}

	/**
	 * Test for verifying the handling of SQL queries with aggregate functions
	 * (e.g., SUM). This test checks if a multiplication operation inside an
	 * aggregate function is handled correctly.
	 * 
	 * @throws JSQLParserException if there is an error parsing the SQL query
	 */
	@Test
	public void testSelectItemsWithFunction() throws JSQLParserException {
		Statement statement = CCJSqlParserUtil.parse("SELECT SUM(A.B*A.B) FROM A");
		Select select = (Select) statement;
		List<SelectItem<?>> selectItems = select.getPlainSelect().getSelectItems();
		for (SelectItem<?> selectItem : selectItems) {
			assertTrue(selectItem.getExpression() instanceof Function);
			Function sumFunction = (Function) selectItem.getExpression();
			assertTrue(sumFunction.getParameters().get(0) instanceof Multiplication);
		}
	}

	/**
	 * Test for verifying the handling of the DISTINCT keyword in SQL queries. This
	 * test checks if the DISTINCT clause is correctly parsed and identified in a
	 * query.
	 * 
	 * @throws JSQLParserException if there is an error parsing the SQL query
	 */
	@Test
	public void testSelectItemsWithDistinct() throws JSQLParserException {
		Statement statement = CCJSqlParserUtil.parse("SELECT * FROM A");
		Select select = (Select) statement;
		Distinct distinct = select.getPlainSelect().getDistinct();
		assertNull(distinct);
	}

	/**
	 * Test for verifying the handling of the GROUP BY clause in SQL queries. This
	 * test checks if the correct grouping expressions are identified in a query
	 * with GROUP BY.
	 * 
	 * @throws JSQLParserException if there is an error parsing the SQL query
	 */
	@Test
	public void testGroupBy() throws JSQLParserException {
		Statement statement = CCJSqlParserUtil
				.parse("SELECT Enrolled.E, SUM(Enrolled.H * Enrolled.H) FROM Enrolled GROUP BY Enrolled.E, Enrolled.F");
		Select select = (Select) statement;
		GroupByElement groupByElement = select.getPlainSelect().getGroupBy();
		ExpressionList<Expression> expressions = groupByElement.getGroupByExpressionList();
		assertEquals("Enrolled.E", expressions.getFirst().toString());
		assertEquals("Enrolled.F", expressions.getLast().toString());
	}
}
