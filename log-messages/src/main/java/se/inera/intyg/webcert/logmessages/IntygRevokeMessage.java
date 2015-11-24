package se.inera.log.messages;

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
