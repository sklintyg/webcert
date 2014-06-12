package se.inera.webcert.service;

import javax.mail.MessagingException;

import se.inera.webcert.persistence.fragasvar.model.FragaSvar;

/**
 * @author andreaskaltenbach
 */
public interface MailNotificationService {

    void sendMailForIncomingQuestion(FragaSvar fragaSvar) throws MessagingException;
    void sendMailForIncomingAnswer(FragaSvar fragaSvar) throws MessagingException;
    public String intygsUrl(FragaSvar fragaSvar);
}
