package ed.inf.adbs.blazedb.operator;

import net.sf.jsqlparser.expression.Expression;

public class JoinOperator {
    private Operator leftChild;
    private Operator rightChild;
    private String leftTableName;
    private String rightTableName;
    private Expression expression;

    public JoinOperator(String leftTableName, String rightTableName, Expression expression) {
        this.leftTableName = leftTableName;
        this.rightTableName = rightTableName;
        this.expression = expression;
    }

    public String getLeftTableName() {
        return leftTableName;
    }

    public String getRightTableName() {
        return rightTableName;
    }
}
