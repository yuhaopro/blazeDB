package ed.inf.adbs.blazedb.operator;

import java.io.IOException;
import java.util.List;

import ed.inf.adbs.blazedb.CSVParser;
import ed.inf.adbs.blazedb.DatabaseCatalog;
import ed.inf.adbs.blazedb.entity.Tuple;

/**
 * The {@code ScanOperator} class represents a relational algebra scan
 * operation. It reads tuples from a CSV file corresponding to a specified
 * table.
 */
public class ScanOperator extends Operator {

    /**
     * The CSV parser used to read the CSV file.
     */
    private CSVParser csvParser;

    /**
     * The name of the table being scanned.
     */
    private final String tableName;

    /**
     * The path to the CSV file representing the table.
     */
    private final String tablePath;

    /**
     * Constructs a new {@code ScanOperator} for the specified table.
     *
     * @param tableName The name of the table to scan.
     */
    public ScanOperator(String tableName) {
        String tablePath = DatabaseCatalog.getInstance().getTablePath(tableName);
        this.tablePath = tablePath;
        this.tableName = tableName;
        this.csvParser = new CSVParser(tablePath);
    }

    /**
     * Sets the CSV parser for this {@code ScanOperator}.
     *
     * @param csvParser The CSV parser to set.
     */
    public void setCsvParser(CSVParser csvParser) {
        this.csvParser = csvParser;
    }

    /**
     * Retrieves the CSV parser used by this {@code ScanOperator}.
     *
     * @return The CSV parser.
     */
    public CSVParser getCsvParser() {
        return csvParser;
    }

    /**
     * Retrieves the next tuple from the CSV file.
     *
     * @return The next tuple, or {@code null} if there are no more tuples.
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
     * Resets the scan operation by closing the current CSV parser and creating a
     * new one.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void reset() throws IOException {
        csvParser.close();
        this.csvParser = new CSVParser(tablePath);
    }
}