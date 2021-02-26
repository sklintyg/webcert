package se.inera.intyg.webcert.notification_sender.notifications.services.postprocessing;

import static se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationErrorTypeEnum.WEBCERT_EXCEPTION;
import static se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationResultTypeEnum.ERROR;
import static se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationResultTypeEnum.UNRECOVERABLE_ERROR;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.notification_sender.notifications.dto.CertificateMessages;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationRedeliveryMessage;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultMessage;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultType;
import se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationErrorTypeEnum;
import se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationResultTypeEnum;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.v3.Arenden;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultType;

@Component
public class NotificationResultMessageCreator {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationResultMessageCreator.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    public NotificationResultMessage createFailureMessage(NotificationMessage notificationMessage, String correlationId, String userId,
        String certificateTypeVersion, Exception exception) throws ModuleNotFoundException, IOException, ModuleException {
        final var moduleApi = moduleRegistry.getModuleApi(notificationMessage.getIntygsTyp(), certificateTypeVersion);
        final var utlatande = moduleApi.getUtlatandeFromJson(notificationMessage.getUtkast());
        final var moduleEntryPoint = moduleRegistry.getModuleEntryPoint(notificationMessage.getIntygsTyp());
        final var certificateTypeExternalId = moduleEntryPoint.getExternalId();

        final var event = createEvent(notificationMessage, utlatande, userId, certificateTypeExternalId);

        final var resultTypeEnum = getResultTypeForExceptionFromDelivery(exception);
        final var notificationResultType = createResultType(exception, resultTypeEnum);

        final var resultMessage = new NotificationResultMessage();
        resultMessage.setCorrelationId(correlationId);
        resultMessage.setEvent(event);
        resultMessage.setResultType(notificationResultType);
        resultMessage.setNotificationSentTime(LocalDateTime.now());

        return resultMessage;
    }

    public NotificationResultMessage createFailureMessage(Handelse event, NotificationRedelivery redelivery, Exception exception) {
        final var resultTypeEnum = getResultTypeForExceptionFromRedelivery(exception);
        final var notificationResultType = createResultType(exception, resultTypeEnum);

        final var notificationResultMessage = new NotificationResultMessage();
        notificationResultMessage.setCorrelationId(redelivery.getCorrelationId());
        notificationResultMessage.setEvent(event);
        notificationResultMessage.setResultType(notificationResultType);
        notificationResultMessage.setNotificationSentTime(LocalDateTime.now());
        notificationResultMessage.setRedeliveryMessageBytes(redelivery.getMessage());
        return notificationResultMessage;
    }

    public NotificationResultMessage createResultMessage(CertificateStatusUpdateForCareType statusUpdate, String correlationId) {
        final var event = createEvent(statusUpdate);

        final var resultMessage = new NotificationResultMessage();
        resultMessage.setCorrelationId(correlationId);
        resultMessage.setEvent(event);
        resultMessage.setNotificationSentTime(LocalDateTime.now());
        return resultMessage;
    }

    public void addToResultMessage(NotificationResultMessage resultMessage, CertificateStatusUpdateForCareType statusUpdate,
        ResultType resultType) {
        addRedeliveryMessageToResultMessage(resultMessage, statusUpdate);
        addResultTypeToResultMessage(resultMessage, resultType);
    }

    public void addToResultMessage(NotificationResultMessage resultMessage, CertificateStatusUpdateForCareType statusUpdate,
        Exception exception) {
        addRedeliveryMessageToResultMessage(resultMessage, statusUpdate);
        addResultTypeToResultMessage(resultMessage, exception);
    }

    private void addRedeliveryMessageToResultMessage(NotificationResultMessage resultMessage,
        CertificateStatusUpdateForCareType statusUpdate) {
        final var redeliveryMessage = createRedeliveryMessage(statusUpdate);
        final var redeliveryMessageAsBytes = redeliveryMessageAsBytes(redeliveryMessage);
        resultMessage.setRedeliveryMessageBytes(redeliveryMessageAsBytes);
    }

    private void addResultTypeToResultMessage(NotificationResultMessage resultMessage, ResultType resultType) {
        final var notificationResultType = new NotificationResultType();

        if (resultType.getResultCode() != null) {
            final var notificationResult = NotificationResultTypeEnum.fromValue(resultType.getResultCode().value());
            notificationResultType.setNotificationResult(notificationResult);
        }

        if (resultType.getErrorId() != null) {
            final var notificationErrorType = NotificationErrorTypeEnum.fromValue(resultType.getErrorId().value());
            notificationResultType.setNotificationErrorType(notificationErrorType);
        }

        notificationResultType.setNotificationResultText(resultType.getResultText());

        resultMessage.setResultType(notificationResultType);
    }

    private void addResultTypeToResultMessage(NotificationResultMessage resultMessage, Exception exception) {
        final var notificationResultType = new NotificationResultType();
        notificationResultType.setNotificationResult(ERROR);
        notificationResultType.setException(exception.getClass().getName());
        notificationResultType.setNotificationResultText(exception.getMessage());
        notificationResultType.setNotificationErrorType(WEBCERT_EXCEPTION);

        resultMessage.setResultType(notificationResultType);
    }

