package net.chesstango.sosa.worker;

import jakarta.annotation.PostConstruct;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.board.moves.Move;
import net.chesstango.board.representations.move.SimpleMoveEncoder;
import net.chesstango.engine.*;
import net.chesstango.gardel.fen.FEN;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Component
public class TangoController implements AutoCloseable, SearchListener {
    private final SimpleMoveEncoder simpleMoveEncoder = new SimpleMoveEncoder();

    private final String gameId;

    private Tango tango;

    private Session session;

    @Setter
    @Value("${app.polyglot_book}")
    private String polyglot_book;

    @Setter
    @Value("${app.syzygy_directory}")
    private String syzygy_directory;

    public TangoController(@Value("${gameId}") String gameId) {
        this.gameId = gameId;
    }

    @PostConstruct
    public void init() {
        log.info("Initializing Tango");

        Config config = new Config();
        config.setSyncSearch(true);

        if (polyglot_book != null) {
            log.info("Setting polyglot book to {}", polyglot_book);
            config.setPolyglotFile(polyglot_book);
        }

        if( syzygy_directory != null ){
            log.info("Setting syzygy directory to {}", syzygy_directory);
            config.setSyzygyDirectory(syzygy_directory);
        }

        tango = Tango.open(config);
    }

    @Override
    public void close() {
        log.info("Closing Tango");
        try {
            tango.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setStartPosition(FEN fen) {
        log.info("[{}] Setting startPosition {}", gameId, fen);
        session = tango.newSession(fen);
        session.setSearchListener(this);
    }

    public String goFast(int wTime, int bTime, int wInc, int bInc, List<String> moves) {
        try {
            log.info("[{}] Setting moves: {}", gameId, moves);
            session.setMoves(moves);

            log.info("[{}] Going fast: wTime {} bTime {} wInc {} bInc {} moves {}", gameId, wTime, bTime, wInc, bInc, moves);
            Future<SearchResponse> searchResponseFuture = session.goFast(wTime, bTime, wInc, bInc);

            SearchResponse searchResponse = searchResponseFuture.get();

            Move move = searchResponse.move();

            return simpleMoveEncoder.encode(move);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void searchStarted() {
        log.info("[{}] Search started", gameId);
    }

    @Override
    public void searchInfo(String searchInfo) {
        log.info("[{}] {}", gameId, searchInfo);
    }

    @Override
    public void searchFinished(SearchResponse searchResponse) {
        log.info("[{}] {}", gameId, searchResponse);
    }
}
