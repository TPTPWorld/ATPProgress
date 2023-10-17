import java.util.List;
import java.util.Map;

/**
 * Represents a data entry with fields for domain, problem, spc, and a map of ratingsVersions.
 */
public class DataEntry {
    String domain;
    String problem;
    String spc;
    Map<String, Double> ratingsVersions;

    /**
     * Initializes the fields with provided parameters
     *
     * @param domain            the domain of problem
     * @param problem           the specific problem associated with a particular domain
     * @param spc               a specific problem code or ID
     * @param ratingsVersions   a map where the keys are the version strings and values are the ratings strings
     */

    public DataEntry(String domain, String problem, String spc, Map<String, Double> ratingsVersions) {
        this.domain = domain;
        this.problem = problem;
        this.spc = spc;
        this.ratingsVersions = ratingsVersions;
    }

    public String toCSV(List<String> versions) {

        StringBuilder sb = new StringBuilder();
        String currentVersion;
        Double currentRating;
        String nextVersion;
        Double nextRating;

// loop going forwards in time through the versions to fill ratings that are omitted because they have not changed
        for (int currentVersionIndex = versions.size()-1; currentVersionIndex >= 1; currentVersionIndex--) {
            currentVersion = versions.get(currentVersionIndex);
            currentRating = ratingsVersions.getOrDefault(currentVersion,Double.NaN);
            nextVersion = versions.get(currentVersionIndex-1);
            nextRating = ratingsVersions.getOrDefault(nextVersion,Double.NaN);

// if current rating is not empty and next rating is not empty and the next rating is larger than the current rating
            if (!currentRating.isNaN() && !nextRating.isNaN() && nextRating > currentRating) {
                ratingsVersions.put(nextVersion,currentRating);
            }
// else if the current rating is not empty and nextRating is empty
            else if (!currentRating.isNaN() && nextRating.isNaN()) {
                ratingsVersions.put(nextVersion,currentRating);
            }
        }

// Loops from the newest versions to the oldest versions filling in ratings before problem added to TPTP
        for (int currentVersionIndex = 0; currentVersionIndex < versions.size()-1; currentVersionIndex++) {
            currentVersion = versions.get(currentVersionIndex);
            currentRating = ratingsVersions.getOrDefault(currentVersion,Double.NaN);
            nextVersion = versions.get(currentVersionIndex+1);
            nextRating = ratingsVersions.getOrDefault(nextVersion,Double.NaN);

// if the current rating is 1.00 and the previous rating is empty and can take 1.00, fill
            if (nextRating.isNaN() && currentRating == 1.00 &&
nextVersion.compareTo(Main.FILL_1_BEFORE) <= 0 && nextVersion.compareTo(Main.NOTHING_BEFORE) >= 0) {
                ratingsVersions.put(nextVersion, currentRating);
            }
// if the current rating is not 1.00 (must be less) and previous version is empty and can take less than 1.00, fill
            else if (nextRating.isNaN() && currentRating != 1.00 &&
nextVersion.compareTo(Main.FILL_LESS_THAN_1_BEFORE) <= 0 &&
nextVersion.compareTo(Main.NOTHING_BEFORE) >= 0) {
                ratingsVersions.put(nextVersion, currentRating);
            }
        }

        sb.append(domain).append(",").append(problem).append(",").append(spc).append(",");
        for (String version: versions) {
            sb.append(ratingsVersions.get(version)).append(",");
        }
        return sb.toString();
    }
}
