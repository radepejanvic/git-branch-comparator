import org.example.GitCommandExecutor;
import org.example.GitCommitHistoryComparator;
import org.example.GitHubApiClient;
import org.example.exceptions.GitCommandException;
import org.example.exceptions.GitHubApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GitCommitHistoryComparatorTest {

    @Mock
    private GitCommandExecutor mockGit;

    @Mock
    private GitHubApiClient mockGitHub;

    private GitCommitHistoryComparator comparator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        comparator = new GitCommitHistoryComparator(mockGit, mockGitHub);
    }

    @ParameterizedTest
    @DisplayName("Test compareModifiedFilesBetweenBranches() - No exceptions path")
    @MethodSource("TestCaseProvider#successTestCases")
    void testCompareModifiedFilesBetweenBranches(String localBranch, String remoteBranch,
                                                 String lastLocalCommit,String lastRemoteCommit,
                                                 List<String> localHistory, List<String> remoteHistory,
                                                 String baseCommit,
                                                 List<String> localModified, List<String> remoteModified,
                                                 List<String> expectedCommonModified) throws Exception {

        when(mockGit.getCommitHistory(localBranch)).thenReturn(localHistory);
        when(mockGitHub.getCommitHistory(remoteBranch)).thenReturn(remoteHistory);

        when(mockGit.getModifiedFilesNames(baseCommit, lastLocalCommit)).thenReturn(localModified);
        when(mockGitHub.getModifiedFilesNames(baseCommit, lastRemoteCommit)).thenReturn(remoteModified);

        List<String> commonModifiedFiles = comparator.compareModifiedFilesBetweenBranches(localBranch, remoteBranch);

        assertNotNull(commonModifiedFiles);
        assertEquals(expectedCommonModified.size(), commonModifiedFiles.size());
        assertEquals(expectedCommonModified, commonModifiedFiles);

        verify(mockGit).getCommitHistory(localBranch);
        verify(mockGitHub).getCommitHistory(remoteBranch);

        if (baseCommit != null) {
            verify(mockGit).getModifiedFilesNames(baseCommit, lastLocalCommit);
            verify(mockGitHub).getModifiedFilesNames(baseCommit, lastRemoteCommit);
        } else {
            verify(mockGit, never()).getModifiedFilesNames(any(), any());
            verify(mockGitHub, never()).getModifiedFilesNames(any(), any());
        }
    }

    @Test
    @DisplayName("Test compareModifiedFilesBetweenBranches() - Local commit history failure - GitCommandException")
    void testCompareModifiedFilesBetweenBranches_LocalCommitHistoryFailure_ThrowsGitCommandException() throws Exception {
        String local = "feature", remote = "main";

        when(mockGit.getCommitHistory(local)).thenThrow(new GitCommandException("Git error"));

        assertThrows(GitCommandException.class, () -> comparator.compareModifiedFilesBetweenBranches(local, remote));

        verify(mockGit).getCommitHistory(local);
    }

    @Test
    @DisplayName("Test compareModifiedFilesBetweenBranches() - Remote commit history failure - GitHubApiException")
    void testCompareModifiedFilesBetweenBranches_RemoteCommitHistoryFailure_ThrowsGitHubApiException() throws Exception {
        String local = "feature", remote = "main";

        when(mockGit.getCommitHistory(local)).thenReturn(List.of("c1", "c2"));
        when(mockGitHub.getCommitHistory(remote)).thenThrow(new GitHubApiException("GitHub error"));

        assertThrows(GitHubApiException.class, () -> comparator.compareModifiedFilesBetweenBranches(local, remote));

        verify(mockGit).getCommitHistory(local);
        verify(mockGitHub).getCommitHistory(remote);
    }

    @ParameterizedTest
    @DisplayName("Test compareModifiedFilesBetweenBranches() - Local modified files failure - GitCommandException")
    @MethodSource("TestCaseProvider#exceptionTestCases")
    void testCompareModifiedFilesBetweenBranches_LocalModifiedFilesFailure_ThrowsGitCommandException(String localBranch, String remoteBranch,
                                                                                                     String lastLocalCommit,String lastRemoteCommit,
                                                                                                     List<String> localHistory, List<String> remoteHistory,
                                                                                                     String baseCommit) throws Exception {

        when(mockGit.getCommitHistory(localBranch)).thenReturn(localHistory);
        when(mockGitHub.getCommitHistory(remoteBranch)).thenReturn(remoteHistory);

        when(mockGit.getModifiedFilesNames(baseCommit, lastLocalCommit)).thenThrow(new GitCommandException("Git error"));

        assertThrows(GitCommandException.class, () -> comparator.compareModifiedFilesBetweenBranches(localBranch, remoteBranch));

        verify(mockGit).getCommitHistory(localBranch);
        verify(mockGitHub).getCommitHistory(remoteBranch);
        verify(mockGit).getModifiedFilesNames(baseCommit, lastLocalCommit);
    }

    @ParameterizedTest
    @DisplayName("Test compareModifiedFilesBetweenBranches() - Remote modified files failure - GitHubApiException")
    @MethodSource("TestCaseProvider#exceptionTestCases")
    void testCompareModifiedFilesBetweenBranches_RemoteModifiedFilesFailure_ThrowsGitHubApiException(String localBranch, String remoteBranch,
                                                                                                     String lastLocalCommit,String lastRemoteCommit,
                                                                                                     List<String> localHistory, List<String> remoteHistory,
                                                                                                     String baseCommit,
                                                                                                     List<String> localModified) throws Exception {

        when(mockGit.getCommitHistory(localBranch)).thenReturn(localHistory);
        when(mockGitHub.getCommitHistory(remoteBranch)).thenReturn(remoteHistory);

        when(mockGit.getModifiedFilesNames(baseCommit, lastLocalCommit)).thenReturn(localModified);
        when(mockGitHub.getModifiedFilesNames(baseCommit, lastRemoteCommit)).thenThrow(new GitHubApiException("GitHub error"));

        assertThrows(GitHubApiException.class, () -> comparator.compareModifiedFilesBetweenBranches(localBranch, remoteBranch));

        verify(mockGit).getCommitHistory(localBranch);
        verify(mockGitHub).getCommitHistory(remoteBranch);
        verify(mockGit).getModifiedFilesNames(baseCommit, lastLocalCommit);
        verify(mockGitHub).getModifiedFilesNames(baseCommit, lastRemoteCommit);
    }




}
