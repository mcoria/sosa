package net.chesstango.sosa.master.lichess;

import chariot.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static net.chesstango.sosa.master.configs.RabbitConfig.BOTS_QUEUE;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Service
public class BotQueue {

    private final LichessClient client;

    private final RabbitTemplate rabbitTemplate;

    private final Map<String, User> onlineBots = new HashMap<>();

    public BotQueue(LichessClient client, RabbitTemplate rabbitTemplate) {
        this.client = client;
        this.rabbitTemplate = rabbitTemplate;
    }

    public synchronized User pickBot() {
        String botName = pollBotNameFromQueue();

        if (botName == null) {
            log.info("No bot in queue");
            loadOnlineBots();
            botName = pollBotNameFromQueue();
        }

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

        return bot;
    }
}
