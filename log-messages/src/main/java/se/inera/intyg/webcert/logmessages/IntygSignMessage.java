package se.inera.intyg.webcert.logmessages;

import java.io.Serializable;

/**
 * @author andreaskaltenbach
 */
public class IntygSignMessage extends AbstractLogMessage implements Serializable {

    private static final long serialVersionUID = -4742054963818862591L;

    public IntygSignMessage(String intygId) {
        super(ActivityType.SIGN);
        setActivityLevel(intygId);
    }
}
