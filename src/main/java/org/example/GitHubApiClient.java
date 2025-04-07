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

public class GitHubApiClient {
    private static final Pattern LINK_PATTERN = Pattern.compile("<(.*?)>;\\s*rel=\"next\"");

    private HttpClient client;
    private ObjectMapper mapper;
    private String repo;
    private String owner;
    private String token;

    public GitHubApiClient(HttpClient client, ObjectMapper mapper, String repo, String owner, String token) {
        this.repo = repo;
        this.owner = owner;
        this.token = token;
        this.client = client;
        this.mapper = mapper;
    }

    private Optional<String> extractNextPageURL(String linkHeader) {
        if (linkHeader == null || linkHeader.isEmpty()) {
            return Optional.empty();
        }

        Matcher matcher = LINK_PATTERN.matcher(linkHeader);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }

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

                commits.addAll(mapper.readValue(response.body(), new TypeReference<>() {}));
            }

            return commits.stream().map(Commit::getSha).toList();
        } catch (JsonProcessingException e) {
            throw new GitHubApiException(String.format("Error parsing the response body: %s", e));
        } catch (IOException | InterruptedException e) {
            throw new GitHubApiException(String.format("GitHub get commit history failed: %s", e));
        }
    }

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

            JsonNode rootNode = mapper.readTree(response.body());
            JsonNode filesNode = rootNode.path("files");

            changedFiles = mapper.readValue(filesNode.traverse(), new TypeReference<>() {});

            return changedFiles.stream().map(ChangedFile::getFilename).toList();
        } catch (JsonProcessingException e) {
            throw new GitHubApiException(String.format("Error parsing the response body: %s", e));
        } catch (IOException | InterruptedException e) {
            throw new GitHubApiException(String.format("GitHub compare commits failed: %s", e));
        }
    }

    public class GitHubApiException extends Exception {
        public GitHubApiException(String message) {
            super(message);
        }
    }
}
