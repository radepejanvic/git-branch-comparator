import org.example.CommandUtils;
import org.example.GitCommandExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;


public class GitCommandExecutorTest {

    @Mock
    private Process mockProcess;

    @Mock
    private CommandUtils mockCommandUtils;

    private GitCommandExecutor gitCommandExecutor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        gitCommandExecutor = new GitCommandExecutor("/repo/path/for/tests", mockCommandUtils);
    }

    @Test
    @DisplayName("Test getCommitHistory() - Success")
    void testGetCommitHistory_Success() throws Exception {
        when(mockCommandUtils.executeCommand(any(), anyList())).thenReturn(mockProcess);
        when(mockCommandUtils.readOutput(any())).thenReturn("commit1\ncommit2\ncommit3");
        when(mockProcess.getInputStream()).thenReturn(new ByteArrayInputStream("commit1\ncommit2\ncommit3".getBytes()));
        when(mockProcess.waitFor()).thenReturn(0);

        List<String> commitHistory = gitCommandExecutor.getCommitHistory("branchB");

        assertNotNull(commitHistory);
        assertEquals(3, commitHistory.size());
        assertEquals("commit1", commitHistory.get(0));
        assertEquals("commit2", commitHistory.get(1));
        assertEquals("commit3", commitHistory.get(2));
    }

    @Test
    @DisplayName("Test getCommitHistory() - Command failed - GitCommandException")
    void testGetCommitHistory_ThrowsGitCommandException() throws Exception {
        when(mockCommandUtils.executeCommand(any(), anyList())).thenReturn(mockProcess);
        when(mockProcess.waitFor()).thenReturn(1);

        GitCommandExecutor.GitCommandException exception = assertThrows(GitCommandExecutor.GitCommandException.class,
                () -> gitCommandExecutor.getCommitHistory("branchB"));

        assertTrue(exception.getMessage().contains("Git rev-list command failed for branch: branchB"));
    }

    @Test
    @DisplayName("Test getCommitHistory() - Error reading input stream - GitCommandException")
    void testGetCommitHistory_ErrorReadingInputStream_ThrowsGitCommandException() throws Exception {
        when(mockCommandUtils.executeCommand(any(), anyList())).thenReturn(mockProcess);
        when(mockCommandUtils.readOutput(any())).thenThrow(new IOException("Error reading input stream"));

        GitCommandExecutor.GitCommandException exception = assertThrows(GitCommandExecutor.GitCommandException.class,
                () -> gitCommandExecutor.getCommitHistory("branchB"));

        assertTrue(exception.getMessage().contains("Error reading input stream"));
    }

    @Test
    @DisplayName("Test getCommitHistory() - Error thread was interrupted - GitCommandException")
    void testGetCommitHistory_ErrorProcessWait_ThrowsGitCommandException() throws Exception {
        when(mockCommandUtils.executeCommand(any(), anyList())).thenReturn(mockProcess);
        when(mockProcess.waitFor()).thenThrow(new InterruptedException("Thread was interrupted"));

        GitCommandExecutor.GitCommandException exception = assertThrows(GitCommandExecutor.GitCommandException.class,
                () -> gitCommandExecutor.getCommitHistory("branchB"));

        assertTrue(exception.getMessage().contains("Thread was interrupted"));
    }

    @Test
    @DisplayName("Test getModifiedFilesNames() - Success")
    void testGetModifiedFilesNames_Success() throws Exception {
        when(mockCommandUtils.executeCommand(any(), anyList())).thenReturn(mockProcess);
        when(mockCommandUtils.readOutput(any())).thenReturn("repo/file1.txt\nrepo/file2.txt\nrepo/file3.txt");
        when(mockProcess.getInputStream()).thenReturn(new ByteArrayInputStream("repo/file1.txt\nrepo/file2.txt\nrepo/file3.txt".getBytes()));
        when(mockProcess.waitFor()).thenReturn(0);

        List<String> modifiedFilesNames = gitCommandExecutor.getModifiedFilesNames("commit1", "commit2");

        assertNotNull(modifiedFilesNames);
        assertEquals(3, modifiedFilesNames.size());
        assertEquals("repo/file1.txt", modifiedFilesNames.get(0));
        assertEquals("repo/file2.txt", modifiedFilesNames.get(1));
        assertEquals("repo/file3.txt", modifiedFilesNames.get(2));
    }

    @Test
    @DisplayName("Test getModifiedFilesNames() - Command failed - GitCommandException")
    void testGetModifiedFilesNames_ThrowsGitCommandException() throws Exception {
        when(mockCommandUtils.executeCommand(any(), anyList())).thenReturn(mockProcess);
        when(mockProcess.waitFor()).thenReturn(1);

        GitCommandExecutor.GitCommandException exception = assertThrows(GitCommandExecutor.GitCommandException.class,
                () -> gitCommandExecutor.getModifiedFilesNames("commit1", "commit2"));

        assertTrue(exception.getMessage().contains("Git diff --name-only command failed for commits: commit1 and commit2."));
    }

    @Test
    @DisplayName("Test getModifiedFilesNames() - Error reading input stream - GitCommandException")
    void testGetModifiedFilesNames_ErrorReadingInputStream_ThrowsGitCommandException() throws Exception {
        when(mockCommandUtils.executeCommand(any(), anyList())).thenReturn(mockProcess);
        when(mockCommandUtils.readOutput(any())).thenThrow(new IOException("Error reading input stream"));

        GitCommandExecutor.GitCommandException exception = assertThrows(GitCommandExecutor.GitCommandException.class,
                () -> gitCommandExecutor.getModifiedFilesNames("commit1", "commit2"));

        assertTrue(exception.getMessage().contains("Error reading input stream"));
    }

    @Test
    @DisplayName("Test getModifiedFilesNames() - Error thread was interrupted - GitCommandException")
    void testGetModifiedFilesNames_ErrorProcessWait_ThrowsGitCommandException() throws Exception {
        when(mockCommandUtils.executeCommand(any(), anyList())).thenReturn(mockProcess);
        when(mockProcess.waitFor()).thenThrow(new InterruptedException("Thread was interrupted"));


        GitCommandExecutor.GitCommandException exception = assertThrows(GitCommandExecutor.GitCommandException.class,
                () -> gitCommandExecutor.getModifiedFilesNames("commit1", "commit2"));

        assertTrue(exception.getMessage().contains("Thread was interrupted"));
    }

}
