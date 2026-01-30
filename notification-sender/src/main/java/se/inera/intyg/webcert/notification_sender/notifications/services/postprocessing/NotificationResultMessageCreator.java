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
package se.inera.intyg.webcert.notification_sender.notifications.services.postprocessing;

import static se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationErrorTypeEnum.WEBCERT_EXCEPTION;
import static se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationResultTypeEnum.ERROR;
import static se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationResultTypeEnum.UNRECOVERABLE_ERROR;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.converter.mapping.UnitMapperUtil;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultMessage;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultType;
import se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationErrorTypeEnum;
import se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationResultTypeEnum;
import se.inera.intyg.webcert.notification_sender.notifications.services.v3.CertificateStatusUpdateForCareCreator;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultType;

@Component
public class NotificationResultMessageCreator {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationResultMessageCreator.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Autowired
    private CertificateStatusUpdateForCareCreator certificateStatusUpdateForCareCreator;

    @Autowired
    private UnitMapperUtil unitMapperUtil;

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
        notificationResultMessage.setStatusUpdateXml(redelivery.getMessage());
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
        addStatusUpdateToResultMessage(resultMessage, statusUpdate);
        addResultTypeToResultMessage(resultMessage, resultType);
    }

    public void addToResultMessage(NotificationResultMessage resultMessage, CertificateStatusUpdateForCareType statusUpdate,
        Exception exception) {
        addStatusUpdateToResultMessage(resultMessage, statusUpdate);
        addResultTypeToResultMessage(resultMessage, exception);
    }

    private void addStatusUpdateToResultMessage(NotificationResultMessage resultMessage,
        CertificateStatusUpdateForCareType statusUpdate) {
        final var statusUpdateXmlAsBytes = getStatusUpdateXmlAsBytes(statusUpdate);
        resultMessage.setStatusUpdateXml(statusUpdateXmlAsBytes);
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

    private byte[] getStatusUpdateXmlAsBytes(CertificateStatusUpdateForCareType statusUpdate) {
        try {
            final var statusUpdateXml = certificateStatusUpdateForCareCreator.marshal(statusUpdate);
            return objectMapper.writeValueAsBytes(statusUpdateXml);
        } catch (JAXBException | JsonProcessingException e) {
            LOG.error("Exception occurred creating NotificationRedeliveryMessage", e);
            return null;
        }
    }

    private Handelse createEvent(CertificateStatusUpdateForCareType statusUpdate) {
        final var topicCode = statusUpdate.getHandelse().getAmne();
        final var userId = statusUpdate.getHanteratAv();

        final var enhet = statusUpdate.getIntyg().getSkapadAv().getEnhet();
        final var mappedUnit = unitMapperUtil.getMappedUnit(
            enhet.getVardgivare().getVardgivareId().getExtension(),
            enhet.getVardgivare().getVardgivarnamn(),
            enhet.getEnhetsId().getExtension(),
            enhet.getEnhetsnamn(),
            statusUpdate.getIntyg().getSigneringstidpunkt() != null ? statusUpdate.getIntyg().getSigneringstidpunkt()
                : statusUpdate.getHandelse().getTidpunkt()
        );

        final var event = new Handelse();
        event.setCode(HandelsekodEnum.fromValue(statusUpdate.getHandelse().getHandelsekod().getCode()));
        event.setEnhetsId(mappedUnit.issuedUnitId());
        event.setIntygsId(statusUpdate.getIntyg().getIntygsId().getExtension());
        event.setCertificateType(statusUpdate.getIntyg().getTyp().getCode());
        event.setCertificateVersion(statusUpdate.getIntyg().getVersion());
        event.setCertificateIssuer(statusUpdate.getIntyg().getSkapadAv().getPersonalId().getExtension());
        event.setPersonnummer(statusUpdate.getIntyg().getPatient().getPersonId().getExtension());
        event.setTimestamp(statusUpdate.getHandelse().getTidpunkt());
        event.setVardgivarId(mappedUnit.careProviderId());
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
