package ed.inf.adbs.blazedb;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import ed.inf.adbs.blazedb.entity.Tuple;
import ed.inf.adbs.blazedb.operator.Operator;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;

/**
 * Lightweight in-memory database system.
 * <p>
 * Feel free to modify/move the provided functions. However, you must keep
 * the existing command-line interface, which consists of three arguments.
 */
public class BlazeDB {

    public static void main(String[] args) throws FileNotFoundException {

        if (args.length != 3) {
            System.err.println("Usage: BlazeDB database_dir input_file output_file");
            return;
        }

        // get inputs
        String databaseDir = args[0];
        String inputFile = args[1];
        String outputFile = args[2];

        // initialize databaseCatalog and generate statistics
        DatabaseCatalog.getInstance().initialize(databaseDir);

        // parsing -> convert sql query to java object
        Statement statement = parseSQLFromFilename(inputFile);

        if (statement == null) {
            System.err.println("Statement is empty!");
            return;
        }
        Select select = (Select) statement;

        System.out.println("Statement: " + select);
        System.out.println("SELECT items: " + select.getPlainSelect().getSelectItems());
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(statement);
        System.out.println("Tables: " + tableList);
        System.out.println("WHERE expression: " + select.getPlainSelect().getWhere());

        QueryPlan queryPlan = new QueryPlanBuilder((Select) select).build();
//        QueryPlan queryPlan = new QueryOptimizer(queryPlans).optimize();
        execute(queryPlan.getRoot(), outputFile);

        // tables we need to scan -> FROM clause
        // selection predicates -> WHERE clause
        // join operator -> JOIN clause
        // projection operator -> SELECT *, column.a, etc

    }

    /**
     * Example method for getting started with JSQLParser. Reads SQL statement
     * from a file or a string and prints the SELECT and WHERE clauses to screen.
     */

    public static Statement parseSQLFromFilename(String filename) {
        try {
			return CCJSqlParserUtil.parse(new FileReader(filename));
//            return CCJSqlParserUtil.parse("SELECT Student.A, Student.B FROM Student WHERE Student.A > 1 AND Student.B >= 100");
        } catch (Exception e) {
            System.err.println("Exception occurred during parsing");
            e.printStackTrace();
            return null;
        }

    }


    /**
     * Executes the provided query plan by repeatedly calling `getNextTuple()`
     * on the root object of the operator tree. Writes the result to `outputFile`.
     *
     * @param root       The root operator of the operator tree (assumed to be non-null).
     * @param outputFile The name of the file where the result will be written.
     */
    public static void execute(Operator root, String outputFile) {
        try {
            // Create a BufferedWriter
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

            // Iterate over the tuples produced by root
            Tuple tuple = root.getNextTuple();
            while (tuple != null) {
                String csv = String.join(", ", tuple.sortRowByColumnOrderInString());
                writer.write(csv);
                writer.newLine();
                tuple = root.getNextTuple();
            }
            root.reset();
            // Close the writer
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
