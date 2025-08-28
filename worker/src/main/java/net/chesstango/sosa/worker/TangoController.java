package net.chesstango.sosa.worker;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.board.representations.move.SimpleMoveEncoder;
import net.chesstango.engine.Config;
import net.chesstango.engine.SearchListener;
import net.chesstango.engine.Session;
import net.chesstango.engine.Tango;
import net.chesstango.gardel.fen.FEN;
import net.chesstango.search.PrincipalVariation;
import net.chesstango.search.SearchResult;
import net.chesstango.search.SearchResultByDepth;
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

    public TangoController(@Value("${gameId}") String gameId) {
        this.gameId = gameId;
    }

    @PostConstruct
    public void init() {
        log.info("Initializing Tango");

        Config config = new Config();
        config.setSyncSearch(true);

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
            Future<SearchResult> searchResultFuture = session.goFast(wTime, bTime, wInc, bInc);
            SearchResult searchResult = searchResultFuture.get();

            return simpleMoveEncoder.encode(searchResult.getBestMove());
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void searchStarted() {
        log.info("Search started");
    }

    @Override
    public void searchInfo(SearchResultByDepth searchResultByDepth) {
        String pvString = String.format("%s %s", simpleMoveEncoder.encodeMoves(searchResultByDepth.getPrincipalVariation().stream().map(PrincipalVariation::move).toList()), searchResultByDepth.isPvComplete() ? "" : "*");
        log.info("[{}] Depth {} seldepth {} eval {} pv {}", gameId, String.format("%2d", searchResultByDepth.getDepth()), String.format("%2d", searchResultByDepth.getDepth()), String.format("%8d", searchResultByDepth.getBestEvaluation()), pvString);
    }

    @Override
    public void searchFinished(SearchResult searchResult) {
        String moveUci = simpleMoveEncoder.encode(searchResult.getBestMove());
        log.info("[{}] Search finished: eval {} move {}", gameId, String.format("%8d", searchResult.getBestEvaluation()), moveUci);
    }
}
