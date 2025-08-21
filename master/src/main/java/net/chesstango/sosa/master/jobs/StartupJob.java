package net.chesstango.sosa.master.jobs;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.BotStreamLoop;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@DisallowConcurrentExecution
@PersistJobDataAfterExecution
@Component
@Slf4j
public class StartupJob extends QuartzJobBean {

    private final BotStreamLoop botStreamLoop;

    public StartupJob(BotStreamLoop botStreamLoop) {
        this.botStreamLoop = botStreamLoop;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) {
        // Aca deberia determinar si suspendo o inicio el loop
        // En caso que inicie, deberia cancelar esta tarea
        // En caso que NO inicie, deberia volver a planificar la tarea
        log.info("Triggering BotStreamLoop ...");
        botStreamLoop.doWorkAsync();
    }
}
