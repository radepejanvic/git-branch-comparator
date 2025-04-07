package org.example;

import java.util.*;

public class CommitComparingUtils {

    /**
     * Finds the most recent common commit (merge base) between local and remote commit histories.
     * The histories are provided in reverse chronological order (most recent first).
     *
     * @param local List of local commit SHA hashes in reverse chronological order (most recent first).
     * @param remote List of local commit SHA hashes in reverse chronological order (most recent first).
     * @return Optional containing the most recent common commit hash, or an empty Optional if no common commit is found.
     */
    public Optional<String> findBaseCommit(List<String> local, List<String> remote) {
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
     * @param local List of modified file paths from local commit history between the merge base and the most recent commit.
     * @param remote List of modified file paths from remote commit history between the merge base and the most recent commit.
     * @return List of file paths that are present in both lists.
     */
    public List<String> findCommonModifiedFiles(List<String> local, List<String> remote) {
        if (local.isEmpty() || remote.isEmpty()) return new ArrayList<>();

        Set<String> localSet = new HashSet<>(local);

        return remote.stream().filter(localSet::contains).toList();
    }
}
