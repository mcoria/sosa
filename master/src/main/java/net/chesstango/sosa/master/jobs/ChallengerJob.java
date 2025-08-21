package net.chesstango.sosa.master.jobs;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.lichess.LichessClientBean;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

/**
 * @author Mauricio Coria
 */
@DisallowConcurrentExecution
@PersistJobDataAfterExecution
@Component
@Slf4j
public class ChallengerJob extends QuartzJobBean {

    private final LichessClientBean lichessClientBean;

    public ChallengerJob(LichessClientBean lichessClientBean) {
        this.lichessClientBean = lichessClientBean;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) {
        log.info("executeInternal ...");

        lichessClientBean.getRatings().forEach((type, statsPerf) -> {
            log.info("{} : {}", type, statsPerf);
        });
    }

}
