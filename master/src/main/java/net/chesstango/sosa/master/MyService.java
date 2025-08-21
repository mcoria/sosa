package net.chesstango.sosa.master;// Java

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class MyService {

    @Async("ioBoundExecutor")
    public CompletableFuture<String> doWorkAsync() {
        log.info("Ahora arranque");
        // your work
        return CompletableFuture.completedFuture("done");
    }
}
