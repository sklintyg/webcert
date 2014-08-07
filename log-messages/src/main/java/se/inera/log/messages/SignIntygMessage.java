package se.inera.log.messages;

public class SignIntygMessage extends AbstractLogMessage {

    public SignIntygMessage(String intygId) {
        super(ActivityType.SIGN);
        setActivityLevel(intygId);
    }

}
