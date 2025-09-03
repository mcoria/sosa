package net.chesstango.sosa.messages;

/**
 * Worker                            Master
 *         ---- WorkerInit  --->    workerId: Worker activo que espera un juego de lichess
 *  gameId <--- GameStart   ----    gameId: Juego de lichess que se va a jugar
 *         ---- WorkerReady --->    workerId, gameId: Para iniciar el loop que lee los mensajes de juego de lichess
 *                                                    Lo almacena en Redis en caso que se reinicie master
 *
 *
 *   fen   <--- StartPosition ----
 *
 *         <--- GoFast       ----
 *         ---- GoFastResult --->   gameId, move: Para notificar el resultado de la jugada
 *
 *         <--- GameEnd      ----
 *
 * @author Mauricio Coria
 */
public class Constants {
    public static final String CHESS_TANGO_EXCHANGE = "chesstango.exchange";
    public static final String MASTER_QUEUE = "master_queue";
    public static final String MASTER_ROUTING_KEY = "master_rk";
}
