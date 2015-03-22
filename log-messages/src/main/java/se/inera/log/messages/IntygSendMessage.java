package se.inera.log.messages;

import java.io.Serializable;

/**
 * @author andreaskaltenbach
 */
public class IntygSendMessage extends AbstractLogMessage implements Serializable {

    public IntygSendMessage(String intygId, String additionalInfo) {
        super(ActivityType.SEND);
        setActivityLevel(intygId);
        setActivityArgs(additionalInfo);
    }
}
