package se.inera.log.messages;

public class DeleteDraftMessage extends AbstractLogMessage {

    public DeleteDraftMessage(String intygId) {
        super(ActivityType.DELETE, ActivityPurpose.CARE_TREATMENT, "Intyg");
        setActivityLevel(intygId);
    }

}
