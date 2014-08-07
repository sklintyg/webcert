package se.inera.log.messages;

public class IntygPrintMessage extends AbstractLogMessage {

    public IntygPrintMessage(String intygId) {
        super(ActivityType.PRINT);
        setActivityLevel(intygId);
        setActivityArgs("Utskrift av intyg");
    }

}
