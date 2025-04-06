package org.example;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GitCommandExecutor {

    private final File repo;
    public final CommandUtils commandUtils;

    public GitCommandExecutor(String repoPath, CommandUtils commandUtils) {
        repo = new File(repoPath);
        this.commandUtils = commandUtils;
    }

    /**
     * Retrieves the commit history for the specified branch.
     *
     * @param branchName The name of the branch to retrieve commits for.
     * @return List of commit hashes in reverse order, sorted from the latest to the oldest.
     * @throws GitCommandException If Git command fails or the branch is invalid.
     * @throws IOException If an I/O error occurs during process execution.
     * @throws InterruptedException If the process is interrupted.
     */
    public List<String> getCommitHistory(String branchName) throws GitCommandException, IOException, InterruptedException {
            Process process = commandUtils.executeCommand(repo, List.of("git", "rev-list", branchName));

            String output = commandUtils.readOutput(process.getInputStream());
            String errorOutput = commandUtils.readOutput(process.getErrorStream());

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new GitCommandException(String.format("Git rev-list command failed for branch: %s. Error: %s", branchName, errorOutput));
            }

            return Arrays.asList(output.split("\n"));
    }

    /**
     * Retrieves the list of files modified between 2 commits.
     *
     * @param commit1 The first commit hash.
     * @param commit2 The second commit hash.
     * @return List of modified file paths.
     * @throws GitCommandException If Git command fails.
     * @throws IOException If an I/O error occurs during process execution.
     * @throws InterruptedException If the process is interrupted.
     */
    public List<String> getModifiedFilesNames(String commit1, String commit2) throws GitCommandException, IOException, InterruptedException {
        Process process = commandUtils.executeCommand(repo, List.of("git", "diff", "--name-only", commit1, commit2));

        String output = commandUtils.readOutput(process.getInputStream());
        String errorOutput = commandUtils.readOutput(process.getErrorStream());

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new GitCommandException(String.format("Git diff --name-only command failed commits: %s and %s. Error: %s", commit1, commit2, errorOutput));
        }

        return Arrays.asList(output.split("\n"));
    }

    /**
     * Custom exception for Git command failures.
     */
    public class GitCommandException extends Exception {
        public GitCommandException(String message) {
            super(message);
        }
    }

}
