package ed.inf.adbs.blazedb;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.JSqlParser;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.Distinct;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.util.TablesNamesFinder;

/**
 * Unit tests for BlazeDB.
 */
public class BlazeDBTest {
	
	/**z
	 * Rigorous Test :-)
	 */
	@Test
	public void shouldAnswerWithTrue() {
       
		assertTrue(true);
	}

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

	@Test
    public void testSelectItemsWithAllColumns() throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM A");
        Select select = (Select) statement;
		List<SelectItem<?>> selectItems = select.getPlainSelect().getSelectItems();
		for (SelectItem<?> selectItem : selectItems) {
			assertTrue(selectItem.getExpression() instanceof AllColumns);
		}
	
    }
	@Test
    public void testSelectItemsWithSingleColumn() throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse("SELECT A.B FROM A");
        Select select = (Select) statement;
		List<SelectItem<?>> selectItems = select.getPlainSelect().getSelectItems();
		for (SelectItem<?> selectItem : selectItems) {
			assertTrue(selectItem.getExpression() instanceof Column);
		}
	
    }

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
	@Test
    public void testSelectItemsWithDistinct() throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM A");
        Select select = (Select) statement;
		Distinct distinct = select.getPlainSelect().getDistinct();
		assertNull(distinct);
    }

}
