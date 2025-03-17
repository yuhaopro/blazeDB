package ed.inf.adbs.blazedb;

import ed.inf.adbs.blazedb.operator.Operator;

public class QueryPlan {
    private Operator root;

    public QueryPlan(Operator root) {
        this.root = root;
    }

    public Operator getRoot() {
        return root;
    }

}
