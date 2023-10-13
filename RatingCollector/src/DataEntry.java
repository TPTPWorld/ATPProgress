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

// loop going forwards in time through the versions to fill ratings that are omitted because they have not changed
        for (int currentVersionIndex = versions.size()-1; currentVersionIndex >= 1; currentVersionIndex--) {
            String currentVersion = versions.get(currentVersionIndex);
            String currentRating = ratingsVersions.getOrDefault(currentVersion, "");
            String nextVersion = versions.get(currentVersionIndex-1);
            String nextRating = ratingsVersions.getOrDefault(nextVersion, "");

// if current rating is not empty and next rating is not empty and the next rating is larger than the current rating
            if (!currentRating.isEmpty() && !nextRating.isEmpty() &&
Double.parseDouble(nextRating) > Double.parseDouble(currentRating)) {
                ratingsVersions.put(nextVersion, currentRating);
                newLists.set(currentVersionIndex, currentRating); // adding it to a newList
            }
// else if the current rating is not empty and nextRating is empty
            else if (!currentRating.isEmpty() && nextRating.isEmpty()) {
                ratingsVersions.put(nextVersion, currentRating);
                newLists.set(currentVersionIndex, currentRating);
            }
            else {
                newLists.set(currentVersionIndex, currentRating);
            }
        }

// Loops from the newest versions to the oldest versions filling in ratings before problem added to TPTP
        for (int currentVersionIndex = 0; currentVersionIndex < versions.size()-1; currentVersionIndex++) {
            String currentVersion = versions.get(currentVersionIndex);
            String currentRating = ratingsVersions.getOrDefault(currentVersion, "");
            String prevVersion = versions.get(currentVersionIndex+1);
            String prevRating = ratingsVersions.getOrDefault(prevVersion, "");

// if the current rating is 1.00 and the previous rating is empty and can take 1.00, fill
            if (prevRating.isEmpty() && currentRating.equals("1.00") &&
prevVersion.compareTo(Main.FILL_1_BEFORE) <= 0 && prevVersion.compareTo(Main.NOTHING_BEFORE) >= 0) {
                ratingsVersions.put(prevVersion, currentRating);
                newLists.add(currentVersionIndex, currentRating);
            }
// if the current rating is not 1.00 (must be less) and previous version is empty and can take less than 1.00, fill
            else if (prevRating.isEmpty() && !currentRating.equals("1.00") &&
prevVersion.compareTo(Main.FILL_LESS_THAN_1_BEFORE) <= 0 &&
prevVersion.compareTo(Main.NOTHING_BEFORE) >= 0) {
                ratingsVersions.put(prevVersion, currentRating);
                newLists.add(currentVersionIndex, currentRating);
            }
        }

        return sb.append(domain).append(",")
                .append(problem).append(",")
                .append(spc).append(",")
                .append(String.join(",", newLists)).toString();
    }

}
