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
    
    private String from;
    private List<String> recipients = new ArrayList<>();
    private String subject;
    private String body;

    public OutgoingMail(MimeMessage message) throws MessagingException, IOException {

        for (Address address : message.getAllRecipients()) {
            recipients.add(address.toString());
        }

        from = message.getFrom()[0].toString();
        subject = message.getSubject();
        body = message.getContent().toString();
    }

    public String getFrom() {
        return from;
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

    // Eclipse-generated hashCode implementation, based on recipients, subject and body
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((body == null) ? 0 : body.hashCode());
        result = prime * result + ((recipients == null) ? 0 : recipients.hashCode());
        result = prime * result + ((subject == null) ? 0 : subject.hashCode());
        return result;
    }

    // Eclipse-generated equals implementation, based on recipients, subject and body
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OutgoingMail other = (OutgoingMail) obj;
        if (body == null) {
            if (other.body != null)
                return false;
        } else if (!body.equals(other.body))
            return false;
        if (recipients == null) {
            if (other.recipients != null)
                return false;
        } else if (!recipients.equals(other.recipients))
            return false;
        if (subject == null) {
            if (other.subject != null)
                return false;
        } else if (!subject.equals(other.subject))
            return false;
        return true;
    }
}
