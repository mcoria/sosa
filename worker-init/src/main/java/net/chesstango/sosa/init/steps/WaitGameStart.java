package net.chesstango.sosa.init.steps;


import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.messages.worker.GameStart;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

/**
 * @author Mauricio Coria
 */
@Component
@Slf4j
public class WaitGameStart implements Tasklet, StepExecutionListener {

    private final RabbitTemplate rabbitTemplate;

    private final Queue workerQueue;

    private GameStart gameStart;

    public WaitGameStart(RabbitTemplate rabbitTemplate, Queue workerQueue) {
        this.rabbitTemplate = rabbitTemplate;
        this.workerQueue = workerQueue;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("Waiting GameStart message");
        this.gameStart = (GameStart) rabbitTemplate.receiveAndConvert(workerQueue.getName(), -1);
        if (gameStart == null) {
            log.warn("No GameStart message received");
            return RepeatStatus.FINISHED;
        }
        log.info("[{}] GameStart message received", gameStart.getGameId());
        return RepeatStatus.FINISHED;
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        if (this.gameStart == null) {
            return ExitStatus.FAILED;
        }
        stepExecution
                .getJobExecution()
                .getExecutionContext()
                .put("gameStart", this.gameStart);
        return ExitStatus.COMPLETED;
    }

}
