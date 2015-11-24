package se.inera.intyg.webcert.logmessages;

import java.io.Serializable;

/**
 * @author andreaskaltenbach
 */
public class IntygRevokeMessage extends AbstractLogMessage implements Serializable {

    private static final long serialVersionUID = 5186634858995110120L;

    public IntygRevokeMessage(String intygId) {
        super(ActivityType.REVOKE);
        setActivityLevel(intygId);
    }
}
