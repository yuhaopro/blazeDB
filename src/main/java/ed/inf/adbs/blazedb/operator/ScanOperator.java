package ed.inf.adbs.blazedb.operator;

import ed.inf.adbs.blazedb.CSVParser;
import ed.inf.adbs.blazedb.DatabaseCatalog;
import ed.inf.adbs.blazedb.RowSource;
import ed.inf.adbs.blazedb.Tuple;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class ScanOperator extends Operator {
    private RowSource rowSource;
    private final String tableName;
    private final String tablePath;

    /**
     *
     * @param tableName
     * @throws FileNotFoundException
     */
    public ScanOperator(String tableName) throws FileNotFoundException {
        String tablePath = DatabaseCatalog.getInstance().getTablePath(tableName);
        this.tablePath = tablePath;
        this.rowSource = new CSVParser(tablePath);
        this.tableName = tableName;
    }
    /**
     * @return Tuple after scanning the csv file
     */
    @Override
    public Tuple getNextTuple() {
        if (rowSource.hasNext()) {
            List<String> row = rowSource.next();
            Tuple tuple = new Tuple(row, tableName);
            tuple.print();
            return tuple;
        }
        // if no more tuple, return null
        return null;
    }

    /**
     * 
     */
    @Override
    public void reset() throws IOException {
        rowSource.close();
        this.rowSource = new CSVParser(tablePath);
    }
}
