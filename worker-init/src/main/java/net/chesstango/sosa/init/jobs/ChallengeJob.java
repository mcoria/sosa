package net.chesstango.sosa.init.jobs;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.messages.master.SendChallenge;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import static net.chesstango.sosa.messages.Constants.MASTER_ROUTING_KEY;
import static net.chesstango.sosa.messages.Constants.SOSA_EXCHANGE;

/**
 * @author Mauricio Coria
 */
@PersistJobDataAfterExecution
@Component
@Slf4j
public class ChallengeJob extends QuartzJobBean {
    private final RabbitTemplate rabbitTemplate;
    private final String workerId;

    public ChallengeJob(RabbitTemplate rabbitTemplate, @Value("${app.workerId}") String workerId) {
        this.rabbitTemplate = rabbitTemplate;
        this.workerId = workerId;
    }


    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("[{}] Triggering challenge", workerId);
        SendChallenge payload = new SendChallenge(workerId);
        rabbitTemplate.convertAndSend(
                SOSA_EXCHANGE,
                MASTER_ROUTING_KEY,
                payload
        );
    }
}
