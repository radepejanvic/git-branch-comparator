package org.example.exceptions;

/**
 * Custom exception for GitHub api client failures.
 */
public class GitHubApiException extends Exception {
    public GitHubApiException(String message) {
        super(message);
    }
}
