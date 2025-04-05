package org.example;

import java.util.List;
import java.util.Optional;

public class DiffChecker {

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

}
