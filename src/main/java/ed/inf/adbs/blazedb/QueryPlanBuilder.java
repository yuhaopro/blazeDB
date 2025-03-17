package ed.inf.adbs.blazedb;

import ed.inf.adbs.blazedb.operator.*;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

public class QueryPlanBuilder {
    private HashMap<String, ScanOperator> scanOperators = new LinkedHashMap<>();
    private List<String> tableOrder = new ArrayList<>();
    private ProjectOperator projectOperator;
    private HashMap<String, SelectOperator> selectOperators = new HashMap<>();
    private ExpressionSplitter expressionSplitter;
    // join key format {left_table_name.right_table_name}
    private HashMap<String, JoinOperator> joinOperators = new HashMap<>();

    public QueryPlanBuilder(Select query) {

        // SELECT
        List<SelectItem<?>> selectItems = query.getPlainSelect().getSelectItems();
        List<String> selectItemsInString = new ArrayList<String>();
        for (SelectItem<?> selectItem : selectItems) {
            selectItemsInString.add(selectItem.toString());
        }

        if (!selectItemsInString.contains("*")) {
            projectOperator = new ProjectOperator(selectItemsInString);
        }

        // FROM
        List<String> tables = getTablesFromQuery(query);
        for (String table : tables) {
            try {
                scanOperators.put(table, new ScanOperator(table));
                tableOrder.add(table);
            } catch (FileNotFoundException e) {
                System.err.println(e.getMessage());
            }
        }

        // WHERE
        Expression expression = query.getPlainSelect().getWhere();
        // extract JOIN conditions
        // extract Selection for a single table
        // exit the selectOperator
        if (expression == null) {
            return;
        }

        ExpressionSplitter expressionSplitter = new ExpressionSplitter(expression);
        expressionSplitter.split();

        // get the final expression that doesn't have
        SplitExpression startingExpression = expressionSplitter.getOutputStack().pop();
        if (startingExpression.isJoinExpression()) {
            expressionSplitter.getJoinExpressions().add(startingExpression);
        } else {
            String tableName = startingExpression.getTableName();
            expressionSplitter.getSingleExpressions().computeIfAbsent(tableName, k -> new ArrayList<>()).add(startingExpression);
        }

        // create select operators for single tables
        for (Map.Entry<String, List<SplitExpression>> entry : expressionSplitter.getSingleExpressions().entrySet()) {
            String combinedExpressionString = "";
            List<SplitExpression> splitExpressions = entry.getValue();
            if (splitExpressions.isEmpty()) {
                continue;
            }

            SplitExpression firstExpression = splitExpressions.get(0);
            combinedExpressionString += firstExpression.getExpression() + " " + "AND" + " ";

            for (int i = 1; i < splitExpressions.size(); i++) {
                combinedExpressionString += splitExpressions.get(i).getExpression() + " " + "AND";
            }

            try {
                Expression expressionObject = CCJSqlParserUtil.parseCondExpression(combinedExpressionString);
                SelectOperator selectOperator = new SelectOperator(entry.getKey(), expressionObject);
                selectOperators.put(entry.getKey(), selectOperator);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }


        }


        // ORDER BY
        List<String> orderBy = query.getPlainSelect().getOrderByElements().stream().map(OrderByElement::toString).collect(Collectors.toList());



    }

    public QueryPlan build() throws FileNotFoundException {

        // Gathering the inputs
        Operator root;

        // case for only 1 table (no joins)
        if (tableOrder.size() == 1) {
            ScanOperator scanOperator = scanOperators.get(tableOrder.get(0));
            SelectOperator selectOperator = selectOperators.get(tableOrder.get(0));

            if (selectOperator != null) {
                selectOperator.setChild(scanOperator);
                if (projectOperator != null) {
                    projectOperator.setChild(selectOperator);
                    root = projectOperator;
                } else {
                    root = selectOperator;
                }
            } else {
                if (projectOperator != null) {
                    projectOperator.setChild(scanOperator);
                    root = projectOperator;
                } else {
                    root = scanOperator;
                }
            }
            return new QueryPlan(root);
        }

        // query with joins
//        String firstTable =  tableOrder.get(0);
//        String secondTable =  tableOrder.get(1);
//        // create subsequent subtrees
//        int tableOrderIndex = 1;
//        while (tableOrderIndex < tableOrder.size()) {
//
//            tableOrderIndex++;
//        }
//
//
//        return new QueryPlan(root);
        return null;
    }

    public List<String> getTablesFromQuery(Select query) {
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList((Statement) query);
        return tableList;
    }

}
