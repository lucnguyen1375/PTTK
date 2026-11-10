package vn.ptit.pttkm3.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public record AppConfig(
        String databaseHost,
        int databasePort,
        String databaseName,
        String databaseUser,
        String databasePassword,
        int databaseConnectTimeoutMs) {

    private static final Pattern DATABASE_IDENTIFIER = Pattern.compile("[A-Za-z0-9_]{1,64}");
    private static final Pattern HOST = Pattern.compile("[A-Za-z0-9._:-]{1,253}");
    private static final int DEFAULT_CONNECT_TIMEOUT_MS = 5_000;

    public static AppConfig fromEnvironment(Map<String, String> environment) {
        List<String> missing = new ArrayList<>();
        String host = required(environment, "DB_HOST", missing);
        String portValue = required(environment, "DB_PORT", missing);
        String name = required(environment, "DB_NAME", missing);
        String user = required(environment, "DB_USER", missing);
        String password = required(environment, "DB_PASSWORD", missing);
        String timeoutValue = environment.getOrDefault("DB_CONNECT_TIMEOUT_MS", "" + DEFAULT_CONNECT_TIMEOUT_MS);

        if (!missing.isEmpty()) {
            throw new InvalidConfigurationException("Missing required configuration: " + String.join(", ", missing));
        }
        if (!HOST.matcher(host).matches()) {
            throw new InvalidConfigurationException("DB_HOST must be a valid hostname or IP address");
        }
        if (!DATABASE_IDENTIFIER.matcher(name).matches()) {
            throw new InvalidConfigurationException("DB_NAME must contain only letters, numbers, and underscores");
        }

        int port = parseInteger("DB_PORT", portValue, 1, 65_535);
        int timeout = parseInteger("DB_CONNECT_TIMEOUT_MS", timeoutValue, 1, 60_000);
        return new AppConfig(host, port, name, user, password, timeout);
    }

    private static String required(Map<String, String> environment, String key, List<String> missing) {
        String value = environment.get(key);
        if (value == null || value.isBlank()) {
            missing.add(key);
            return "";
        }
        return value;
    }

    private static int parseInteger(String key, String value, int min, int max) {
        try {
            int parsed = Integer.parseInt(value);
            if (parsed < min || parsed > max) {
                throw new NumberFormatException();
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new InvalidConfigurationException(key + " must be an integer between " + min + " and " + max);
        }
    }

    @Override
    public String toString() {
        return "AppConfig[databaseHost=" + databaseHost
                + ", databasePort=" + databasePort
                + ", databaseName=" + databaseName
                + ", databaseUser=" + databaseUser
                + ", databasePassword=<redacted>"
                + ", databaseConnectTimeoutMs=" + databaseConnectTimeoutMs + "]";
    }

    public static final class InvalidConfigurationException extends IllegalStateException {
        public InvalidConfigurationException(String message) {
            super(message);
        }
    }
}
