package net.chesstango.sosa.worker;

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
public class WorkerApplication {

    private static int exitCode = 0;

    private static final CountDownLatch countDownLatch = new CountDownLatch(1);

    public static void main(String[] args) throws InterruptedException {

        ConfigurableApplicationContext context = SpringApplication.run(WorkerApplication.class, args);

        log.info("Playing");

        countDownLatch.await();

        SpringApplication.exit(context, () -> exitCode);
    }

    public static void finishFail() {
        exitCode = -1;
        countDownLatch.countDown();
    }

    public static void finishSuccess() {
        exitCode = 0;
        countDownLatch.countDown();
    }

}
