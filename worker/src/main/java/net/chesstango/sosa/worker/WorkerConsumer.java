package net.chesstango.sosa.worker;


import lombok.extern.slf4j.Slf4j;
import net.chesstango.gardel.fen.FEN;
import net.chesstango.sosa.model.GoFast;
import net.chesstango.sosa.model.StartPosition;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author Mauricio Coria
 */
@Slf4j
@RabbitListener(queues = "${gameId}")
@Component
public class WorkerConsumer {

    private final TangoController tangoController;

    public WorkerConsumer(TangoController tangoController) {
        this.tangoController = tangoController;
    }

    @RabbitHandler
    public synchronized void handle(StartPosition startPosition) {
        // Process message
        log.info("Received: {}", startPosition);

        tangoController.setStartPosition(FEN.of(startPosition.getFen()));
    }


    @RabbitHandler
    public synchronized void handle(GoFast goFast) {
        // Process message
        log.info("Received: {}", goFast);

        String bestMove = tangoController.goFast(goFast.getWTime(), goFast.getBTime(), goFast.getWInc(), goFast.getBInc(), goFast.getMoves());


    }
}
