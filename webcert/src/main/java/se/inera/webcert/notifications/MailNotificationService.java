package se.inera.webcert.notifications;

import javax.mail.MessagingException;

import se.inera.webcert.persistence.fragasvar.model.FragaSvar;

/**
 * @author andreaskaltenbach
 */
public interface MailNotificationService {

    void sendMailForIncomingQuestion(FragaSvar fragaSvar) throws MessagingException;
}
