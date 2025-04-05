package org.example;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GitHubAPI {
    private static final Pattern LINK_PATTERN = Pattern.compile("<(.*?)>;\\s*rel=\"next\"");
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private String repo;
    private String owner;
    private String token;

    public GitHubAPI(String repo, String owner, String token) {
        this.repo = repo;
        this.owner = owner;
        this.token = token;
    }

    private Optional<String> extractNextPageURL(String linkHeader) {
        if (linkHeader == null || linkHeader.isEmpty()) {
            return Optional.empty();
        }

        Matcher matcher = LINK_PATTERN.matcher(linkHeader);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }

    public List<String> listCommits(String branch) throws IOException, InterruptedException {
        List<Commit> commits = new ArrayList<>();

        String pageUrl = String.format("https://api.github.com/repos/%s/%s/commits?sha=%s&page=1", owner, repo, branch);
        String linkHeader;

        while (pageUrl != null) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(pageUrl))
                    .header("Authorization", "token " + token)
                    .header("Accept", "application/vnd.github.v3+json")
                    .GET()
                    .build();

            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                linkHeader = response.headers().firstValue("Link").orElse(null);
                pageUrl = extractNextPageURL(linkHeader).orElse(null);

                commits.addAll(MAPPER.readValue(response.body(), new TypeReference<>() {}));
            } else {
                throw new IOException("Error: " + response.statusCode());
            }
        }

        return commits.stream().map(Commit::getSha).toList();
    }

    public List<String> compareCommits(String commit1, String commit2) throws IOException, InterruptedException {
        List<ChangedFile> changedFiles;

        String url = String.format("https://api.github.com/repos/%s/%s/compare/%s...%s", owner, repo, commit1, commit2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "token " + token)
                .header("Accept", "application/vnd.github.v3+json")
                .GET()
                .build();

        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JsonNode rootNode = MAPPER.readTree(response.body());
            JsonNode filesNode = rootNode.path("files");

            changedFiles = MAPPER.readValue(filesNode.traverse(), new TypeReference<>() {});
        } else {
            throw new IOException("Error: " + response.statusCode());
        }

        return changedFiles.stream().map(ChangedFile::getFilename).toList();
    }
}
