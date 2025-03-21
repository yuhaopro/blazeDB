package ed.inf.adbs.blazedb;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ed.inf.adbs.blazedb.entity.TableData;

/**
 * Unit tests for the {@link DatabaseCatalog} class. These tests verify the
 * correct functionality of initializing the database catalog, retrieving the
 * schema of a table, and ensuring the correct columns are associated with a
 * table in the database.
 */
public class DatabaseCatalogTest {

    /**
     * Test to ensure that the {@link DatabaseCatalog} is successfully initialized
     * and the table schema for a specific table (e.g., "Student") is correctly
     * loaded.
     * 
     * <p>
     * This test checks the following:
     * <ul>
     * <li>The database directory is correctly set.</li>
     * <li>The table schema for the specified table (e.g., "Student") is correctly
     * fetched.</li>
     * <li>The correct columns for the table are retrieved and compared to the
     * expected values.</li>
     * </ul>
     * </p>
     * 
     * @throws Exception if any error occurs during the test
     */
    @Test
    public void initializationIsSuccessful() {

        String databaseDirectory = "samples/db";

        DatabaseCatalog.getInstance().initialize(databaseDirectory);

        assertEquals(databaseDirectory, DatabaseCatalog.getInstance().getDatabaseDirectory());

        TableData tableData = DatabaseCatalog.getInstance().getTableSchema("Student");

        assertEquals("Student", tableData.getTableName());

        List<String> actualColumns = new ArrayList<String>();
        actualColumns.add("A");
        actualColumns.add("B");
        actualColumns.add("C");
        actualColumns.add("D");

        for (int i = 0; i < actualColumns.size(); i++) {
            assertEquals(actualColumns.get(i), tableData.getColumns().get(i));
        }
    }
}
