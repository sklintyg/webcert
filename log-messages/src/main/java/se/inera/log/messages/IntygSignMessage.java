package se.inera.log.messages;

import java.io.Serializable;

/**
 * @author andreaskaltenbach
 */
public class IntygSignMessage extends AbstractLogMessage implements Serializable {

    public IntygSignMessage(String intygId) {
        super(ActivityType.SIGN);
        setActivityLevel(intygId);
    }
}
