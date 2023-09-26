import java.util.*;
import java.io.*;
import java.util.regex.*;

public class Main {

    private static List<String> readFromFile(String fileDirectory) {
        List<String> rows = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(fileDirectory))) {
            String line;

            while ((line = br.readLine()) != null) {
                rows.add(line);
            }
        } catch (IOException e) {
            e.getMessage();
        }
        return rows;
    }

    private static List<String> filterSPC(List<String> rows, String spcRegex) {
        List<String> matchingRows = new ArrayList<>();

        for (String row : rows) {
            String[] parts = row.split(",");
            String spc = parts[2];
            if (Pattern.matches(spcRegex, spc)) {
                matchingRows.add(row);
            }
        }
        return matchingRows;
    }

//    public static void main(String[] args) throws FileNotFoundException {
//        String fileDirectory = "C:/Users/fam/Documents/UM/SPCFilter/output_data.csv";
//        Scanner keyboard = new Scanner(System.in);
//
//        List<String> rows = readFromFile(fileDirectory);
//
//        // Check if there are any rows in the file
//        if (!rows.isEmpty()) {
//            // Print the first row
//
//            System.out.println("Enter SPC: ");
//            String input = keyboard.nextLine();
//
//            System.out.println(rows.get(0)); // prints first row
//
//            String spcRegex = input.replace("*", ".*");
//
//            // Start filtering from the second row
//            List<String> matchingRows = filterSPC(rows.subList(1, rows.size()), spcRegex);
//
//            for (String match : matchingRows) {
//                System.out.println(match);
//            }
//        } else {
//            System.out.println("The file is empty.");
//        }
//    }

    public static void main(String[] args) throws FileNotFoundException {
        String fileDirectory = "C:/Users/fam/Documents/UM/SPCFilter/output_data.csv";
        Scanner keyboard = new Scanner(System.in);

        List<String> rows = readFromFile(fileDirectory);

        // Check if there are any rows in the file
        if (!rows.isEmpty()) {
            // Print the first row
            System.out.println("Enter SPC: ");
            String input = keyboard.nextLine();

            String[] header = rows.get(0).split(",");
            System.out.println(rows.get(0)); // prints first row

            String spcRegex = input.replace("*", ".*");

            // Start filtering from the second row
            List<String> matchingRows = filterSPC(rows.subList(1, rows.size()), spcRegex);

            // Create a map to store the count of each version
            Map<String, Integer> versionCountMap = new HashMap<>();

            for (String match : matchingRows) {
                System.out.println(match);

                String[] parts = match.split(",");
                for (int i = 3; i < parts.length && i < header.length; i++) {
                    String version = header[i];
                    if (!parts[i].isEmpty()) { // assuming a '1' in a cell under a version column means that version is present
                        versionCountMap.put(version, versionCountMap.getOrDefault(version, 0) + 1);
                    }
                }
            }

            // Print the count of each version
            System.out.print("Version Count: ,");
            for (Map.Entry<String, Integer> entry : versionCountMap.entrySet()) {
                System.out.print(entry.getKey() + ": " + entry.getValue() + ",");
            }
        } else {
            System.out.println("The file is empty.");
        }
    }

}
