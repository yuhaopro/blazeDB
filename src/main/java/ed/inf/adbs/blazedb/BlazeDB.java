package ed.inf.adbs.blazedb;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

import ed.inf.adbs.blazedb.entity.Tuple;
import ed.inf.adbs.blazedb.operator.Operator;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

/**
 * Lightweight in-memory database system.
 * <p>
 * Feel free to modify/move the provided functions. However, you must keep
 * the existing command-line interface, which consists of three arguments.
 */
public class BlazeDB {
    private static final Logger logger = Logger.getLogger(BlazeDB.class.getName());
    public static void main(String[] args) throws FileNotFoundException {

        if (args.length != 3) {
            logger.warning("Usage: BlazeDB database_dir input_file output_file");
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
            logger.warning("Statement is empty!");
            return;
        }
        Select select = (Select) statement;

        QueryPlan queryPlan = new QueryPlanBuilder(select).build();
//      QueryPlan queryPlan = new QueryOptimizer(queryPlans).optimize();
        execute(queryPlan.getRoot(), outputFile);


    }

    /**
     * Example method for getting started with JSQLParser. Reads SQL statement
     * from a file or a string and prints the SELECT and WHERE clauses to screen.
     */

    public static Statement parseSQLFromFilename(String filename) {
        try {
			return CCJSqlParserUtil.parse(new FileReader(filename));
        } catch (Exception e) {
            logger.warning("Exception occurred during parsing");
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
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            Tuple tuple = root.getNextTuple();
            while (tuple != null) {
                String csv = String.join(", ", tuple.sortRowByColumnOrderInString());
                writer.write(csv);
                writer.newLine();
                tuple = root.getNextTuple();
            }
            root.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
