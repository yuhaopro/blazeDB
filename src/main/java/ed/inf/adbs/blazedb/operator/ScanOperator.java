package ed.inf.adbs.blazedb.operator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import ed.inf.adbs.blazedb.CSVParser;
import ed.inf.adbs.blazedb.DatabaseCatalog;
import ed.inf.adbs.blazedb.entity.Tuple;

public class ScanOperator extends Operator {
    private CSVParser csvParser;
    private final String tableName;
    private final String tablePath;

    /**
     *
     * @param tableName
     */
    public ScanOperator(String tableName) throws FileNotFoundException {
        String tablePath = DatabaseCatalog.getInstance().getTablePath(tableName);
        this.tablePath = tablePath;
        this.tableName = tableName;
        this.csvParser = new CSVParser(tablePath);
    }

    public void setCsvParser(CSVParser csvParser) {
        this.csvParser = csvParser;
    }

    public CSVParser getCsvParser() {
        return csvParser;
    }

    /**
     * @return Tuple after scanning the csv file
     */
    @Override
    public Tuple getNextTuple() {
        if (csvParser.hasNext()) {
            List<String> row = csvParser.next();
            return new Tuple(row, tableName);
        }
        // if no more tuple, return null
        return null;
    }

    /**
     * 
     */
    @Override
    public void reset() throws IOException {
        csvParser.close();
        this.csvParser = new CSVParser(tablePath);
    }
}
