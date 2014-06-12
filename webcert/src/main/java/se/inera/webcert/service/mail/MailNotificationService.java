package se.inera.webcert.service.mail;

import se.inera.webcert.persistence.fragasvar.model.FragaSvar;

/**
 * @author andreaskaltenbach
 */
public interface MailNotificationService {

    void sendMailForIncomingQuestion(FragaSvar fragaSvar);
    void sendMailForIncomingAnswer(FragaSvar fragaSvar);
    public String intygsUrl(FragaSvar fragaSvar);
}
