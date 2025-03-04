package ed.inf.adbs.blazedb;

import ed.inf.adbs.blazedb.operator.Operator;
import ed.inf.adbs.blazedb.operator.ScanOperator;
import ed.inf.adbs.blazedb.operator.SelectOperator;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class QueryPlanBuilder {
    private Select query;
    private List<QueryPlan> queryPlans = new ArrayList<QueryPlan>();


    public QueryPlanBuilder(Select query) {
        this.query = query;
    }

    public List<QueryPlan> build() throws FileNotFoundException {

//      List<String> tables = getTablesFromQuery(query);
        // root of query tree
        Operator root;
        // List<SelectItem<?>> selectItems = query.getPlainSelect().getSelectItems();
        Expression expression = query.getPlainSelect().getWhere();
        SelectOperator selectOperator = new SelectOperator(expression);
        String table = query.getPlainSelect().getFromItem().toString();
        ScanOperator scanOperator = new ScanOperator(table);
        selectOperator.setChild(scanOperator);
        root = selectOperator;
        QueryPlan queryPlan = new QueryPlan(root);
        queryPlans.add(queryPlan);

        return queryPlans;
    }

    public List<QueryPlan> getQueryPlans() {
        return queryPlans;
    }

    public List<String> getTablesFromQuery(Select query) {
        List<String> tables = new ArrayList<String>();
        List<Join> joinTables = query.getPlainSelect().getJoins();
        // get first from table
        tables.add(query.getPlainSelect().getFromItem().toString());
        for (Join join : joinTables) {
            tables.add(join.getFromItem().toString());
        }
        return tables;
    }

}
