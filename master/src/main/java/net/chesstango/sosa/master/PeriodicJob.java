package net.chesstango.sosa.master;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.model.DemoPayload;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@DisallowConcurrentExecution
@PersistJobDataAfterExecution
@Component
@Slf4j
public class PeriodicJob extends QuartzJobBean {


    private final DemoProducer demoProducer;

    public PeriodicJob(@Autowired DemoProducer demoProducer) {
        this.demoProducer = demoProducer;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) {
        log.info("doing something");
        demoProducer.send(new DemoPayload("1", "asd"));
    }

}
