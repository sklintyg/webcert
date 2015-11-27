package se.inera.intyg.webcert.logmessages;

import java.io.Serializable;

/**
 * @author andreaskaltenbach
 */
public class IntygSendMessage extends AbstractLogMessage implements Serializable {

    private static final long serialVersionUID = 6541962406214622626L;

    public IntygSendMessage(String intygId, String additionalInfo) {
        super(ActivityType.SEND);
        setActivityLevel(intygId);
        setActivityArgs(additionalInfo);
    }
}
