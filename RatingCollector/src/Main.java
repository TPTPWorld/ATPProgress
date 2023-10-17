import java.io.*;
import java.util.*;

public class Main {

    private static final String CWD = "/Users/geoff/MyDocuments/Development/ATPProgress";
// "C:/Users/fam/Documents/UM/TPTPRESEARCH/TPTPOrganizer
    public static final String TPTP_PROBLEMS_DIRECTORY = CWD + "/TPTP-v8.2.0/Problems";
    public static final String OUTPUT_CSV_FILE = CWD + "/output_data.csv";
    public static final String NOTHING_BEFORE = "v2.0.0"; //----Do not keep data before this release
    public static final String FILL_1_BEFORE = "v8.2.0";
    public static final String FILL_LESS_THAN_1_BEFORE = "v8.2.0"; //----Can fill with rating < 1.00 before this release

    private static boolean hasMissingDataAfterVersion(DataEntry entry) {
        List<String> versions = new ArrayList<>(entry.ratingsVersions.keySet());
        Collections.sort(versions);

        System.out.println("Check that there is all the data for "+entry.problem);
        // Find the index of the given version
        int lastIndex = versions.indexOf(NOTHING_BEFORE);

        // If version doesn't exist in the list, then we assume the data is missing for that version
        if (lastIndex == -1) {
            System.out.println("No column for " + entry.problem + " at " + NOTHING_BEFORE);
            return true;
        }

        for (int versionIndex = lastIndex; versionIndex < versions.size(); versionIndex++) {
            // If the rating in that version is empty, then return true.
            if (entry.ratingsVersions.get(versions.get(versionIndex)).isNaN()) {
                System.out.println("Problem "+entry.problem + " missing data for " + versions.get(versionIndex));
                return true;
            }
        }
        return false;
    }

//    private static void removeOldData() {
//        List<String> adjustedLines = new ArrayList<>();
//        try (BufferedReader reader = new BufferedReader(new FileReader(OUTPUT_CSV_FILE))) {
//            String header = reader.readLine();
//            if (header != null) {
//                String[] headerColumns = header.split(",");
//                adjustedLines.add(header);  // add header as is
//                List<Integer> columnsToRemove = new ArrayList<>();
//                // Start from the 4th column since the first 3 are not version columns.
//                for (int i = 3; i < headerColumns.length; i++) {
//                    if (headerColumns[i].compareTo(NOTHING_BEFORE) < 0) { // if version is before "v4.0.0"
//                        columnsToRemove.add(i);
//                    }
//                }
//
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    String[] values = line.split(",");
//                    StringBuilder adjustedLine = new StringBuilder();
//                    for (int i = 0; i < values.length; i++) {
//                        if (!columnsToRemove.contains(i)) {
//                            if (adjustedLine.length() > 0) {
//                                adjustedLine.append(",");
//                            }
//                            adjustedLine.append(values[i]);
//                        }
//                    }
//                    adjustedLines.add(adjustedLine.toString());
//                }
//            }
//        } catch (IOException e) {
//            System.out.println("Error reading CSV : " + e.getMessage());
//        }
//
//        // Now, rewrite the CSV with the adjusted lines
//        try (FileWriter writer = new FileWriter(OUTPUT_CSV_FILE)) {
//            for (String line : adjustedLines) {
//                writer.write(line + "\n");
//            }
//        } catch (IOException e) {
//            System.out.println("Error writing adjusted CSV : " + e.getMessage());
//        }
//    }

