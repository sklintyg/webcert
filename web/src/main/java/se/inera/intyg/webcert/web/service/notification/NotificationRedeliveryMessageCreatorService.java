package se.inera.intyg.webcert.web.service.notification;

import static se.inera.intyg.common.support.Constants.HSA_ID_OID;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.infra.integration.hsatk.services.legacy.HsaOrganizationsService;
import se.inera.intyg.infra.integration.hsatk.services.legacy.HsaPersonService;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.notification_sender.notifications.dto.CertificateMessages;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationRedeliveryMessage;
import se.inera.intyg.webcert.notification_sender.notifications.services.NotificationTypeConverter;
import se.inera.intyg.webcert.notification_sender.notifications.services.v3.CertificateStatusUpdateForCareCreator;
import se.inera.intyg.webcert.notification_sender.notifications.util.NotificationRedeliveryUtil;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.v3.Arenden;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;

@Service
public class NotificationRedeliveryMessageCreatorService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationRedeliveryMessageCreatorService.class);
    
    @Autowired
    private HandelseRepository handelseRepo;

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Autowired
    private UtkastService draftService;

    @Autowired
    private UtkastRepository draftRepo;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HsaOrganizationsService hsaOrganizationsService;

    @Autowired
    private HsaPersonService hsaPersonService;

    @Autowired
    private CertificateStatusUpdateForCareCreator certificateStatusUpdateForCareCreator;

    @Autowired
    private IntygService certificateService;

    @Autowired
    private NotificationMessageFactory notificationMessageFactory;

    public CertificateStatusUpdateForCareType getCertificateStatusUpdate(NotificationRedelivery redelivery)
        throws IOException, ModuleException, ModuleNotFoundException, TemporaryException {
        final Handelse event = getEventById(redelivery.getEventId());
        if (redelivery.getMessage() == null) {
            return createStatusUpdate(event);
        } else {
            final NotificationRedeliveryMessage redeliveryMessage = objectMapper.readValue(redelivery.getMessage(),
                NotificationRedeliveryMessage.class);
            final var statusUpdate = new CertificateStatusUpdateForCareType();
            statusUpdate.setSkickadeFragor(createMessages(redeliveryMessage.getSent()));
            statusUpdate.setMottagnaFragor(createMessages(redeliveryMessage.getReceived()));
            statusUpdate.setRef(redeliveryMessage.getReference());
            if (!redeliveryMessage.hasCertificate()) {
                final var certificate = getCertificate(event.getIntygsId(), event.getCertificateType(), event.getCertificateVersion());
                certificate.setPatient(redeliveryMessage.getPatient());
                NotificationTypeConverter.complementIntyg(certificate);
                statusUpdate.setIntyg(certificate);
            } else {
                statusUpdate.setIntyg(redeliveryMessage.getCert());
            }

            // TODO Why update
            statusUpdate.setHanteratAv(NotificationRedeliveryUtil.getIIType(new HsaId(), event.getHanteratAv(), HSA_ID_OID));
            statusUpdate.setHandelse(NotificationRedeliveryUtil.getEventV3(event.getCode(), event.getTimestamp(), event.getAmne(),
                event.getSistaDatumForSvar()));
            return statusUpdate;
        }
    }

    private Handelse getEventById(Long id) {
        return handelseRepo.findById(id).orElseThrow();
    }

    private Intyg getCertificate(String certificateId, String certificateType, String certificateVersion)
        throws ModuleNotFoundException, ModuleException, IOException {
        var certificate = getCertificateFromWebcert(certificateId, certificateType, certificateVersion);
        if (certificate == null) {
            certificate = getCertificateFromIntygstjanst(certificateId, certificateType, certificateVersion);
        }
        return certificate;
    }

    private Intyg getCertificateFromWebcert(String certificateId, String certificateType, String certificateVersion)
        throws ModuleNotFoundException, ModuleException, IOException {
        try {
            final var draft = draftService.getDraft(certificateId, moduleRegistry.getModuleIdFromExternalId(certificateType), false);
            final var moduleApi = moduleRegistry
                .getModuleApi(moduleRegistry.getModuleIdFromExternalId(certificateType), certificateVersion);
            final var utlatande = moduleApi.getUtlatandeFromJson(draft.getModel());
            return moduleApi.getIntygFromUtlatande(utlatande);
        } catch (WebCertServiceException e) {
            LOG.warn("Could not find certificate {} of type {} in webcert's database. Will check intygstjanst...", certificateId,
                certificateType, e);
            return null;
        }
    }

    private Intyg getCertificateFromIntygstjanst(String certificateId, String certificateType, String certificateVersion)
        throws ModuleNotFoundException, ModuleException, WebCertServiceException {
        try {
            final var certificateContentHolder = certificateService.fetchIntygDataForInternalUse(certificateId, true);
            final var moduleApi = moduleRegistry
                .getModuleApi(moduleRegistry.getModuleIdFromExternalId(certificateType), certificateVersion);
            final var utlatande = certificateContentHolder.getUtlatande();
            return moduleApi.getIntygFromUtlatande(utlatande);
        } catch (WebCertServiceException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND,
                String.format("Could not find certificate id: %s of type %s in intygstjanst's database", certificateId,
                    certificateType), e);
        }
    }

    private Arenden createMessages(CertificateMessages certificateMessages) {
        if (certificateMessages == null) {
            return new Arenden();
        }

        final var messages = new Arenden();
        messages.setTotalt(certificateMessages.getTotal());
        messages.setEjBesvarade(certificateMessages.getUnanswered());
        messages.setBesvarade(certificateMessages.getAnswered());
        messages.setHanterade(certificateMessages.getHandled());
        return messages;
    }

    private CertificateStatusUpdateForCareType createStatusUpdate(Handelse event)
        throws TemporaryException, ModuleNotFoundException, IOException, ModuleException {
        if (event.getCode() == HandelsekodEnum.RADERA) {
            final var careProvider = hsaOrganizationsService.getVardgivareInfo(event.getVardgivarId());
            final var careUnit = hsaOrganizationsService.getVardenhet(event.getEnhetsId());
            final var personInfo = hsaPersonService.getHsaPersonInfo(event.getCertificateIssuer()).get(0);
            return certificateStatusUpdateForCareCreator.create(event, careProvider, careUnit, personInfo);
        } else {
            final var notificationMessage = createNotificationMessage(event);
            return certificateStatusUpdateForCareCreator.create(notificationMessage, event.getCertificateVersion());
        }
    }

    private NotificationMessage createNotificationMessage(Handelse event)
        throws ModuleNotFoundException, IOException, ModuleException {
        final var draft = draftRepo.findById(event.getIntygsId()).orElse(null);
        if (draft != null) {
            return notificationMessageFactory.createNotificationMessage(event, draft.getModel());
        } else {
            final var certificateContentHolder = certificateService.fetchIntygDataForInternalUse(event.getIntygsId(), true);
            return notificationMessageFactory.createNotificationMessage(event, certificateContentHolder.getContents());
        }
    }
}
