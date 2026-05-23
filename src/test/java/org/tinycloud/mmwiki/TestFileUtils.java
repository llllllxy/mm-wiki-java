package org.tinycloud.mmwiki;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TestFileUtils {

    private static final Path TEST_TEMP_ROOT = Path.of("target/test-temp");

    private TestFileUtils() {
    }

    public static Path createTempDirectory(String prefix) throws IOException {
        Files.createDirectories(TEST_TEMP_ROOT);
        return Files.createTempDirectory(TEST_TEMP_ROOT, prefix);
    }

    public static void deleteRecursively(Path path) throws IOException {
        if (path == null || !Files.exists(path)) {
            return;
        }
        try (var stream = Files.walk(path)) {
            for (Path item : stream.sorted((left, right) -> right.getNameCount() - left.getNameCount()).toList()) {
                Files.deleteIfExists(item);
            }
        }
    }
}
