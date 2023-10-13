import java.util.*;
import java.io.*;
import java.util.stream.Collectors;

public class Main {

    public static final String TPTP_PROBLEMS_DIRECTORY =
// "C:/Users/fam/Documents/UM/TPTPRESEARCH/TPTPOrganizer/TPTP-v8.2.0/Problems";
"/Users/geoff/MyDocuments/Development/ATPProgress/Problems";
    public static final String OUTPUT_CSV_FILE = "output_data.csv";
    public static final String NOTHING_BEFORE = "v6.2.0"; //----Do not keep data before this release
    public static final String FILL_1_BEFORE = "v7.3.0";
    public static final String FILL_LESS_THAN_1_BEFORE = "v7.3.0"; //----Can fill with rating < 1.00 before this release

    private static boolean hasMissingDataAfterVersion(DataEntry entry, String version) {
        List<String> versions = new ArrayList<>(entry.ratingsVersions.keySet());
        Collections.sort(versions);

        // Find the index of the given version
        int startIndex = versions.indexOf(version);

        // If version doesn't exist in the list, then we assume the data is missing for that version
        if (startIndex == -1) {
            return true;
        }

        for (int i = startIndex; i < versions.size(); i++) {
            // If the rating in that version is empty, then return true.
            if (entry.ratingsVersions.get(versions.get(i)).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static void removeOldData(String OUTPUT_CSV_FILE, String NOTHING_BEFORE) {
        List<String> adjustedLines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(OUTPUT_CSV_FILE))) {
            String header = reader.readLine();
            if (header != null) {
                String[] headerColumns = header.split(",");
                adjustedLines.add(header);  // add header as is
                List<Integer> columnsToRemove = new ArrayList<>();
                // Start from the 4th column since the first 3 are not version columns.
                for (int i = 3; i < headerColumns.length; i++) {
                    if (headerColumns[i].compareTo(NOTHING_BEFORE) < 0) { // if version is before "v4.0.0"
                        columnsToRemove.add(i);
                    }
                }

                String line;
                while ((line = reader.readLine()) != null) {
                    String[] values = line.split(",");
                    StringBuilder adjustedLine = new StringBuilder();
                    for (int i = 0; i < values.length; i++) {
                        if (!columnsToRemove.contains(i)) {
                            if (adjustedLine.length() > 0) {
                                adjustedLine.append(",");
                            }
                            adjustedLine.append(values[i]);
                        }
                    }
                    adjustedLines.add(adjustedLine.toString());
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading CSV : " + e.getMessage());
        }

        // Now, rewrite the CSV with the adjusted lines
        try (FileWriter writer = new FileWriter(OUTPUT_CSV_FILE)) {
            for (String line : adjustedLines) {
                writer.write(line + "\n");
            }
        } catch (IOException e) {
            System.out.println("Error writing adjusted CSV : " + e.getMessage());
        }
    }

    private static void writeToCSV(List<DataEntry> entries, Set<String> versionSet, String OUTPUT_CSV_FILE) {
        List<String> versions = new ArrayList<>(versionSet);
        Collections.sort(versions);
        Collections.reverse(versions);

        Map<String, Integer> versionCounts = new HashMap<>(); // Map to store the count of each version

        try (FileWriter writer = new FileWriter(OUTPUT_CSV_FILE)) {
            writer.write("Domain,Problem,SPC");
            for (String version : versions)
                writer.write("," + version);
            writer.write("\n");

            for (DataEntry entry : entries) {
                writer.write(entry.toCSV(versions) + "\n");

                // Calculate the count for each version
                for (String version : versions) {
                    if (entry.ratingsVersions.containsKey(version) && !entry.ratingsVersions.get(version).isEmpty()) {
                        versionCounts.put(version, versionCounts.getOrDefault(version, 0) + 1);
                    }
                }
            }

            // Write the total count for each version to the CSV file
            writer.write("Total,,");
            for (String version : versions) {
                writer.write("," + versionCounts.getOrDefault(version, 0));
            }
            writer.write("\n");

        } catch (IOException e) {
            System.out.println("Error writing in CSV : " + e.getMessage());
        }
    }

    private static List<String> getDomains(String TPTP_PROBLEMS_DIRECTORY) {
        List<String> domainNames = new ArrayList<>();
        File directory = new File(TPTP_PROBLEMS_DIRECTORY);
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files)
                    domainNames.add(file.getName());
            }
        }
        return domainNames;
    }

