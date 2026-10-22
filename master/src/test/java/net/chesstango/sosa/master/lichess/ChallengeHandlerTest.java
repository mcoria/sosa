package net.chesstango.sosa.master.lichess;

import chariot.model.*;
import net.chesstango.sosa.master.SosaState;
import net.chesstango.sosa.master.events.LichessTooManyExpired;
import net.chesstango.sosa.master.events.LichessTooManyGamesPlayed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Mauricio Coria
 */
@ExtendWith(MockitoExtension.class)
public class ChallengeHandlerTest {

    ChallengeHandler challengeHandler;

    @Mock
    LichessClient client;

    @Mock
    SosaState sosaState;

    @BeforeEach
    void before() {
        challengeHandler = new ChallengeHandler(client, sosaState);
        challengeHandler.init();
    }

    /**
     * Si yo creo un challenge, no respondemos nada
     *
     * Esto previene la situacion la cual:
     * 1) Yo creo un challenge
     * 2) El bot oponente declina
     * 3) Yo declino / acepto el challenge. Pero dado que se encuentra declinado, la respuesta es 404
     *
     */
    @Test
    void ignoreMyChallengesTest() {
        Event.ChallengeCreatedEvent challengeCreatedEvent = mock(Event.ChallengeCreatedEvent.class);
        ChallengeInfo challengeInfo = mock(ChallengeInfo.class);
        ChallengeInfo.FromTo fromToPlayers = mock(ChallengeInfo.FromTo.class);
        ChallengeInfo.Player player = mock(ChallengeInfo.Player.class);
        IdNameTitle user = mock(IdNameTitle.class);

        when(challengeCreatedEvent.id()).thenReturn("challengeId");
        when(challengeCreatedEvent.challenge()).thenReturn(challengeInfo);
        when(challengeInfo.players()).thenReturn(fromToPlayers);
        when(fromToPlayers.challenger()).thenReturn(player);
        when(player.user()).thenReturn(user);
        when(user.id()).thenReturn("myId");

        /**
         * Sosa setup
         */
        UserAuth userAuth = mock(UserProfileData.class);
        when(sosaState.getMyProfile()).thenReturn(userAuth);
        when(userAuth.id()).thenReturn("myId");

        challengeHandler.challengeCreated(challengeCreatedEvent);

        /**
         * No respondemos nada
         */
        verify(sosaState, never()).addChallenge(any());
        verifyNoInteractions(client);
    }

    @Test
    @DisplayName("Ignore challenge when players type is From and challenger is my profile")
    void ignoreMyChallenges_fromTypeTest() {
        Event.ChallengeCreatedEvent challengeCreatedEvent = mock(Event.ChallengeCreatedEvent.class);
        ChallengeInfo challengeInfo = mock(ChallengeInfo.class);
        ChallengeInfo.From fromPlayers = mock(ChallengeInfo.From.class);
        ChallengeInfo.Player player = mock(ChallengeInfo.Player.class);
        IdNameTitle user = mock(IdNameTitle.class);

        when(challengeCreatedEvent.id()).thenReturn("challengeFromId");
        when(challengeCreatedEvent.challenge()).thenReturn(challengeInfo);
        when(challengeInfo.players()).thenReturn(fromPlayers);
        when(fromPlayers.challenger()).thenReturn(player);
        when(player.user()).thenReturn(user);
        when(user.id()).thenReturn("myId");

        UserAuth userAuth = mock(UserProfileData.class);
        when(sosaState.getMyProfile()).thenReturn(userAuth);
        when(userAuth.id()).thenReturn("myId");

        challengeHandler.challengeCreated(challengeCreatedEvent);

        verify(sosaState, never()).addChallenge(any());
        verifyNoInteractions(client);
    }

