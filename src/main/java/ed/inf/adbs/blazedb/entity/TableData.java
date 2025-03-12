package ed.inf.adbs.blazedb.entity;

import java.util.List;

public class TableData {
    
    private String tableName;
    private List<String> columns;
    private int tupleCount;

    public TableData(String tableName, List<String> columns) {
        this.tableName = tableName;
        this.columns = columns;
    }

    public String getTableName() {
        return tableName;
    }

    public List<String> getColumns() {
        return columns;
    }

    public int getTupleCount() {
        return tupleCount;
    }

}   
