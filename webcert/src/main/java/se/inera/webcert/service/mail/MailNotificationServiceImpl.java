package se.inera.webcert.service.mail;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import se.inera.certificate.logging.LogMarkers;
import se.inera.ifv.hsawsresponder.v3.GetCareUnitResponseType;
import se.inera.ifv.hsawsresponder.v3.GetHsaUnitResponseType;
import se.inera.ifv.webcert.spi.authorization.impl.HSAWebServiceCalls;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;

/**
 * @author andreaskaltenbach
 */
@Service
public class MailNotificationServiceImpl implements MailNotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(MailNotificationServiceImpl.class);

    private static final String INCOMING_QUESTION_SUBJECT = "Inkommen fråga från Försäkringskassan";
    private static final String INCOMING_ANSWER_SUBJECT = "Försäkringskassan har svarat på en fråga";

    @Value("${mail.admin}")
    private String adminMailAddress;

    @Value("${mail.webcert.host.url}")
    private String webCertHostUrl;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private HSAWebServiceCalls hsaClient;

    @Override
    @Async("threadPoolTaskExecutor")
    public void sendMailForIncomingQuestion(FragaSvar fragaSvar) {

        String careUnitId = fragaSvar.getVardperson().getEnhetsId();

        GetHsaUnitResponseType recipient = getHsaUnit(careUnitId);

        try {
            sendNotificationMailToEnhet(fragaSvar, INCOMING_QUESTION_SUBJECT, mailBodyForFraga(recipient, fragaSvar), recipient);
            LOG.info(LogMarkers.MONITORING, "Mail sent to unit '{}' for incoming question '{}'", careUnitId, fragaSvar.getInternReferens());
        } catch (MailSendException | MessagingException e) {
            Long frageId = fragaSvar.getInternReferens();
            String intygsId = fragaSvar.getIntygsReferens().getIntygsId();
            String enhetsId = fragaSvar.getVardperson().getEnhetsId();
            String enhetsNamn = fragaSvar.getVardperson().getEnhetsnamn();
            LOG.error("Notification mail for question '" + frageId
                    + "' concerning certificate '" + intygsId
                    + "' couldn't be sent to " + enhetsId
                    + " (" + enhetsNamn + "): " + e.getMessage());
        }
    }

    @Override
    @Async("threadPoolTaskExecutor")
    public void sendMailForIncomingAnswer(FragaSvar fragaSvar) {

        String careUnitId = fragaSvar.getVardperson().getEnhetsId();

        GetHsaUnitResponseType recipient = getHsaUnit(careUnitId);

        try {
            sendNotificationMailToEnhet(fragaSvar, INCOMING_ANSWER_SUBJECT, mailBodyForSvar(recipient, fragaSvar), recipient);
            LOG.info(LogMarkers.MONITORING, "Mail sent to unit '{}' for incoming answer on question '{}'", careUnitId, fragaSvar.getInternReferens());
        } catch (MailSendException | MessagingException e) {
            Long svarsId = fragaSvar.getInternReferens();
            String intygsId = fragaSvar.getIntygsReferens().getIntygsId();
            String enhetsId = fragaSvar.getVardperson().getEnhetsId();
            String enhetsNamn = fragaSvar.getVardperson().getEnhetsnamn();
            LOG.error("Notification mail for answer '" + svarsId
                    + "' concerning certificate '" + intygsId
                    + "' couldn't be sent to " + enhetsId
                    + " (" + enhetsNamn + "): " + e.getMessage());
        }
    }

    public void setAdminMailAddress(String adminMailAddress) {
        this.adminMailAddress = adminMailAddress;
    }

    public void setWebCertHostUrl(String webCertHostUrl) {
        this.webCertHostUrl = webCertHostUrl;
    }

    private void sendNotificationMailToEnhet(FragaSvar fragaSvar, String subject, String body,
                                             GetHsaUnitResponseType receivingEnhet) throws MessagingException {
        String recipientAddress = receivingEnhet.getEmail();

        // if recipient unit does not have a mail address configured, we try to lookup the unit's parent
        if (recipientAddress == null) {
            recipientAddress = getParentMailAddress(receivingEnhet.getHsaIdentity());
        }

        if (recipientAddress != null) {
            sendNotificationToUnit(recipientAddress, subject, body);
        } else {
            sendAdminMailAboutMissingEmailAddress(receivingEnhet, fragaSvar);
        }
    }

    private String getParentMailAddress(String mottagningsId) {
        GetCareUnitResponseType response = hsaClient.callGetCareunit(mottagningsId);
        if (response != null) {
            GetHsaUnitResponseType parentEnhet = getHsaUnit(response.getCareUnitHsaIdentity());
            if (parentEnhet != null) {
                return parentEnhet.getEmail();
            }
        }
        return null;
    }

    private String mailBodyForFraga(GetHsaUnitResponseType unit, FragaSvar fragaSvar) {
        return "<p>En ny fråga-svar har inkommit till " + unit.getName()
                + " i Webcert.<br><a href=\"" + intygsUrl(fragaSvar) + "\">Svara i Webcert</a></p>";
    }

    private String mailBodyForSvar(GetHsaUnitResponseType unit, FragaSvar fragaSvar) {
        return "<p>En fråga-svar från " + unit.getName() + " har besvarats"
                + ".<br><a href=\"" + intygsUrl(fragaSvar) + "\">Se Svaret i Webcert</a></p>";
    }

    private void sendNotificationToUnit(String mailAddress, String subject, String body) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(mailAddress));

        message.setContent(body, "text/html;charset=utf-8");
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    private void sendAdminMailAboutMissingEmailAddress(GetHsaUnitResponseType unit, FragaSvar fragaSvar)
            throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(adminMailAddress));

        message.setSubject("Fråga/svar Webcert: Enhet utan mailadress eller koppling");

        StringBuilder body = new StringBuilder();
        body.append("<p>En fråga eller ett svar är mottaget av Webcert. ");
        body.append("Detta för en enhet som ej har en mailadress satt eller så är enheten ej kopplad till en överliggande vårdenhet.</p>");
        body.append("<p>Vårdenhetens id är <b>");
        body.append(unit.getHsaIdentity()).append("</b> och namn är <b>");
        body.append(unit.getName()).append("</b>.");

        body.append("<br>");
        body.append("<a href=\"").append(intygsUrl(fragaSvar)).append("\">Länk till frågan</a>");

        body.append("</p>");
        message.setText(body.toString());
        LOG.info(body.toString());

        mailSender.send(message);

    }

    private GetHsaUnitResponseType getHsaUnit(String hsaId) {
        GetHsaUnitResponseType response = hsaClient.callGetHsaunit(hsaId);
        if (response == null) {
            throw new IllegalArgumentException("HSA Id " + hsaId + " does not exist in HSA catalogue.");
        }
        return response;
    }

    public String intygsUrl(FragaSvar fragaSvar) {
        String url = webCertHostUrl + "/webcert/web/user/certificate/" + fragaSvar.getIntygsReferens().getIntygsId() + "/questions";
        LOG.debug("Intygsurl: " + url);
        return url;
    }
}
