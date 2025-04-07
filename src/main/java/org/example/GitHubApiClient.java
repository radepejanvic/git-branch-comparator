package org.example;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.exceptions.GitHubApiException;

public class GitHubApiClient {
    private static final Pattern LINK_PATTERN = Pattern.compile("<(.*?)>;\\s*rel=\"next\"");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private HttpClient client;
    private String repo;
    private String owner;
    private String token;

    public GitHubApiClient(HttpClient client, String repo, String owner, String token) {
        this.repo = repo;
        this.owner = owner;
        this.token = token;
        this.client = client;
    }

    private Optional<String> extractNextPageURL(String linkHeader) {
        if (linkHeader == null || linkHeader.isEmpty()) {
            return Optional.empty();
        }

        Matcher matcher = LINK_PATTERN.matcher(linkHeader);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }

    /**
     * Retrieves the commit history for the specified branch from the GitHub repository.
     *
     * @param branch The name of the branch for which commit history is to be fetched.
     * @return List of commit SHA hashes as strings in reverse chronological order (most recent first).
     * @throws GitHubApiException If an error occurs during the GitHub API request or response processing,
     *                            including network issues, HTTP error responses, or parsing errors.
     *
     * @see HttpClient#send(HttpRequest, HttpResponse.BodyHandler)
     * @see HttpResponse
     */
    public List<String> getCommitHistory(String branch) throws GitHubApiException {
        try {
            List<Commit> commits = new ArrayList<>();

            String pageUrl = String.format("https://api.github.com/repos/%s/%s/commits?sha=%s&page=1", owner, repo, branch);
            String linkHeader;

            while (pageUrl != null) {
                HttpRequest request = HttpRequest.newBuilder()
                        .timeout(Duration.ofSeconds(10))
                        .uri(URI.create(pageUrl))
                        .header("Authorization", "token " + token)
                        .header("Accept", "application/vnd.github.v3+json")
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    throw new GitHubApiException(String.format("GitHub get commit history failed: %s", response.statusCode()));
                }

                linkHeader = response.headers().firstValue("Link").orElse(null);
                pageUrl = extractNextPageURL(linkHeader).orElse(null);

                commits.addAll(MAPPER.readValue(response.body(), new TypeReference<>() {}));
            }

            return commits.stream().map(Commit::getSha).toList();
        } catch (JsonProcessingException e) {
            throw new GitHubApiException(String.format("Error parsing the response body: %s", e));
        } catch (IOException | InterruptedException e) {
            throw new GitHubApiException(String.format("GitHub get commit history failed: %s", e));
        }
    }

    /**
     * Retrieves the list of modified file names between two commits in the specified GitHub repository.
     *
     * @param commit1 The SHA of the first commit in the comparison.
     * @param commit2 The SHA of the second commit in the comparison.
     * @return List of modified file names between the two commits.
     * @throws GitHubApiException If an error occurs during the GitHub API request or response processing,
     *                            including network issues, HTTP error responses, or parsing errors.
     *
     * @see HttpClient#send(HttpRequest, HttpResponse.BodyHandler)
     * @see HttpResponse
     */
    public List<String> getModifiedFilesNames(String commit1, String commit2) throws GitHubApiException {
        try {
            List<ChangedFile> changedFiles;

            String url = String.format("https://api.github.com/repos/%s/%s/compare/%s...%s", owner, repo, commit1, commit2);

            HttpRequest request = HttpRequest.newBuilder()
                    .timeout(Duration.ofSeconds(10))
                    .uri(URI.create(url))
                    .header("Authorization", "token " + token)
                    .header("Accept", "application/vnd.github.v3+json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new GitHubApiException(String.format("GitHub get commit history failed: %s", response.statusCode()));
            }

            JsonNode rootNode = MAPPER.readTree(response.body());
            JsonNode filesNode = rootNode.path("files");

            changedFiles = MAPPER.readValue(filesNode.traverse(), new TypeReference<>() {});

            return changedFiles.stream().map(ChangedFile::getFilename).toList();
        } catch (JsonProcessingException e) {
            throw new GitHubApiException(String.format("Error parsing the response body: %s", e));
        } catch (IOException | InterruptedException e) {
            throw new GitHubApiException(String.format("GitHub compare commits failed: %s", e));
        }
    }
}
