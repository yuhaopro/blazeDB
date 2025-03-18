package ed.inf.adbs.blazedb;

import java.util.HashSet;

import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;

public class ExtractColumnExpressionDeparser extends ExpressionDeParser {
	
	private HashSet<String> extractedColumns = new HashSet<>();

	public HashSet<String> getExtractedColumns() {
		return extractedColumns;
	}

    @Override
    public void visit(Column tableColumn) {
        super.visit(tableColumn);
        String columnName = tableColumn.toString();
        extractedColumns.add(columnName);
    }
	
}
