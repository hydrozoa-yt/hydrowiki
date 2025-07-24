package dk.hydrozoa.hydrowiki.model;

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
}
