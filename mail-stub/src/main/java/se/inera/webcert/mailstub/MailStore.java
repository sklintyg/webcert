package se.inera.webcert.mailstub;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;

/**
 * @author andreaskaltenbach
 */
@Component
public class MailStore {

    private List<OutgoingMail> mails = new CopyOnWriteArrayList<>();

    public List<OutgoingMail> getMails() {
        return mails;
    }
}
