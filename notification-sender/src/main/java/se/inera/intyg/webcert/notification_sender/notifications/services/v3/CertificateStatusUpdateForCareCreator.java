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
package se.inera.intyg.webcert.notification_sender.notifications.services.v3;

import static se.inera.intyg.common.support.Constants.HSA_ID_OID;

import com.helger.xml.transform.StringStreamResult;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import java.io.IOException;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3._2002._06.xmldsig_filter2.XPathType;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.ModuleEntryPoint;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.common.support.modules.support.api.notification.ArendeCount;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.infra.integration.hsatk.model.PersonInformation;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.webcert.notification_sender.notifications.services.NotificationPatientEnricher;
import se.inera.intyg.webcert.notification_sender.notifications.services.NotificationTypeConverter;
import se.inera.intyg.webcert.notification_sender.notifications.util.NotificationRedeliveryUtil;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.DatePeriodType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PQType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PartialDateType;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;

@Component
public class CertificateStatusUpdateForCareCreator {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateStatusUpdateForCareCreator.class);

    @Autowired
    private NotificationPatientEnricher notificationPatientEnricher;

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    private static final String XML_LOCAL_PART = "CertificateStatusUpdateForCare";

    public CertificateStatusUpdateForCareType create(NotificationMessage notificationMessage, String certificateTypeVersion)
        throws ModuleNotFoundException, IOException, ModuleException {
        final var moduleApi = moduleRegistry.getModuleApi(notificationMessage.getIntygsTyp(), certificateTypeVersion);
        final var utlatande = moduleApi.getUtlatandeFromJson(notificationMessage.getUtkast());
        final var intyg = moduleApi.getIntygFromUtlatande(utlatande);
        notificationPatientEnricher.enrichWithPatient(intyg);
        return NotificationTypeConverter.convert(notificationMessage, intyg);
    }

    public CertificateStatusUpdateForCareType create(Handelse event, Vardgivare careProvider,
        Vardenhet careUnit, PersonInformation personInfo) throws ModuleNotFoundException {

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

    public String marshal(CertificateStatusUpdateForCareType statusUpdate) throws JAXBException {
        final QName qName = new QName(XML_LOCAL_PART);
        final JAXBElement<CertificateStatusUpdateForCareType> jaxbElement =
            new JAXBElement<>(qName, CertificateStatusUpdateForCareType.class, JAXBElement.GlobalScope.class, statusUpdate);

        StringStreamResult stringStreamResult = new StringStreamResult();
        JAXBContext jaxbContext = JAXBContext.newInstance(CertificateStatusUpdateForCareType.class, DatePeriodType.class,
            PartialDateType.class, XPathType.class, PQType.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(jaxbElement, stringStreamResult);
        return stringStreamResult.getAsString();
    }
}
