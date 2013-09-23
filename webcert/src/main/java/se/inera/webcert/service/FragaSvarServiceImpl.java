package se.inera.webcert.service;

import javax.mail.MessagingException;

import com.google.common.base.Throwables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import se.inera.webcert.notifications.MailNotificationService;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;

/**
 * @author andreaskaltenbach
 */
@Service
public class FragaSvarServiceImpl implements FragaSvarService {

    @Autowired
    private MailNotificationService mailNotificationService;


    @Override
    public void processIncomingQuestion(FragaSvar fragaSvar) {

        // persist the question

        // send mail to enhet to inform about new question
        try {
            mailNotificationService.sendMailForIncomingQuestion(fragaSvar);
        } catch (MessagingException e) {
            Throwables.propagate(e);
        }
    }

    @Override
    public void processIncomingAnswer(FragaSvar fragaSvar) {

        // update the FragaSvar

    }
}
