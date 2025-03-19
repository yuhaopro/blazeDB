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
 * The Tuple class represents a row of data.
 * */
public class Tuple {
    // to keep track of column order in this tuple
    private List<String> columns = new ArrayList<>();

    // Hashmap for quick lookups on column value.
    private HashMap<String, Integer> lookup = new HashMap<>();

    // for creating tuple with scan operator
    public Tuple(List<String> row, String tableName) {
        TableData tableData =  DatabaseCatalog.getInstance().getTableSchema(tableName);
        List<String> columnsFromTableData = tableData.getColumns();
        // set the current order based on table schema
        for (int i = 0; i < row.size(); i++) {
            this.columns.add(tableName.trim() + "." + columnsFromTableData.get(i).trim());
            this.lookup.put(tableName.trim() + "." + columnsFromTableData.get(i).trim(), Integer.valueOf(row.get(i).trim()));
        }
    }

    public Tuple() {}
    
    public Tuple(List<String> columns, HashMap<String, Integer> lookup) {
        this.columns = columns;
        this.lookup = lookup;
    }

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
        List<String> sortedRow = new ArrayList<>();
        for (String column : columns) {
            sortedRow.add(lookup.get(column).toString());
        }
        return sortedRow;
    }

    public Tuple join(Tuple tuple) {
        this.columns = Stream.of(this.columns, tuple.getColumns())
            .flatMap(Collection::stream)
            .distinct()
            .collect(Collectors.toList());
        
        HashMap<String, Integer> mergedLookup = Stream.of(this.lookup, tuple.getLookup())
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (v1, v2) -> v1, // keeps first hashmap value if duplicate keys
                        HashMap::new
                ));
        this.lookup = mergedLookup;
        return this;
    }

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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Tuple) {
            Tuple tuple = (Tuple) obj;
            if (this.columns.size() != tuple.getColumns().size()) return false;

            for (int i = 0; i < this.columns.size(); i++) {
                String columnKey = this.columns.get(i);
                String otherColumnKey = tuple.getColumns().get(i);

                if (!columnKey.equals(otherColumnKey)) return false;

                if (this.lookup.get(columnKey) != tuple.getLookup().get(otherColumnKey)) return false;
            }
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(columns); // different ordering of columns will lead to different hashes.

        for (String columnKey : columns) {
            result = 31 * result + Objects.hashCode(lookup.get(columnKey));
        }

        return result;
    }

}