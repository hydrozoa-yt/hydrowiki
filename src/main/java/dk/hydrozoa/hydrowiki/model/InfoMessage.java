package dk.hydrozoa.hydrowiki.model;

import java.util.Map;

/**
 * For messages displayed at the top of a page after an interaction.
 * For example "Wrong credentials" when logging in.
 */
public class InfoMessage {

    public enum TYPE {
        ERROR,
        SUCCESS,
        WARNING,
        ;
    }

    public record Message(TYPE type, String message){};

    public static Map<String, String> toModel(Message message) {
        return Map.of("type", message.type().name(),
                "message", message.message);
    }
}
