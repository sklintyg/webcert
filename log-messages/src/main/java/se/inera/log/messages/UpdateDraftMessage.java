package se.inera.log.messages;

public class UpdateDraftMessage extends AbstractLogMessage {

    public UpdateDraftMessage(String intygsId) {
        super(ActivityType.WRITE, ActivityPurpose.CARE_TREATMENT, "Intyg");
        setActivityLevel(intygsId);
    }

}
