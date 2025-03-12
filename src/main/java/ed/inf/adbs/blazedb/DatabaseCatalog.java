package ed.inf.adbs.blazedb;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;

import ed.inf.adbs.blazedb.entity.TableData;

public class DatabaseCatalog {

    private static DatabaseCatalog instance;
    private String databaseDirectory;

    // global shared database schema in hashmap form
    private final HashMap<String, TableData> dataBaseSchema = new HashMap<>();

    private DatabaseCatalog() {}

    public void initialize(String databaseDirectory) {
        this.databaseDirectory = databaseDirectory;
        // parse the schema file to create a dictionary to search for column index
        String schemaPath = this.databaseDirectory + "/schema.txt";
        String schemaDelimiter = " ";

        try {
            CSVParser csvParser = new CSVParser(schemaPath, schemaDelimiter);
            while (csvParser.hasNext()) {
                List<String> row = csvParser.next();
                String tableName = row.get(0);
                List<String> columns = row.subList(1, row.size());
                TableData tableData = new TableData(tableName, columns);
                dataBaseSchema.put(tableName, tableData);
            }
            csvParser.close();
        } catch (IOException e) {
            System.err.println("Schema not found: " + schemaPath);
            e.printStackTrace();
        }
    }

    public static DatabaseCatalog getInstance() {
        if (instance == null) {
            instance = new DatabaseCatalog();
        }
        return instance;
    }

    public String getDatabaseDirectory() {
        return this.databaseDirectory;
    }

    public String getTablePath(String tableName) {
        
        String dataFilePathFormat = this.databaseDirectory + "/data/{0}.csv";
        // System.out.println("getTablePath(): " + MessageFormat.format(dataFilePathFormat, tableName));
        return MessageFormat.format(dataFilePathFormat, tableName);
    }

    public TableData getTableSchema(String tableName) {
        return dataBaseSchema.get(tableName);
    }
}

