package se.inera.intyg.webcert.notification_sender.notifications.services;

import static se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationResultTypeEnum.FAILURE;

import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultMessage;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultType;
import se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationErrorTypeEnum;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;

@Component
public class NotificationResultMessageCreator {

    public NotificationResultMessage createFailureMessage(String correlationId, String userId, NotificationMessage notificationMessage,
        Utlatande utlatande, Exception exception) {
        final var event = createEvent(notificationMessage, utlatande, userId);

        final var notificationResultType = createResultType(exception);

        final var resultMessage = new NotificationResultMessage();
        resultMessage.setCorrelationId(correlationId);
        resultMessage.setEvent(event);
        resultMessage.setResultType(notificationResultType);

        return resultMessage;
    }

    private NotificationResultType createResultType(Exception exception) {
        final var notificationResultType = new NotificationResultType();
        // TODO: Could ERROR be used or is it necessary with a specific type?
        notificationResultType.setNotificationResult(FAILURE);
        notificationResultType.setNotificationResultText(exception.getMessage());
        notificationResultType.setNotificationErrorType(NotificationErrorTypeEnum.WEBCERT_EXCEPTION);
        notificationResultType.setException(exception.getClass().getName());
        return notificationResultType;
    }

    private Handelse createEvent(NotificationMessage notificationMessage, Utlatande utlatande, String user) {
        final var event = new Handelse();
        event.setIntygsId(utlatande.getId());
        event.setCertificateType(utlatande.getTyp());
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