    @Test
    @DisplayName("handleChallengeEvent dispatches challengeCreated event correctly")
    void handleChallengeEvent_challengeCreated() {
        Event.ChallengeCreatedEvent challengeCreatedEvent = mock(Event.ChallengeCreatedEvent.class);
        ChallengeInfo challengeInfo = mock(ChallengeInfo.class);
        ChallengeInfo.FromTo fromToPlayers = mock(ChallengeInfo.FromTo.class);
        ChallengeInfo.Player player = mock(ChallengeInfo.Player.class);
        IdNameTitle user = mock(IdNameTitle.class);

        when(challengeCreatedEvent.type()).thenReturn(Event.Type.challenge);
        when(challengeCreatedEvent.id()).thenReturn("challengeEventId");
        when(challengeCreatedEvent.challenge()).thenReturn(challengeInfo);
        when(challengeInfo.players()).thenReturn(fromToPlayers);
        when(fromToPlayers.challenger()).thenReturn(player);
        when(player.user()).thenReturn(user);
        when(user.id()).thenReturn("myId");

        UserAuth userAuth = mock(UserProfileData.class);
        when(sosaState.getMyProfile()).thenReturn(userAuth);
        when(userAuth.id()).thenReturn("myId");

        challengeHandler.handleChallengeEvent(challengeCreatedEvent);

        verify(sosaState, never()).addChallenge(any());
        verifyNoInteractions(client);
    }

    @Test
    @DisplayName("handleChallengeEvent dispatches challengeCanceled event correctly")
    void handleChallengeEvent_challengeCanceled() {
        Event.ChallengeCanceledEvent challengeCanceledEvent = mock(Event.ChallengeCanceledEvent.class);
        when(challengeCanceledEvent.type()).thenReturn(Event.Type.challengeCanceled);
        when(challengeCanceledEvent.id()).thenReturn("canceledId");

        challengeHandler.handleChallengeEvent(challengeCanceledEvent);

        verify(sosaState).removeChallenge("canceledId");
    }

    @Test
    @DisplayName("handleChallengeEvent dispatches challengeDeclined event correctly")
    void handleChallengeEvent_challengeDeclined() {
        Event.ChallengeDeclinedEvent challengeDeclinedEvent = mock(Event.ChallengeDeclinedEvent.class);
        when(challengeDeclinedEvent.type()).thenReturn(Event.Type.challengeDeclined);
        when(challengeDeclinedEvent.id()).thenReturn("declinedId");

        challengeHandler.handleChallengeEvent(challengeDeclinedEvent);

        verify(sosaState).removeChallenge("declinedId");
    }

    @Test
    @DisplayName("challengeCanceled removes challenge from state")
    void challengeCanceled_removesChallengeFromState() {
        Event.ChallengeCanceledEvent event = mock(Event.ChallengeCanceledEvent.class);
        when(event.id()).thenReturn("c1");

        challengeHandler.challengeCanceled(event);

        verify(sosaState).removeChallenge("c1");
    }

    @Test
    @DisplayName("challengeDeclined removes challenge from state")
    void challengeDeclined_removesChallengeFromState() {
        Event.ChallengeDeclinedEvent event = mock(Event.ChallengeDeclinedEvent.class);
        when(event.id()).thenReturn("d1");

        challengeHandler.challengeDeclined(event);

        verify(sosaState).removeChallenge("d1");
    }

    @Test
    @DisplayName("Accept challenge from human when worker is available and time control is valid")
    void challengeCreated_acceptHumanChallengeWhenWorkerAvailable() {
        Event.ChallengeCreatedEvent event = setupChallengeEvent(
                "cAccept1",
                "opponentId",
                Optional.empty(),
                1500,
                Variant.Basic.standard,
                createRealTimeControl(Enums.Speed.blitz, 180L)
        );

        when(sosaState.isChallengeAcceptable(anyBoolean())).thenReturn(true);

        challengeHandler.challengeCreated(event);

        verify(sosaState).addChallenge("cAccept1");
        verify(client).challengeAccept("cAccept1");
        verify(client, never()).challengeDecline(any());
    }

    @Test
    @DisplayName("Decline challenge from human when no worker is available")
    void challengeCreated_declineHumanChallengeWhenNoWorkerAvailable() {
        Event.ChallengeCreatedEvent event = setupChallengeEvent(
                "cDeclineNoWorker",
                "opponentId",
                Optional.empty(),
                1500,
                Variant.Basic.standard,
                createRealTimeControl(Enums.Speed.blitz, 180L)
        );

        when(sosaState.isChallengeAcceptable(anyBoolean())).thenReturn(false);

        challengeHandler.challengeCreated(event);

        verify(sosaState, never()).addChallenge(any());
        verify(client).challengeDecline("cDeclineNoWorker");
        verify(client, never()).challengeAccept(any());
    }

