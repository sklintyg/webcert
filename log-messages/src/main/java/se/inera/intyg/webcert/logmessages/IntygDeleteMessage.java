package se.inera.intyg.webcert.logmessages;

import java.io.Serializable;

/**
 * @author andreaskaltenbach
 */
public class IntygDeleteMessage extends AbstractLogMessage implements Serializable {

    private static final long serialVersionUID = -5533285582058768566L;

    public IntygDeleteMessage(String intygId) {
        super(ActivityType.DELETE);
        setActivityLevel(intygId);
    }
}
