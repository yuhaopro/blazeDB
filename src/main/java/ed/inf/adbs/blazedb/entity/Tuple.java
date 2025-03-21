package ed.inf.adbs.blazedb.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ed.inf.adbs.blazedb.DatabaseCatalog;

/**
 * The {@code Tuple} class represents a row of data, typically retrieved from a
 * database table. It stores the column names and their corresponding values in
 * a structured manner, allowing for efficient access and manipulation.
 */
public class Tuple {
    /**
     * The list of column names in the tuple, maintaining the order in which they
     * appear.
     */
    private List<String> columns = new ArrayList<>();

    /**
     * A map that allows for quick lookups of column values using their names.
     */
    private HashMap<String, Integer> lookup = new HashMap<>();

    /**
     * Constructs a new {@code Tuple} object from a list of row values and a table
     * name. This constructor is typically used when creating tuples from scan
     * operations.
     *
     * @param row       The list of string representations of the row values.
     * @param tableName The name of the table from which the row was retrieved.
     */
    public Tuple(List<String> row, String tableName) {
        TableData tableData = DatabaseCatalog.getInstance().getTableSchema(tableName);
        List<String> columnsFromTableData = tableData.getColumns();
        // set the current order based on table schema
        for (int i = 0; i < row.size(); i++) {
            this.columns.add(tableName.trim() + "." + columnsFromTableData.get(i).trim());
            this.lookup.put(tableName.trim() + "." + columnsFromTableData.get(i).trim(),
                    Integer.valueOf(row.get(i).trim()));
        }
    }

    /**
     * Constructs an empty {@code Tuple} object.
     */
    public Tuple() {
    }

    /**
     * Constructs a new {@code Tuple} object with the specified column names and
     * lookup map.
     *
     * @param columns The list of column names.
     * @param lookup  The map containing column names and their corresponding
     *                values.
     */
    public Tuple(List<String> columns, HashMap<String, Integer> lookup) {
        this.columns = columns;
        this.lookup = lookup;
    }

    /**
     * Retrieves the list of column names in the tuple.
     *
     * @return The list of column names.
     */
    public List<String> getColumns() {
        return columns;
    }

    /**
     * Sets the list of column names in the tuple.
     *
     * @param columns The new list of column names.
     */
    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    /**
     * Retrieves the lookup map containing column names and their corresponding
     * values.
     *
     * @return The lookup map.
     */
    public HashMap<String, Integer> getLookup() {
        return lookup;
    }

    /**
     * Sets the lookup map for the tuple.
     *
     * @param lookup The new lookup map.
     */
    public void setLookup(HashMap<String, Integer> lookup) {
        this.lookup = lookup;
    }

    /**
     * Returns a list of string representations of the row values, sorted according
     * to the column order.
     *
     * @return A list of string representations of the row values.
     */
    public List<String> sortRowByColumnOrderInString() {
        List<String> sortedRow = new ArrayList<>();
        for (String column : columns) {
            sortedRow.add(lookup.get(column).toString());
        }
        return sortedRow;
    }

    /**
     * Prints the column names and row values of the tuple to the console.
     */
    public void print() {
        // [Student.A, Student.B, Student.C, Student.D]
        System.out.println(columns);

        // print out tuple row values
        List<Integer> row = new ArrayList<>();
        for (int i = 0; i < columns.size(); i++) {
            row.add(lookup.get(columns.get(i).trim()));
        }
        System.out.println(row);
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj The reference object with which to compare.
     * @return {@code true} if this object is the same as the obj argument;
     *         {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Tuple) {
            Tuple tuple = (Tuple) obj;
            if (this.columns.size() != tuple.getColumns().size())
                return false;

            for (int i = 0; i < this.columns.size(); i++) {
                String columnKey = this.columns.get(i);
                String otherColumnKey = tuple.getColumns().get(i);

                if (!columnKey.equals(otherColumnKey))
                    return false;

                if (this.lookup.get(columnKey) != tuple.getLookup().get(otherColumnKey))
                    return false;
            }
            return true;
        }

        return false;
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        int result = Objects.hash(columns); // different ordering of columns will lead to different hashes.

        for (String columnKey : columns) {
            result = 31 * result + Objects.hashCode(lookup.get(columnKey));
        }

        return result;
    }
}