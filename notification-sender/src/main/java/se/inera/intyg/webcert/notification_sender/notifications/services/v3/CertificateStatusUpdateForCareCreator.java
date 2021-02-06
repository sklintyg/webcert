package se.inera.intyg.webcert.notification_sender.notifications.services.v3;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;
import se.inera.intyg.webcert.notification_sender.notifications.services.NotificationPatientEnricher;
import se.inera.intyg.webcert.notification_sender.notifications.services.NotificationTypeConverter;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;

@Component
public class CertificateStatusUpdateForCareCreator {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateStatusUpdateForCareCreator.class);

    @Autowired
    private NotificationPatientEnricher notificationPatientEnricher;

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    public CertificateStatusUpdateForCareType create(NotificationMessage notificationMessage, String certificateTypeVersion)
        throws TemporaryException, ModuleNotFoundException, IOException, ModuleException {
        final var moduleApi = moduleRegistry.getModuleApi(notificationMessage.getIntygsTyp(), certificateTypeVersion);
        final var utlatande = moduleApi.getUtlatandeFromJson(notificationMessage.getIntygsTyp());
        final var intyg = moduleApi.getIntygFromUtlatande(utlatande);
        notificationPatientEnricher.enrichWithPatient(intyg);
        return NotificationTypeConverter.convert(notificationMessage, intyg);
    }
}
