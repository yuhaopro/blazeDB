package ed.inf.adbs.blazedb.operator;

import java.io.IOException;

import ed.inf.adbs.blazedb.JoinExpressionEvaluator;
import ed.inf.adbs.blazedb.entity.Tuple;
import net.sf.jsqlparser.expression.Expression;

public class JoinOperator extends Operator {
    private Operator leftChild;
    private Operator rightChild;
    private final String leftTableName;
    private final String rightTableName;
    private final Expression expression;
    private final JoinExpressionEvaluator joinExpressionEvaluator;

    public JoinOperator(String leftTableName, String rightTableName, Expression expression, JoinExpressionEvaluator joinExpressionEvaluator) {
        this.leftTableName = leftTableName;
        this.rightTableName = rightTableName;
        this.expression = expression;
        this.joinExpressionEvaluator = joinExpressionEvaluator;
    }

    public String getLeftTableName() {
        return leftTableName;
    }

    public String getRightTableName() {
        return rightTableName;
    }

    public void setLeftChild(Operator leftChild) {
        this.leftChild = leftChild;
    }

    public void setRightChild(Operator rightChild) {
        this.rightChild = rightChild;
    }

    public Tuple getNextTuple() {

        // left table is outer loop
        Tuple leftTuple;
        Tuple rightTuple;
        while ((leftTuple = leftChild.getNextTuple()) != null) {
            while ((rightTuple = rightChild.getNextTuple()) != null) {
                joinExpressionEvaluator.setLeftTuple(leftTuple);
                joinExpressionEvaluator.setRightTuple(rightTuple);
                expression.accept(joinExpressionEvaluator);

                if (joinExpressionEvaluator.getOutput()) {
                    return leftTuple.join(rightTuple, joinExpressionEvaluator.getRightJoinColumns());
                }
            }
            try {
                rightChild.reset();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * @throws IOException
     */
    @Override
    public void reset() throws IOException {
        leftChild.reset();
        rightChild.reset();
    }
}
