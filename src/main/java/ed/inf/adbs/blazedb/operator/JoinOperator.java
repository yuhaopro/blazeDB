package ed.inf.adbs.blazedb.operator;

import java.io.IOException;

import ed.inf.adbs.blazedb.EvaluationDeparser;
import ed.inf.adbs.blazedb.entity.Tuple;
import net.sf.jsqlparser.expression.Expression;

public class JoinOperator extends Operator {
    private Operator leftChild;
    private Operator rightChild;
    private final String leftTableName;
    private final String rightTableName;
    private final Expression expression;

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
                EvaluationDeparser evaluationDeparser = new EvaluationDeparser();
                evaluationDeparser.addTuple(leftTuple);
                evaluationDeparser.addTuple(rightTuple);
                expression.accept(evaluationDeparser);

                if (evaluationDeparser.getOutput()) {
                    return leftTuple.join(rightTuple);
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
