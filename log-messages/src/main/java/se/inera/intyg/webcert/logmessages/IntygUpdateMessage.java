package se.inera.intyg.webcert.logmessages;

import java.io.Serializable;

/**
 * @author andreaskaltenbach
 */
public class IntygUpdateMessage extends AbstractLogMessage implements Serializable {

    private static final long serialVersionUID = -1873845701706544120L;

    public IntygUpdateMessage(String intygId) {
        super(ActivityType.UPDATE);
        setActivityLevel(intygId);
    }
}
