package net.chesstango.sosa.master;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

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


    /*
    @EventListener(LichessTooManyRequestsSent.class)
    public void onLichessExceptionDetected() {
        log.error("Lichess API: too many requests. Ext application;");
        countDownLatch.countDown();
    }
     */
}
