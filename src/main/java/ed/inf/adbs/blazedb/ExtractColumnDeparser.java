package ed.inf.adbs.blazedb;

import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;

/**
 * {@code ExtractColumnDeparser} is a custom implementation of the
 * {@code ExpressionDeParser} that extracts the columns used in an SQL query.
 * 
 * <p>
 * This class is used to traverse an SQL expression and collect all the
 * {@code Column} objects (i.e., the columns referenced in the query). It
 * maintains a list of these columns, which can be accessed using the
 * {@code getExtractedColumns()} method.
 * </p>
 * 
 * <p>
 * The class overrides the {@code visit(Column)} method from
 * {@code ExpressionDeParser} to capture each column encountered during the
 * expression traversal.
 * </p>
 */
public class ExtractColumnDeparser extends ExpressionDeParser {

    // List to store the columns extracted from the SQL expression.
    private List<Column> extractedColumns = new ArrayList<>();

    /**
     * Returns the list of columns that have been extracted during the parsing
     * process.
     * 
     * @return a list of {@code Column} objects that were encountered in the SQL
     *         expression
     */
    public List<Column> getExtractedColumns() {
        return extractedColumns;
    }

    /**
     * Visits a {@code Column} expression and adds it to the list of extracted
     * columns.
     * 
     * <p>
     * This method is invoked for each {@code Column} encountered during the
     * traversal of the SQL expression. The column is added to the
     * {@code extractedColumns} list.
     * </p>
     * 
     * @param tableColumn the column expression to visit
     */
    @Override
    public void visit(Column tableColumn) {
        super.visit(tableColumn);
        extractedColumns.add(tableColumn);
    }
}
