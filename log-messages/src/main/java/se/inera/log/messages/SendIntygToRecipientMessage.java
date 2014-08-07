package se.inera.log.messages;

public class SendIntygToRecipientMessage extends AbstractLogMessage {

    public SendIntygToRecipientMessage(String intygId, String recipient) {
        super(ActivityType.PRINT);
        setActivityLevel(intygId);
        setActivityArgs(recipient);
    }

}