    @Test
    @DisplayName("Decline challenge when not accepting challenges (too many games played)")
    void challengeCreated_declineWhenNotAcceptingChallenges() {
        challengeHandler.onLichessTooManyGamesPlayed();

        Event.ChallengeCreatedEvent event = mock(Event.ChallengeCreatedEvent.class);
        ChallengeInfo challengeInfo = mock(ChallengeInfo.class);
        ChallengeInfo.FromTo fromToPlayers = mock(ChallengeInfo.FromTo.class);
        ChallengeInfo.Player player = mock(ChallengeInfo.Player.class);
        IdNameTitle user = mock(IdNameTitle.class);

        when(event.id()).thenReturn("cDisabled");
        when(event.challenge()).thenReturn(challengeInfo);
        when(challengeInfo.players()).thenReturn(fromToPlayers);
        when(fromToPlayers.challenger()).thenReturn(player);
        when(player.user()).thenReturn(user);
        when(user.id()).thenReturn("opponentId");

        UserAuth userAuth = mock(UserProfileData.class);
        when(sosaState.getMyProfile()).thenReturn(userAuth);
        when(userAuth.id()).thenReturn("myId");

        challengeHandler.challengeCreated(event);

        verify(sosaState, never()).addChallenge(any());
        verify(client).challengeDecline("cDisabled");
        verify(client, never()).challengeAccept(any());
    }

    @Test
    @DisplayName("Resume accepting challenges when onLichessTooManyExpired with GAMES expiration type")
    void onLichessTooManyExpired_games_resumesAcceptingChallenges() {
        challengeHandler.onLichessTooManyGamesPlayed();

        LichessTooManyExpired expiredEvent = new LichessTooManyExpired(this, LichessTooManyExpired.ExpirationType.GAMES);
        challengeHandler.onLichessTooManyExpired(expiredEvent);

        Event.ChallengeCreatedEvent event = setupChallengeEvent(
                "cResumed",
                "opponentId",
                Optional.empty(),
                1500,
                Variant.Basic.standard,
                createRealTimeControl(Enums.Speed.rapid, 600L)
        );

        when(sosaState.isChallengeAcceptable(anyBoolean())).thenReturn(true);

        challengeHandler.challengeCreated(event);

        verify(client).challengeAccept("cResumed");
        verify(sosaState).addChallenge("cResumed");
    }

    @Test
    @DisplayName("Do not resume accepting challenges when onLichessTooManyExpired with REQUESTS expiration type")
    void onLichessTooManyExpired_requests_doesNotResumeAcceptingChallenges() {
        challengeHandler.onLichessTooManyGamesPlayed();

        LichessTooManyExpired expiredEvent = new LichessTooManyExpired(this, LichessTooManyExpired.ExpirationType.REQUESTS);
        challengeHandler.onLichessTooManyExpired(expiredEvent);

        Event.ChallengeCreatedEvent event = mock(Event.ChallengeCreatedEvent.class);
        ChallengeInfo challengeInfo = mock(ChallengeInfo.class);
        ChallengeInfo.FromTo fromToPlayers = mock(ChallengeInfo.FromTo.class);
        ChallengeInfo.Player player = mock(ChallengeInfo.Player.class);
        IdNameTitle user = mock(IdNameTitle.class);

        when(event.id()).thenReturn("cStillDisabled");
        when(event.challenge()).thenReturn(challengeInfo);
        when(challengeInfo.players()).thenReturn(fromToPlayers);
        when(fromToPlayers.challenger()).thenReturn(player);
        when(player.user()).thenReturn(user);
        when(user.id()).thenReturn("opponentId");

        UserAuth userAuth = mock(UserProfileData.class);
        when(sosaState.getMyProfile()).thenReturn(userAuth);
        when(userAuth.id()).thenReturn("myId");

        challengeHandler.challengeCreated(event);

        verify(client).challengeDecline("cStillDisabled");
    }

