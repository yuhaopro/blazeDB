package ed.inf.adbs.blazedb;

import static org.junit.Assert.*;

import ed.inf.adbs.blazedb.entity.TableData;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DatabaseCatalogTest {
    @Test
    public void initializationIsSuccessful() {
        String databaseDirectory = "samples/db";
        DatabaseCatalog.getInstance().initialize(databaseDirectory);
        assertEquals(databaseDirectory, DatabaseCatalog.getInstance().getDatabaseDirectory());

        TableData tableData = DatabaseCatalog.getInstance().getTableSchema("Student");

        assertEquals("Student", tableData.getTableName());

        // List of columns of Student
        // A B C D
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
