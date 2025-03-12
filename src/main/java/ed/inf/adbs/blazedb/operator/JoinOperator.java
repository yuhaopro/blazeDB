package ed.inf.adbs.blazedb.operator;

import ed.inf.adbs.blazedb.entity.Tuple;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import java.util.Stack;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import java.io.IOException;

public class JoinOperator extends Operator {
    private Operator leftChild;
    private Operator rightChild;
    private String leftTableName;
    private String rightTableName;
    private Expression expression;
    private ExpressionDeParser expressionDeParser;

    public JoinOperator(String leftTableName, String rightTableName, Expression expression, ExpressionDeParser expressionDeParser) {
        this.leftTableName = leftTableName;
        this.rightTableName = rightTableName;
        this.expression = expression;
        this.expressionDeParser = expressionDeParser;
    }

    public String getLeftTableName() {
        return leftTableName;
    }

    public String getRightTableName() {
        return rightTableName;
    }

    public Tuple getNextTuple() {

        // left table is outer loop
        Tuple leftTuple;
        Tuple rightTuple;
        while ((leftTuple = leftChild.getNextTuple()) != null) {
            while ((rightTuple = rightChild.getNextTuple()) != null) {

                String joinColumn =
                leftTuple.getLookup().get("");
            }
        }
    }

    /**
     * @throws IOException
     */
    @Override
    public void reset() throws IOException {

    }
}
