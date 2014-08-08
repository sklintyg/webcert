package se.inera.log.messages;

public class IntygPrintMessage extends AbstractLogMessage {

    public IntygPrintMessage(String intygId, String printMethod) {
        super(ActivityType.PRINT);
        setActivityLevel(intygId);
        setActivityArgs(printMethod);
    }

}
