package se.inera.intyg.webcert.logmessages;

import java.io.Serializable;

/**
 * @author andreaskaltenbach
 */
public class IntygCreateMessage extends AbstractLogMessage implements Serializable {

    private static final long serialVersionUID = 5465818282454398723L;

    public IntygCreateMessage(String intygId) {
        super(ActivityType.CREATE);
        setActivityLevel(intygId);
    }
}
