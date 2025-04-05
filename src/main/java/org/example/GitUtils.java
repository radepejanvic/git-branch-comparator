package org.example;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

public class GitUtils {

    private File repoPath;

    public GitUtils(String repoPath) {
        this.repoPath = new File(repoPath);
    }

    public List<String> revList(String branchName) throws IOException, InterruptedException {
            ProcessBuilder processBuilder = new ProcessBuilder("git", "rev-list", branchName);
            processBuilder.directory(repoPath);

            Process process = processBuilder.start();

            return readOutput(process.getInputStream());
    }

    private List<String> readOutput(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines().collect(Collectors.toList());
        }
    }

}
