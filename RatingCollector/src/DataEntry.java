import java.util.*;

public class DataEntry {
    String domain;
    String problem;
    String spc;
    Map<String, String> ratingsVersions;

    public DataEntry(String domain, String problem, String spc, Map<String, String> ratingsVersions) {
        this.domain = domain;
        this.problem = problem;
        this.spc = spc;
        this.ratingsVersions = ratingsVersions;
    }

    public String toCSV(List<String> versions) {
        StringBuilder sb = new StringBuilder();
        List<String> newLists = new ArrayList<>(Collections.nCopies(versions.size(), ""));

        // backwards for loop going through the versions list
        for (int i = versions.size() - 1; i >= 0; i--) {
            try {
                String currentVersion = versions.get(i);
                String currentRating = ratingsVersions.getOrDefault(currentVersion, "");
                String nextVersion = "";

                // To prevent out of bounds exception.
                if (i != 0) {
                    nextVersion = versions.get(i - 1);
                } else {
                    nextVersion = versions.get(i);
                }
                String nextRating = ratingsVersions.getOrDefault(nextVersion, "");
                String prevVersion = versions.get(i + 1);
                String prevRating = ratingsVersions.getOrDefault(prevVersion, "");
                // if current rating is not empty and next rating is not empty and the next rating is larger than the current rating
                if (!currentRating.isEmpty() && !nextRating.isEmpty() && Double.parseDouble(nextRating) > Double.parseDouble(currentRating)) {
                    ratingsVersions.put(nextVersion, currentRating);

                    // adding it to a newList
                    newLists.set(i, currentRating);
                }

                // else if the current rating is not empty and not equal to one and nextRating is empty
                else if (!currentRating.isEmpty() && !currentRating.equals("1.00") && nextRating.isEmpty()) {
                    ratingsVersions.put(nextVersion, currentRating);
                    newLists.set(i, currentRating);
                }
                // else if current rating is 1 and previous rating is empty
                else if (currentRating.equals("1.00") && prevRating.isEmpty()) {
//                    System.out.println("RATING SHOULD BE ONE : " + currentRating);
                    ratingsVersions.put(prevVersion, currentRating); // previous rating is also one
                    newLists.set(i, currentRating);
                }

                else
                    newLists.set(i, currentRating);
            } catch (IndexOutOfBoundsException e) {
                System.out.println(e.getMessage());
            }

        }

        // For loop to go through entire list of versions
//        for (int i = 0; i < versions.size(); i++) {
//            try {
//                String currentVersion = versions.get(i);
//                String currentRating = ratingsVersions.getOrDefault(currentVersion, "");
//                String prevVersion = versions.get(i + 1);
//                String prevRating = ratingsVersions.getOrDefault(prevVersion, "");
//
//                // if the currentRating is 1.00 and previous version does not contain a rating
//                if (currentRating.equals("1.00") && prevRating.isEmpty()) {
//                    System.out.println("RATING SHOULD BE ONE : " + currentRating);
//                    ratingsVersions.put(prevVersion, currentRating);
//                    newLists.set(i, currentRating);
//                } else
//                    newLists.set(i, currentRating);
//            } catch (IndexOutOfBoundsException e) {
//                System.out.println(e.getMessage());
//            }
//        }

        return sb.append(domain).append(",")
                .append(problem).append(",")
                .append(spc).append(",")
                .append(String.join(",", newLists)).toString();
    }

//    public String toCSV2(List<String> versions) {
//        StringBuilder sb = new StringBuilder();
//        List<String> newLists = new ArrayList<>(Collections.nCopies(versions.size(), ""));
//
//        for (int i = versions.size() - 1; i > 0; i--) {
//            String currentVersion = versions.get(i);
//            String currentRating = ratingsVersions.getOrDefault(currentVersion, "");
//
//            if (!currentRating.isEmpty() && !currentRating.equals("1")) {
//                if (currentRating.equals("?"))
//                    System.out.println("Current rating  : " + currentRating);
//                try {
//                    String nextVersion = versions.get(i - 1);
//                    String nextRating = ratingsVersions.getOrDefault(nextVersion, "");
//
//                    if (!nextRating.isEmpty()) {
//                        if (Double.parseDouble(nextRating) > Double.parseDouble(currentRating))
//                            ratingsVersions.put(nextVersion, currentRating);
//                        else if (nextRating.equals("1"))
//                            System.out.println("Value  : " + nextRating);
//                    }
//                } catch (ArrayIndexOutOfBoundsException e) {
//                    System.out.println(e.getMessage());
//                }
//            }
//
//            else if (currentRating.equals("1")) {
//
//            }
//
//        }
//        return null;
//
//    }

//    public String toCSV(List<String> versions) {
//        StringBuilder sb = new StringBuilder();
//
//        List<String> newLists = new ArrayList<>(Collections.nCopies(versions.size(), "")); // initialize with empty strings
//        String lastRating = "";

//        for (int i = 0; i < versions.size(); i++) {
//            String version = versions.get(i);
//            String current = "";
//
//            if (ratingsVersions.containsKey(version)) {
//                current = ratingsVersions.get(version);
//            }
//
//            // If the rating is "1", set all the ratings from this index to the end of the list to "1"
//            if(current.equals("1")) {
//                newLists.set(i, "1");
//                // No need to check the previous versions since they are all set to "1" now
//            }
////            else {
////                newLists.set(i, current);
////            }
//        }


//        for (int i = versions.size() - 1; i >= 0; i--) {
//            String version = versions.get(i);
//            String currentRating = ratingsVersions.getOrDefault(version, ""); // get the current rating, default to empty string if not found
//
//            // Only continue if the current rating is not empty.
//            if (!currentRating.isEmpty() && !currentRating.equals("1")) {
//                for (int j = i - 1; j >= 0; j--) {
//                    String laterVersion = versions.get(j);
//                    String earlierRating = ratingsVersions.getOrDefault(laterVersion, ""); // get the earlier rating, default to empty string if not found
//
//                    // Check if the earlier version is empty and update it with the current rating if needed
//                    if (earlierRating.isEmpty() || earlierRating.equals("?")) {
//                        System.out.println(earlierRating);
//                        ratingsVersions.put(laterVersion, currentRating);
//                    }
//                }
//            }
//
//            for(int j = i - 1; j >= 0; j--) {
//                String earlierVersion = versions.get(j);
//                String earlierRating = ratingsVersions.getOrDefault(earlierVersion, ""); // get the earlier rating, default to empty string if not found
//                try {
//                    // Check if the earlier rating is larger than the current rating and update if needed
//                    if (!earlierRating.equals("1")
//                            && !currentRating.equals("1")
//                            && !earlierRating.isEmpty()
//                            && !currentRating.isEmpty()
//                            && Double.parseDouble(earlierRating) > Double.parseDouble(currentRating)) {
//                        ratingsVersions.put(earlierVersion, currentRating);
//                    }
//                } catch (NumberFormatException e) {
//                    continue;
//                }
//            }
//            newLists.set(i, currentRating);
//        }
//
////        for (int i = 0; i < versions.size(); i++) {
////            String version = versions.get(i);
////            String current = "";
////
////            if (ratingsVersions.containsKey(version)) {
////                current = ratingsVersions.get(version);
////            }
////
////            // If the rating is "1", set all the ratings from this index to the end of the list to "1"
////            if(current.equals("1")) {
////                newLists.set(i, "1");
////                // No need to check the previous versions since they are all set to "1" now
////            }
//////            else {
//////                newLists.set(i, current);
//////            }
////        }
//
//        return sb.append(domain).append(",")
//                .append(problem).append(",")
//                .append(spc).append(",")
//                .append(String.join(",", newLists)).toString();
//    }


//    public String toCSV(List<String> versions) {
//        StringBuilder sb = new StringBuilder();
//
//        List<String> newLists = new ArrayList<>();
//        // Collections.reverse(versions);
//        String lastRating = "";
//        for (int i = versions.size() - 1; i >= 0; i--) {
//            String version = versions.get(i);
//            if (ratingsVersions.containsKey(version)) {
//                lastRating = ratingsVersions.get(version);
//            }
//
//            newLists.add(0, lastRating);
//        }
//
//        return sb.append(domain).append(",")
//                .append(problem).append(",")
//                .append(spc).append(",")
//                .append(String.join(",", newLists)).toString();
//    }

}
