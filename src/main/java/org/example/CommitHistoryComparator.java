package org.example;

import java.util.*;

public class CommitHistoryComparator {

    /**
     * Finds the most recent common commit (merge base) between local and remote commit histories.
     * The histories are provided in reverse order (from latest to oldest).
     *
     * @param local List of local commit hashes in reverse order, from the latest to the oldest.
     * @param remote List of local commit hashes in reverse order, from the latest to the oldest.
     * @return Optional containing the most recent common commit hash, or an empty Optional if no common commit is found.
     */
    public static Optional<String> findBaseCommit(List<String> local, List<String> remote) {
        if (local.isEmpty() || remote.isEmpty()) return Optional.empty();

        int i = local.size() - 1;
        int j = remote.size() - 1;

        while (i >= 0 && j >= 0 && local.get(i).equals(remote.get(j))) {
            i--;
            j--;
        }

        if (i >= 0) return Optional.of(local.get(++i));
        else if (j >= 0) return Optional.of(remote.get(++j));
        else return Optional.empty();
    }

    /**
     * Finds the common modified files paths between given local and remote lists.
     *
     * @param local List of modified file paths from local commit history between the merge base and the last commit.
     * @param remote List of modified file paths from remote commit history between the merge base and the last commit.
     * @return List of file paths that are present in both lists.
     */
    public static List<String> findCommonModifiedFiles(List<String> local, List<String> remote) {
        if (local.isEmpty() || remote.isEmpty()) return new ArrayList<>();

        Set<String> localSet = new HashSet<>(local);

        return remote.stream().filter(localSet::contains).toList();
    }
}
