package ed.inf.adbs.blazedb;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;

import ed.inf.adbs.blazedb.entity.TableData;

/**
 * {@code DatabaseCatalog} is a singleton class responsible for managing the
 * schema and metadata of a database. It reads the database schema from a file
 * and provides access to table schema and file paths for the tables.
 * 
 * <p>
 * The schema is loaded from a file named "schema.txt" located in the specified
 * database directory. The schema defines the tables and their columns. The
 * catalog allows access to table metadata and provides the file path to access
 * each table's data in the database.
 * </p>
 */
public class DatabaseCatalog {

    private static DatabaseCatalog instance;
    private String databaseDirectory;

    private final HashMap<String, TableData> dataBaseSchema = new HashMap<>();

    /**
     * Private constructor to prevent external instantiation. The class follows the
     * Singleton design pattern to ensure that only one instance of
     * {@code DatabaseCatalog} exists.
     */
    private DatabaseCatalog() {
    }

    /**
     * Initializes the {@code DatabaseCatalog} by reading the schema file located in
     * the specified database directory. The schema file should be named
     * "schema.txt".
     * 
     * <p>
     * The schema file contains the names of tables and their associated columns.
     * This method reads the schema and populates the catalog with the information.
     * </p>
     * 
     * @param databaseDirectory the directory containing the database and schema
     *                          file
     */
    public void initialize(String databaseDirectory) {
        this.databaseDirectory = databaseDirectory;
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

    /**
     * Returns the singleton instance of {@code DatabaseCatalog}.
     * 
     * <p>
     * If the instance does not exist, it creates a new one. This method follows the
     * Singleton design pattern to ensure a single instance is used across the
     * application.
     * </p>
     * 
     * @return the singleton instance of {@code DatabaseCatalog}
     */
    public static DatabaseCatalog getInstance() {
        if (instance == null) {
            instance = new DatabaseCatalog();
        }
        return instance;
    }

    /**
     * Gets the directory where the database is stored.
     * 
     * @return the path to the database directory
     */
    public String getDatabaseDirectory() {
        return this.databaseDirectory;
    }

    /**
     * Returns the file path for the data of a given table.
     * 
     * <p>
     * The data for each table is stored in a CSV file within a subdirectory called
     * "data" inside the database directory. This method constructs the file path
     * based on the table name.
     * </p>
     * 
     * @param tableName the name of the table whose data file path is needed
     * @return the file path to the CSV file storing the table's data
     */
    public String getTablePath(String tableName) {
        String dataFilePathFormat = this.databaseDirectory + "/data/{0}.csv";
        return MessageFormat.format(dataFilePathFormat, tableName);
    }

    /**
     * Retrieves the schema of a table given its name.
     * 
     * <p>
     * The schema includes the table's name and the list of columns in the table.
     * </p>
     * 
     * @param tableName the name of the table whose schema is to be retrieved
     * @return the {@code TableData} object containing the schema for the specified
     *         table, or {@code null} if the table does not exist in the catalog
     */
    public TableData getTableSchema(String tableName) {
        return dataBaseSchema.get(tableName);
    }
}
