package ed.inf.adbs.blazedb;

import ed.inf.adbs.blazedb.operator.Operator;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class CSVParser implements RowSource {
//    private final Scanner scanner;
    private final BufferedReader reader;
    private List<String> currentRow;
    private String delimiter = ",";

    public CSVParser(String filePath, String delimiter) throws FileNotFoundException {
//        this.scanner = new Scanner(new File(filePath));
        this.reader = new BufferedReader(new FileReader(filePath));
        this.delimiter = delimiter;
    }

    public CSVParser(String filePath) throws FileNotFoundException {
//        this.scanner = new Scanner(new File(filePath));
        this.reader = new BufferedReader(new FileReader(filePath));

    }

    // advance the scanner
    public boolean hasNext() {
//        try {
//            String line = scanner.nextLine();
//            if (line != null) {
//                currentRow = Arrays.asList(line.split(delimiter));
//                return true;
//            }
//            return false;
//        } catch (Exception e) {
//            // e.printStackTrace();
//            return false;
//        }
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
//        scanner.close();
        reader.close();
    }

}
