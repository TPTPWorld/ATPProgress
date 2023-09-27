import java.util.*;
import java.io.*;

public class Main {


    private static void writeToCSV(List<DataEntry> entries, Set<String> versionSet, String outputFile) {
        List<String> versions = new ArrayList<>(versionSet);
        Collections.sort(versions);
        Collections.reverse(versions);

        Map<String, Integer> versionCounts = new HashMap<>(); // Map to store the count of each version

        try (FileWriter writer = new FileWriter(outputFile)) {
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


    private static List<String> getDomains(String fileDirectory) {
        List<String> domainNames = new ArrayList<>();
        File directory = new File(fileDirectory);
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





    private static List<DataEntry> collectData(String fileDirectory, Set<String> versions) {
        List<DataEntry> entries = new ArrayList<>();
        List<String> domains = getDomains(fileDirectory);
        int counter = 0;
        for (String domain : domains) {
            String newDirectory = fileDirectory + "/" + domain;
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
//                        if (counter > 500)
//                            return entries;
                    }
                }
            }
        }
        return entries;
    }

    public static void main(String[] args) {
        String fileDirectory = "C:/Users/fam/Documents/UM/Research/TPTP-v8.2.0/Problems";
//        fileDirectory = "/Users/geoff/MyDocuments/Development/ATPProgress/Problems";
        String outputFile = "output_data.csv";
        String nothingBefore = "v4.0.0"; //----Do not keep data before this release
        String fillLessThan1Before = "v5.0.0"; //----Can fill with rating < 1.00 before this release
        Set<String> versions = new HashSet<>(); // Use this to create non-repetitive columns in csv *MISSING SOME*
        List<DataEntry> entries = collectData(fileDirectory, versions);

        for (DataEntry entry : entries) {
//            System.out.println("ENTRY : " + entry);
//            System.out.println("ENTRY : RATINGS_VERSION : HASHMAP : " + entry.ratingsVersions);
//            System.out.println("ENTRY : RATINGS_VERSIONS : KEYSET : " + entry.ratingsVersions.keySet());
//            System.out.println("ENTRY : RATINGS_VERSIONS : ENTRYSET : " + entry.ratingsVersions.entrySet());
            System.out.println("FULL DATA : " + entry.domain + " : " + entry.problem + " : " + entry.spc + " : "
                    + entry.ratingsVersions.keySet() + " : " + entry.ratingsVersions.entrySet());
        }

        for (String version : versions)
            System.out.println(version);

        writeToCSV(entries, versions, outputFile);

    }

}

