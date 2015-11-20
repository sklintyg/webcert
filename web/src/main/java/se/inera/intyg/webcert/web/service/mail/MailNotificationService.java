package se.inera.intyg.webcert.web.service.mail;

import se.inera.webcert.persistence.fragasvar.model.FragaSvar;

/**
 * @author andreaskaltenbach
 */
public interface MailNotificationService {

    void sendMailForIncomingQuestion(FragaSvar fragaSvar);

    void sendMailForIncomingAnswer(FragaSvar fragaSvar);

    String intygsUrl(FragaSvar fragaSvar);
}
