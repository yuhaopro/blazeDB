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

    /**
     * Constructs a {@code QueryPlanBuilder} from the provided SQL {@code Select}
     * query.
     * 
     * <p>
     * This constructor parses the query and initializes various flags, hash maps,
     * and other properties that help build the execution plan.
     * </p>
     * 
     * @param query the SQL {@code Select} query to build the execution plan for
     */
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
        addUniqueColumnsFromGroupBy();

    }

    /**
     * Builds and returns the query execution plan (operator tree) based on the
     * parsed SQL query.
     * 
     * @return the root operator of the query plan
     * @throws FileNotFoundException if any required data file is not found
     */
    public Operator build() throws FileNotFoundException {

        // single table
        if (tableOrder.size() == 1) {
            return buildQueryPlanForSingleTable();
        } else {
            return buildQueryPlanForJoinTables();
        }
    }

    /**
     * Builds the query execution plan for a query with multiple tables (joins).
     * 
     * @return the root operator of the query plan for multiple tables
     */
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

        // optimize query performance by early projection to reduce size of tuples
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

        // join the previous output with the next join table.
        for (int i = 2; i < tableOrder.size(); i++) {

            // gets Enrolled
            String newRightTableName = tableOrder.get(i);

            // Enrolled Scan Operator
            ScanOperator newJoinRightScan = new ScanOperator(newRightTableName);
            right = newJoinRightScan;

            List<Expression> newJoinRightExpressions = this.singleExpressions.get(newRightTableName);
            if (newJoinRightExpressions != null && !newJoinRightExpressions.isEmpty()) {
                right = createSelectOperator(newJoinRightScan, newRightTableName, newJoinRightExpressions);
            }

            HashSet<String> newJoinRightColumns = projectedColumnLookup.get(newRightTableName);
            if (newJoinRightColumns != null && !newJoinRightColumns.isEmpty()) {
                right = createProjectOperator(newJoinRightColumns, right);
            }
            left = createJoinOperator(leftTableNameList, newRightTableName, left, right);
            leftTableNameList.add(newRightTableName);
        }

        // group by
        if (isThereGroupBy || isThereSumFunction) {
            SumOperator sumOperator = new SumOperator(groupByElement, selectExpressionList);
            sumOperator.setChild(left);
            sumOperator.initialize();
            left = sumOperator;
        }

        if (!isAllColumns) {
            // List<String> columns = projectedColumnLookup.get(table).stream().toList();

            ProjectOperator projectOperator = new ProjectOperator(columnOrder);
            projectOperator.setChild(left);
            left = projectOperator;
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

        return left;
    }

    /**
     * Creates a {@code ProjectOperator} that projects the specified columns on the
     * given operator.
     * 
     * @param columns  the columns to project
     * @param operator the operator to apply the projection on
     * @return a new {@code ProjectOperator}
     */
    public Operator createProjectOperator(HashSet<String> columns, Operator operator) {
        List<String> columnList = columns.stream().toList();
        ProjectOperator projectOperator = new ProjectOperator(columnList);
        projectOperator.setChild(operator);
        return projectOperator;
    }

    /**
     * Creates a {@code JoinOperator} that joins two tables based on the specified
     * join conditions.
     * 
     * @param leftTableNames the list of left table names
     * @param rightTableName the name of the right table
     * @param left           the left operator
     * @param right          the right operator
     * @return a new {@code JoinOperator}
     */
    public Operator createJoinOperator(List<String> leftTableNames, String rightTableName, Operator left,
            Operator right) {
        // create the left join expressions assuming left could already be a joined
        // tuple, which means I need to check multiple tables
        List<Expression> combinedLeftJoinExpressions = new ArrayList<>();
        for (String leftTableName : leftTableNames) {
            List<Expression> leftJoinExpressions = joinExpressions.get(leftTableName);
            if (leftJoinExpressions != null) {
                combinedLeftJoinExpressions.addAll(leftJoinExpressions);
            }
        }

        List<Expression> rightJoinExpressions = joinExpressions.get(rightTableName);
        List<Expression> commonJoinExpressions = new ArrayList<>();
        if (rightJoinExpressions != null) {
            commonJoinExpressions = new ArrayList<>(rightJoinExpressions);
        }

        // get the common expression
        commonJoinExpressions.retainAll(combinedLeftJoinExpressions);
        JoinOperator joinOperator = null;
        // if there is no join expression, it is a cross product, expression always true
        if (commonJoinExpressions == null || commonJoinExpressions.size() == 0) {
            joinOperator = new JoinOperator();

        } else {
            Expression joinExpression = combineListOfExpressions(commonJoinExpressions);
            joinOperator = new JoinOperator(joinExpression);
        }

        joinOperator.setLeftChild(left);
        joinOperator.setRightChild(right);
        return joinOperator;
    }

    /**
     * Combines a list of expressions into a single {@code AndExpression}.
     * 
     * @param expressions the list of expressions to combine
     * @return the combined {@code AndExpression}
     */
    public static Expression combineListOfExpressions(List<Expression> expressions) {
        if (expressions.size() == 1)
            return expressions.getFirst();

        Expression firstExpression = expressions.get(0);
        Expression secondExpression = expressions.get(1);
        AndExpression andExpression = new AndExpression(firstExpression, secondExpression);
        for (int i = 2; i < expressions.size(); i++) {
            andExpression = new AndExpression(andExpression, expressions.get(i));
        }

        return andExpression;
    }

    /**
     * Builds the query execution plan for a query with a single table.
     * 
     * @return the root operator of the query plan for a single table
     */
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
            List<String> columns = projectedColumnLookup.get(tableName).stream().toList();
            ProjectOperator projectOperator = new ProjectOperator(columns);
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

        // before this final projection, for SUM expressions this column needs to be
        // added prior
        if (!isAllColumns) {
            ProjectOperator projectOperator = new ProjectOperator(columnOrder);
            projectOperator.setChild(root);
            root = projectOperator;
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

    /**
     * Creates a {@code SelectOperator} that applies a selection condition on the
     * given operator.
     * 
     * @param root        the root operator to apply the selection on
     * @param tableName   the table name for the selection
     * @param expressions the list of selection expressions (conditions)
     * @return a new {@code SelectOperator}
     */
    public Operator createSelectOperator(Operator root, String tableName, List<Expression> expressions) {
        Expression combinedExpression = combineListOfExpressions(expressions);
        SelectOperator selectOperator = new SelectOperator(tableName, combinedExpression);
        selectOperator.setChild(root);
        return selectOperator;
    }

    /**
     * Initializes the DISTINCT flag based on the SQL query.
     * 
     * @param query the SQL {@code Select} query
     */
    public void initializeIsDistinct(Select query) {
        if (query.getPlainSelect().getDistinct() != null)
            this.isQueryDistinct = true;
    }

    /**
     * Initializes the hash maps that store information about columns and
     * expressions.
     * 
     * @param tableOrder the list of table names in the query
     */
    public void initializeHashmaps(List<String> tableOrder) {
        // initialize hashmaps
        for (String tableName : tableOrder) {
            projectedColumnLookup.putIfAbsent(tableName, new HashSet<>());
            singleExpressions.putIfAbsent(tableName, new ArrayList<>());
            joinExpressions.putIfAbsent(tableName, new ArrayList<>());
        }
    }

    /**
     * Initializes the ORDER BY clause information based on the SQL query.
     * 
     * @param query the SQL {@code Select} query
     */
    public void initializeOrderBy(Select query) {
        List<OrderByElement> orderByElements = query.getPlainSelect().getOrderByElements();
        if (orderByElements == null)
            return;
        this.orderBy = orderByElements.stream().map(OrderByElement::toString).collect(Collectors.toList());
        isThereOrderBy = true;
    }

    /**
     * Initializes the GROUP BY element based on the SQL query.
     * 
     * @param query the SQL {@code Select} query
     */
    public void initializeGroupByElement(Select query) {
        GroupByElement actualGroupByElement = query.getPlainSelect().getGroupBy();
        if (actualGroupByElement == null)
            return;
        this.groupByElement = actualGroupByElement;
        isThereGroupBy = true;
    }

    /**
     * Initializes the SELECT expression list based on the SQL query.
     * 
     * @param query the SQL {@code Select} query
     */
    public void initializeSelectExpressionList(Select query) {
        List<SelectItem<?>> selectItems = query.getPlainSelect().getSelectItems();
        List<Expression> selectExpressions = selectItems.stream().map(SelectItem::getExpression)
                .collect(Collectors.toList());
        this.selectExpressionList = new ExpressionList<>(selectExpressions);
    }

    /**
     * Adds unique columns from the GROUP BY clause to the projected column lookup.
     */
    public void addUniqueColumnsFromGroupBy() {
        if (this.groupByElement == null)
            return;
        ExpressionList<Expression> groupByExpressions = this.groupByElement.getGroupByExpressionList();
        for (Expression groupByExpression : groupByExpressions) {
            if (groupByExpression instanceof Column) {
                Column column = (Column) groupByExpression;
                String tableName = column.getTable().getName();
                projectedColumnLookup.putIfAbsent(tableName, new HashSet<>());
                projectedColumnLookup.get(tableName).add(column.toString());
            }
        }

    }

    /**
     * Adds unique columns from the SELECT clause to the projected column lookup.
     * 
     * @param query the SQL {@code Select} query
     */
    public void addUniqueColumnsFromSelect(Select query) {
        // SELECT
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
                projectedColumnLookup.putIfAbsent(tableName, new HashSet<>());
                projectedColumnLookup.get(tableName).add(column.toString());
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

                if (sumExpression instanceof Multiplication || sumExpression instanceof Column) {
                    // extract all the columns
                    ExtractColumnDeparser extractColumnDeparser = new ExtractColumnDeparser();
                    sumExpression.accept(extractColumnDeparser);
                    List<Column> extractedColumns = extractColumnDeparser.getExtractedColumns();
                    addExtractedColumnsToProjectedLookup(extractedColumns);
                    continue;
                }

            }

        }
    }

    /**
     * Adds tables from the FROM of the query to the table order list.
     * 
     * @param query the SQL {@code Select} query
     */
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

    /**
     * Processes the WHERE expressions from the query and adds them to the list of
     * expressions.
     * 
     * @param query the SQL {@code Select} query
     */
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

    /**
     * Adds columns extracted from the query to the projected column lookup hashmap.
     * 
     * @param extractedColumns the list of columns to be added to projection column
     *                         lookup
     */
    public void addExtractedColumnsToProjectedLookup(List<Column> extractedColumns) {
        for (Column column : extractedColumns) {
            String tableName = column.getTable().getName();
            projectedColumnLookup.putIfAbsent(tableName, new HashSet<>());
            projectedColumnLookup.get(tableName).add(column.toString());
        }
    }

    /**
     * Retrieves the columns found in the WHERE clause.
     * 
     * @param whereExpression takes a WHERE expression from the {@code SELECT} query
     */
    public List<Column> getUniqueColumnsFromWhereExpression(Expression whereExpression) {
        ExtractColumnDeparser extractColumnDeparser = new ExtractColumnDeparser();
        whereExpression.accept(extractColumnDeparser);
        return extractColumnDeparser.getExtractedColumns();
    }

}
