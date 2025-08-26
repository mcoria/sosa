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
public class NewInitApplication {

    public static CountDownLatch countDownLatch = new CountDownLatch(1);

    public static void main(String[] args) throws InterruptedException {

        ConfigurableApplicationContext context = SpringApplication.run(NewInitApplication.class, args);

        log.info("Listener started, waiting for new game");

        countDownLatch.await();

        SpringApplication.exit(context, () -> 0);
    }

}
