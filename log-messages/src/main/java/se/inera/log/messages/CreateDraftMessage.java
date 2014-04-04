package se.inera.log.messages;

public class CreateDraftMessage extends AbstractLogMessage {

    public CreateDraftMessage(String intygId) {
        super(ActivityType.WRITE, ActivityPurpose.CARE_TREATMENT, "Intyg");
        setActivityLevel(intygId);
    }

}
