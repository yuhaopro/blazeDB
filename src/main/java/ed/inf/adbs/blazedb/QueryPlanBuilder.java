package ed.inf.adbs.blazedb;

import ed.inf.adbs.blazedb.operator.Operator;
import ed.inf.adbs.blazedb.operator.ProjectOperator;
import ed.inf.adbs.blazedb.operator.ScanOperator;
import ed.inf.adbs.blazedb.operator.SelectOperator;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.util.TablesNamesFinder;

import javax.swing.plaf.nimbus.State;
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


        // projecting the
        Operator root;
         List<SelectItem<?>> selectItems = query.getPlainSelect().getSelectItems();
         List<String> selectItemsInString = new ArrayList<String>();
         for (SelectItem<?> selectItem : selectItems) {
             selectItemsInString.add(selectItem.toString());
         }
        ProjectOperator projectOperator = new ProjectOperator(selectItemsInString);
        Expression expression = query.getPlainSelect().getWhere();
        SelectOperator selectOperator = new SelectOperator(expression);
        String table = query.getPlainSelect().getFromItem().toString();
        ScanOperator scanOperator = new ScanOperator(table);

        // creating the operator tree
        selectOperator.setChild(scanOperator);
        projectOperator.setChild(selectOperator);

        // creating the query plan
        root = projectOperator;
        QueryPlan queryPlan = new QueryPlan(root);
        queryPlans.add(queryPlan);

        return queryPlans;
    }

    public List<QueryPlan> getQueryPlans() {
        return queryPlans;
    }

    public List<String> getTablesFromQuery(Select query) {
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList((Statement) query);
        return tableList;
    }

}
