package ed.inf.adbs.blazedb;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class CSVParser {
    private final BufferedReader reader;
    private List<String> currentRow;
    private String delimiter = ",";

    public CSVParser(String filePath, String delimiter) throws FileNotFoundException {
        this.reader = new BufferedReader(new FileReader(filePath));
        this.delimiter = delimiter;
    }

    public CSVParser(String filePath) throws FileNotFoundException {
        this.reader = new BufferedReader(new FileReader(filePath));

    }

    // advance the scanner
    public boolean hasNext() {
        try {
            String line = reader.readLine();
            if (line != null) {
                currentRow = Arrays.asList(line.split(delimiter));
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String> next() {
        return currentRow;
    }

    public void close() throws IOException {
        reader.close();
    }

}
