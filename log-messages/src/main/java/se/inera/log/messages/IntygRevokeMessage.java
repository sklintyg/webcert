package se.inera.log.messages;

import java.io.Serializable;

/**
 * @author andreaskaltenbach
 */
public class IntygRevokeMessage extends AbstractLogMessage implements Serializable {

    public IntygRevokeMessage(String intygId) {
        super(ActivityType.REVOKE);
        setActivityLevel(intygId);
    }
}
