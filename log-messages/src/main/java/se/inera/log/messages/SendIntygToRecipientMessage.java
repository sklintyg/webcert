package se.inera.log.messages;

import java.io.Serializable;

public class SendIntygToRecipientMessage extends AbstractLogMessage implements Serializable {

    public SendIntygToRecipientMessage(String intygId, String recipient) {
        super(ActivityType.PRINT);
        setActivityLevel(intygId);
        setActivityArgs(recipient);
    }

}
