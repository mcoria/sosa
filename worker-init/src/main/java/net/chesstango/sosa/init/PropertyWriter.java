package net.chesstango.sosa.init;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.Properties;

/**
 * @author Mauricio Coria
 */
@Component
@Slf4j
public class PropertyWriter {

    private Path directory;

    public PropertyWriter(@Value("${WORKER_INIT_DIRECTORY}") String propertyDirectoryStr) {
        this.directory = Path.of(propertyDirectoryStr);
        File directoryFile = directory.toFile();
        if (!directoryFile.exists()) {
            throw new IllegalArgumentException("Directory does not exist: " + propertyDirectoryStr);
        }
        if (!directoryFile.isDirectory()) {
            throw new IllegalArgumentException("Path is not a directory: " + propertyDirectoryStr);
        }
    }

    public void writePropertyFile(String gameId) {
        try {
            File propertyFile = directory.resolve("game.properties").toFile();

            Properties props = new Properties();
            props.setProperty("gameId", gameId);
            props.store(new FileOutputStream(propertyFile), null);

            log.info("Property file created successfully with gameId: {}", gameId);
        } catch (Exception e) {
            log.error("Error writing property file", e);
        }
    }
}
