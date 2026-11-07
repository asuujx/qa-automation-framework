package io.github.asuujx.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

/**
 * Test configuration. Each value is looked up by the same key in this order:
 * system property (-DKEY=...), environment variable (KEY=...), then config.properties.
 */
public final class Config {

    private static final String FILE_NAME = "config.properties";
    private static final Properties FILE_PROPERTIES = loadFile();

    private Config() {
    }

    public static String baseUrl() {
        return get("BASE_URL");
    }

    public static String adminUsername() {
        return get("ADMIN_USERNAME");
    }

    public static String adminPassword() {
        return get("ADMIN_PASSWORD");
    }

    public static boolean headless() {
        return getBoolean("HEADLESS");
    }

    private static String get(String key) {
        String value = System.getProperty(key);
        if (isBlank(value)) {
            value = System.getenv(key);
        }
        if (isBlank(value)) {
            value = FILE_PROPERTIES.getProperty(key);
        }
        if (isBlank(value)) {
            throw new IllegalStateException("Missing config value '" + key + "'. Set it in " + FILE_NAME
                    + ", as an environment variable, or with -D" + key + "=...");
        }
        return value.strip();
    }

    private static boolean getBoolean(String key) {
        String value = get(key);
        // Stricter than Boolean.parseBoolean, which silently treats typos like "ture" as false
        if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
            throw new IllegalStateException("Config value '" + key + "' must be true or false, but was '" + value + "'");
        }
        return Boolean.parseBoolean(value);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static Properties loadFile() {
        try (InputStream input = Config.class.getClassLoader().getResourceAsStream(FILE_NAME)) {
            if (input == null) {
                throw new IllegalStateException(FILE_NAME + " not found on the classpath");
            }
            Properties properties = new Properties();
            properties.load(input);
            return properties;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read " + FILE_NAME, e);
        }
    }
}
