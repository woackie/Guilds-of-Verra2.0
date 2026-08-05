package com.guildsofverra;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class VersionDeclarationTest {
    @Test
    void displayedVersionMatchesGradleProjectVersion() throws Exception {
        Properties properties = new Properties();
        try (var reader = Files.newBufferedReader(Path.of("gradle.properties"))) {
            properties.load(reader);
        }

        assertEquals(properties.getProperty("mod_version"), GvVersion.CURRENT);
    }
}
