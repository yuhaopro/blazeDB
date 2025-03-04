package ed.inf.adbs.blazedb;

import java.util.HashMap;
import java.util.List;

/**
 * The Tuple class represents a row of data.
 *
 * You will need to modify this class, obviously :).
 */
public class Tuple extends HashMap<String, Integer> {
    public Tuple(List<String> row, String tableName) {
        List<String> tableSchema = DatabaseCatalog.getInstance().getTableSchema(tableName);
        for (int i = 0; i < row.size(); i++) {
            this.put(tableSchema.get(i).trim(), Integer.valueOf(row.get(i).trim()));
        }
    }
}