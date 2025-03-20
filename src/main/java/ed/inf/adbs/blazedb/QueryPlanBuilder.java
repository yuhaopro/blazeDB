package ed.inf.adbs.blazedb;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import ed.inf.adbs.blazedb.operator.DuplicateEliminationOperator;
import ed.inf.adbs.blazedb.operator.JoinOperator;
import ed.inf.adbs.blazedb.operator.Operator;
import ed.inf.adbs.blazedb.operator.ProjectOperator;
import ed.inf.adbs.blazedb.operator.ScanOperator;
import ed.inf.adbs.blazedb.operator.SelectOperator;
import ed.inf.adbs.blazedb.operator.SortOperator;
import ed.inf.adbs.blazedb.operator.SumOperator;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
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
    private boolean isAllColumns = false;
    private boolean isThereWhereExpressions = false;
    private boolean isThereGroupBy = false;
    private boolean isThereSumFunction = false;
    private boolean isThereOrderBy = false;
    private List<String> tableOrder = new ArrayList<>();
    private List<String> columnOrder = new ArrayList<>();
    private HashMap<String, HashSet<String>> projectedColumnLookup = new HashMap<>();
    private HashMap<String, List<Expression>> singleExpressions = new HashMap<>();
    private HashMap<String, List<Expression>> joinExpressions = new HashMap<>();
    private List<String> orderBy = new ArrayList<>();
    private GroupByElement groupByElement;
    private ExpressionList<Expression> selectExpressionList;

    public QueryPlanBuilder(Select query) {

        // FROM
        addTablesFromQuery(query);
        initializeHashmaps(tableOrder);

        // SELECT, PROJECT
        initializeSelectExpressionList(query);
        addUniqueColumnsFromSelect(query);

        // DISTINCT
        initializeIsDistinct(query);

        // WHERE, PROJECT
        processWhereExpression(query);
        // ORDER BY
        initializeOrderBy(query);

        // GROUP BY
        initializeGroupByElement(query);

    }

    public Operator build() throws FileNotFoundException {

        // single table
        if (tableOrder.size() == 1) {
            return buildQueryPlanForSingleTable();
        } else {
            return buildQueryPlanForJoinTables();
        }
    }


    private Operator buildQueryPlanForJoinTables() {

        Operator left = null;
        Operator right = null;
        String leftTableName = tableOrder.get(0);
        String rightTableName = tableOrder.get(1);

        ScanOperator leftScan = new ScanOperator(leftTableName);
        left = leftScan;
        ScanOperator rightScan = new ScanOperator(rightTableName);
        right = rightScan;

        // check select for left and right
        if (isThereWhereExpressions) {
            List<Expression> leftExpressions = this.singleExpressions.get(leftTableName);
            if (leftExpressions != null && !leftExpressions.isEmpty()) {
                left = createSelectOperator(left, leftTableName, leftExpressions);             
              
            }

            List<Expression> rightExpressions = this.singleExpressions.get(rightTableName);
            if (rightExpressions != null && !rightExpressions.isEmpty()) {
                right = createSelectOperator(right, rightTableName, rightExpressions);             
            }
        }

        // early projection to reduce size of tuples.
        if (!isAllColumns) {
            HashSet<String> leftColumns = projectedColumnLookup.get(leftTableName);
            if (leftColumns != null && !leftColumns.isEmpty()) {
                left = createProjectOperator(leftColumns, left);
            }

            HashSet<String> rightColumns = projectedColumnLookup.get(rightTableName);
            if (rightColumns != null && !rightColumns.isEmpty()) {
                right = createProjectOperator(rightColumns, right);

            }
        }

        List<String> leftTableNameList = new ArrayList<>();
        leftTableNameList.add(leftTableName);
        left = createJoinOperator(leftTableNameList, rightTableName, left, right);
        leftTableNameList.add(rightTableName);

        for (int i = 2; i < tableOrder.size(); i++) {
            rightTableName = tableOrder.get(i);
            ScanOperator joinRightScan = new ScanOperator(rightTableName);
            
            List<Expression> joinRightExpressions = this.singleExpressions.get(rightTableName);
            if (joinRightExpressions != null && !joinRightExpressions.isEmpty()) {
                right = createSelectOperator(joinRightScan, rightTableName, joinRightExpressions);             
            }

            HashSet<String> joinRightColumns = projectedColumnLookup.get(rightTableName);
            if (joinRightColumns != null && !joinRightColumns.isEmpty()) {
                right = createProjectOperator(joinRightColumns, right);
            }
            left = createJoinOperator(leftTableNameList, rightTableName, left, right);
            leftTableNameList.add(rightTableName);
        }

        // group by
        if (isThereGroupBy || isThereSumFunction) {
            SumOperator sumOperator = new SumOperator(groupByElement, selectExpressionList);
            sumOperator.setChild(left);
            sumOperator.initialize();
            left = sumOperator;            
        }


        // order by and distinct
        if (isThereOrderBy) {
            SortOperator sortOperator = new SortOperator(orderBy, isQueryDistinct);
            sortOperator.setChild(left);
            sortOperator.initialize();
            left = sortOperator;
        
        // just distinct
        } else if (isQueryDistinct) {
            DuplicateEliminationOperator duplicateEliminationOperator = new DuplicateEliminationOperator();
            duplicateEliminationOperator.setChild(left);
            left = duplicateEliminationOperator;
        }

        if (!isAllColumns) {
            // List<String> columns = projectedColumnLookup.get(table).stream().toList();

            ProjectOperator projectOperator = new ProjectOperator(columnOrder);
            projectOperator.setChild(left);
            left = projectOperator;
        }


        return left;
    }

    public Operator createProjectOperator(HashSet<String> columns, Operator operator) {
        List<String> columnList = columns.stream().toList();
        ProjectOperator projectOperator = new ProjectOperator(columnList);
        projectOperator.setChild(operator);
        return projectOperator;
    }

    public Operator createJoinOperator(List<String> leftTableNames, String rightTableName, Operator left, Operator right) {
        // create the left join expressions assuming left could already be a joined tuple, which means I need to check multiple tables
        List<Expression> combinedLeftJoinExpressions = new ArrayList<>();
        for (String leftTableName : leftTableNames) {
            List<Expression> leftJoinExpressions = joinExpressions.get(leftTableName);
            combinedLeftJoinExpressions.addAll(leftJoinExpressions);
        }

        List<Expression> rightJoinExpressions = joinExpressions.get(rightTableName);
        List<Expression> commonJoinExpressions = new ArrayList<>(rightJoinExpressions);

        // get the common expression
        commonJoinExpressions.retainAll(combinedLeftJoinExpressions);
        Expression joinExpression = combineListOfExpressions(commonJoinExpressions);
        JoinOperator joinOperator = new JoinOperator(joinExpression);
        joinOperator.setLeftChild(left);
        joinOperator.setRightChild(right);
        return joinOperator;
    }

    public static Expression combineListOfExpressions(List<Expression> expressions) {
        if (expressions.size() == 1) return expressions.getFirst();

        Expression firstExpression = expressions.get(0);
        Expression secondExpression = expressions.get(1);
        AndExpression andExpression = new AndExpression(firstExpression, secondExpression);
        for (int i = 2; i < expressions.size(); i++) {
            andExpression = new AndExpression(andExpression, expressions.get(i));
        }

        return andExpression;
    }

    private Operator buildQueryPlanForSingleTable() {
        Operator root = null;

        String tableName = tableOrder.get(0);
        // table
        ScanOperator scanOperator = new ScanOperator(tableName);
        root = scanOperator;

        // selection
        if (isThereWhereExpressions) {
            List<Expression> expressions = this.singleExpressions.get(tableName);
            if (expressions != null && !expressions.isEmpty()) {
                root = createSelectOperator(root, tableName, expressions);        
            }

        }

        // projection first to reduce columns, if all columns required, then don't
        // create projection.
        if (!isAllColumns) {
            // List<String> columns = projectedColumnLookup.get(table).stream().toList();

            ProjectOperator projectOperator = new ProjectOperator(columnOrder);
            projectOperator.setChild(root);
            root = projectOperator;
        }

        // group by
        if (isThereGroupBy || isThereSumFunction) {
            SumOperator sumOperator = new SumOperator(groupByElement, selectExpressionList);
            sumOperator.setChild(root);
            sumOperator.initialize();
            root = sumOperator;            
        }


        // order by and distinct
        if (isThereOrderBy) {
            SortOperator sortOperator = new SortOperator(orderBy, isQueryDistinct);
            sortOperator.setChild(root);
            sortOperator.initialize();
            root = sortOperator;
        
        // just distinct
        } else if (isQueryDistinct) {
            DuplicateEliminationOperator duplicateEliminationOperator = new DuplicateEliminationOperator();
            duplicateEliminationOperator.setChild(root);
            root = duplicateEliminationOperator;
        }

        return root;
    }

    public Operator createSelectOperator(Operator root, String tableName, List<Expression> expressions) {
        Expression combinedExpression = combineListOfExpressions(expressions);
        SelectOperator selectOperator = new SelectOperator(tableName, combinedExpression);
        selectOperator.setChild(root);
        return selectOperator;
    }

    public void initializeIsDistinct(Select query) {
        if (query.getPlainSelect().getDistinct() != null)
            this.isQueryDistinct = true;
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
        List<OrderByElement> orderByElements = query.getPlainSelect().getOrderByElements();
        if (orderByElements == null)
            return;
        this.orderBy = orderByElements.stream().map(OrderByElement::toString).collect(Collectors.toList());
        isThereOrderBy = true;
    }

    public void initializeGroupByElement(Select query) {
        GroupByElement groupByElement = query.getPlainSelect().getGroupBy();
        if (this.groupByElement == null) return;
        this.groupByElement = groupByElement;
        isThereGroupBy = true;
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
                this.isAllColumns = true;
                break;
            }

            if (selectExpression instanceof Column) {
                Column column = (Column) selectExpression;
                String tableName = column.getTable().getName();
                projectedColumns.putIfAbsent(tableName, new HashSet<>());
                projectedColumns.get(tableName).add(column.toString());
                columnOrder.add(column.toString());
                continue;
            }

            if (selectExpression instanceof Function) {
                this.isThereSumFunction = true;
                Function sumFunction = (Function) selectExpression;
                columnOrder.add(sumFunction.toString());
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
        if (joinTables != null) {
            for (Join join : joinTables) {
                tableList.add(join.toString());
            }
        }

        this.tableOrder = tableList;
    }

    public void processWhereExpression(Select query) {
        Expression whereExpression = query.getPlainSelect().getWhere();
        if (whereExpression == null)
            return;
        List<Column> extractedColumns = getUniqueColumnsFromWhereExpression(whereExpression);
        addExtractedColumnsToProjectedLookup(extractedColumns);

        SplitExpressionDeparser splitExpressionDeparser = new SplitExpressionDeparser();
        whereExpression.accept(splitExpressionDeparser);
        this.singleExpressions = splitExpressionDeparser.getSingleExpressions();
        this.joinExpressions = splitExpressionDeparser.getJoinExpressions();
        this.isThereWhereExpressions = true;
    }


    public void addExtractedColumnsToProjectedLookup(List<Column> extractedColumns) {
        for (Column column : extractedColumns) {
            String tableName = column.getTable().getName();
            projectedColumnLookup.putIfAbsent(tableName, new HashSet<>());
            projectedColumnLookup.get(tableName).add(column.toString());
        }
    }

    public List<Column> getUniqueColumnsFromWhereExpression(Expression whereExpression) {
        ExtractColumnDeparser extractColumnDeparser = new ExtractColumnDeparser();
        whereExpression.accept(extractColumnDeparser);
        return extractColumnDeparser.getExtractedColumns();
    }

}
