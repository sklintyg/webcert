package se.inera.logsender.messages;

import java.io.Serializable;

/**
 * @author andreaskaltenbach
 */
public class IntygReadMessage extends AbstractLogMessage implements Serializable {

    public IntygReadMessage() {
        super("Läsa", "Vård och behandling", "intyg");
    }
}
