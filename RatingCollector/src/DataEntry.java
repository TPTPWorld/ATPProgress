import java.util.*;

/**
 * Represents a data entry with fields for domain, problem, spc, and a map of ratingsVersions.
 */
public class DataEntry {
    String domain;
    String problem;
    String spc;
    Map<String, String> ratingsVersions;

    /**
     * Initializes the fields with provided parameters
     *
     * @param domain            the domain of problem
     * @param problem           the specific problem associated with a particular domain
     * @param spc               a specific problem code or ID
     * @param ratingsVersions   a map where the keys are the version strings and values are the ratings strings
     */

    public DataEntry(String domain, String problem, String spc, Map<String, String> ratingsVersions) {
        this.domain = domain;
        this.problem = problem;
        this.spc = spc;
        this.ratingsVersions = ratingsVersions;
    }

    /**
     * Converts data entry to a CSV file based on strings from versions list
     *
     * @param versions          a list of version strings
     * @return a CSV string representation of the data entry
     */
    public String toCSV(List<String> versions) {
        StringBuilder sb = new StringBuilder();
        List<String> newLists = new ArrayList<>(Collections.nCopies(versions.size(), ""));

        // backwards for loop going through the versions list
        for (int i = versions.size() - 1; i >= 0; i--) {
            String currentVersion = versions.get(i);
            String currentRating = ratingsVersions.getOrDefault(currentVersion, "");
            String nextVersion = i > 0 ? versions.get(i -1) : currentVersion;
            String nextRating = ratingsVersions.getOrDefault(nextVersion, "");
            String prevVersion = i < versions.size() - 1  ? versions.get(i + 1) : currentVersion;
            String prevRating = ratingsVersions.getOrDefault(prevVersion, "");

            // if current rating is not empty and next rating is not empty and the next rating is larger than the current rating
            if (!currentRating.isEmpty() && !nextRating.isEmpty() && Double.parseDouble(nextRating) > Double.parseDouble(currentRating)) {
                ratingsVersions.put(nextVersion, currentRating);
                newLists.set(i, currentRating); // adding it to a newList
            }
            // else if the current rating is not empty and not equal to one and nextRating is empty
            else if (!currentRating.isEmpty() && nextRating.isEmpty()) {
                ratingsVersions.put(nextVersion, currentRating);
                newLists.set(i, currentRating);
            }
            else {
                newLists.set(i, currentRating);
            }

        }

        // Loops from the newest versions to the oldest versions
        for (int i = 0; i < versions.size(); i++) {
            String currentVersion = versions.get(i);
            String currentRating = ratingsVersions.getOrDefault(currentVersion, "");
            String prevVersion = i < versions.size() - 1 ? versions.get(i + 1) : currentVersion;
            String prevRating = ratingsVersions.getOrDefault(prevVersion, "");

            // if the previous rating is empty and the current is 1.00, fill previous with 1.00
            if (prevRating.isEmpty() && currentRating.equals("1.00")) {
                ratingsVersions.put(prevVersion, currentRating);
                newLists.add(i, currentRating);
            }
        }

        return sb.append(domain).append(",")
                .append(problem).append(",")
                .append(spc).append(",")
                .append(String.join(",", newLists)).toString();
    }

}
