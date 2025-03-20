package ed.inf.adbs.blazedb.operator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
                    return join(leftTuple, rightTuple);
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
