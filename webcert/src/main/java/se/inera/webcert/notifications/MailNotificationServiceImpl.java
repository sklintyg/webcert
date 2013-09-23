package se.inera.webcert.notifications;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import se.inera.ifv.hsawsresponder.v3.GetHsaUnitResponseType;
import se.inera.ifv.webcert.spi.authorization.impl.HSAWebServiceCalls;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;

/**
 * @author andreaskaltenbach
 */
@Service
public class MailNotificationServiceImpl implements MailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(MailNotificationServiceImpl.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private HSAWebServiceCalls hsaClient;

    @Override
    public void sendMailForIncomingQuestion(FragaSvar fragaSvar) throws MessagingException {

        // fetch email address for unit
        String email = getEmailAddress(fragaSvar.getVardperson().getEnhetsId());

        MimeMessage message = mailSender.createMimeMessage();
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
        message.setSubject("Inkommen fråga från Försäkringskassan finns att hämta i WebCert.");

        mailSender.send(message);

    }

    private String getEmailAddress(String hsaId) {
        GetHsaUnitResponseType response = hsaClient.callGetHsaunit(hsaId);
        if (response == null) {
            throw new IllegalArgumentException("HSA Id " + hsaId + " does not exist in HSA catalogue.");
        }
        return response.getEmail();
    }
}
