import org.example.GitCommandExecutor;
import org.example.BranchComparator;
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

public class BranchComparatorTest {

    @Mock
    private GitCommandExecutor mockGit;

    @Mock
    private GitHubApiClient mockGitHub;

    private BranchComparator comparator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        comparator = new BranchComparator(mockGit, mockGitHub);
    }

    @ParameterizedTest
    @DisplayName("Test compareModifiedFiles() - No exceptions path")
    @MethodSource("TestCaseProvider#successTestCases")
    void testCompareModifiedFiles(String localBranch, String remoteBranch,
                                  String lastLocalCommit, String lastRemoteCommit,
                                  List<String> localHistory, List<String> remoteHistory,
                                  String baseCommit,
                                  List<String> localModified, List<String> remoteModified,
                                  List<String> expectedCommonModified) throws Exception {

        when(mockGit.getCommitHistory(localBranch)).thenReturn(localHistory);
        when(mockGitHub.getCommitHistory(remoteBranch)).thenReturn(remoteHistory);

        when(mockGit.getModifiedFilesNames(baseCommit, lastLocalCommit)).thenReturn(localModified);
        when(mockGitHub.getModifiedFilesNames(baseCommit, lastRemoteCommit)).thenReturn(remoteModified);

        List<String> commonModifiedFiles = comparator.compareModifiedFiles(localBranch, remoteBranch);

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
    @DisplayName("Test compareModifiedFiles() - Local commit history failure - GitCommandException")
    void testCompareModifiedFiles_LocalCommitHistoryFailure_ThrowsGitCommandException() throws Exception {
        String local = "feature", remote = "main";

        when(mockGit.getCommitHistory(local)).thenThrow(new GitCommandException("Git error"));

        assertThrows(GitCommandException.class, () -> comparator.compareModifiedFiles(local, remote));

        verify(mockGit).getCommitHistory(local);
    }

    @Test
    @DisplayName("Test compareModifiedFiles() - Remote commit history failure - GitHubApiException")
    void testCompareModifiedFiles_RemoteCommitHistoryFailure_ThrowsGitHubApiException() throws Exception {
        String local = "feature", remote = "main";

        when(mockGit.getCommitHistory(local)).thenReturn(List.of("c1", "c2"));
        when(mockGitHub.getCommitHistory(remote)).thenThrow(new GitHubApiException("GitHub error"));

        assertThrows(GitHubApiException.class, () -> comparator.compareModifiedFiles(local, remote));

        verify(mockGit).getCommitHistory(local);
        verify(mockGitHub).getCommitHistory(remote);
    }

    @ParameterizedTest
    @DisplayName("Test compareModifiedFiles() - Local modified files failure - GitCommandException")
    @MethodSource("TestCaseProvider#exceptionTestCases")
    void testCompareModifiedFilesBetweenBranches_LocalModifiedFilesFailure_ThrowsGitCommandException(String localBranch, String remoteBranch,
                                                                                                     String lastLocalCommit,String lastRemoteCommit,
                                                                                                     List<String> localHistory, List<String> remoteHistory,
                                                                                                     String baseCommit) throws Exception {

        when(mockGit.getCommitHistory(localBranch)).thenReturn(localHistory);
        when(mockGitHub.getCommitHistory(remoteBranch)).thenReturn(remoteHistory);

        when(mockGit.getModifiedFilesNames(baseCommit, lastLocalCommit)).thenThrow(new GitCommandException("Git error"));

        assertThrows(GitCommandException.class, () -> comparator.compareModifiedFiles(localBranch, remoteBranch));

        verify(mockGit).getCommitHistory(localBranch);
        verify(mockGitHub).getCommitHistory(remoteBranch);
        verify(mockGit).getModifiedFilesNames(baseCommit, lastLocalCommit);
    }

    @ParameterizedTest
    @DisplayName("Test compareModifiedFiles() - Remote modified files failure - GitHubApiException")
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

        assertThrows(GitHubApiException.class, () -> comparator.compareModifiedFiles(localBranch, remoteBranch));

        verify(mockGit).getCommitHistory(localBranch);
        verify(mockGitHub).getCommitHistory(remoteBranch);
        verify(mockGit).getModifiedFilesNames(baseCommit, lastLocalCommit);
        verify(mockGitHub).getModifiedFilesNames(baseCommit, lastRemoteCommit);
    }




}
