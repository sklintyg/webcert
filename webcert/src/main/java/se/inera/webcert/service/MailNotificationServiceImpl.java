package se.inera.webcert.service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import se.inera.ifv.hsawsresponder.v3.GetCareUnitResponseType;
import se.inera.ifv.hsawsresponder.v3.GetHsaUnitResponseType;
import se.inera.ifv.webcert.spi.authorization.impl.HSAWebServiceCalls;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;

/**
 * @author andreaskaltenbach
 */
@Service
public class MailNotificationServiceImpl implements MailNotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailNotificationServiceImpl.class);

    private static final String INCOMING_QUESTION_SUBJECT = "Inkommen fråga från Försäkringskassan finns att hämta i WebCert.";
    private static final String INCOMING_ANSWER_SUBJECT = "Inkommet svar finns att hämta i WebCert.";

    @Value("${mail.admin}")
    private String adminMailAddress;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private HSAWebServiceCalls hsaClient;

    @Override
    public void sendMailForIncomingQuestion(FragaSvar fragaSvar) throws MessagingException {

        // fetch email address for unit
        GetHsaUnitResponseType unit = getNotificationRecipient(fragaSvar.getVardperson().getEnhetsId());

        if (unit.getEmail() != null) {
            sendNotificationToUnit(unit, INCOMING_QUESTION_SUBJECT);
        } else {
            // in case no mail is available for unit, we'll inform the admin
            sendAdminMailAboutMissingEmailAddress(unit);
        }
    }

    @Override
    public void sendMailForIncomingAnswer(FragaSvar fragaSvar) throws MessagingException {
        // fetch email address for unit
        GetHsaUnitResponseType unit = getNotificationRecipient(fragaSvar.getVardperson().getEnhetsId());

        if (unit.getEmail() != null) {
            sendNotificationToUnit(unit, INCOMING_ANSWER_SUBJECT);
        } else {
            // in case no mail is available for unit, we'll inform the admin
            sendAdminMailAboutMissingEmailAddress(unit);
        }
    }

    private String mailBody(GetHsaUnitResponseType unit) {
        StringBuffer body = new StringBuffer();
        body.append("Vårdenhetens namn är " + unit.getName() + " och id är " + unit.getHsaIdentity());

        // TODO - decide whether or not to attach link to care unit (see MedCert for more info)
        // addCareunitLinkInMessage(message, careunitId, certificateId);

        return body.toString();
    }

    private void sendNotificationToUnit(GetHsaUnitResponseType unit, String subject) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(unit.getEmail()));
        message.setSubject(subject);
        message.setText(mailBody(unit));
        mailSender.send(message);
    }

    private GetHsaUnitResponseType getNotificationRecipient(String hsaId) {

        // get email for given unit
        GetHsaUnitResponseType responseType = getHsaUnit(hsaId);

        if (responseType.getEmail() == null) {
            // if unit does not have a mail configured, we try to lookup the unit's parent recursively
            GetCareUnitResponseType response = hsaClient.callGetCareunit(hsaId);
            if (response != null) {
                return getNotificationRecipient(response.getCareUnitHsaIdentity());
            }
        }
        return responseType;
    }

    private void sendAdminMailAboutMissingEmailAddress(GetHsaUnitResponseType unit) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(adminMailAddress));
        message.setSubject("Fråga/svar WebCert: Enhet utan mailadress eller koppling");
        StringBuffer body = new StringBuffer();
        body.append("En fråga eller ett svar är mottaget av WebCert.");
        body.append("Detta för en enhet som ej har en mailadress satt eller så är enheten ej kopplad till en överliggande vårdenhet.");
        body.append("Vårdenhetens id är ");
        body.append(unit.getHsaIdentity() + " och namn är ");
        body.append(unit.getName() + ". ");
        message.setText(body.toString());
        LOGGER.info(body.toString());

        // TODO - decide whether or not to attach link to care unit (see MedCert for more info)
        // addCareunitLinkInMessage(message, careunitId, certificateId);

        mailSender.send(message);

    }

    private GetHsaUnitResponseType getHsaUnit(String hsaId) {
        GetHsaUnitResponseType response = hsaClient.callGetHsaunit(hsaId);
        if (response == null) {
            throw new IllegalArgumentException("HSA Id " + hsaId + " does not exist in HSA catalogue.");
        }
        return response;
    }

    private String getEmailForUnit(String hsaId) {
        GetHsaUnitResponseType response = hsaClient.callGetHsaunit(hsaId);
        if (response == null) {
            throw new IllegalArgumentException("HSA Id " + hsaId + " does not exist in HSA catalogue.");
        }
        return response.getEmail();
    }
}
