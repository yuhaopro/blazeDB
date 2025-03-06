package ed.inf.adbs.blazedb;

import net.sf.jsqlparser.schema.Column;

public class SplitExpression {
    private final String left;
    private final String right;
    private final String operator;
    private String conditionBeforeExpression = null;

    public SplitExpression(String left, String right, String operator) {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    public String getConditionBeforeExpression() {
        return conditionBeforeExpression;
    }

    public void setConditionBeforeExpression(String conditionBeforeExpression) {
        this.conditionBeforeExpression = conditionBeforeExpression;
    }


    public String getExpression() {
        return left + operator + right;
    }

    // either side must not be integer and column table must be different
    public boolean isJoinExpression() {
        if (isNumber(left) || isNumber(right)) {
            return false;
        }
        String leftTableName = left.split("\\.")[0];
        String rightTableName = right.split("\\.")[0];
        return !leftTableName.equals(rightTableName);
    }
    public boolean isNumber(String str) {
        return str.matches("-?\\d+");
    }

    public String getTableName() {
        if (isNumber(left)) {
            return right.split("\\.")[0];
        }

        return left.split("\\.")[0];
    }

    public String getLeftTableName() {
        return left.split("\\.")[0];
    }

    public String getRightTableName() {
        return right.split("\\.")[0];
    }

}
