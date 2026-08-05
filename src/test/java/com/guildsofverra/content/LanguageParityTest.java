package com.guildsofverra.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.junit.jupiter.api.Test;

class LanguageParityTest {
    private static final Gson GSON = new Gson();

    @Test
    void englishAndBelgianDutchExposeTheSameKeys() {
        JsonObject english = load("/assets/guildsofverra/lang/en_us.json");
        JsonObject dutch = load("/assets/guildsofverra/lang/nl_be.json");

        Set<String> englishKeys = english.keySet();
        Set<String> dutchKeys = dutch.keySet();
        assertEquals(englishKeys, dutchKeys);
    }

    private static JsonObject load(String path) {
        var stream = LanguageParityTest.class.getResourceAsStream(path);
        assertNotNull(stream, "Missing language resource: " + path);
        try (stream; var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return GSON.fromJson(reader, JsonObject.class);
        } catch (Exception exception) {
            throw new AssertionError("Failed to load language resource: " + path, exception);
        }
    }
}
