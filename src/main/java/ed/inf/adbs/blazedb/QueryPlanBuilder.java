package ed.inf.adbs.blazedb;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import ed.inf.adbs.blazedb.operator.JoinOperator;
import ed.inf.adbs.blazedb.operator.Operator;
import ed.inf.adbs.blazedb.operator.ProjectOperator;
import ed.inf.adbs.blazedb.operator.ScanOperator;
import ed.inf.adbs.blazedb.operator.SelectOperator;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;

public class QueryPlanBuilder {
    private static final Logger logger = Logger.getLogger(QueryPlanBuilder.class.getName());
    private HashMap<String, ScanOperator> scanOperators = new LinkedHashMap<>();
    private List<String> tableOrder = new ArrayList<>();
    private HashMap<String, HashSet<String>> projectedColumnLookup = new HashMap<>();
    private HashMap<String, Expression> singleExpressions = new HashMap<>();
    private HashMap<String, List<Expression>> joinExpressions = new HashMap<>();
    private List<String> orderBy = new ArrayList<>();

    public QueryPlanBuilder(Select query) {

        // FROM
        this.tableOrder = getTablesFromQuery(query);

        // initialize projectedColumnLookup
        for (String tableName : tableOrder) {
            projectedColumnLookup.putIfAbsent(tableName, new HashSet<>());
        }

        // SELECT
        this.projectedColumnLookup = addUniqueColumnsFromSelect(query);

        // WHERE 


        // ORDER BY
        this.orderBy = query.getPlainSelect().getOrderByElements().stream().map(OrderByElement::toString).collect(Collectors.toList());



    }

    public QueryPlan build() throws FileNotFoundException {

        // Gathering the inputs
        ScanOperator scanOperator = new ScanOperator(null);
        return new QueryPlan(scanOperator);

        // case for only 1 table (no joins)
        // if (tableOrder.size() == 1) {
        //     ScanOperator scanOperator = scanOperators.get(tableOrder.get(0));
        //     SelectOperator selectOperator = selectOperators.get(tableOrder.get(0));

        //     if (selectOperator != null) {
        //         selectOperator.setChild(scanOperator);
        //         if (projectOperator != null) {
        //             projectOperator.setChild(selectOperator);
        //             root = projectOperator;
        //         } else {
        //             root = selectOperator;
        //         }
        //     } else {
        //         if (projectOperator != null) {
        //             projectOperator.setChild(scanOperator);
        //             root = projectOperator;
        //         } else {
        //             root = scanOperator;
        //         }
        //     }
        //     return new QueryPlan(root);
        // }
        // return null;
    }

    public HashMap<String, HashSet<String>> addUniqueColumnsFromSelect(Select query) {
        // SELECT
        HashMap<String, HashSet<String>> projectedColumns = new HashMap<>();
        List<SelectItem<?>> selectItems = query.getPlainSelect().getSelectItems();
        for (SelectItem<?> selectItem : selectItems) {

            Expression selectExpression = selectItem.getExpression();
            // no projection required
            if (selectExpression instanceof AllColumns) {
                break;
            }

            if (selectExpression instanceof Column) {
                Column column = (Column) selectExpression;
                String tableName = column.getTable().getName();
                projectedColumns.get(tableName).add(column.toString());
                continue;
            }

            if (selectExpression instanceof Function) {
                Function sumFunction = (Function) selectExpression;

                // should only have 1 expression
                ExpressionList<Expression> sumExpressionList = sumFunction.getParameters();
                if (sumExpressionList.size() != 1) {
                    logger.warning("sumExpressionList does not have size of 1!");
                    continue;
                }

                Expression sumExpression = sumExpressionList.getFirst();
                if (sumExpression instanceof Multiplication) {
                    // extract all the columns
                    ExtractColumnDeparser extractColumnDeparser = new ExtractColumnDeparser();
                    sumExpression.accept(extractColumnDeparser);
                    List<Column> extractedColumns = extractColumnDeparser.getExtractedColumns();
                    addExtractedColumnsToProjectedLookup(extractedColumns);
                }
            }

        }
        return projectedColumns;
    }

    public List<String> getTablesFromQuery(Select query) {
        List<String> tableList = new ArrayList<>();
        FromItem firstTable = query.getPlainSelect().getFromItem();
        tableList.add(firstTable.toString());
        
        List<Join> joinTables = query.getPlainSelect().getJoins();
        for (Join join : joinTables) {
            tableList.add(join.toString());
        }
        return tableList;
    }

    public void processWhereExpressions(Select query) {
        Expression expression = query.getPlainSelect().getWhere();

        List<Column> extractedColumns = getUniqueColumnsFromWhereExpression(expression);
        addExtractedColumnsToProjectedLookup(extractedColumns);


    }

    public void 

    public void addExtractedColumnsToProjectedLookup(List<Column> extractedColumns) {
        for (Column column : extractedColumns) {
            String tableName = column.getTable().getName();
            projectedColumnLookup.get(tableName).add(column.toString());
        }
    }

    public List<Column> getUniqueColumnsFromWhereExpression(Expression whereExpression) {
        ExtractColumnDeparser extractColumnDeparser = new ExtractColumnDeparser();
        whereExpression.accept(extractColumnDeparser);
        return extractColumnDeparser.getExtractedColumns();
    }

}
