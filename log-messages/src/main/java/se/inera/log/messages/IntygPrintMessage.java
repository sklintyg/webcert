package se.inera.log.messages;

import java.io.Serializable;

public class IntygPrintMessage extends AbstractLogMessage implements Serializable {

    public IntygPrintMessage(String intygId, String printMethod) {
        super(ActivityType.PRINT);
        setActivityLevel(intygId);
        setActivityArgs(printMethod);
    }

}
