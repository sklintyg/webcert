package se.inera.intyg.webcert.logmessages;

import java.io.Serializable;

/**
 * @author andreaskaltenbach
 */
public class IntygReadMessage extends AbstractLogMessage implements Serializable {

    private static final long serialVersionUID = -4683928451142580674L;

    public IntygReadMessage(String intygId) {
        super(ActivityType.READ);
        setActivityLevel(intygId);
    }
}
