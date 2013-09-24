package se.inera.webcert.mailstub;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author andreaskaltenbach
 */
public class OutgoingMail {
    
    private List<String> recipients = new ArrayList<>();
    private String subject;
    private String body;

    public OutgoingMail(MimeMessage message) throws MessagingException, IOException {

        for (Address address : message.getAllRecipients()) {
            recipients.add(address.toString());
        }

        subject = message.getSubject();
        body = message.getContent().toString();
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }
}
