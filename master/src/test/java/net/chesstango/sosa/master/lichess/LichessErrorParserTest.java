package net.chesstango.sosa.master.lichess;

import net.chesstango.sosa.master.lichess.errors.RetryIn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Mauricio Coria
 */
public class LichessErrorParserTest {
    LichessErrorParser lichessErrorParser;

    @BeforeEach
    void setUp() {
        lichessErrorParser = new LichessErrorParser();
    }

    @Test
    void parseTooManyGamesTest() {
        Object result = lichessErrorParser.parse("{\"error\":\"You played 100 games against other bots today, please wait before challenging another bot.\",\"ratelimit\":{\"key\":\"bot.vsBot.day\",\"seconds\":27594}}");

        assertNotNull(result);
        assertInstanceOf(RetryIn.class, result);

        RetryIn retryIn = (RetryIn) result;
        assertEquals(27594L, retryIn.getSeconds());
    }


    @Test
    void parseAnyTest() {
        Object result = lichessErrorParser.parse("bla");

        assertNotNull(result);
        assertInstanceOf(String.class, result);

        assertEquals("bla", result);
    }
}