    @Test
    @DisplayName("Decline challenge when challenger is empty")
    void challengeCreated_declineWhenChallengerOptEmpty() {
        Event.ChallengeCreatedEvent event = mock(Event.ChallengeCreatedEvent.class);
        ChallengeInfo challengeInfo = mock(ChallengeInfo.class);
        ChallengeInfo.FromTo fromTo = mock(ChallengeInfo.FromTo.class);
        ChallengeInfo.Player player = mock(ChallengeInfo.Player.class);
        IdNameTitle user = mock(IdNameTitle.class);

        when(event.id()).thenReturn("cNoChallenger");
        when(event.challenge()).thenReturn(challengeInfo);
        when(challengeInfo.players()).thenReturn(fromTo);
        when(fromTo.challenger()).thenReturn(player);
        when(player.user()).thenReturn(user);
        when(user.id()).thenReturn("opponentId");

        UserAuth userAuth = mock(UserProfileData.class);
        when(sosaState.getMyProfile()).thenReturn(userAuth);
        when(userAuth.id()).thenReturn("myId");

        when(fromTo.challengerOpt()).thenReturn(Optional.empty());

        challengeHandler.challengeCreated(event);

        verify(sosaState, never()).addChallenge(any());
        verify(client).challengeDecline("cNoChallenger");
    }

    @Test
    @DisplayName("Decline challenge when variant is unsupported (e.g. non-standard custom variant)")
    void challengeCreated_declineUnsupportedVariant() {
        Variant unsupportedVariant = Variant.fromString("chess960");
        Event.ChallengeCreatedEvent event = setupChallengeEvent(
                "cUnsupportedVariant",
                "opponentId",
                Optional.empty(),
                1500,
                unsupportedVariant,
                createRealTimeControl(Enums.Speed.blitz, 180L)
        );

        challengeHandler.challengeCreated(event);

        verify(sosaState, never()).addChallenge(any());
        verify(client).challengeDecline("cUnsupportedVariant");
        verify(client, never()).challengeAccept(any());
    }

    @Test
    @DisplayName("Accept challenge with FromPosition variant when worker available")
    void challengeCreated_acceptFromPositionVariant() {
        Variant fromPositionVariant = Variant.fromString("fromPosition");
        Event.ChallengeCreatedEvent event = setupChallengeEvent(
                "cFromPos",
                "opponentId",
                Optional.empty(),
                1500,
                fromPositionVariant,
                createRealTimeControl(Enums.Speed.blitz, 180L)
        );

        when(sosaState.isChallengeAcceptable(anyBoolean())).thenReturn(true);

        challengeHandler.challengeCreated(event);

        verify(client).challengeAccept("cFromPos");
        verify(sosaState).addChallenge("cFromPos");
    }

    @Test
    @DisplayName("Decline challenge when time control is not RealTime (e.g. Unlimited)")
    void challengeCreated_declineNonRealTimeControl() {
        TimeControl unlimited = mock(Unlimited.class);
        Event.ChallengeCreatedEvent event = setupChallengeEvent(
                "cUnlimited",
                "opponentId",
                Optional.empty(),
                1500,
                Variant.Basic.standard,
                unlimited
        );

        challengeHandler.challengeCreated(event);

        verify(client).challengeDecline("cUnlimited");
    }

    @Test
    @DisplayName("Decline challenge when real time initial duration is less than 30 seconds")
    void challengeCreated_declineRealTimeDurationLessThan30Seconds() {
        Event.ChallengeCreatedEvent event = setupChallengeEvent(
                "cShortTime",
                "opponentId",
                Optional.empty(),
                1500,
                Variant.Basic.standard,
                createRealTimeControl(Enums.Speed.bullet, 15L)
        );

        challengeHandler.challengeCreated(event);

        verify(client).challengeDecline("cShortTime");
    }

    @Test
    @DisplayName("Decline challenge when real time speed is unsupported (e.g. classical)")
    void challengeCreated_declineUnsupportedSpeed() {
        Event.ChallengeCreatedEvent event = setupChallengeEvent(
                "cClassical",
                "opponentId",
                Optional.empty(),
                1500,
                Variant.Basic.standard,
                createRealTimeControl(Enums.Speed.classical, 1800L)
        );

        challengeHandler.challengeCreated(event);

        verify(client).challengeDecline("cClassical");
    }