    private static void writeToCSV(List<DataEntry> entries, Set<String> versionSet) {
        List<String> versions = new ArrayList<>(versionSet);
        Collections.sort(versions);
        Collections.reverse(versions);

        Map<String, Integer> versionCounts = new HashMap<>(); // Map to store the count of each version

        try (FileWriter writer = new FileWriter(OUTPUT_CSV_FILE)) {
            writer.write("Domain,Problem,SPC");
            for (String version : versions) {
                writer.write("," + version);
            }
            writer.write("\n");

            for (DataEntry entry : entries) {
                writer.write(entry.toCSV(versions) + "\n");

                // Calculate the count for each version
                for (String version : versions) {
                    if (entry.ratingsVersions.containsKey(version) && !entry.ratingsVersions.get(version).isNaN()) {
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

    private static List<String> getDomains() {

        List<String> domainNames = new ArrayList<>();
        File directory = new File(TPTP_PROBLEMS_DIRECTORY);

        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files)
                    domainNames.add(file.getName());
            }
        }
        Collections.sort(domainNames);
        return domainNames;
    }

    private static void parseRatingsAndVersions(String line, ExtractedData data) {

//System.out.println("LINE: "+line);
        String[] pairs = line.replace("% Rating   : ", "").split(","); // "0.01 v8.10"
        for (String pair : pairs) {
//System.out.println("One pair "+pair);
            pair = pair.trim();
            String[] parts = pair.split(" ");


            if (parts.length == 2) {
                try {
//System.out.println(("Release " + parts[0] + " rating " + parts[1]));
                    parts[0] = parts[0].trim().replaceAll("[^ .,a-zA-Z0-9]","");
                    data.ratingsVersions.put(parts[1],Double.valueOf(parts[0]));
                } catch (NumberFormatException e) {
                }
            }
        }
    }

    private static ExtractedData extractedData(File file) {
        ExtractedData data = new ExtractedData();

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("% SPC      : ")) {
                    data.spc = line.replace("% SPC      : ", "").trim();
                    return data;
//DEBUG System.out.println("SUCCESS : SPC : " + data.spc);
                } else if (line.startsWith("% Rating   : ")) { // "0.01 v8.10, 0.02 v7.9.9"
                    parseRatingsAndVersions(line, data);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("ERROR File not found ");
        }
        return data;
    }

    private static List<DataEntry> collectData(Set<String> versions) {

        List<DataEntry> entries = new ArrayList<>();
        List<String> domains = getDomains();

        int counter = 0;
        for (String domain : domains) {
            String newDirectory = TPTP_PROBLEMS_DIRECTORY + "/" + domain;
            File directory = new File(newDirectory);
            if (directory.isDirectory()) {
                File[] files = directory.listFiles();
                Arrays.sort(files,(a, b) -> a.getName().compareTo(b.getName()));
                if (files != null) {
                    for (File file : files) {
                        if (!file.toString().endsWith(".rm")) {
//System.out.println("Processing in " + domain + " number " + counter + " : " + file);
                            ExtractedData data = extractedData(file);
                            versions.addAll(data.ratingsVersions.keySet());
                            DataEntry entry = new DataEntry(domain,file.getName(),data.spc,data.ratingsVersions);
                            entries.add(entry);
//System.out.println("For "+entry.problem+" the rating for 8.2.0 is "+entry.ratingsVersions.get("v8.2.0"));
                            counter++;
//                        if (counter > 1000)
//                            return entries;
                        }
                    }
                }
            }
        }
        return entries;
    }

    public static void main(String[] args) {

        Set<String> versions = new HashSet<>(); // Use this to create non-repetitive columns in csv *MISSING SOME*
        List<DataEntry> entries = collectData(versions);
System.out.println("Read " + entries.size() + " problems data");
//System.out.print("The versions are ");
//for (String version: versions) { System.out.print(version+ " ");} System.out.println();
//        for (DataEntry entry : entries) {
//            System.out.println("FULL DATA : " + entry.domain + " : " + entry.problem + " : " + entry.spc + " : "
//                    + entry.ratingsVersions.keySet() + " : " + entry.ratingsVersions.entrySet());
//            System.out.println(entries);
//        }

//        List<DataEntry> filteredEntries = entries.stream()
//                .filter(entry -> !hasMissingDataAfterVersion(entry))
//                .collect(Collectors.toList());

        writeToCSV(entries,versions);
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

