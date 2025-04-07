package org.example.exceptions;

/**
 * Custom exception for Git command failures.
 */
public class GitCommandException extends Exception {
    public GitCommandException(String message) {
        super(message);
    }
}
