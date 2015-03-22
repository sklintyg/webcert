package se.inera.log.messages;

import java.io.Serializable;

/**
 * @author andreaskaltenbach
 */
public class IntygCreateMessage extends AbstractLogMessage implements Serializable {

    public IntygCreateMessage(String intygId) {
        super(ActivityType.CREATE);
        setActivityLevel(intygId);
    }
}
