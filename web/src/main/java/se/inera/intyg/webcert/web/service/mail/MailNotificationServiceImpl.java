/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

import se.inera.intyg.common.integration.hsa.client.OrganizationUnitService;
import se.inera.intyg.common.support.modules.support.api.exception.ExternalServiceCallException;
import se.inera.intyg.intygstyper.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.webcert.integration.pp.services.PPService;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.riv.infrastructure.directory.organization.gethealthcareunitresponder.v1.HealthCareUnitType;
import se.riv.infrastructure.directory.organization.getunitresponder.v1.UnitType;
import se.riv.infrastructure.directory.privatepractitioner.v1.EnhetType;
import se.riv.infrastructure.directory.privatepractitioner.v1.HoSPersonType;

/**
 * @author andreaskaltenbach
 */
@Service
public class MailNotificationServiceImpl implements MailNotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(MailNotificationServiceImpl.class);

    private static final String QA_NOTIFICATION_UTHOPP_PATH_SEGMENT = "certificate";
    private static final String QA_NOTIFICATION_DEFAULT_PATH_SEGMENT = "basic-certificate";
    private static final String QA_NOTIFICATION_PRIVATE_PRACTITIONER_PATH_SEGMENT = "pp-certificate";

    private static final String INCOMING_QUESTION_SUBJECT = "Försäkringskassan har ställt en fråga angående ett intyg";
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
    private OrganizationUnitService organizationUnitService;

    @Autowired
    private MonitoringLogService monitoringService;

    @Autowired
    private PPService ppService;

    @Value("${privatepractitioner.logicaladdress}")
    private String ppLogicalAddress;

    @Autowired
    private UtkastRepository utkastRepository;

    private void logError(String type, MailNotification mailNotification, Exception e) {
        String message = "";
        if (e != null) {
            message = ": " + e.getMessage();
        }
        LOG.error("Notification mail for " + type + " '" + mailNotification.getQaId()
                + "' concerning certificate '" + mailNotification.getCertificateId()
                + "' couldn't be sent to " + mailNotification.getCareUnitId()
                + " (" + mailNotification.getCareUnitName() + ")" + message);
    }

    @Override
    @Async("threadPoolTaskExecutor")
    public void sendMailForIncomingQuestion(MailNotification mailNotification) {
        String type = "question";
        MailNotificationEnhet recipient = getUnit(mailNotification);

        if (recipient == null) {
            logError(type, mailNotification, null);
        } else {
            try {
                String reason = "incoming question '" + mailNotification.getQaId() + "'";
                sendNotificationMailToEnhet(type, mailNotification, INCOMING_QUESTION_SUBJECT, mailBodyForFraga(recipient, mailNotification), recipient, reason);
            } catch (MailSendException | MessagingException e) {
                logError(type, mailNotification, e);
            }
        }
    }

    @Override
    @Async("threadPoolTaskExecutor")
    public void sendMailForIncomingAnswer(MailNotification mailNotification) {
        String type = "answer";
        MailNotificationEnhet recipient = getUnit(mailNotification);

        if (recipient == null) {
            logError(type, mailNotification, null);
        } else {
            try {
                String reason = "incoming answer on question '" + mailNotification.getQaId() + "'";
                sendNotificationMailToEnhet(type, mailNotification, INCOMING_ANSWER_SUBJECT, mailBodyForSvar(recipient, mailNotification), recipient, reason);
            } catch (MailSendException | MessagingException e) {
                logError(type, mailNotification, e);
            }
        }
    }

    public void setAdminMailAddress(String adminMailAddress) {
        this.adminMailAddress = adminMailAddress;
    }

    public void setWebCertHostUrl(String webCertHostUrl) {
        this.webCertHostUrl = webCertHostUrl;
    }

    private void sendNotificationMailToEnhet(String type, MailNotification mailNotification, String subject, String body, MailNotificationEnhet receivingEnhet,
            String reason) throws MessagingException {

        String recipientAddress = receivingEnhet.getEmail();

        try {
            // if recipient unit does not have a mail address configured, we try to lookup the unit's parent
            if (recipientAddress == null) {
                recipientAddress = getParentMailAddress(receivingEnhet.getHsaId());
            }
        } catch (WebServiceException e) {
            LOG.error("Failed to contact HSA to get HSA Id '" + receivingEnhet.getHsaId() + "' : " + e.getMessage());
            logError(type, mailNotification, null);
            return;
        }

        if (recipientAddress != null) {
            sendNotificationToUnit(recipientAddress, subject, body);
            monitoringService.logMailSent(receivingEnhet.getHsaId(), reason);
        } else {
            sendAdminMailAboutMissingEmailAddress(receivingEnhet, mailNotification);
            monitoringService.logMailMissingAddress(receivingEnhet.getHsaId(), reason);
        }
    }

    private String getParentMailAddress(String mottagningsId) {
        // Ändring: Vi nyttjar här getHealthCareUnit för att explicit få mottagningen i ett objekt som inkluderar hsaId
        // till föräldern.
        HealthCareUnitType response;
        try {
            response = organizationUnitService.getHealthCareUnit(mottagningsId);
        } catch (ExternalServiceCallException e) {
            LOG.warn("Could not call HSA for {}, cause: {}", mottagningsId, e.getMessage());
            return null;
        }
        MailNotificationEnhet parentEnhet = getHsaUnit(response.getHealthCareUnitHsaId());
        return (parentEnhet != null) ? parentEnhet.getEmail() : null;
    }

    private String mailBodyForFraga(MailNotificationEnhet unit, MailNotification mailNotification) {
        return "<p>" + unit.getName() + " har fått en fråga från Försäkringskassan angående ett intyg."
                + "<br><a href=\"" + intygsUrl(mailNotification) + "\">Läs och besvara frågan i Webcert</a></p>";
    }

    private String mailBodyForSvar(MailNotificationEnhet unit, MailNotification mailNotification) {
        return "<p>Det har kommit ett svar från Försäkringskassan på en fråga som " + unit.getName() + " har ställt"
                + ".<br><a href=\"" + intygsUrl(mailNotification) + "\">Läs svaret i Webcert</a></p>";
    }

    private void sendNotificationToUnit(String mailAddress, String subject, String body) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        message.setFrom(new InternetAddress(fromAddress));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(mailAddress));

        message.setSubject(subject);
        message.setContent(body, "text/html; charset=utf-8");
        mailSender.send(message);
    }

    private void sendAdminMailAboutMissingEmailAddress(MailNotificationEnhet unit, MailNotification mailNotification)
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
        body.append("<a href=\"").append(intygsUrl(mailNotification)).append("\">Länk till frågan</a>");

        body.append("</p>");
        message.setContent(body.toString(), "text/html; charset=utf-8");

        mailSender.send(message);

    }

    private MailNotificationEnhet getUnit(MailNotification mailNotification) {
        String careUnitId = mailNotification.getCareUnitId();
        if (isPrivatePractitionerEnhet(careUnitId)) {
            return getPrivatePractitionerEnhet(mailNotification.getSignedByHsaId());
        }
        return getHsaUnit(careUnitId);
    }

    private boolean isPrivatePractitionerEnhet(String careUnitId) {
        return careUnitId != null && careUnitId.toUpperCase(Locale.ENGLISH).startsWith(PRIVATE_PRACTITIONER_HSAID_PREFIX);
    }

    private MailNotificationEnhet getHsaUnit(String careUnitId) {
        try {
            UnitType response = organizationUnitService.getUnit(careUnitId);
            if (response == null) {
                throw new IllegalArgumentException(
                        "HSA Id " + careUnitId + " does not exist in HSA catalogue, fetched over infrastructure:directory:organization:getunit.");
            }
            return new MailNotificationEnhet(response.getUnitHsaId(), response.getUnitName(), response.getMail());
        } catch (WebServiceException | ExternalServiceCallException e) {
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
    public String intygsUrl(MailNotification mailNotification) {
        String url = String.valueOf(webCertHostUrl) + "/webcert/web/user/"
                + resolvePathSegment(mailNotification.getCareUnitId(), mailNotification.getCertificateId()) + "/";
        if (!Fk7263EntryPoint.MODULE_ID.equalsIgnoreCase(mailNotification.getCertificateType())) {
            url += mailNotification.getCertificateType() + "/";
        }
        url += mailNotification.getCertificateId() + "/questions";
        LOG.debug("Intygsurl: " + url);
        return url;
    }

    private String resolvePathSegment(String enhetsId, String intygsId) {
        if (isPrivatePractitionerEnhet(enhetsId)) {
            return QA_NOTIFICATION_PRIVATE_PRACTITIONER_PATH_SEGMENT;
        }
        // We can't check the WebcertUser role since there is no logged in user here
        // Assume that the receiving user should have UTHOPP role if the certificate was not registered through webcert
        if (utkastRepository.findOne(intygsId) == null) {
            return QA_NOTIFICATION_UTHOPP_PATH_SEGMENT;
        }
        return QA_NOTIFICATION_DEFAULT_PATH_SEGMENT;
    }

}
