package se.inera.log.messages;

public class SignDraftMessage extends AbstractLogMessage {

    public SignDraftMessage(String intygsId) {
        super(ActivityType.SIGN, ActivityPurpose.CARE_TREATMENT, "Intyg");
        setActivityLevel(intygsId);
    }

}
