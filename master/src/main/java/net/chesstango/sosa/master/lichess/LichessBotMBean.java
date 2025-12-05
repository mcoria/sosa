package net.chesstango.sosa.master.lichess;

/**
 * @author Mauricio Coria
 */
public interface LichessBotMBean {
    /**
     * Send challenge request
     *
     * @param user The user or bot name
     * @param type SendChallenge type
     */
    void challengeUser(String user, String type);


    /**
     * Send challenge request to a random bot with rating within acceptable threshold
     */
    void challengeRandomBot();

    /**
     * Stop accepting/sending challenges
     */
    void stopAcceptingChallenges();

    /**
     *     @Override
     *     public void challengeUser(String user, String type) {
     *         ChallengeType challengeType = switch (type) {
     *             case "BULLET" -> ChallengeType.BULLET;
     *             case "BLITZ" -> ChallengeType.BLITZ;
     *             case "RAPID" -> ChallengeType.RAPID;
     *             default -> null;
     *         };
     *
     *         if (challengeType == null) {
     *             log.error("unknown challengeType type, valid values: BULLET BLITZ RAPID");
     *             return;
     *         }
     *
     *         lichessChallenger.challengeUser(user, challengeType);
     *     }
     *
     *     @Override
     *     public void challengeRandomBot() {
     *         try {
     *             lichessChallenger.challengeRandomBot();
     *         } catch (RuntimeException e) {
     *             log.error("challengeRandomBot failed", e);
     *             System.exit(-1);
     *         }
     *     }
     *
     *     @Override
     *     public void stopAcceptingChallenges() {
     *         log.info("stopAcceptingChallenges() invoked");
     *         lichessChallengeHandler.stopAcceptingChallenges();
     *     }
     */
}
