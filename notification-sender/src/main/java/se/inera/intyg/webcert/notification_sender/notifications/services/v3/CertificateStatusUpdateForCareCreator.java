package se.inera.intyg.webcert.notification_sender.notifications.services.v3;

import static se.inera.intyg.common.support.Constants.HSA_ID_OID;

import java.io.IOException;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.ModuleEntryPoint;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.common.support.modules.support.api.notification.ArendeCount;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.common.support.xml.XmlMarshallerHelper;
import se.inera.intyg.infra.integration.hsatk.model.PersonInformation;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;
import se.inera.intyg.webcert.notification_sender.notifications.services.NotificationPatientEnricher;
import se.inera.intyg.webcert.notification_sender.notifications.services.NotificationTypeConverter;
import se.inera.intyg.webcert.notification_sender.notifications.util.NotificationRedeliveryUtil;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;

@Component
public class CertificateStatusUpdateForCareCreator {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateStatusUpdateForCareCreator.class);

    @Autowired
    private NotificationPatientEnricher notificationPatientEnricher;

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    private static final String XML_LOCAL_PART = "CertificateStatusUpdateForCare";
    private static final String XML_NAMESPACE_URL =
        "urn:riv:clinicalprocess:healthcond:certificate:CertificateStatusUpdateForCareResponder:3";

    public CertificateStatusUpdateForCareType create(NotificationMessage notificationMessage, String certificateTypeVersion)
        throws TemporaryException, ModuleNotFoundException, IOException, ModuleException {
        final var moduleApi = moduleRegistry.getModuleApi(notificationMessage.getIntygsTyp(), certificateTypeVersion);
        final var utlatande = moduleApi.getUtlatandeFromJson(notificationMessage.getUtkast());
        final var intyg = moduleApi.getIntygFromUtlatande(utlatande);
        notificationPatientEnricher.enrichWithPatient(intyg);
        return NotificationTypeConverter.convert(notificationMessage, intyg);
    }

    public CertificateStatusUpdateForCareType create(Handelse event, Vardgivare careProvider,
        Vardenhet careUnit, PersonInformation personInfo) throws ModuleNotFoundException, TemporaryException {

        String moduleId = moduleRegistry.getModuleIdFromExternalId(event.getCertificateType());
        ModuleEntryPoint moduleEntryPoint = moduleRegistry.getModuleEntryPoint(moduleId);

        Intyg certificate = new Intyg();
        certificate.setIntygsId(NotificationRedeliveryUtil.getIIType(new IntygId(), event.getIntygsId(), event.getEnhetsId()));
        certificate.setTyp(NotificationRedeliveryUtil.getCertificateType(moduleEntryPoint));
        certificate.setVersion(event.getCertificateVersion());
        certificate.setPatient(NotificationRedeliveryUtil.getPatient(event.getPersonnummer()));
        certificate.setSkapadAv(NotificationRedeliveryUtil.getHosPersonal(careProvider, careUnit, personInfo));

        notificationPatientEnricher.enrichWithPatient(certificate);
        NotificationTypeConverter.complementIntyg(certificate);

        final var statusUpdate = new CertificateStatusUpdateForCareType();
        statusUpdate.setIntyg(certificate);
        statusUpdate.setHandelse(NotificationRedeliveryUtil.getEventV3(event.getCode(), event.getTimestamp(), event.getAmne(),
            event.getSistaDatumForSvar()));
        statusUpdate.setSkickadeFragor(NotificationTypeConverter.toArenden(ArendeCount.getEmpty()));
        statusUpdate.setMottagnaFragor(NotificationTypeConverter.toArenden(ArendeCount.getEmpty()));
        statusUpdate.setHanteratAv(NotificationRedeliveryUtil.getIIType(new HsaId(), event.getHanteratAv(), HSA_ID_OID));

        return statusUpdate;
    }

    public String marshal(CertificateStatusUpdateForCareType statusUpdate) {
        final QName qName = new QName(XML_NAMESPACE_URL, XML_LOCAL_PART);
        final JAXBElement<CertificateStatusUpdateForCareType> jaxbElement =
            new JAXBElement<>(qName, CertificateStatusUpdateForCareType.class, JAXBElement.GlobalScope.class, statusUpdate);

        return XmlMarshallerHelper.marshal(jaxbElement);
    }
}
