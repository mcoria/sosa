package net.chesstango.sosa.init.steps;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.model.GameStart;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
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
public class WriteGamePropertyFile implements Tasklet, StepExecutionListener {

    private Path directory;

    private GameStart gameStart;

    private boolean success = false;

    public WriteGamePropertyFile(@Value("${WORKER_INIT_DIRECTORY}") String propertyDirectoryStr) {
        this.directory = Path.of(propertyDirectoryStr);
        File directoryFile = directory.toFile();
        if (!directoryFile.exists()) {
            throw new IllegalArgumentException("Directory does not exist: " + propertyDirectoryStr);
        }
        if (!directoryFile.isDirectory()) {
            throw new IllegalArgumentException("Path is not a directory: " + propertyDirectoryStr);
        }
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        ExecutionContext executionContext = stepExecution
                .getJobExecution()
                .getExecutionContext();
        this.gameStart = (GameStart) executionContext.get("gameStart");
        log.info("[{}] Writing property file", gameStart.getGameId());
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        if (gameStart != null) {
            writePropertyFile(gameStart.getGameId());
        }
        return RepeatStatus.FINISHED;
    }

    void writePropertyFile(String gameId) {
        try {
            File propertyFile = directory.resolve("game.properties").toFile();

            Properties props = new Properties();
            props.setProperty("gameId", gameId);
            props.store(new FileOutputStream(propertyFile), null);

            log.info("Property file created successfully with gameId: {}", gameId);
            success = true;
        } catch (Exception e) {
            log.error("Error writing property file", e);
            success = false;
        }
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return success ? ExitStatus.COMPLETED : ExitStatus.FAILED;
    }

}
