package se.inera.log.messages;

public class IntygPrintMessage extends AbstractLogMessage {

    public IntygPrintMessage(String intygId) {
        super(ActivityType.PRINT, ActivityPurpose.CARE_TREATMENT, "Intyg");
        setActivityLevel(intygId);
    }

}
