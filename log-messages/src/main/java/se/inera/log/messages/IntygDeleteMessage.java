package se.inera.log.messages;

import java.io.Serializable;

/**
 * @author andreaskaltenbach
 */
public class IntygDeleteMessage extends AbstractLogMessage implements Serializable {

    public IntygDeleteMessage(String intygId) {
        super(ActivityType.DELETE);
        setActivityLevel(intygId);
    }
}
