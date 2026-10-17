
package blockgame.client;

import java.io.IOException;
import java.util.Properties;

public final class BuildInfo {
    public static final String VERSION = loadVersion();

    private BuildInfo() { }

    private static String loadVersion() {
        try (var stream = BuildInfo.class.getResourceAsStream(
                "/blockgame.properties")) {

            if (stream == null) {
                throw new IllegalStateException("Missing build version resource");
            }

            Properties properties = new Properties();
            properties.load(stream);

            return properties.getProperty("version", "unknown");
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load build version", e);
        }
    }
}
