package net.chesstango.sosa.master;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.gardel.fen.FEN;
import net.chesstango.sosa.master.configs.RabbitConfig;
import net.chesstango.sosa.model.GameEnd;
import net.chesstango.sosa.model.GameStart;
import net.chesstango.sosa.model.GoFast;
import net.chesstango.sosa.model.StartPosition;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;

/**
 * @author Mauricio Coria
 */
@Slf4j
public class GameProducer {
    private final RabbitTemplate rabbitTemplate;
    private final String gameId;


    public GameProducer(RabbitTemplate rabbitTemplate, String gameId) {
        this.rabbitTemplate = rabbitTemplate;
        this.gameId = gameId;
    }

    // Este mensaje va destinado a worker-init
    public void send_GameStart() {
        GameStart gameStart = new GameStart(gameId);
        rabbitTemplate.convertAndSend(
                RabbitConfig.CHESS_TANGO_EXCHANGE,
                RabbitConfig.MASTER_REQUESTS_ROUTING_KEY,
                gameStart
        );
        log.info("[{}] NewGame sent", gameId);
    }

    // Este mensaje va destinado a worker
    public void send_GameEnd() {
        GameEnd gameEnd = new GameEnd(gameId);
        rabbitTemplate.convertAndSend(
                RabbitConfig.CHESS_TANGO_EXCHANGE,
                gameId,
                gameEnd
        );
        log.info("[{}] GameEnd sent", gameId);
    }

    public void send_StartPosition(FEN fen) {
        StartPosition startPosition = new StartPosition(fen.toString());
        rabbitTemplate.convertAndSend(
                RabbitConfig.CHESS_TANGO_EXCHANGE,
                gameId,
                startPosition
        );
        log.info("[{}] StartPosition sent", gameId);
    }

    public void send_GoFast(int wTime, int bTime, int wInc, int bInc, List<String> strings) {
        GoFast goFast = new GoFast(wTime, bTime, wInc, bInc, strings);
        rabbitTemplate.convertAndSend(
                RabbitConfig.CHESS_TANGO_EXCHANGE,
                gameId,
                goFast
        );
        log.info("[{}] GoFast sent", gameId);
    }
}
