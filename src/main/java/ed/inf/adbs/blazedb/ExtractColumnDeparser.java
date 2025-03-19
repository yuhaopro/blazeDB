package ed.inf.adbs.blazedb;

import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;

public class ExtractColumnDeparser extends ExpressionDeParser {
	
	private List<Column> extractedColumns = new ArrayList<>();

	public List<Column> getExtractedColumns() {
		return extractedColumns;
	}

    @Override
    public void visit(Column tableColumn) {
        super.visit(tableColumn);
        extractedColumns.add(tableColumn);
    }
	
}
