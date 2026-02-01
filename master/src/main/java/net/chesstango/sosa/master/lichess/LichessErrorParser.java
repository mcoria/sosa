package net.chesstango.sosa.master.lichess;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.lichess.errors.RetryIn;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Component
public class LichessErrorParser {

    public Object parse(String jsonString) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            JsonNode rootNode = objectMapper.readTree(jsonString);

            JsonNode rateLimitNode = rootNode.get("ratelimit");

            if (rateLimitNode != null) {
                String rateLimitKey = rateLimitNode.get("key").asText();
                if ("bot.vsBot.day".equals(rateLimitKey)) {
                    long seconds = rateLimitNode.get("seconds").asLong();
                    return new RetryIn(seconds);
                }
            }

        } catch (JacksonException e) {
            log.warn("Error parsing payload: {}", jsonString);
        }

        return jsonString;
    }
}
