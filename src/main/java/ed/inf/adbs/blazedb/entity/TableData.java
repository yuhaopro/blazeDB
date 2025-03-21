package ed.inf.adbs.blazedb.entity;

import java.util.List;

/**
 * Represents metadata about a table, including its name, columns, and tuple
 * count.
 */
public class TableData {

    /**
     * The name of the table.
     */
    private String tableName;

    /**
     * The list of column names in the table.
     */
    private List<String> columns;

    /**
     * The number of tuples (rows) in the table.
     */
    private int tupleCount;

    /**
     * Constructs a new {@code TableData} object with the specified table name and
     * columns.
     *
     * @param tableName The name of the table.
     * @param columns   The list of column names in the table.
     */
    public TableData(String tableName, List<String> columns) {
        this.tableName = tableName;
        this.columns = columns;
    }

    /**
     * Retrieves the name of the table.
     *
     * @return The table name.
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Retrieves the list of column names in the table.
     *
     * @return The list of column names.
     */
    public List<String> getColumns() {
        return columns;
    }

    /**
     * Retrieves the number of tuples (rows) in the table.
     *
     * @return The tuple count.
     */
    public int getTupleCount() {
        return tupleCount;
    }
}