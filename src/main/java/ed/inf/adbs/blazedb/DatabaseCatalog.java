package ed.inf.adbs.blazedb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class DatabaseCatalog {

    private static DatabaseCatalog instance;

    // global shared database schema in hashmap form
    private final HashMap<String, List<String>> dataBaseSchema = new HashMap<>();

    private DatabaseCatalog() {
        // parse the schema file to create a dictionary to search for column index
        String schemaPath = "samples/db/schema.txt";
        String schemaDelimiter = " ";
        try {
            CSVParser csvParser = new CSVParser(schemaPath, schemaDelimiter);
            while (csvParser.hasNext()) {
                List<String> row = csvParser.next();
                String tableName = row.get(0);
                List<String> columns = row.subList(1, row.size());
                dataBaseSchema.put(tableName, columns);
            }
            csvParser.close();
        } catch (IOException e) {
            System.err.println("Schema not found: " + schemaPath);
            e.printStackTrace();
        }
    }

    public static synchronized DatabaseCatalog getInstance() {
        if (instance == null) {
            instance = new DatabaseCatalog();
        }
        return instance;
    }

    public String getTablePath(String tableName) {
        String dataFilePathFormat = "samples/db/data/{0}.csv";
        System.out.println("getTablePath(): " + MessageFormat.format(dataFilePathFormat, tableName));
        return MessageFormat.format(dataFilePathFormat, tableName);
    }

    public List<String> getTableSchema(String tableName) {
        return dataBaseSchema.get(tableName);
    }
}

