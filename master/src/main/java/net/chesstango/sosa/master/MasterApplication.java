package net.chesstango.sosa.master;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.events.LichessApiCallFailed;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;

import java.util.concurrent.CountDownLatch;

/**
 * @author Mauricio Coria
 */
@SpringBootApplication
@Slf4j
public class MasterApplication {

    public static CountDownLatch countDownLatch = new CountDownLatch(1);

    public static void main(String[] args) throws InterruptedException {
        ConfigurableApplicationContext context = SpringApplication.run(MasterApplication.class, args);

        countDownLatch.await();

        log.info("Exiting application");

        SpringApplication.exit(context, () -> 0);
    }

    @EventListener
    public void onLichessExceptionDetected(LichessApiCallFailed lichessApiCallFailed) {
        log.error("API call failed: {}", lichessApiCallFailed.getFailMessage());
        countDownLatch.countDown();
    }
}