    @Test
    @DisplayName("Accept bot challenge when bot rating is within threshold for blitz")
    void challengeCreated_acceptBotChallenge_blitzWithinThreshold() {
        Event.ChallengeCreatedEvent event = setupChallengeEvent(
                "cBotBlitz",
                "botOpponent",
                Optional.of("BOT"),
                1800,
                Variant.Basic.standard,
                createRealTimeControl(Enums.Speed.blitz, 180L)
        );

        when(sosaState.getMyRating(StatsPerfType.blitz)).thenReturn(1500);
        when(sosaState.isChallengeAcceptable(anyBoolean())).thenReturn(true);

        challengeHandler.challengeCreated(event);

        verify(client).challengeAccept("cBotBlitz");
        verify(sosaState).addChallenge("cBotBlitz");
    }

    @Test
    @DisplayName("Accept bot challenge when bot rating is within threshold for bullet")
    void challengeCreated_acceptBotChallenge_bulletWithinThreshold() {
        Event.ChallengeCreatedEvent event = setupChallengeEvent(
                "cBotBullet",
                "botOpponent",
                Optional.of("BOT"),
                1750,
                Variant.Basic.standard,
                createRealTimeControl(Enums.Speed.bullet, 60L)
        );

        when(sosaState.getMyRating(StatsPerfType.bullet)).thenReturn(1500);
        when(sosaState.isChallengeAcceptable(anyBoolean())).thenReturn(true);

        challengeHandler.challengeCreated(event);

        verify(client).challengeAccept("cBotBullet");
        verify(sosaState).addChallenge("cBotBullet");
    }

    @Test
    @DisplayName("Accept bot challenge when bot rating is within threshold for rapid")
    void challengeCreated_acceptBotChallenge_rapidWithinThreshold() {
        Event.ChallengeCreatedEvent event = setupChallengeEvent(
                "cBotRapid",
                "botOpponent",
                Optional.of("BOT"),
                1800,
                Variant.Basic.standard,
                createRealTimeControl(Enums.Speed.rapid, 600L)
        );

        when(sosaState.getMyRating(StatsPerfType.rapid)).thenReturn(1500);
        when(sosaState.isChallengeAcceptable(anyBoolean())).thenReturn(true);

        challengeHandler.challengeCreated(event);

        verify(client).challengeAccept("cBotRapid");
        verify(sosaState).addChallenge("cBotRapid");
    }

    @Test
    @DisplayName("Decline bot challenge when bot rating exceeds threshold")
    void challengeCreated_declineBotChallenge_ratingExceedsThreshold() {
        Event.ChallengeCreatedEvent event = setupChallengeEvent(
                "cBotHighRating",
                "botOpponent",
                Optional.of("BOT"),
                1801, // 1500 + 300 = 1800 threshold
                Variant.Basic.standard,
                createRealTimeControl(Enums.Speed.blitz, 180L)
        );

        when(sosaState.getMyRating(StatsPerfType.blitz)).thenReturn(1500);

        challengeHandler.challengeCreated(event);

        verify(client).challengeDecline("cBotHighRating");
        verify(client, never()).challengeAccept(any());
    }

    @Test
    @DisplayName("Static isVariantAcceptable returns true for standard and fromPosition, false for other variants")
    void isVariantAcceptable_staticMethodTest() {
        assertTrue(ChallengeHandler.isVariantAcceptable(Variant.Basic.standard));
        assertTrue(ChallengeHandler.isVariantAcceptable(Variant.fromString("fromPosition")));
        assertFalse(ChallengeHandler.isVariantAcceptable(Variant.fromString("chess960")));
        assertFalse(ChallengeHandler.isVariantAcceptable(Variant.fromString("antichess")));
        assertFalse(ChallengeHandler.isVariantAcceptable(null));
    }

