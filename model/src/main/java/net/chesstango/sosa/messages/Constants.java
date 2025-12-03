package net.chesstango.sosa.messages;

/**
 * =======================================
 * Worker Init                      Master
 *         ---- WorkerInit  --->    workerId: Worker activo que espera un juego de lichess
 *  gameId <--- GameStart   ----    gameId:   Juego de lichess que se va a jugar
 *
 * =======================================
 * Worker                           Master
 *         ---- WorkerReady --->    workerId, gameId: Iniciar el loop que lee los mensajes de juego de lichess
 *   fen   <--- StartPosition ----
 *
 *         <--- GoFast       ----
 *         ---- GoResult --->   gameId, move: Para notificar el resultado de la jugada
 *          .
 *          .
 *          .
 *         <--- GameEnd      ----   Exit(), termino el juego, terminar el job
 *
 * @author Mauricio Coria
 */
public class Constants {
    public static final String SOSA_EXCHANGE = "sosa.exchange";
    public static final String MASTER_QUEUE = "sosa-master";
    public static final String MASTER_ROUTING_KEY = "sosa_master_rk";
}
