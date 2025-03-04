package ed.inf.adbs.blazedb;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The Tuple class represents a row of data.
 *
 * You will need to modify this class, obviously :).
 */
public class Tuple {
    private List<String> columns = new ArrayList<String>();
    private HashMap<String, Integer> lookup = new HashMap<>();

    // for creating tuple with scan operator
    public Tuple(List<String> row, String tableName) {
        List<String> tableSchema =  DatabaseCatalog.getInstance().getTableSchema(tableName);
        // set the current order based on table schema
        for (int i = 0; i < row.size(); i++) {
            this.columns.add(tableName.trim() + "." + tableSchema.get(i).trim());
            this.lookup.put(tableName.trim() + "." + tableSchema.get(i).trim(), Integer.valueOf(row.get(i).trim()));
        }
    }

    public Tuple() {}

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public HashMap<String, Integer> getLookup() {
        return lookup;
    }

    public void setLookup(HashMap<String, Integer> lookup) {
        this.lookup = lookup;
    }

    public List<String> sortRowByColumnOrderInString() {
        List<String> sortedRow = new ArrayList<String>();
        for (String column : columns) {
            sortedRow.add(lookup.get(column).toString());
        }
        return sortedRow;
    }

    public void print() {
        // [Student.A, Student.B, Student.C, Student.D]
        System.out.println(columns);

        // print out tuple row values
        List<Integer> row = new ArrayList<Integer>();
        for (int i = 0; i < columns.size(); i++) {
            row.add(lookup.get(columns.get(i).trim()));
        }
        System.out.println(row);
    }

}