    @Test
    @DisplayName("Static isTimeControlAcceptable validates speed and minimum duration")
    void isTimeControlAcceptable_staticMethodTest() {
        RealTime validBullet = createRealTimeControl(Enums.Speed.bullet, 30L);
        RealTime validBlitz = createRealTimeControl(Enums.Speed.blitz, 180L);
        RealTime validRapid = createRealTimeControl(Enums.Speed.rapid, 600L);
        RealTime tooShortBullet = createRealTimeControl(Enums.Speed.bullet, 29L);
        RealTime classical = createRealTimeControl(Enums.Speed.classical, 1800L);
        RealTime ultraBullet = createRealTimeControl(Enums.Speed.ultraBullet, 15L);
        Unlimited unlimited = mock(Unlimited.class);

        assertTrue(ChallengeHandler.isTimeControlAcceptable(validBullet));
        assertTrue(ChallengeHandler.isTimeControlAcceptable(validBlitz));
        assertTrue(ChallengeHandler.isTimeControlAcceptable(validRapid));
        assertFalse(ChallengeHandler.isTimeControlAcceptable(tooShortBullet));
        assertFalse(ChallengeHandler.isTimeControlAcceptable(classical));
        assertFalse(ChallengeHandler.isTimeControlAcceptable(ultraBullet));
        assertFalse(ChallengeHandler.isTimeControlAcceptable(unlimited));
        assertFalse(ChallengeHandler.isTimeControlAcceptable(null));
    }

    @Test
    @DisplayName("Static isChallengerHuman returns true for users without title, false for titled users")
    void isChallengerHuman_staticMethodTest() {
        ChallengeInfo.Player humanPlayer = mock(ChallengeInfo.Player.class);
        IdNameTitle humanUser = mock(IdNameTitle.class);
        when(humanPlayer.user()).thenReturn(humanUser);
        when(humanUser.titleOpt()).thenReturn(Optional.empty());

        ChallengeInfo.Player botPlayer = mock(ChallengeInfo.Player.class);
        IdNameTitle botUser = mock(IdNameTitle.class);
        when(botPlayer.user()).thenReturn(botUser);
        when(botUser.titleOpt()).thenReturn(Optional.of("BOT"));

        assertTrue(ChallengeHandler.isChallengerHuman(humanPlayer));
        assertFalse(ChallengeHandler.isChallengerHuman(botPlayer));
    }

    private RealTime createRealTimeControl(Enums.Speed speed, long initialSeconds) {
        RealTime realTime = mock(RealTime.class);
        lenient().when(realTime.speed()).thenReturn(speed);
        lenient().when(realTime.initial()).thenReturn(Duration.ofSeconds(initialSeconds));
        return realTime;
    }

    private Event.ChallengeCreatedEvent setupChallengeEvent(
            String challengeId,
            String challengerId,
            Optional<String> titleOpt,
            int rating,
            Variant variant,
            TimeControl timeControl
    ) {
        Event.ChallengeCreatedEvent event = mock(Event.ChallengeCreatedEvent.class);
        ChallengeInfo challengeInfo = mock(ChallengeInfo.class);
        ChallengeInfo.FromTo fromToPlayers = mock(ChallengeInfo.FromTo.class);
        ChallengeInfo.Player player = mock(ChallengeInfo.Player.class);
        IdNameTitle user = mock(IdNameTitle.class);
        GameType gameType = mock(GameType.class);

        when(event.id()).thenReturn(challengeId);
        when(event.challenge()).thenReturn(challengeInfo);
        when(challengeInfo.players()).thenReturn(fromToPlayers);
        when(fromToPlayers.challenger()).thenReturn(player);
        when(player.user()).thenReturn(user);
        when(user.id()).thenReturn(challengerId);

        lenient().when(fromToPlayers.challengerOpt()).thenReturn(Optional.of(player));
        lenient().when(player.rating()).thenReturn(rating);
        lenient().when(user.titleOpt()).thenReturn(titleOpt);

        lenient().when(challengeInfo.gameType()).thenReturn(gameType);
        lenient().when(gameType.variant()).thenReturn(variant);
        lenient().when(gameType.timeControl()).thenReturn(timeControl);

        UserAuth userAuth = mock(UserProfileData.class);
        lenient().when(sosaState.getMyProfile()).thenReturn(userAuth);
        lenient().when(userAuth.id()).thenReturn("myId");

        return event;
    }
}
