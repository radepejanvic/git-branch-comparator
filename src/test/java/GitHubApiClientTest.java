import org.example.GitHubApiClient;
import org.example.exceptions.GitHubApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GitHubApiClientTest {

    @Mock
    private HttpClient mockClient;

    private GitHubApiClient gitHubApiClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        gitHubApiClient = new GitHubApiClient(mockClient, "repo", "owner", "token");
    }

    @Test
    @DisplayName("Test getCommitHistory() - Single pages response - Success")
    void testGetCommitHistory_Success() throws Exception {
        String branchName = "main";
        String mockResponseBody = "[{\"sha\": \"commit1\"}, {\"sha\": \"commit2\"}, {\"sha\": \"commit3\"}]";

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(mockResponseBody);
        when(mockResponse.headers()).thenReturn(HttpHeaders.of(new HashMap<>(), (k, v) -> true));

        when(mockClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(mockResponse);

        List<String> commitHistory = gitHubApiClient.getCommitHistory(branchName);

        assertNotNull(commitHistory);
        assertEquals(3, commitHistory.size());
        assertEquals("commit1", commitHistory.get(0));
        assertEquals("commit2", commitHistory.get(1));
        assertEquals("commit3", commitHistory.get(2));
    }

    @Test
    @DisplayName("Test getCommitHistory() - Multiple pages response - Success")
    void testGetCommitHistoryPagination() throws Exception {
        String branchName = "main";
        String page1ResponseBody = "[{\"sha\": \"commit1\"}, {\"sha\": \"commit2\"}]";
        String page2ResponseBody = "[{\"sha\": \"commit3\"}]";
        HttpResponse<String> mockResponsePage1 = mock(HttpResponse.class);
        HttpResponse<String> mockResponsePage2 = mock(HttpResponse.class);

        when(mockResponsePage1.statusCode()).thenReturn(200);
        when(mockResponsePage1.body()).thenReturn(page1ResponseBody);
        Map<String, List<String>> headersPage1 = new HashMap<>();
        headersPage1.put("Link", List.of("<https://api.github.com/repos/owner/repo/commits?sha=main&page=2>; rel=\"next\""));
        when(mockResponsePage1.headers()).thenReturn(HttpHeaders.of(headersPage1, (k, v) -> true));

        when(mockResponsePage2.statusCode()).thenReturn(200);
        when(mockResponsePage2.body()).thenReturn(page2ResponseBody);
        when(mockResponsePage2.headers()).thenReturn(HttpHeaders.of(new HashMap<>(), (k, v) -> true));

        when(mockClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(mockResponsePage1)
                .thenReturn(mockResponsePage2);

        List<String> commitHistory = gitHubApiClient.getCommitHistory(branchName);

        assertNotNull(commitHistory);
        assertEquals(3, commitHistory.size());
        assertEquals("commit1", commitHistory.get(0));
        assertEquals("commit2", commitHistory.get(1));
        assertEquals("commit3", commitHistory.get(2));
    }

    @Test
    @DisplayName("Test getCommitHistory() - Unsuccessful response - GitCommandException")
    void testGetCommitHistory_UnsuccessfulResponse_ThrowsGitHubApiException() throws Exception {
        String branchName = "main";
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(500);
        when(mockResponse.body()).thenReturn("Internal Server Error");

        when(mockClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(mockResponse);

        GitHubApiException exception = assertThrows(GitHubApiException.class, () -> {
            gitHubApiClient.getCommitHistory(branchName);
        });

        assertTrue(exception.getMessage().contains("GitHub get commit history failed: 500"));
    }

    @Test
    @DisplayName("Test getCommitHistory() - Invalid JSON - GitCommandException")
    void testGetCommitHistory_InvalidJSON_ThrowsGitHubApiException() throws Exception {
        String branchName = "main";
        String invalidResponseBody = "Invalid JSON";

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(invalidResponseBody);
        when(mockResponse.headers()).thenReturn(HttpHeaders.of(new HashMap<>(), (k, v) -> true));

        when(mockClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(mockResponse);

        GitHubApiException exception = assertThrows(GitHubApiException.class, () -> {
            gitHubApiClient.getCommitHistory(branchName);
        });

        assertTrue(exception.getMessage().contains("Error parsing the response body"));
    }

    @Test
    @DisplayName("Test getCommitHistory() - Network error - GitCommandException")
    void testGetCommitHistory_NetworkError_ThrowsGitHubApiException() throws Exception {
        String branchName = "main";

        when(mockClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString())))
                .thenThrow(new IOException("Network error"));

        GitHubApiException exception = assertThrows(GitHubApiException.class, () -> {
            gitHubApiClient.getCommitHistory(branchName);
        });

        assertTrue(exception.getMessage().contains("Network error"));
    }

    @Test
    @DisplayName("Test getModifiedFilesNames() - Success")
    void testGetModifiedFilesNames() throws Exception {
        String commit1 = "commit1";
        String commit2 = "commit2";
        String mockResponseBody = "{ \"files\": [ { \"filename\": \"file1.txt\" }, { \"filename\": \"file2.java\" } ] }";

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(mockResponseBody);
        when(mockResponse.headers()).thenReturn(HttpHeaders.of(new HashMap<>(), (k, v) -> true));

        when(mockClient.send(any(), eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(mockResponse);

        List<String> modifiedFiles = gitHubApiClient.getModifiedFilesNames(commit1, commit2);

        assertNotNull(modifiedFiles);
        assertEquals(2, modifiedFiles.size());
        assertTrue(modifiedFiles.contains("file1.txt"));
        assertTrue(modifiedFiles.contains("file2.java"));
    }

    @Test
    @DisplayName("Test getModifiedFilesNames() - Unsuccessful response - GitCommandException")
    void testGetModifiedFilesNames_UnsuccessfulResponse_ThrowsGitHubApiException() throws Exception {
        String commit1 = "commit1";
        String commit2 = "commit2";

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(500);
        when(mockResponse.body()).thenReturn("Internal Server Error");

        when(mockClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(mockResponse);

        GitHubApiException exception = assertThrows(GitHubApiException.class, () -> {
            gitHubApiClient.getModifiedFilesNames(commit1, commit2);
        });

        assertTrue(exception.getMessage().contains("GitHub get commit history failed: 500"));
    }

    @Test
    @DisplayName("Test getModifiedFilesNames() - Invalid JSON - GitCommandException")
    void testGetModifiedFilesNames_InvalidJSON_ThrowsGitHubApiException() throws Exception {
        String commit1 = "commit1";
        String commit2 = "commit2";
        String invalidResponseBody = "Invalid JSON";

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(invalidResponseBody);
        when(mockResponse.headers()).thenReturn(HttpHeaders.of(new HashMap<>(), (k, v) -> true));

        when(mockClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(mockResponse);

        GitHubApiException exception = assertThrows(GitHubApiException.class, () -> {
            gitHubApiClient.getModifiedFilesNames(commit1, commit2);
        });

        assertTrue(exception.getMessage().contains("Error parsing the response body"));
    }

    @Test
    @DisplayName("Test getModifiedFilesNames() - Network error - GitCommandException")
    void testGetModifiedFilesNames_NetworkError_ThrowsGitHubApiException() throws Exception {
        String commit1 = "commit1";
        String commit2 = "commit2";

        when(mockClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString())))
                .thenThrow(new IOException("Network error"));

        GitHubApiException exception = assertThrows(GitHubApiException.class, () -> {
            gitHubApiClient.getModifiedFilesNames(commit1, commit2);
        });

        assertTrue(exception.getMessage().contains("Network error"));
    }
}
