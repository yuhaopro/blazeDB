package ed.inf.adbs.blazedb;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import ed.inf.adbs.blazedb.operator.DuplicateEliminationOperator;
import ed.inf.adbs.blazedb.operator.Operator;
import ed.inf.adbs.blazedb.operator.ProjectOperator;
import ed.inf.adbs.blazedb.operator.ScanOperator;
import ed.inf.adbs.blazedb.operator.SelectOperator;
import ed.inf.adbs.blazedb.operator.SortOperator;
import ed.inf.adbs.blazedb.operator.SumOperator;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.GroupByElement;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;

public class QueryPlanBuilder {
    private static final Logger logger = Logger.getLogger(QueryPlanBuilder.class.getName());
    private boolean isQueryDistinct = false;
    private List<String> tableOrder = new ArrayList<>();
    private HashMap<String, HashSet<String>> projectedColumnLookup = new HashMap<>();
    private HashMap<String, List<Expression>> singleExpressions = new HashMap<>();
    private HashMap<String, List<Expression>> joinExpressions = new HashMap<>();
    private List<String> orderBy = new ArrayList<>();
    private GroupByElement groupByElement;
    private ExpressionList<Expression> selectExpressionList;

    public QueryPlanBuilder(Select query) {
        query.getPlainSelect().getDistinct();
        // FROM
        addTablesFromQuery(query);
        initializeHashmaps(tableOrder);

        // SELECT
        initializeSelectExpressionList(query);
        addUniqueColumnsFromSelect(query);

        // DISTINCT
        initializeIsDistinct(query);

        // WHERE 
        addUniqueColumnsFromWhereClause(query);
        initializeSingleAndJoinExpressions(query);
        // ORDER BY 
        initializeOrderBy(query);

        // GROUP BY
        initializeGroupByElement(query);

        


    }

    public QueryPlan build() throws FileNotFoundException {

        // single table
        if (tableOrder.size() == 1) {
            return buildQueryPlanForSingleTable();
        } else {
            return buildQueryPlanForJoinTables();
        }
    }

    private QueryPlan buildQueryPlanForJoinTables() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'buildQueryPlanForJoinTables'");
    }

    private QueryPlan buildQueryPlanForSingleTable() {
        Operator root = null;

        String table = tableOrder.get(0);
        // table
        ScanOperator scanOperator = new ScanOperator(table);
        root = scanOperator;

        // selection
        SelectOperator selectOperator = new SelectOperator(table, this.singleExpressions.get(table));
        selectOperator.setChild(root);
        root = selectOperator;
        


        // projection first to reduce columns
        List<String> columns = projectedColumnLookup.get(table).stream().toList();
        ProjectOperator projectOperator = new ProjectOperator(columns);
        projectOperator.setChild(root);
        root = projectOperator;
        
        // group by
        SumOperator sumOperator = new SumOperator(groupByElement, selectExpressionList);
        sumOperator.setChild(root);
        root = sumOperator;
        
        // order by and distinct
        if (!orderBy.isEmpty()) {
            SortOperator sortOperator = new SortOperator(orderBy ,isQueryDistinct);
            sortOperator.setChild(root);
            root = sortOperator;
        } else if (isQueryDistinct) {
            DuplicateEliminationOperator duplicateEliminationOperator = new DuplicateEliminationOperator();
            duplicateEliminationOperator.setChild(root);
            root = duplicateEliminationOperator;
        }
        return new QueryPlan(root);
    }

    public void initializeIsDistinct(Select query) {
        this.isQueryDistinct = !query.getPlainSelect().getDistinct().equals(null);
    }

    public void initializeHashmaps(List<String> tableOrder) {
        // initialize hashmaps
        for (String tableName : tableOrder) {
            projectedColumnLookup.putIfAbsent(tableName, new HashSet<>());
            singleExpressions.putIfAbsent(tableName, new ArrayList<>());
            joinExpressions.putIfAbsent(tableName, new ArrayList<>());
        }
    }

    public void initializeOrderBy(Select query) {
        this.orderBy = query.getPlainSelect().getOrderByElements().stream().map(OrderByElement::toString).collect(Collectors.toList());

    }

    public void initializeGroupByElement(Select query) {
        this.groupByElement = query.getPlainSelect().getGroupBy();
    }

    public void initializeSelectExpressionList(Select query) {
        List<SelectItem<?>> selectItems = query.getPlainSelect().getSelectItems();
        List<Expression> selectExpressions = selectItems.stream().map(SelectItem::getExpression)
                .collect(Collectors.toList());
        this.selectExpressionList = new ExpressionList<>(selectExpressions);
    }

    public void addUniqueColumnsFromSelect(Select query) {
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
        this.projectedColumnLookup = projectedColumns;
    }

    public void addTablesFromQuery(Select query) {
        List<String> tableList = new ArrayList<>();
        FromItem firstTable = query.getPlainSelect().getFromItem();
        tableList.add(firstTable.toString());
        
        List<Join> joinTables = query.getPlainSelect().getJoins();
        for (Join join : joinTables) {
            tableList.add(join.toString());
        }
        this.tableOrder = tableList;
    }

    public void addUniqueColumnsFromWhereClause(Select query) {
        Expression expression = query.getPlainSelect().getWhere();
        List<Column> extractedColumns = getUniqueColumnsFromWhereExpression(expression);
        addExtractedColumnsToProjectedLookup(extractedColumns);
    }

    public void initializeSingleAndJoinExpressions(Select query) {
        Expression whereExpression = query.getPlainSelect().getWhere();
        SplitExpressionDeparser splitExpressionDeparser = new SplitExpressionDeparser();
        whereExpression.accept(splitExpressionDeparser);
        this.singleExpressions = splitExpressionDeparser.getSingleExpressions();
        this.joinExpressions = splitExpressionDeparser.getJoinExpressions();
    }

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
