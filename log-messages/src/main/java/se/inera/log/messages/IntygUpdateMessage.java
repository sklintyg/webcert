package se.inera.log.messages;

import java.io.Serializable;

/**
 * @author andreaskaltenbach
 */
public class IntygUpdateMessage extends AbstractLogMessage implements Serializable {

    public IntygUpdateMessage(String intygId) {
        super(ActivityType.UPDATE);
        setActivityLevel(intygId);
    }
}