    private byte[] redeliveryMessageAsBytes(NotificationRedeliveryMessage redeliveryMessage) {
        try {
            return objectMapper.writeValueAsBytes(redeliveryMessage);
        } catch (JsonProcessingException e) {
            LOG.error("Exception occured converting NotificationRedeliveryMessage to bytes.", e);
            return null;
        }
    }

    private NotificationRedeliveryMessage createRedeliveryMessage(CertificateStatusUpdateForCareType statusUpdate) {
        final var redeliveryMessage = new NotificationRedeliveryMessage();

        final var certificate = statusUpdate.getIntyg();
        certificate.setUnderskrift(null);
        redeliveryMessage.setCert(certificate);

        final var sentQuestions = createCertificateMessages(statusUpdate.getSkickadeFragor());
        redeliveryMessage.setSent(sentQuestions);

        final var recievedQuestions = createCertificateMessages(statusUpdate.getMottagnaFragor());
        redeliveryMessage.setReceived(recievedQuestions);

        redeliveryMessage.setReference(statusUpdate.getRef());

        return redeliveryMessage;
    }

    private CertificateMessages createCertificateMessages(Arenden questions) {
        if (questions == null) {
            return new CertificateMessages();
        }

        final var certificateMessages = new CertificateMessages();
        certificateMessages.setUnanswered(questions.getEjBesvarade());
        certificateMessages.setAnswered(questions.getBesvarade());
        certificateMessages.setHandled(questions.getHanterade());
        certificateMessages.setTotal(questions.getTotalt());

        return certificateMessages;
    }

    private Handelse createEvent(CertificateStatusUpdateForCareType statusUpdate) {
        final var topicCode = statusUpdate.getHandelse().getAmne();
        final var userId = statusUpdate.getHanteratAv();

        final var event = new Handelse();
        event.setCode(HandelsekodEnum.fromValue(statusUpdate.getHandelse().getHandelsekod().getCode()));
        event.setEnhetsId(statusUpdate.getIntyg().getSkapadAv().getEnhet().getEnhetsId().getExtension());
        event.setIntygsId(statusUpdate.getIntyg().getIntygsId().getExtension());
        event.setCertificateType(statusUpdate.getIntyg().getTyp().getCode());
        event.setCertificateVersion(statusUpdate.getIntyg().getVersion());
        event.setCertificateIssuer(statusUpdate.getIntyg().getSkapadAv().getPersonalId().getExtension());
        event.setPersonnummer(statusUpdate.getIntyg().getPatient().getPersonId().getExtension());
        event.setTimestamp(statusUpdate.getHandelse().getTidpunkt());
        event.setVardgivarId(statusUpdate.getIntyg().getSkapadAv().getEnhet().getVardgivare().getVardgivareId().getExtension());
        event.setAmne(topicCode != null ? ArendeAmne.valueOf(topicCode.getCode()) : null);
        event.setSistaDatumForSvar(statusUpdate.getHandelse().getSistaDatumForSvar());
        event.setHanteratAv(userId != null ? userId.getExtension() : null);
        return event;
    }

    private NotificationResultType createResultType(Exception exception, NotificationResultTypeEnum resultTypeEnum) {
        final var notificationResultType = new NotificationResultType();
        notificationResultType.setNotificationResult(resultTypeEnum);
        notificationResultType.setNotificationResultText(exception.getMessage());
        notificationResultType.setNotificationErrorType(NotificationErrorTypeEnum.WEBCERT_EXCEPTION);
        notificationResultType.setException(exception.getClass().getName());
        return notificationResultType;
    }

    private NotificationResultTypeEnum getResultTypeForExceptionFromRedelivery(Exception exception) {
        return exception instanceof WebCertServiceException ? UNRECOVERABLE_ERROR : ERROR;
    }

    private NotificationResultTypeEnum getResultTypeForExceptionFromDelivery(Exception exception) {
        return exception instanceof TemporaryException ? ERROR : UNRECOVERABLE_ERROR;
    }

    private Handelse createEvent(NotificationMessage notificationMessage, Utlatande utlatande, String user,
        String certificateTypeExternalId) {
        final var event = new Handelse();
        event.setIntygsId(utlatande.getId());
        event.setCertificateType(certificateTypeExternalId);
        event.setCertificateVersion(utlatande.getTextVersion());
        event.setVardgivarId(utlatande.getGrundData().getSkapadAv().getVardenhet().getVardgivare().getVardgivarid());
        event.setCertificateIssuer(utlatande.getGrundData().getSkapadAv().getPersonId());
        event.setPersonnummer(utlatande.getGrundData().getPatient().getPersonId().getPersonnummer());

        event.setCode(notificationMessage.getHandelse());
        event.setTimestamp(notificationMessage.getHandelseTid());
        event.setAmne(notificationMessage.getAmne() != null ? ArendeAmne.valueOf(notificationMessage.getAmne().getCode()) : null);
        event.setEnhetsId(notificationMessage.getLogiskAdress());
        event.setSistaDatumForSvar(notificationMessage.getSistaSvarsDatum());

        event.setHanteratAv(user);

        return event;
    }
}
