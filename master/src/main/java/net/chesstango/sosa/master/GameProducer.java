package net.chesstango.sosa.master;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.gardel.fen.FEN;
import net.chesstango.sosa.messages.Constants;
import net.chesstango.sosa.messages.worker.GameEnd;
import net.chesstango.sosa.messages.worker.GameStart;
import net.chesstango.sosa.messages.worker.GoFast;
import net.chesstango.sosa.messages.worker.StartPosition;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;

/**
 * @author Mauricio Coria
 */
@Slf4j
public class GameProducer {
    private final RabbitTemplate rabbitTemplate;
    private final String workerId;

    public GameProducer(RabbitTemplate rabbitTemplate, String workerId) {
        this.rabbitTemplate = rabbitTemplate;
        this.workerId = workerId;
    }

    // Este mensaje va destinado a worker-init
    public void send_GameStart(String gameId) {
        GameStart gameStart = new GameStart(gameId);
        rabbitTemplate.convertAndSend(
                Constants.CHESS_TANGO_EXCHANGE,
                workerId,
                gameStart
        );
        log.info("[{}] NewGame sent", gameId);
    }

    // Este mensaje va destinado a worker
    public void send_GameEnd(String gameId) {
        GameEnd gameEnd = new GameEnd(gameId);
        rabbitTemplate.convertAndSend(
                Constants.CHESS_TANGO_EXCHANGE,
                workerId,
                gameEnd
        );
        log.info("[{}] GameEnd sent", gameId);
    }

    public void send_StartPosition(String gameId, FEN fen) {
        StartPosition startPosition = new StartPosition(gameId, fen.toString());
        rabbitTemplate.convertAndSend(
                Constants.CHESS_TANGO_EXCHANGE,
                workerId,
                startPosition
        );
        log.info("[{}] StartPosition sent", gameId);
    }

    public void send_GoFast(String gameId, int wTime, int bTime, int wInc, int bInc, List<String> strings) {
        GoFast goFast = new GoFast(gameId, wTime, bTime, wInc, bInc, strings);
        rabbitTemplate.convertAndSend(
                Constants.CHESS_TANGO_EXCHANGE,
                workerId,
                goFast
        );
        log.info("[{}] GoFast sent", gameId);
    }
}
