/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.notification;

import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.ANDRAT;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.HANFRFM;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.HANFRFV;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.KFSIGN;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.MAKULE;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.NYFRFM;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.NYFRFV;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.NYSVFM;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.RADERA;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.SIGNAT;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.SKAPAT;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.SKICKA;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.CORRELATION_ID;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.HANDELSE;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.INTYGS_ID;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.INTYGS_TYP;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.INTYG_TYPE_VERSION;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.USER_ID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.mail.MailSendException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.common.support.modules.support.api.notification.SchemaVersion;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.common.service.notification.AmneskodCreator;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.csintegration.certificate.IntegratedUnitNotificationEvaluator;
import se.inera.intyg.webcert.web.integration.registry.IntegreradeEnheterRegistry;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygWithNotificationsRequest;
import se.inera.intyg.webcert.web.service.mail.MailNotification;
import se.inera.intyg.webcert.web.service.mail.MailNotificationService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.referens.ReferensService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Amneskod;

/**
 * Service that notifies a unit care of incoming changes.
 *
 * @author Magnus Ekstrand
 */
@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Autowired
    private IntegreradeEnheterRegistry integreradeEnheterRegistry;

    @Autowired
    private MailNotificationService mailNotificationService;

    @Autowired(required = false)
    @Qualifier("jmsNotificationTemplateForAggregation")
    private JmsTemplate jmsTemplateForAggregation;

    @Autowired
    private SendNotificationStrategy sendNotificationStrategy;

    @Autowired
    private NotificationMessageFactory notificationMessageFactory;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MonitoringLogService monitoringLog;

    @Autowired
    private UtkastRepository utkastRepo;

    @Autowired
    private HandelseRepository handelseRepo;

    @Autowired
    private ReferensService referensService;

    @Autowired
    private IntygService intygService;

    @Autowired
    private IntegratedUnitNotificationEvaluator integratedUnitNotificationEvaluator;

    @PostConstruct
    public void checkJmsTemplate() {
        if (Objects.isNull(jmsTemplateForAggregation)) {
            LOGGER.error("Notification is disabled!");
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * se.inera.intyg.webcert.web.service.notification.NewNotificationService#sendNotificationForDraftCreated(se.inera.
     * intyg.webcert.web
     * .persistence.utkast.model.Utkast)
     */
    @Override
    public void sendNotificationForDraftCreated(Utkast utkast) {
        createAndSendNotification(utkast, SKAPAT);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * se.inera.intyg.webcert.web.service.notification.NewNotificationService#sendNotificationForDraftSigned(se.inera.
     * intyg.webcert.web.
     * persistence.utkast.model.Utkast)
     */
    @Override
    public void sendNotificationForDraftSigned(Utkast utkast) {
        createAndSendNotification(utkast, SIGNAT);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * se.inera.intyg.webcert.web.service.notification.NewNotificationService#sendNotificationForDraftChanged(se.inera.
     * intyg.webcert.web
     * .persistence.utkast.model.Utkast)
     */
    @Override
    public void sendNotificationForDraftChanged(Utkast utkast) {
        createAndSendNotification(utkast, ANDRAT);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * se.inera.intyg.webcert.web.service.notification.NewNotificationService#sendNotificationForDraftDeleted(se.inera.
     * intyg.webcert.web
     * .persistence.utkast.model.Utkast)
     */
    @Override
    public void sendNotificationForDraftDeleted(Utkast utkast) {
        createAndSendNotification(utkast, RADERA);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * se.inera.intyg.webcert.web.service.notification.NewNotificationService#sendNotificationForDraftRevoked(se.inera.
     * intyg.webcert.web
     * .persistence.utkast.model.Utkast)
     */
    @Override
    public void sendNotificationForDraftRevoked(Utkast utkast) {
        createAndSendNotification(utkast, MAKULE);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * se.inera.intyg.webcert.web.service.notification.NotificationService#sendNotificationForDraftReadyToSign(se.inera.
     * intyg.webcert.web.persistence.utkast.model.Utkast)
     */
    @Override
    public void sendNotificationForDraftReadyToSign(Utkast utkast) {
        createAndSendNotification(utkast, KFSIGN);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * se.inera.intyg.webcert.web.service.notification.NewNotificationService#sendNotificationForIntygSent(se.inera.
     * intyg.webcert.web.
     * persistence.utkast.model.Utkast)
     */
    @Override
    public void sendNotificationForIntygSent(String intygsId) {
        Utkast utkast = getUtkast(intygsId);
        if (utkast != null) {
            createAndSendNotification(utkast, SKICKA);
        } else {
            createAndSendNotification(intygsId, SKICKA);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * se.inera.intyg.webcert.web.service.notification.NewNotificationService#sendNotificationForIntygRevoked(se.inera.
     * intyg.webcert.web
     * .persistence.utkast.model.Utkast)
     */
    @Override
    public void sendNotificationForIntygRevoked(String intygsId) {
        Utkast utkast = getUtkast(intygsId);
        if (utkast != null) {
            createAndSendNotification(utkast, MAKULE);
        } else {
            createAndSendNotification(intygsId, MAKULE);
        }
    }

    @Override
    public void sendNotificationForQuestionReceived(FragaSvar fragaSvar) {
        if (unitIsIntegrated(fragaSvar) && shouldNotRecieveNotificationByMail(fragaSvar)) {
            sendNotificationForQAs(fragaSvar.getIntygsReferens().getIntygsId(), NotificationEvent.NEW_QUESTION_FROM_RECIPIENT,
                fragaSvar.getSistaDatumForSvar(), ArendeAmne.fromAmne(fragaSvar.getAmne()).orElse(null));
        } else {
            sendNotificationForIncomingQuestionByMail(new MailNotification(fragaSvar.getInternReferens().toString(),
                fragaSvar.getIntygsReferens().getIntygsId(), Fk7263EntryPoint.MODULE_ID,
                fragaSvar.getVardperson().getEnhetsId(), fragaSvar.getVardperson().getEnhetsnamn(),
                fragaSvar.getVardperson().getHsaId()));
        }
    }

    private boolean unitIsIntegrated(FragaSvar fragaSvar) {
        return integreradeEnheterRegistry.isEnhetIntegrerad(fragaSvar.getVardperson().getEnhetsId(), Fk7263EntryPoint.MODULE_ID);
    }

    private boolean shouldNotRecieveNotificationByMail(FragaSvar fragaSvar) {
        return !integratedUnitNotificationEvaluator.mailNotification(
            fragaSvar.getVardperson().getVardgivarId(),
            fragaSvar.getVardperson().getEnhetsId(),
            fragaSvar.getIntygsReferens().getIntygsId(),
            fragaSvar.getIntygsReferens().getSigneringsDatum()
        );
    }

    @Override
    public void sendNotificationForAnswerRecieved(FragaSvar fragaSvar) {
        if (unitIsIntegrated(fragaSvar)) {
            sendNotificationForQAs(fragaSvar.getIntygsReferens().getIntygsId(), NotificationEvent.NEW_ANSWER_FROM_RECIPIENT);
        } else {
            sendNotificationForIncomingAnswerByMail(new MailNotification(fragaSvar.getInternReferens().toString(),
                fragaSvar.getIntygsReferens().getIntygsId(), Fk7263EntryPoint.MODULE_ID,
                fragaSvar.getVardperson().getEnhetsId(), fragaSvar.getVardperson().getEnhetsnamn(),
                fragaSvar.getVardperson().getHsaId()));
        }
    }

    @Override
    public void sendNotificationForQuestionReceived(Arende arende, String careProviderId, LocalDateTime issuingDate) {
        if (unitIsIntegrated(arende) && shouldNotRecieveNotificationByMail(arende, careProviderId, issuingDate)) {
            sendNotificationForQAs(arende.getIntygsId(), NotificationEvent.NEW_QUESTION_FROM_RECIPIENT, arende.getSistaDatumForSvar(),
                arende.getAmne());
        } else {
            sendNotificationForIncomingQuestionByMail(new MailNotification(arende.getMeddelandeId(), arende.getIntygsId(),
                arende.getIntygTyp(), arende.getEnhetId(), arende.getEnhetName(), arende.getSigneratAv()));
        }
    }

    private boolean unitIsIntegrated(Arende arende) {
        return integreradeEnheterRegistry.isEnhetIntegrerad(arende.getEnhetId(), arende.getIntygTyp());
    }

    @Override
    public void sendNotificationForAnswerRecieved(Arende arende, String careProviderId, LocalDateTime issuingDate) {
        if (unitIsIntegrated(arende) && shouldNotRecieveNotificationByMail(arende, careProviderId, issuingDate)) {
            sendNotificationForQAs(arende.getIntygsId(), NotificationEvent.NEW_ANSWER_FROM_RECIPIENT);
        } else {
            sendNotificationForIncomingAnswerByMail(new MailNotification(arende.getMeddelandeId(), arende.getIntygsId(),
                arende.getIntygTyp(), arende.getEnhetId(), arende.getEnhetName(), arende.getSigneratAv()));
        }
    }

    private boolean shouldNotRecieveNotificationByMail(Arende arende, String careProviderId, LocalDateTime issuingDate) {
        return !integratedUnitNotificationEvaluator.mailNotification(
            careProviderId, arende.getEnhetId(), arende.getIntygsId(), issuingDate
        );
    }

    @Override
    public List<Handelse> getNotifications(String intygsId) {
        return handelseRepo.findByIntygsId(intygsId);
    }

    @Override
    public void sendNotificationForQAs(String intygsId, NotificationEvent event) {
        sendNotificationForQAs(intygsId, event, null, null);
    }

    @Override
    public void forwardInternalNotification(final String intygsId, final String intygstyp, final Utlatande utlatande,
        final HandelsekodEnum handelse) {
        final String careUnitId = utlatande.getGrundData().getSkapadAv().getVardenhet().getEnhetsid();
        final String reference = referensService.getReferensForIntygsId(intygsId);

        try {
            String json = objectMapper.writeValueAsString(utlatande);
            NotificationMessage notificationMessage = notificationMessageFactory.createNotificationMessage(intygsId, intygstyp, careUnitId,
                json, handelse,
                SchemaVersion.VERSION_3, reference, null, null);

            send(notificationMessage, careUnitId, utlatande.getTextVersion());
        } catch (JsonProcessingException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, e.getMessage());
        }

    }

    @Override
    public List<Handelse> findNotifications(IntygWithNotificationsRequest request) {
        final var hasStartDate = request.getStartDate() != null;
        final var hasEndDate = request.getEndDate() != null;

        if (hasStartDate && hasEndDate) {
            return findNotificationsBetweenTimestamp(request);
        } else if (hasStartDate) {
            return findNotificationsAfterTimestamp(request);
        } else if (hasEndDate) {
            return findNotificationsBeforeTimestamp(request);
        } else {
            return findNotificationsWithoutTimestamp(request);
        }
    }

    private List<Handelse> findNotificationsBetweenTimestamp(IntygWithNotificationsRequest request) {
        if (request.shouldUseEnhetId()) {
            return handelseRepo.findByPersonnummerAndEnhetsIdInAndTimestampBetween(request.getPersonnummer().getPersonnummer(),
                request.getEnhetId(), request.getStartDate(), request.getEndDate());
        } else {
            return handelseRepo.findByPersonnummerAndVardgivarIdAndTimestampBetween(request.getPersonnummer().getPersonnummer(),
                request.getVardgivarId(), request.getStartDate(), request.getEndDate());
        }
    }

    private List<Handelse> findNotificationsAfterTimestamp(IntygWithNotificationsRequest request) {
        if (request.shouldUseEnhetId()) {
            return handelseRepo.findByPersonnummerAndEnhetsIdInAndTimestampAfter(request.getPersonnummer().getPersonnummer(),
                request.getEnhetId(), request.getStartDate());
        } else {
            return handelseRepo.findByPersonnummerAndVardgivarIdAndTimestampAfter(request.getPersonnummer().getPersonnummer(),
                request.getVardgivarId(), request.getStartDate());
        }
    }

    private List<Handelse> findNotificationsBeforeTimestamp(IntygWithNotificationsRequest request) {
        if (request.shouldUseEnhetId()) {
            return handelseRepo.findByPersonnummerAndEnhetsIdInAndTimestampBefore(request.getPersonnummer().getPersonnummer(),
                request.getEnhetId(), request.getEndDate());
        } else {
            return handelseRepo.findByPersonnummerAndVardgivarIdAndTimestampBefore(request.getPersonnummer().getPersonnummer(),
                request.getVardgivarId(), request.getEndDate());
        }
    }

    private List<Handelse> findNotificationsWithoutTimestamp(IntygWithNotificationsRequest request) {
        if (request.shouldUseEnhetId()) {
            return handelseRepo.findByPersonnummerAndEnhetsIdIn(request.getPersonnummer().getPersonnummer(), request.getEnhetId());
        } else {
            return handelseRepo.findByPersonnummerAndVardgivarId(request.getPersonnummer().getPersonnummer(), request.getVardgivarId());
        }
    }

    private void sendNotificationForQAs(String intygsId, NotificationEvent event, LocalDate date, ArendeAmne amne) {
        Utkast utkast = getUtkast(intygsId);
        if (utkast != null) {
            createAndSendNotificationForQAs(utkast, event, amne, date);
        } else {
            createAndSendNotificationForQAs(intygsId, event, amne, date);
        }
    }

    void createAndSendNotification(Utkast utkast, HandelsekodEnum handelse) {
        createAndSendNotification(utkast, handelse, null, null);
    }

    private void createAndSendNotification(Utkast utkast, HandelsekodEnum handelse,
        ArendeAmne amne, LocalDate sistaDatumForSvar) {

        Optional<SchemaVersion> version = sendNotificationStrategy.decideNotificationForIntyg(utkast);
        if (version.isEmpty()) {
            LOGGER.debug("Schema version is not present. Notification message not sent for event {}", handelse);
            return;
        }

        String hanteratAv = null;
        if (utkast.getSenastSparadAv() != null) {
            hanteratAv = utkast.getSenastSparadAv().getHsaId();
        }
        createAndSendNotification(utkast, handelse, amne, sistaDatumForSvar, version.get(), hanteratAv);
    }

    private void createAndSendNotification(Utkast utkast, HandelsekodEnum handelse,
        ArendeAmne amne, LocalDate sistaDatumForSvar, SchemaVersion version, String hanteratAv) {
        final Amneskod amneskod = getAmnesKod(amne);

        String reference = referensService.getReferensForIntygsId(utkast.getIntygsId());

        NotificationMessage notificationMessage = notificationMessageFactory.createNotificationMessage(utkast, handelse,
            version, reference, amneskod, sistaDatumForSvar);

        send(notificationMessage, utkast.getEnhetsId(), utkast.getIntygTypeVersion());
    }

    void createAndSendNotification(String certificateId, HandelsekodEnum handelse) {
        final var certificate = intygService.fetchIntygDataForInternalUse(certificateId, false);
        createAndSendNotification(certificate, handelse, null, null);
    }

    private void createAndSendNotification(IntygContentHolder certificate, HandelsekodEnum handelse, ArendeAmne amne,
        LocalDate sistaDatumForSvar) {

        final var optionalSchemaVersion = sendNotificationStrategy.decideNotificationForIntyg(certificate.getUtlatande());
        if (optionalSchemaVersion.isEmpty()) {
            LOGGER.debug("Schema version is not present. Notification message not sent for event {}", handelse);
            return;
        }

        final var hanteratAv = currentUserId();
        createAndSendNotification(certificate, handelse, amne, sistaDatumForSvar, optionalSchemaVersion.get(), hanteratAv);
    }

    private void createAndSendNotification(IntygContentHolder certificate, HandelsekodEnum handelse, ArendeAmne amne,
        LocalDate sistaDatumForSvar, SchemaVersion version, String hanteratAv) {
        final var amneskod = getAmnesKod(amne);

        final var reference = referensService.getReferensForIntygsId(certificate.getUtlatande().getId());

        final var certificateId = certificate.getUtlatande().getId();
        final var certificateType = certificate.getUtlatande().getTyp();
        final var careUnitId = certificate.getUtlatande().getGrundData().getSkapadAv().getVardenhet().getEnhetsid();
        final var draftJson = certificate.getContents();

        final var notificationMessage = notificationMessageFactory.createNotificationMessage(certificateId, certificateType, careUnitId,
            draftJson, handelse, version, reference, amneskod, sistaDatumForSvar);

        send(notificationMessage, careUnitId, certificate.getUtlatande().getTextVersion());
    }

    private Amneskod getAmnesKod(ArendeAmne amne) {
        return amne != null ? AmneskodCreator.create(amne.name(), amne.getDescription()) : null;
    }

    private void createAndSendNotificationForQAs(Utkast utkast, NotificationEvent event, ArendeAmne amne, LocalDate sistaDatumForSvar) {

        Optional<SchemaVersion> version = sendNotificationStrategy.decideNotificationForIntyg(utkast);
        if (version.isEmpty()) {
            LOGGER.debug("Schema version is not present. Notification message not sent");
            return;
        }

        final var handelseKod = getHandelseKod(version.get(), event);
        if (handelseKod == null) {
            LOGGER.debug("Notification message not sent for event {} in version {}", event.name(), version.get().name());
            return;
        }

        createAndSendNotification(utkast, handelseKod, amne, sistaDatumForSvar, version.get(), null);
    }

    private void createAndSendNotificationForQAs(String certificateId, NotificationEvent event, ArendeAmne amne,
        LocalDate sistaDatumForSvar) {

        final var certificate = intygService.fetchIntygDataForInternalUse(certificateId, false);

        final var optionalSchemaVersion = sendNotificationStrategy.decideNotificationForIntyg(certificate.getUtlatande());
        if (optionalSchemaVersion.isEmpty()) {
            LOGGER.debug("Schema version is not present. Notification message not sent");
            return;
        }

        final var handelseKod = getHandelseKod(optionalSchemaVersion.get(), event);
        if (handelseKod == null) {
            LOGGER.debug("Notification message not sent for event {} in version {}", event.name(), optionalSchemaVersion.get().name());
            return;
        }

        createAndSendNotification(certificate, handelseKod, amne, sistaDatumForSvar, optionalSchemaVersion.get(), null);
    }

    private HandelsekodEnum getHandelseKod(SchemaVersion schemaVersion, NotificationEvent event) {
        if (SchemaVersion.VERSION_3 == schemaVersion) {
            return getHandelseV3(event);
        } else {
            return getHandelseV1(event);
        }
    }

    private HandelsekodEnum getHandelseV1(NotificationEvent event) {
        return switch (event) {
            case QUESTION_FROM_CARE_WITH_ANSWER_HANDLED -> HANFRFV;
            case QUESTION_FROM_CARE_WITH_ANSWER_UNHANDLED, NEW_ANSWER_FROM_RECIPIENT -> NYSVFM;
            case NEW_ANSWER_FROM_CARE, QUESTION_FROM_RECIPIENT_HANDLED -> HANFRFM;
            case NEW_QUESTION_FROM_RECIPIENT, QUESTION_FROM_RECIPIENT_UNHANDLED -> NYFRFM;
            case NEW_QUESTION_FROM_CARE -> NYFRFV;
            case QUESTION_FROM_CARE_HANDLED, QUESTION_FROM_CARE_UNHANDLED -> null;
        };
    }

    private HandelsekodEnum getHandelseV3(NotificationEvent event) {
        return switch (event) {
            case QUESTION_FROM_CARE_WITH_ANSWER_HANDLED, QUESTION_FROM_CARE_WITH_ANSWER_UNHANDLED, QUESTION_FROM_CARE_HANDLED, QUESTION_FROM_CARE_UNHANDLED ->
                HANFRFV;
            case NEW_ANSWER_FROM_CARE, QUESTION_FROM_RECIPIENT_HANDLED, QUESTION_FROM_RECIPIENT_UNHANDLED -> HANFRFM;
            case NEW_QUESTION_FROM_CARE -> NYFRFV;
            case NEW_QUESTION_FROM_RECIPIENT -> NYFRFM;
            case NEW_ANSWER_FROM_RECIPIENT -> NYSVFM;
        };
    }

    @Override
    public void send(NotificationMessage notificationMessage, String enhetsId, String intygTypeVersion) {
        if (Objects.isNull(jmsTemplateForAggregation)) {
            LOGGER.warn("Can not notify listeners! The JMS transport is not initialized.");
            return;
        }

        final String notificationMessageAsJson = notificationMessageToJson(notificationMessage);
        final String correlationId = UUID.randomUUID().toString();

        try {
            jmsTemplateForAggregation.send(
                new NotificationMessageCreator(
                    notificationMessageAsJson, notificationMessage.getIntygsId(), notificationMessage.getIntygsTyp(),
                    intygTypeVersion, notificationMessage.getHandelse(),
                    currentUserId(),
                    correlationId
                ));
        } catch (JmsException e) {
            LOGGER.error("Could not send message", e);
            throw e;
        }

        LOGGER.debug("Notification message generated and sent to aggregation queue: {}", notificationMessage);
        monitoringLog.logStatusUpdateQueued(notificationMessage.getIntygsId(), correlationId, notificationMessage.getLogiskAdress(),
            notificationMessage.getIntygsTyp(), intygTypeVersion, notificationMessage.getHandelse().name(),
            notificationMessage.getHandelseTid(), currentUserId());
    }


    private String currentUserId() {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null || !(auth.getPrincipal() instanceof WebCertUser) ? null : ((WebCertUser) auth.getPrincipal()).getHsaId();
    }

    private Utkast getUtkast(String intygsId) {
        return utkastRepo.findById(intygsId).orElse(null);
    }

    private String notificationMessageToJson(NotificationMessage notificationMessage) {
        try {
            return objectMapper.writeValueAsString(notificationMessage);
        } catch (JsonProcessingException e) {
            LOGGER.error("Problem occured when trying to create and marshall NotificationMessage.", e);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, e);
        }
    }

    private void sendNotificationForIncomingQuestionByMail(MailNotification mailNotification) {
        try {
            mailNotificationService.sendMailForIncomingQuestion(mailNotification);
        } catch (MailSendException ex) {
            LOGGER.error(
                String.format("Notification mail for question '%s' concerning certificate '%s' couldn't be sent to '%s' (%s)",
                    mailNotification.getQaId(), mailNotification.getCertificateId(), mailNotification.getCareUnitId(),
                    mailNotification.getCareUnitName()), ex);
        }
    }

    private void sendNotificationForIncomingAnswerByMail(MailNotification mailNotification) {
        try {
            mailNotificationService.sendMailForIncomingAnswer(mailNotification);
        } catch (MailSendException ex) {
            LOGGER.error(
                String.format("Notification mail for answer '%s' concerning certificate '%s' couldn't be sent to '%s' (%s)",
                    mailNotification.getQaId(), mailNotification.getCertificateId(), mailNotification.getCareUnitId(),
                    mailNotification.getCareUnitName()
                ),
                ex
            );
        }
    }

    private static final class NotificationMessageCreator implements MessageCreator {

        private final String value;
        private final String intygsId;
        private final String intygsTyp;
        private final String intygTypeVersion;
        private final HandelsekodEnum handelseTyp;
        private final String userId;
        private final String correlationId;


        private NotificationMessageCreator(String notificationMessage, String intygsId, String intygsTyp, String intygTypeVersion,
            HandelsekodEnum handelseTyp, String userId, String correlationId) {
            this.value = notificationMessage;
            this.intygsId = intygsId;
            this.intygsTyp = intygsTyp;
            this.intygTypeVersion = intygTypeVersion;
            this.handelseTyp = handelseTyp;
            this.userId = userId;
            this.correlationId = correlationId;
        }

        /**
         * Note that we add intygsTyp and handelseTyp as JMS headers to simplify subsequent routing.
         */
        @Override
        public Message createMessage(Session session) throws JMSException {
            final TextMessage msg = session.createTextMessage(this.value);
            msg.setStringProperty(INTYGS_ID, this.intygsId);
            msg.setStringProperty(INTYGS_TYP, this.intygsTyp);
            msg.setStringProperty(INTYG_TYPE_VERSION, this.intygTypeVersion);
            msg.setStringProperty(HANDELSE, this.handelseTyp.value());
            if (Objects.nonNull(this.userId)) {
                msg.setStringProperty(USER_ID, this.userId);
            }
            msg.setStringProperty(CORRELATION_ID, correlationId);
            return msg;
        }
    }

}
