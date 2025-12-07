package net.chesstango.sosa.master.lichess;

import chariot.model.User;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.SosaState;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static net.chesstango.sosa.master.configs.RabbitConfig.BOTS_QUEUE;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Component
public class BotQueue {

    private final LichessClient client;

    private final RabbitTemplate rabbitTemplate;

    private final Map<String, User> onlineBots = new HashMap<>();

    public BotQueue(LichessClient client, SosaState sosaState, RabbitTemplate rabbitTemplate) {
        this.client = client;
        this.rabbitTemplate = rabbitTemplate;
    }

    public synchronized User pickBot() {
        String botName = pollBotNameFromQueue();

        if (botName == null) {
            log.info("Bot queue empty, loading online bots from lichess server");
            loadOnlineBots();
            botName = pollBotNameFromQueue();
        }

        // Avoid Fail[status=400, info=Info[message={"error":"You cannot challenge yourself"}]]
        //if (!Objects.equals(theBot.id(), sosaState.getMyProfile().id())) {

        return botName != null ? loadBot(botName) : null;
    }

    private String pollBotNameFromQueue() {
        String botName = (String) rabbitTemplate.receiveAndConvert(BOTS_QUEUE);

        if (botName != null) {
            log.info("Picked bot {}", botName);
            return botName;
        }

        return null;
    }

    private void loadOnlineBots() {
        log.info("Querying lichess for online bots");
        client.botsOnline()
                .forEach(this::addBotToQueue);
        log.info("Finished querying lichess for online bots");
    }

    private void addBotToQueue(User user) {
        log.info("Bot {} online", user.id());
        String botName = user.id();
        rabbitTemplate.convertAndSend(BOTS_QUEUE, botName);
        onlineBots.put(botName, user);
    }

    private User loadBot(String botName) {
        User bot = onlineBots.remove(botName);

        if (bot == null) {
            log.info("Loading bot {} from lichess server", botName);
            bot = client.findUser(botName).orElse(null);
        }

        if(bot != null) {
            log.warn("Bot: {}", bot);
        }

        return bot;
    }
}
