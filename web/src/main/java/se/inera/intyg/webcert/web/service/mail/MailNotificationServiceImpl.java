/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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

import com.google.common.base.Strings;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.xml.ws.WebServiceException;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.infra.integration.hsatk.exception.HsaServiceCallException;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.services.legacy.HsaOrganizationsService;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.privatepractitioner.PrivatePractitionerService;
import se.inera.intyg.webcert.web.service.employee.EmployeeNameService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;

/**
 * @author andreaskaltenbach
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MailNotificationServiceImpl implements MailNotificationService {

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

    @Value("${mail.webcert.host.url}")
    private String webCertHostUrl;

    private final JavaMailSender mailSender;

    private final MonitoringLogService monitoringService;

    @Nullable
    private final PrivatePractitionerService privatePractitionerService;

    private final UtkastRepository utkastRepository;

    private final HsaOrganizationsService hsaOrganizationsService;

    private final EmployeeNameService employeeNameService;

    private void logError(String type, MailNotification mailNotification, Exception ex) {
        log.error("Notification mail for {} '{}' concerning certificate '{}' couldn't be sent to {} ({}) due to reason '{}'", type,
            mailNotification.getQaId(), mailNotification.getCertificateId(), mailNotification.getCareUnitId(),
            mailNotification.getCareUnitName(), ex.getMessage(), ex);
    }

    private void logError(String type, MailNotification mailNotification, String reason) {
        log.error(
            "Notification mail for {} '{}' concerning certificate '{}' couldn't be sent to {} ({}) due to reason '{}'",
            type, mailNotification.getQaId(), mailNotification.getCertificateId(),
            mailNotification.getCareUnitId(),
            mailNotification.getCareUnitName(), reason
        );
    }

    @Override
    @Async("threadPoolTaskExecutor")
    public void sendMailForIncomingQuestion(MailNotification mailNotification) {
        String type = "question";
        MailNotificationEnhet recipient = getUnit(mailNotification);
        final var employeeHsaName = employeeNameService.getEmployeeHsaName(
            mailNotification.getSignedByHsaId()
        );

        if (recipient == null) {
            logError(type, mailNotification, "Missing recipient");
        } else {
            try {
                String reason = "incoming question '" + mailNotification.getQaId() + "'";
                sendNotificationMailToEnhet(type, mailNotification, INCOMING_QUESTION_SUBJECT,
                    mailBodyForFraga(recipient, mailNotification, employeeHsaName), recipient, reason);
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
        final var employeeHsaName = employeeNameService.getEmployeeHsaName(
            mailNotification.getSignedByHsaId()
        );

        if (recipient == null) {
            logError(type, mailNotification, "Missing recipient");
        } else {
            try {
                String reason = "incoming answer on question '" + mailNotification.getQaId() + "'";
                sendNotificationMailToEnhet(type, mailNotification, INCOMING_ANSWER_SUBJECT,
                    mailBodyForSvar(recipient, mailNotification, employeeHsaName),
                    recipient, reason);
            } catch (MailSendException | MessagingException e) {
                logError(type, mailNotification, e);
            }
        }
    }

    private void sendNotificationMailToEnhet(String type, MailNotification mailNotification,
        String subject, String body,
        MailNotificationEnhet receivingEnhet,
        String reason) throws MessagingException {

        String recipientAddress = receivingEnhet.getEmail();

        try {
            // if recipient unit does not have a mail address configured, we try to lookup the unit's parent
            if (recipientAddress == null) {
                recipientAddress = getParentMailAddress(receivingEnhet.getHsaId());
            }
        } catch (WebServiceException ex) {
            logError(type, mailNotification, ex);
            return;
        }

        if (recipientAddress != null) {
            sendNotificationToUnit(recipientAddress, subject, body);
            monitoringService.logMailSent(receivingEnhet.getHsaId(), reason, mailNotification);
        } else {
            sendAdminMailAboutMissingEmailAddress(receivingEnhet, mailNotification);
            monitoringService.logMailMissingAddress(receivingEnhet.getHsaId(), reason, mailNotification);
        }
    }

    private String getParentMailAddress(String mottagningsId) {
        String parent;
        try {
            parent = hsaOrganizationsService.getParentUnit(mottagningsId);
        } catch (HsaServiceCallException ex) {
            log.warn(
                String.format("Could not call HSA for '%s', cause: '%s'", mottagningsId, ex.getMessage()),
                ex);
            return null;
        }
        MailNotificationEnhet parentEnhet = retrieveDataFromHsa(parent);
        return (parentEnhet != null) ? parentEnhet.getEmail() : null;
    }

    private String mailBodyForFraga(MailNotificationEnhet unit, MailNotification mailNotification,
        String employeeName) {
        return "<p>" + "Försäkringskassan har ställt en fråga på ett intyg utfärdat av <b>%s</b> på <b>%s</b>.".formatted(employeeName,
            unit.getName())
            + "<br><a href=\"" + intygsUrl(mailNotification)
            + "\">Läs och besvara frågan i Webcert</a></p><p>OBS! Sätt i ditt SITHS-kort innan du klickar på länken.</p>";
    }

    private String mailBodyForSvar(MailNotificationEnhet unit, MailNotification mailNotification,
        String employeeName) {
        return "<p>" + "Det har kommit ett svar från Försäkringskassan på en fråga som <b>%s</b> på <b>%s</b> har ställt.".formatted(
            employeeName,
            unit.getName())
            + " har ställt"
            + ".<br><a href=\"" + intygsUrl(mailNotification)
            + "\">Läs svaret i Webcert</a></p><p>OBS! Sätt i ditt SITHS-kort innan du klickar på länken.</p>";
    }

    private void sendNotificationToUnit(String mailAddress, String subject, String body)
        throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        message.setFrom(new InternetAddress(fromAddress));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(mailAddress));

        message.setSubject(subject);
        message.setContent(body, "text/html; charset=utf-8");
        mailSender.send(message);
    }

    private void sendAdminMailAboutMissingEmailAddress(MailNotificationEnhet unit,
        MailNotification mailNotification)
        throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        message.setFrom(new InternetAddress(fromAddress));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(adminMailAddress));

        message.setSubject("Fråga/svar Webcert: Enhet utan mailadress eller koppling");

        StringBuilder body = new StringBuilder();
        body.append("<p>En fråga eller ett svar är mottaget av Webcert. ");
        body.append(
            "Detta för en enhet som antingen saknar mailadress i HSA eller så är enheten inte kopplad till en överliggande vårdenhet.</p>");
        body.append("<p> Enhetens id och namn är <b>%s %s</b>.".formatted(unit.getHsaId(), unit.getName()));

        body.append("<br>");
        body.append("<a href=\"").append(intygsUrl(mailNotification)).append("\">Länk till frågan</a>");

        body.append("</p>");
        body.append("<p>OBS! Sätt i ditt SITHS-kort innan du klickar på länken.</p>");
        message.setContent(body.toString(), "text/html; charset=utf-8");

        mailSender.send(message);

    }

    private MailNotificationEnhet getUnit(MailNotification mailNotification) {
        String careUnitId = mailNotification.getCareUnitId();
        if (isPrivatePractitionerEnhet(careUnitId)) {
            return getPrivatePractitionerEnhet(mailNotification.getSignedByHsaId());
        }
        return retrieveDataFromHsa(careUnitId);
    }

    private boolean isPrivatePractitionerEnhet(String careUnitId) {
        return careUnitId != null && careUnitId.toUpperCase(Locale.ENGLISH)
            .startsWith(PRIVATE_PRACTITIONER_HSAID_PREFIX);
    }

    private MailNotificationEnhet retrieveDataFromHsa(String careUnitId) {
        try {
            Vardenhet enhetData = hsaOrganizationsService.getVardenhet(careUnitId);
            return new MailNotificationEnhet(enhetData.getId(), enhetData.getNamn(),
                enhetData.getEpost());
        } catch (WebServiceException ex) {
            log.error(String.format("Failed to contact HSA to get HSA Id '%s' : '%s'", careUnitId,
                ex.getMessage()), ex);
            return null;
        }
    }

    private MailNotificationEnhet getPrivatePractitionerEnhet(String hsaId) {
        return getMailNotificationEnhetFromPPS(hsaId);
    }

    private MailNotificationEnhet getMailNotificationEnhetFromPPS(String hsaId) {
        if (privatePractitionerService == null) {
            throw new IllegalStateException("PrivatePractitionerIntegrationService is not available");
        }

        try {
            final var privatePractitioner = privatePractitionerService.getPrivatePractitioner(hsaId);
            if (privatePractitioner != null) {
                return new MailNotificationEnhet(
                    hsaId,
                    privatePractitioner.getCareUnitName(),
                    privatePractitioner.getEmail()
                );
            }
        } catch (Exception e) {
            log.error("Failed to contact PrivatePractitionerService to get HSA Id '{}'", hsaId, e);
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public String intygsUrl(MailNotification mailNotification) {
        String url = webCertHostUrl + "/webcert/web/user/"
            + resolvePathSegment(mailNotification.getCareUnitId(), mailNotification.getCertificateId())
            + "/";
        if (!Fk7263EntryPoint.MODULE_ID.equalsIgnoreCase(mailNotification.getCertificateType())) {
            url += mailNotification.getCertificateType() + "/";
        }
        url += mailNotification.getCertificateId() + "/questions";
        if (!Strings.nullToEmpty(mailNotification.getCareUnitId()).trim().isEmpty()) {
            url += "?enhet=" + mailNotification.getCareUnitId();
        }

        log.debug("Intygsurl: {}", url);
        return url;
    }

    private String resolvePathSegment(String enhetsId, String intygsId) {
        if (isPrivatePractitionerEnhet(enhetsId)) {
            return QA_NOTIFICATION_PRIVATE_PRACTITIONER_PATH_SEGMENT;
        }
        // We can't check the WebcertUser role since there is no logged in user here
        // Assume that the receiving user should have UTHOPP role if the certificate was not registered through webcert
        if (utkastRepository.findById(intygsId).orElse(null) == null) {
            return QA_NOTIFICATION_UTHOPP_PATH_SEGMENT;
        }
        return QA_NOTIFICATION_DEFAULT_PATH_SEGMENT;
    }

}