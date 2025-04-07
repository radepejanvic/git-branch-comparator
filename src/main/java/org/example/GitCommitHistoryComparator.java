package org.example;

import org.example.exceptions.GitCommandException;
import org.example.exceptions.GitHubApiException;

import java.util.*;

public class GitCommitHistoryComparator {

    private final GitCommandExecutor git;
    private final GitHubApiClient github;

    public GitCommitHistoryComparator(GitCommandExecutor gitCommandExecutor, GitHubApiClient githubApiClient) {
        this.git = gitCommandExecutor;
        this.github = githubApiClient;
    }

    /**
     * Finds the common modified files between the local and remote branches by comparing their commit histories.
     * This method retrieves the commit history of both branches, identifies the common base commit, and
     * then compares the list of modified files between the two branches from that base commit up to the most
     * recent commit in each branch. It returns the list of files that have been modified in both branches.
     *
     * @param localBranch The name of the local branch for which commit history is to be fetched.
     * @param remoteBranch The name of the remote branch for which commit history is to be fetched.
     * @return List of file paths that have been modified in both the local and remote branches,
     *         from the common base commit to the most recent commits.
     * @throws GitCommandException If an error occurs while executing Git commands to retrieve commit history
     *                             or modified files for the local branch.
     * @throws GitHubApiException If an error occurs while fetching commit history or modified files for the
     *                             remote branch through the GitHub API.
     *
     * @see GitCommandExecutor#getCommitHistory(String)
     * @see GitHubApiClient#getCommitHistory(String)
     * @see GitCommandExecutor#getModifiedFilesNames(String, String)
     * @see GitHubApiClient#getModifiedFilesNames(String, String)
     */
    public List<String> compareModifiedFilesBetweenBranches(String localBranch, String remoteBranch) throws GitCommandException, GitHubApiException {
        List<String> localCommits = git.getCommitHistory(localBranch);
        List<String> remoteCommits = github.getCommitHistory(remoteBranch);

        String baseCommit = findBaseCommit(localCommits, remoteCommits).orElse(null);

        List<String> localModifiedFiles = git.getModifiedFilesNames(baseCommit, localCommits.getFirst());
        List<String> remoteModifiedFiles = github.getModifiedFilesNames(baseCommit, remoteCommits.getFirst());

        return findCommonModifiedFiles(localModifiedFiles, remoteModifiedFiles);
    }

    /**
     * Finds the most recent common commit (merge base) between local and remote commit histories.
     * The histories are provided in reverse chronological order (most recent first).
     *
     * @param local List of local commit SHA hashes in reverse chronological order (most recent first).
     * @param remote List of local commit SHA hashes in reverse chronological order (most recent first).
     * @return Optional containing the most recent common commit hash, or an empty Optional if no common commit is found.
     */
    private Optional<String> findBaseCommit(List<String> local, List<String> remote) {
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
    private List<String> findCommonModifiedFiles(List<String> local, List<String> remote) {
        if (local.isEmpty() || remote.isEmpty()) return new ArrayList<>();

        Set<String> localSet = new HashSet<>(local);

        return remote.stream().filter(localSet::contains).toList();
    }
}
