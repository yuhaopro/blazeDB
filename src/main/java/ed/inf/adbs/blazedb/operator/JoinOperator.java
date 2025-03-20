package ed.inf.adbs.blazedb.operator;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ed.inf.adbs.blazedb.EvaluationDeparser;
import ed.inf.adbs.blazedb.entity.Tuple;
import net.sf.jsqlparser.expression.Expression;

public class JoinOperator extends Operator {
    private Operator leftChild;
    private Operator rightChild;
    private boolean advanceLeftTuple = true;
    private Tuple leftTuple = null;
    private final Expression expression;

    public JoinOperator(Expression expression) {
        this.expression = expression;
    }

    public void setLeftChild(Operator leftChild) {
        this.leftChild = leftChild;
    }

    public void setRightChild(Operator rightChild) {
        this.rightChild = rightChild;
    }

    //TODO: BUG, Left tuple should not advance every call, only advance once right has been consumed.
    public Tuple getNextTuple() {

        if (advanceLeftTuple) {
            leftTuple = leftChild.getNextTuple();

            // if left tuple is null, it means the outer loop has ended, no more matching tuples
            if (leftTuple == null) {
                return null;
            }

            advanceLeftTuple = false;
        }

        Tuple rightTuple = null;

        while ((rightTuple = rightChild.getNextTuple()) != null) {
            EvaluationDeparser evaluationDeparser = new EvaluationDeparser();
            evaluationDeparser.addTuple(leftTuple);
            evaluationDeparser.addTuple(rightTuple);
            expression.accept(evaluationDeparser);

            if (evaluationDeparser.getOutput()) {
                return join(leftTuple, rightTuple);
            }
        }

        // no more matching right tuple with left tuple, advance left tuple and try again.
        advanceLeftTuple = true;

        // make sure to reset the right child before trying again.
        try {
            rightChild.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // try to find the next match with the next left tuple.
        return getNextTuple();
        
    }

    /**
     * @throws IOException
     */
    @Override
    public void reset() throws IOException {
        leftChild.reset();
        rightChild.reset();
    }

    public Tuple join(Tuple leftTuple, Tuple rightTuple) {
        List<String> columns = new ArrayList<>();
        columns = Stream.of(leftTuple.getColumns(), rightTuple.getColumns())
            .flatMap(Collection::stream)
            .distinct()
            .collect(Collectors.toList());
        
        HashMap<String, Integer> mergedLookup = Stream.of(leftTuple.getLookup(), rightTuple.getLookup())
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (v1, v2) -> v1, // keeps first hashmap value if duplicate keys
                        HashMap::new
                ));
        Tuple tuple = new Tuple(columns, mergedLookup);
        return tuple;
    }
}
