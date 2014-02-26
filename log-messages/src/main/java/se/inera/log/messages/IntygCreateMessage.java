package se.inera.log.messages;

public class IntygCreateMessage extends AbstractLogMessage {

    public IntygCreateMessage(String intygId) {
        super(ActivityType.WRITE, ActivityPurpose.CARE_TREATMENT, "Intyg");
        setActivityLevel(intygId);
    }

}
