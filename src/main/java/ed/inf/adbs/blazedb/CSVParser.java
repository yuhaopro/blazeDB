package ed.inf.adbs.blazedb;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CSVParser {
    private BufferedReader reader;
    private List<String> currentRow;
    private String delimiter = ",";

    public CSVParser(String filePath, String delimiter) throws FileNotFoundException {
        this.reader = new BufferedReader(new FileReader(filePath));
        this.delimiter = delimiter;
    }

    public CSVParser(String filePath) {
        try {
            this.reader = new BufferedReader(new FileReader(filePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            this.reader = null;
        }
    }

    // advance the scanner
    public boolean hasNext() {
        if (this.reader == null) return false;
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

    public List<String> next() {
        return currentRow;
    }

    public void close() throws IOException {
        reader.close();
    }

}
