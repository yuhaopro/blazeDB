package ed.inf.adbs.blazedb;

import java.util.List;

public class QueryOptimizer {
    private final List<QueryPlan> queryPlans;

    public QueryOptimizer(List<QueryPlan> queryPlans) {
        this.queryPlans = queryPlans;
    }

    public QueryPlan optimize() {
        if (queryPlans.isEmpty()) {
            return null;
        }
        return queryPlans.get(0);
    }
}
