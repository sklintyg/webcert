package se.inera.intyg.webcert.logmessages;

import java.io.Serializable;

public class IntygPrintMessage extends AbstractLogMessage implements Serializable {

    private static final long serialVersionUID = 6590364960205870820L;

    public IntygPrintMessage(String intygId, String printMethod) {
        super(ActivityType.PRINT);
        setActivityLevel(intygId);
        setActivityArgs(printMethod);
    }

}
