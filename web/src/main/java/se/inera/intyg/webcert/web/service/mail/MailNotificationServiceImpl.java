package se.inera.intyg.webcert.web.service.mail;

import java.util.Locale;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.xml.ws.WebServiceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import se.inera.ifv.hsawsresponder.v3.GetCareUnitResponseType;
import se.inera.ifv.hsawsresponder.v3.GetHsaUnitResponseType;
import se.inera.ifv.webcert.spi.authorization.impl.HSAWebServiceCalls;
import se.inera.intyg.webcert.integration.pp.services.PPService;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.riv.infrastructure.directory.privatepractitioner.v1.EnhetType;
import se.riv.infrastructure.directory.privatepractitioner.v1.HoSPersonType;

/**
 * @author andreaskaltenbach
 */
@Service
public class MailNotificationServiceImpl implements MailNotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(MailNotificationServiceImpl.class);

    private static final String QA_NOTIFICATION_DEFAULT_PATH_SEGMENT = "certificate";
    private static final String QA_NOTIFICATION_LANDSTING_PATH_SEGMENT = "basic-certificate";
    private static final String QA_NOTIFICATION_PRIVATE_PRACTITIONER_PATH_SEGMENT = "pp-certificate";

    private static final String INCOMING_QUESTION_SUBJECT = "Inkommen fråga från Försäkringskassan";
    private static final String INCOMING_ANSWER_SUBJECT = "Försäkringskassan har svarat på en fråga";

    static final String PRIVATE_PRACTITIONER_HSAID_PREFIX = "SE165565594230-WEBCERT";

    @Value("${mail.admin}")
    private String adminMailAddress;

    @Value("${mail.from}")
    private String fromAddress;
    // package scope for testability
    void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    @Value("${mail.webcert.host.url}")
    private String webCertHostUrl;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private HSAWebServiceCalls hsaClient;

    @Autowired
    private MonitoringLogService monitoringService;

    @Autowired
    private PPService ppService;

    @Value("${privatepractitioner.logicaladdress}")
    private String ppLogicalAddress;

    @Autowired
    private WebCertUserService webCertUserService;

    private void logError(String type, FragaSvar fragaSvar, Exception e) {
        Long id = fragaSvar.getInternReferens();
        String intygsId = fragaSvar.getIntygsReferens().getIntygsId();
        String enhetsId = fragaSvar.getVardperson().getEnhetsId();
        String enhetsNamn = fragaSvar.getVardperson().getEnhetsnamn();
        String message = "";
        if (e != null) {
            message = ": " + e.getMessage();
        }
        LOG.error("Notification mail for " + type + " '" + id
                + "' concerning certificate '" + intygsId
                + "' couldn't be sent to " + enhetsId
                + " (" + enhetsNamn + ")" + message);
    }

    @Override
    @Async("threadPoolTaskExecutor")
    public void sendMailForIncomingQuestion(FragaSvar fragaSvar) {
        String type = "question";
        MailNotificationEnhet recipient = getUnit(fragaSvar);

        if (recipient == null) {
            logError(type, fragaSvar, null);
        } else {
            try {
                String reason = "incoming question '" + fragaSvar.getInternReferens() + "'";
                sendNotificationMailToEnhet(type, fragaSvar, INCOMING_QUESTION_SUBJECT, mailBodyForFraga(recipient, fragaSvar), recipient, reason);
            } catch (MailSendException | MessagingException e) {
                logError(type, fragaSvar, e);
            }
        }
    }

    @Override
    @Async("threadPoolTaskExecutor")
    public void sendMailForIncomingAnswer(FragaSvar fragaSvar) {
        String type = "answer";
        MailNotificationEnhet recipient = getUnit(fragaSvar);

        if (recipient == null) {
            logError(type, fragaSvar, null);
        } else {
            try {
                String reason = "incoming answer on question '" + fragaSvar.getInternReferens() + "'";
                sendNotificationMailToEnhet(type, fragaSvar, INCOMING_ANSWER_SUBJECT, mailBodyForSvar(recipient, fragaSvar), recipient, reason);
            } catch (MailSendException | MessagingException e) {
                logError(type, fragaSvar, e);
            }
        }
    }

    public void setAdminMailAddress(String adminMailAddress) {
        this.adminMailAddress = adminMailAddress;
    }

    public void setWebCertHostUrl(String webCertHostUrl) {
        this.webCertHostUrl = webCertHostUrl;
    }

    private void sendNotificationMailToEnhet(String type, FragaSvar fragaSvar, String subject, String body, MailNotificationEnhet receivingEnhet, String reason) throws MessagingException {

        String recipientAddress = receivingEnhet.getEmail();

        try {
            // if recipient unit does not have a mail address configured, we try to lookup the unit's parent
            if (recipientAddress == null) {
                recipientAddress = getParentMailAddress(receivingEnhet.getHsaId());
            }
        } catch (WebServiceException e) {
            LOG.error("Failed to contact HSA to get HSA Id '" + receivingEnhet.getHsaId() + "' : " + e.getMessage());
            logError(type, fragaSvar, null);
            return;
        }

        if (recipientAddress != null) {
            sendNotificationToUnit(recipientAddress, subject, body);
            monitoringService.logMailSent(receivingEnhet.getHsaId(), reason);
        } else {
            sendAdminMailAboutMissingEmailAddress(receivingEnhet, fragaSvar);
            monitoringService.logMailMissingAddress(receivingEnhet.getHsaId(), reason);
        }
    }

    private String getParentMailAddress(String mottagningsId) {
        GetCareUnitResponseType response = hsaClient.callGetCareunit(mottagningsId);
        if (response != null) {
            MailNotificationEnhet parentEnhet = getHsaUnit(response.getCareUnitHsaIdentity());
            if (parentEnhet != null) {
                return parentEnhet.getEmail();
            }
        }
        return null;
    }

    private String mailBodyForFraga(MailNotificationEnhet unit, FragaSvar fragaSvar) {
        return "<p>En ny fråga-svar har inkommit till " + unit.getName()
                + " i Webcert.<br><a href=\"" + intygsUrl(fragaSvar) + "\">Svara i Webcert</a></p>";
    }

    private String mailBodyForSvar(MailNotificationEnhet unit, FragaSvar fragaSvar) {
        return "<p>En fråga-svar från " + unit.getName() + " har besvarats"
                + ".<br><a href=\"" + intygsUrl(fragaSvar) + "\">Se svaret i Webcert</a></p>";
    }

    private void sendNotificationToUnit(String mailAddress, String subject, String body) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        message.setFrom(new InternetAddress(fromAddress));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(mailAddress));

        message.setSubject(subject);
        message.setContent(body, "text/html; charset=utf-8");
        mailSender.send(message);
    }

    private void sendAdminMailAboutMissingEmailAddress(MailNotificationEnhet unit, FragaSvar fragaSvar)
            throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        message.setFrom(new InternetAddress(fromAddress));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(adminMailAddress));

        message.setSubject("Fråga/svar Webcert: Enhet utan mailadress eller koppling");

        StringBuilder body = new StringBuilder();
        body.append("<p>En fråga eller ett svar är mottaget av Webcert. ");
        body.append("Detta för en enhet som ej har en mailadress satt eller så är enheten ej kopplad till en överliggande vårdenhet.</p>");
        body.append("<p>Vårdenhetens id är <b>");
        body.append(unit.getHsaId()).append("</b> och namn är <b>");
        body.append(unit.getName()).append("</b>.");

        body.append("<br>");
        body.append("<a href=\"").append(intygsUrl(fragaSvar)).append("\">Länk till frågan</a>");

        body.append("</p>");
        message.setContent(body.toString(), "text/html; charset=utf-8");

        mailSender.send(message);

    }

    private MailNotificationEnhet getUnit(FragaSvar fragaSvar) {
        String careUnitId = fragaSvar.getVardperson().getEnhetsId();
        if (isPrivatePractitionerEnhet(careUnitId)) {
            String personHsaId = fragaSvar.getVardperson().getHsaId();
            return getPrivatePractitionerEnhet(personHsaId);
        }
        return getHsaUnit(careUnitId);
    }

    private boolean isPrivatePractitionerEnhet(String careUnitId) {
        return careUnitId != null && careUnitId.toUpperCase(Locale.ENGLISH).startsWith(PRIVATE_PRACTITIONER_HSAID_PREFIX);
    }

    private MailNotificationEnhet getHsaUnit(String careUnitId) {
        try {
            GetHsaUnitResponseType response = hsaClient.callGetHsaunit(careUnitId);
            if (response == null) {
                throw new IllegalArgumentException("HSA Id " + careUnitId + " does not exist in HSA catalogue.");
            }
            return new MailNotificationEnhet(response.getHsaIdentity(), response.getName(), response.getEmail());
        } catch (WebServiceException e) {
            LOG.error("Failed to contact HSA to get HSA Id '" + careUnitId + "' : " + e.getMessage());
            return null;
        }
    }

    private MailNotificationEnhet getPrivatePractitionerEnhet(String hsaId) {
        try {
            HoSPersonType privatePractitioner = ppService.getPrivatePractitioner(ppLogicalAddress, hsaId, null);
            if (privatePractitioner != null) {
                EnhetType enhet = privatePractitioner.getEnhet();
                if (enhet != null) {
                    return new MailNotificationEnhet(hsaId, enhet.getEnhetsnamn(), enhet.getEpost());
                }
            }
            LOG.error("Failed to lookup privatepractitioner with HSA Id '" + hsaId + "'");
        } catch (Exception e) {
            LOG.error("Failed to contact ppService to get HSA Id '" + hsaId + "'", e);
        }
        return null;
    }

    @Override
    public String intygsUrl(FragaSvar fragaSvar) {
        String url = String.valueOf(webCertHostUrl) + "/webcert/web/user/" + resolvePathSegment(fragaSvar.getVardperson().getEnhetsId()) + "/" + fragaSvar.getIntygsReferens().getIntygsId() + "/questions";
        LOG.debug("Intygsurl: " + url);
        return url;
    }

    private String resolvePathSegment(String enhetsId) {
        if (isPrivatePractitionerEnhet(enhetsId)) {
            return QA_NOTIFICATION_PRIVATE_PRACTITIONER_PATH_SEGMENT;
        }
        if (webCertUserService != null && webCertUserService.getUser() != null && webCertUserService.getUser().isRoleUthopp()) {
            return QA_NOTIFICATION_DEFAULT_PATH_SEGMENT;
        }
        return QA_NOTIFICATION_LANDSTING_PATH_SEGMENT;
    }

}
