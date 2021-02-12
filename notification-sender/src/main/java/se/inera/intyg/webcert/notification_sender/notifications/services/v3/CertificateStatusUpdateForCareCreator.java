package se.inera.intyg.webcert.notification_sender.notifications.services.v3;

import static se.inera.intyg.common.support.Constants.HSA_ID_OID;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.ModuleEntryPoint;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.common.support.modules.support.api.notification.ArendeCount;
import se.inera.intyg.common.support.modules.support.api.notification.FragorOchSvar;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.common.support.xml.XmlMarshallerHelper;
import se.inera.intyg.infra.integration.hsa.model.Vardenhet;
import se.inera.intyg.infra.integration.hsa.model.Vardgivare;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;
import se.inera.intyg.webcert.common.service.notification.AmneskodCreator;
import se.inera.intyg.webcert.notification_sender.notifications.services.NotificationPatientEnricher;
import se.inera.intyg.webcert.notification_sender.notifications.services.NotificationTypeConverter;
import se.inera.intyg.webcert.notification_sender.notifications.util.NotificationRedeliveryUtil;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Amneskod;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;
import se.riv.infrastructure.directory.v1.PersonInformationType;

@Component
public class CertificateStatusUpdateForCareCreator {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateStatusUpdateForCareCreator.class);

    @Autowired
    private NotificationPatientEnricher notificationPatientEnricher;

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    //@Value("${notification.redelivery.xml.local.part}")
    private String xmlLocalPart = "CertificateStatusUpdateForCare";

    //@Value("${notification.redelivery.xml.namespace.url}")
    private String xmlNamespaceUrl = "urn:riv:clinicalprocess:healthcond:certificate:CertificateStatusUpdateForCareResponder:3";

    public CertificateStatusUpdateForCareType create(NotificationMessage notificationMessage, String certificateTypeVersion)
        throws TemporaryException, ModuleNotFoundException, IOException, ModuleException {
        final var moduleApi = moduleRegistry.getModuleApi(notificationMessage.getIntygsTyp(), certificateTypeVersion);
        final var utlatande = moduleApi.getUtlatandeFromJson(notificationMessage.getUtkast());
        final var intyg = moduleApi.getIntygFromUtlatande(utlatande);
        notificationPatientEnricher.enrichWithPatient(intyg);
        return NotificationTypeConverter.convert(notificationMessage, intyg);
    }

    public CertificateStatusUpdateForCareType create(Handelse event, Vardgivare careProvider,
        Vardenhet careUnit, PersonInformationType personInfo) throws ModuleNotFoundException, TemporaryException {
        String certificateId = event.getIntygsId();
        String certificateType = event.getCertificateType();
        String certificateVersion = event.getCertificateVersion();
        LocalDateTime eventTime = event.getTimestamp();
        HandelsekodEnum eventType = event.getCode();
        String logicalAddress = event.getEnhetsId();
        FragorOchSvar qa = FragorOchSvar.getEmpty();
        ArendeCount sentQuestions = ArendeCount.getEmpty();
        ArendeCount receivedQuestions = ArendeCount.getEmpty();
        Amneskod topicCode = event.getAmne() != null ? AmneskodCreator.create(event.getAmne().name(), event.getAmne().getDescription())
            : null;
        LocalDate lastReplyDate = event.getSistaDatumForSvar();

        String moduleId = moduleRegistry.getModuleIdFromExternalId(certificateType);
        ModuleEntryPoint moduleEntryPoint = moduleRegistry.getModuleEntryPoint(moduleId);

        Intyg certificate = new Intyg();
        certificate.setIntygsId(NotificationRedeliveryUtil.getIIType(new IntygId(), certificateId, logicalAddress));
        //certificate.setTyp(NotificationRedeliveryUtil.getCertificateType(certificateType));
        certificate.setTyp(NotificationRedeliveryUtil.getCertificateType(moduleEntryPoint));
        certificate.setVersion(certificateVersion);
        certificate.setPatient(NotificationRedeliveryUtil.getPatient(event.getPersonnummer()));
        certificate.setSkapadAv(NotificationRedeliveryUtil.getHosPersonal(careProvider, careUnit, personInfo));

        notificationPatientEnricher.enrichWithPatient(certificate);
        NotificationTypeConverter.complementIntyg(certificate);

        final var statusUpdate = new CertificateStatusUpdateForCareType();
        statusUpdate.setIntyg(certificate);
        statusUpdate.setHandelse(NotificationRedeliveryUtil.getEventV3(eventType, eventTime, event.getAmne(), lastReplyDate));
        statusUpdate.setSkickadeFragor(NotificationTypeConverter.toArenden(sentQuestions));
        statusUpdate.setMottagnaFragor(NotificationTypeConverter.toArenden(receivedQuestions));
        statusUpdate.setHanteratAv(NotificationRedeliveryUtil.getIIType(new HsaId(), event.getHanteratAv(), HSA_ID_OID));

        return statusUpdate;
    }

    public String marshal(CertificateStatusUpdateForCareType statusUpdate) {
        final QName qName = new QName(xmlNamespaceUrl, xmlLocalPart);
        final JAXBElement<CertificateStatusUpdateForCareType> jaxbElement =
            new JAXBElement<>(qName, CertificateStatusUpdateForCareType.class, JAXBElement.GlobalScope.class, statusUpdate);

        return XmlMarshallerHelper.marshal(jaxbElement);
    }
}
