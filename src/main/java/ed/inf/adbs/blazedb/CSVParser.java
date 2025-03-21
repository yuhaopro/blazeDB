package ed.inf.adbs.blazedb;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * {@code CSVParser} is a utility class for reading and parsing CSV files. It
 * reads a CSV file line by line, splits each line by a delimiter (comma by
 * default), and provides access to the parsed data.
 * 
 * <p>
 * The class supports a customizable delimiter and offers methods to advance
 * through the file and retrieve the current row as a list of strings.
 * </p>
 */
public class CSVParser {

    private BufferedReader reader;
    private List<String> currentRow;
    private String delimiter = ",";

    /**
     * Constructs a {@code CSVParser} instance for the given file path with a
     * specified delimiter.
     * 
     * <p>
     * This constructor allows the user to specify a custom delimiter for parsing
     * CSV data. If not specified, a comma (",") is used as the default delimiter.
     * </p>
     *
     * @param filePath  the path to the CSV file to be parsed
     * @param delimiter the delimiter used to separate values in the CSV file
     * @throws FileNotFoundException if the file at the specified path does not
     *                               exist
     */
    public CSVParser(String filePath, String delimiter) throws FileNotFoundException {
        this.reader = new BufferedReader(new FileReader(filePath));
        this.delimiter = delimiter;
    }

    /**
     * Constructs a {@code CSVParser} instance for the given file path with the
     * default delimiter (",").
     * 
     * <p>
     * This constructor uses a comma (",") as the default delimiter to parse the CSV
     * data.
     * </p>
     *
     * @param filePath the path to the CSV file to be parsed
     */
    public CSVParser(String filePath) {
        try {
            this.reader = new BufferedReader(new FileReader(filePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            this.reader = null;
        }
    }

    /**
     * Checks whether there are more rows in the CSV file.
     *
     * <p>
     * This method reads the next line from the CSV file, splits it by the specified
     * delimiter, and trims whitespace from each field. It returns {@code true} if a
     * new row is available, and {@code false} if the end of the file is reached or
     * if an error occurs.
     * </p>
     *
     * @return {@code true} if the next row exists, {@code false} otherwise
     */
    public boolean hasNext() {
        if (this.reader == null)
            return false;
        try {
            String line = reader.readLine();
            if (line != null) {
                currentRow = Arrays.asList(line.split(delimiter));
                currentRow = currentRow.stream().map(String::trim).collect(Collectors.toList());
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Returns the current row of data as a list of strings.
     *
     * <p>
     * This method retrieves the current row of the CSV file as a list of strings,
     * where each string corresponds to a field in the row. The row is split by the
     * specified delimiter.
     * </p>
     *
     * @return the current row as a list of strings
     */
    public List<String> next() {
        return currentRow;
    }

    /**
     * Closes the underlying {@code BufferedReader}.
     *
     * <p>
     * This method closes the reader and releases any resources associated with the
     * file. It should be called after finishing reading the file to ensure proper
     * cleanup.
     * </p>
     *
     * @throws IOException if an I/O error occurs while closing the reader
     */
    public void close() throws IOException {
        reader.close();
    }

}