    private static void parseRatingsAndVersions(String line, ExtractedData data) {
        String[] pairs = line.replace("% Rating   : ", "").split(","); // "0.01 v8.10"
        for (String pair : pairs) {
            pair = pair.trim();
            String[] parts = pair.split(" "); // "0.01"

            if (parts.length != 2) {
                continue;
            }

//            Double num = Double.valueOf(parts[0]);
            String rating = parts[0].trim().replaceAll("[^ .,a-zA-Z0-9]","");
            String version = parts[1];

            data.ratingsVersions.put(version, rating);
        }
    }

    private static ExtractedData extractedData(File file) {
        ExtractedData data = new ExtractedData();

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                if (line.startsWith("% SPC      : ")) {
                    data.spc = line.replace("% SPC      : ", "").trim();
                    System.out.println("SUCCESS : SPC : " + data.spc);
                } else if (line.startsWith("% Rating   : ")) { // "0.01 v8.10, 0.02 v7.9.9"
                    parseRatingsAndVersions(line, data);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("ERROR File not found ");
        }
        return data;
    }

    private static List<DataEntry> collectData(String TPTP_PROBLEMS_DIRECTORY, Set<String> versions) {
        List<DataEntry> entries = new ArrayList<>();
        List<String> domains = getDomains(TPTP_PROBLEMS_DIRECTORY);
        int counter = 0;
        for (String domain : domains) {
            String newDirectory = TPTP_PROBLEMS_DIRECTORY + "/" + domain;
            File directory = new File(newDirectory);
            if (directory.isDirectory()) {
                File[] files = directory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        ExtractedData data = extractedData(file);
                        versions.addAll(data.ratingsVersions.keySet());
                        DataEntry entry = new DataEntry(domain, file.getName(), data.spc, data.ratingsVersions);
                        entries.add(entry);
                        counter++;
//                        if (counter > 1000)
//                            return entries;
                    }
                }
            }
        }
        return entries;
    }

    public static void main(String[] args) {
        Set<String> versions = new HashSet<>(); // Use this to create non-repetitive columns in csv *MISSING SOME*
        List<DataEntry> entries = collectData(TPTP_PROBLEMS_DIRECTORY, versions);

//        for (DataEntry entry : entries) {
//            System.out.println("FULL DATA : " + entry.domain + " : " + entry.problem + " : " + entry.spc + " : "
//                    + entry.ratingsVersions.keySet() + " : " + entry.ratingsVersions.entrySet());
//            System.out.println(entries);
//        }

        /*
        Break down of the following code:
        - DataEntry: objects that do not have missing data after v4.0.0 will be kept.
        - entries.stream(): allows us to perform a sequence of computations on each item in the list.
        - .filter(entry -> !hasMissingDataAfterVersion(entry, NOTHING_BEFORE)):  filters using the lambda function.
            It retains only versions after v4.0.0
        - .collect(Collectors.toList()): returns the new results back into list
         */
        List<DataEntry> filteredEntries = entries.stream()
                .filter(entry -> !hasMissingDataAfterVersion(entry, NOTHING_BEFORE))
                .collect(Collectors.toList());

        writeToCSV(filteredEntries, versions, OUTPUT_CSV_FILE);

//        writeToCSV(entries, versions, OUTPUT_CSV_FILE);
        removeOldData(OUTPUT_CSV_FILE, NOTHING_BEFORE);
    }

    /*
    Release v8.1.1, Fri Oct 7 12:13:23 EDT 2022
    Release v8.1.0, Sat Jul 30 18:16:58 EDT 2022
    Release v8.0.0, Tue Apr 19 11:02:31 EDT 2022
    Release v7.5.0, Tue Jul 13 12:56:51 EDT 2021
    Release v7.4.0, Wed Jun 10 15:43:32 EDT 2020
    Release v7.3.0, Fri Aug 2 16:17:10 EDT 2019
    Release v7.2.0, Tue Jul 10 11:20:43 EDT 2018
    Release v7.1.0, Tue Mar 6 12:00:10 EST 2018
    Release v7.0.0, Mon Jul 24 17:36:26 EDT 2017
    Release v6.4.0, Mon Jun 13 09:59:56 EDT 2016
    Release v6.3.0, Sat Nov 28 16:04:51 EST 2015
    Release v6.2.0, Tue Jul 14 10:21:30 EDT 2015
     */

}

