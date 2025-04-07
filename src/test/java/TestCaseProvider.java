import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.stream.Stream;

public class TestCaseProvider {

    static Stream<Arguments> successTestCases() {
        return Stream.of(
                Arguments.of("feature1", "main", "c3", "r1", List.of("c3", "c2", "c1"), List.of("r1", "r2", "c1"), "c1", List.of("fileA", "fileB", "fileC"), List.of("fileA", "file1", "file2"), List.of("fileA")),
                Arguments.of("feature1", "main", "c3", "r1", List.of("c3", "c2", "c1"), List.of("r1", "r2", "c1"), "c1", List.of("fileA", "fileB", "fileC"), List.of("file1", "file2", "file3"), List.of()),
                Arguments.of("feature1", "main", "c3", "r1", List.of("c3", "c2", "c1"), List.of("r1", "r2", "c1"), "c1", List.of("fileA", "fileB", "fileC"), List.of("fileA", "fileB", "fileC"), List.of("fileA", "fileB", "fileC")),
                Arguments.of("feature1", "main", "c5", "c3", List.of("c5", "c4", "c3", "c2", "c1"), List.of("c3", "c2", "c1"), "c3", List.of("fileA", "fileB", "fileC"), List.of(), List.of()),
                Arguments.of("feature2", "main", "c3", "c3", List.of("c3", "c2", "c1"), List.of("c3", "c2", "c1"), "c3", List.of(), List.of(), List.of()),
                Arguments.of("feature3", "main", null, "r3", List.of(), List.of("r3", "r2", "c1"), null, List.of(), List.of(), List.of()),
                Arguments.of("feature3", "main", "c3", null, List.of("c3", "c2", "c1"), List.of(), null, List.of(), List.of(), List.of()),
                Arguments.of("feature3", "main", null, null, List.of(), List.of(), null, List.of(), List.of(), List.of()),
                Arguments.of("feature4", "main", "c1", "c1", List.of("c1"), List.of("c1"), "c1", List.of(), List.of(), List.of()),
                Arguments.of("feature4", "main", "r1", "c1", List.of("r1"), List.of("c1"), null, List.of(), List.of(), List.of())
        );
    }

    static Stream<Arguments> exceptionTestCases() {
        return Stream.of(
                Arguments.of("feature1", "main", "c3", "r1", List.of("c3", "c2", "c1"), List.of("r1", "r2", "c1"), "c1", List.of("fileA", "fileB", "fileC"), List.of("fileA", "file1", "file2"), List.of("fileA")),
                Arguments.of("feature1", "main", "c3", "r1", List.of("c3", "c2", "c1"), List.of("r1", "r2", "c1"), "c1", List.of("fileA", "fileB", "fileC"), List.of("file1", "file2", "file3"), List.of()),
                Arguments.of("feature1", "main", "c3", "r1", List.of("c3", "c2", "c1"), List.of("r1", "r2", "c1"), "c1", List.of("fileA", "fileB", "fileC"), List.of("fileA", "fileB", "fileC"), List.of("fileA", "fileB", "fileC")),
                Arguments.of("feature1", "main", "c5", "c3", List.of("c5", "c4", "c3", "c2", "c1"), List.of("c3", "c2", "c1"), "c3", List.of("fileA", "fileB", "fileC"), List.of(), List.of()),
                Arguments.of("feature2", "main", "c3", "c3", List.of("c3", "c2", "c1"), List.of("c3", "c2", "c1"), "c3", List.of(), List.of(), List.of())
        );
    }
}
