package ed.inf.adbs.blazedb;

import ed.inf.adbs.blazedb.operator.Operator;

public class QueryPlan {
    private Operator root;
    private float cost;
    public QueryPlan(Operator root) {
        this.root = root;
    }

    public Operator getRoot() {
        return root;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }
}
