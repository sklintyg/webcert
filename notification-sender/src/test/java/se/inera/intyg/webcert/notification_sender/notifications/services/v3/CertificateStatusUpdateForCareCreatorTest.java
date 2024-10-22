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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static se.inera.intyg.common.support.Constants.HSA_ID_OID;
import static se.inera.intyg.common.support.Constants.KV_HANDELSE_CODE_SYSTEM;
import static se.inera.intyg.common.support.Constants.KV_INTYGSTYP_CODE_SYSTEM;
import static se.inera.intyg.common.support.Constants.PERSON_ID_OID;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.TextNode;
import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.lisjp.v1.model.internal.LisjpUtlatandeV1;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.ModuleEntryPoint;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.common.support.modules.support.api.notification.ArendeCount;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.infra.integration.hsatk.model.PersonInformation;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;
import se.inera.intyg.webcert.common.service.notification.AmneskodCreator;
import se.inera.intyg.webcert.notification_sender.notifications.services.NotificationPatientEnricher;
import se.inera.intyg.webcert.notification_sender.notifications.util.NotificationRedeliveryUtil;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.TypAvIntyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;

@RunWith(MockitoJUnitRunner.class)
public class CertificateStatusUpdateForCareCreatorTest {

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @Mock
    private ModuleApi moduleApi;

    @Mock
    private ModuleEntryPoint moduleEntryPoint;

    @Mock
    NotificationPatientEnricher notificationPatientEnricher;

    @InjectMocks
    private CertificateStatusUpdateForCareCreator certificateStatusUpdateForCareCreator;

    private static final String CERTIFICATE_ID = "CERTIFICATE_ID";
    private static final String CERTIFICATE_TYPE_EXTERNAL = "LISJP";
    private static final String CERTIFICATE_TYPE_INTERNAL = "lisjp";
    private static final String CERTIFICATE_DISPLAY_NAME = "Läkarintyg för sjukpenning";
    private static final String TEXT_VERSION = "TEXT_VERSION";

    private static final String CARE_PROVIDER_ID = "CARE_PROVIDER_ID";
    private static final String UNIT_ID = "UNIT_ID";
    private static final String ISSUER_ID = "ISSUER_ID";
    private static final String PATIENT_ID = "191212121212";
    private static final String USER_ID = "USER_ID";

    private static final Long EVENT_ID = 1000L;
    private static final HandelsekodEnum EVENT_ENUM = HandelsekodEnum.SKAPAT;
    private static final LocalDateTime EVENT_TIMESTAMP = LocalDateTime.of(2021, 2, 22, 15, 15, 15, 123456789);

    private static final TextNode UTKAST_JSON = JsonNodeFactory.instance.textNode("UTKAST_JSON");
    private static final String SUBJECT_CODE = "KOMPLT";

    @Test
    public void shouldDoProperMethodCallsWhenCreatingStatusUpdateFromNotificationMessage() throws ModuleNotFoundException,
        TemporaryException, ModuleException, IOException {
        final var notificationMessage = createNotificationMessage();
        final var utlatande = createUtlatande();
        final var certificate = createCertificateV3();

        doReturn(moduleApi).when(moduleRegistry).getModuleApi(CERTIFICATE_TYPE_INTERNAL, TEXT_VERSION);
        doReturn(utlatande).when(moduleApi).getUtlatandeFromJson(UTKAST_JSON.toString());
        doReturn(certificate).when(moduleApi).getIntygFromUtlatande(utlatande);
        doNothing().when(notificationPatientEnricher).enrichWithPatient(certificate);

        final var statusUpdate = certificateStatusUpdateForCareCreator.create(notificationMessage, TEXT_VERSION);

        verify(moduleRegistry).getModuleApi(CERTIFICATE_TYPE_INTERNAL, TEXT_VERSION);
        verify(moduleApi).getUtlatandeFromJson(UTKAST_JSON.toString());
        verify(moduleApi, times(1)).getIntygFromUtlatande(utlatande);
        verify(notificationPatientEnricher).enrichWithPatient(certificate);
        assertEquals(CERTIFICATE_ID, statusUpdate.getIntyg().getIntygsId().getExtension());
    }

    @Test
    public void shouldSetPropertiesInStatusUpdateCorrectlyWhenCreatingFromEvent() throws ModuleNotFoundException, TemporaryException {
        final var event = createEvent();
        final var careProvider = createCareProvider();
        final var careUnit = createCareUnit();
        final var personInfo = createPersonInformation();

        doReturn(CERTIFICATE_TYPE_INTERNAL).when(moduleRegistry).getModuleIdFromExternalId(CERTIFICATE_TYPE_EXTERNAL);
        doReturn(moduleEntryPoint).when(moduleRegistry).getModuleEntryPoint(CERTIFICATE_TYPE_INTERNAL);
        doReturn(CERTIFICATE_TYPE_EXTERNAL).when(moduleEntryPoint).getExternalId();
        doReturn(CERTIFICATE_DISPLAY_NAME).when(moduleEntryPoint).getModuleName();
        doNothing().when(notificationPatientEnricher).enrichWithPatient(any(Intyg.class));

        final var statusUpdate = certificateStatusUpdateForCareCreator.create(event, careProvider, careUnit,
            personInfo);

        assertEquals(CERTIFICATE_ID, statusUpdate.getIntyg().getIntygsId().getExtension());
        assertEquals(UNIT_ID, statusUpdate.getIntyg().getIntygsId().getRoot());

        assertEquals(CERTIFICATE_TYPE_EXTERNAL, statusUpdate.getIntyg().getTyp().getCode());
        assertEquals(KV_INTYGSTYP_CODE_SYSTEM, statusUpdate.getIntyg().getTyp().getCodeSystem());
        assertEquals(CERTIFICATE_DISPLAY_NAME, statusUpdate.getIntyg().getTyp().getDisplayName());

        assertEquals(PATIENT_ID, statusUpdate.getIntyg().getPatient().getPersonId().getExtension());
        assertEquals(PERSON_ID_OID, statusUpdate.getIntyg().getPatient().getPersonId().getRoot());

        assertEquals(CARE_PROVIDER_ID, statusUpdate.getIntyg().getSkapadAv().getEnhet().getVardgivare().getVardgivareId().getExtension());
        assertEquals(HSA_ID_OID, statusUpdate.getIntyg().getSkapadAv().getEnhet().getVardgivare().getVardgivareId().getRoot());
        assertEquals(UNIT_ID, statusUpdate.getIntyg().getSkapadAv().getEnhet().getEnhetsId().getExtension());
        assertEquals(HSA_ID_OID, statusUpdate.getIntyg().getSkapadAv().getEnhet().getEnhetsId().getRoot());
        assertEquals(ISSUER_ID, statusUpdate.getIntyg().getSkapadAv().getPersonalId().getExtension());
        assertEquals(HSA_ID_OID, statusUpdate.getIntyg().getSkapadAv().getPersonalId().getRoot());

        assertEquals(EVENT_ENUM.name(), statusUpdate.getHandelse().getHandelsekod().getCode());
        assertEquals(KV_HANDELSE_CODE_SYSTEM, statusUpdate.getHandelse().getHandelsekod().getCodeSystem());
        assertEquals(EVENT_TIMESTAMP, statusUpdate.getHandelse().getTidpunkt());

        assertEquals(USER_ID, statusUpdate.getHanteratAv().getExtension());
        assertEquals(HSA_ID_OID, statusUpdate.getHanteratAv().getRoot());

        assertNotNull(statusUpdate.getMottagnaFragor());
        assertNotNull(statusUpdate.getSkickadeFragor());
    }

    @Test
    public void shouldEnrichCertificateWithPatientWhenCreatingStatusUpdateFromEvent() throws ModuleNotFoundException, TemporaryException {
        final var event = createEvent();
        final var careProvider = createCareProvider();
        final var careUnit = createCareUnit();
        final var personInfo = createPersonInformation();

        doReturn(CERTIFICATE_TYPE_INTERNAL).when(moduleRegistry).getModuleIdFromExternalId(CERTIFICATE_TYPE_EXTERNAL);
        doReturn(moduleEntryPoint).when(moduleRegistry).getModuleEntryPoint(CERTIFICATE_TYPE_INTERNAL);
        doReturn(CERTIFICATE_TYPE_EXTERNAL).when(moduleEntryPoint).getExternalId();
        doReturn(CERTIFICATE_DISPLAY_NAME).when(moduleEntryPoint).getModuleName();
        doNothing().when(notificationPatientEnricher).enrichWithPatient(any(Intyg.class));

        certificateStatusUpdateForCareCreator.create(event, careProvider, careUnit, personInfo);

        verify(notificationPatientEnricher).enrichWithPatient(any(Intyg.class));
    }

    @Test
    public void shouldReturnXmlStringAfterCallToMarshalWithStatusUpdate() throws JAXBException {
        final var statusUpdate = new CertificateStatusUpdateForCareType();

        final var stringXml = certificateStatusUpdateForCareCreator.marshal(statusUpdate);

        assertTrue(stringXml.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"));
    }

    private Handelse createEvent() {
        final var event = new Handelse();
        event.setIntygsId(CERTIFICATE_ID);
        event.setCertificateType(CERTIFICATE_TYPE_EXTERNAL);
        event.setCertificateVersion(TEXT_VERSION);
        event.setVardgivarId(CARE_PROVIDER_ID);
        event.setEnhetsId(UNIT_ID);
        event.setCertificateIssuer((ISSUER_ID));
        event.setPersonnummer(PATIENT_ID);
        event.setId(EVENT_ID);
        event.setCode(EVENT_ENUM);
        event.setTimestamp(EVENT_TIMESTAMP);
        event.setHanteratAv(USER_ID);
        return event;
    }

    private NotificationMessage createNotificationMessage() {
        final var notificationMessage = new NotificationMessage();
        notificationMessage.setIntygsTyp(CERTIFICATE_TYPE_INTERNAL);
        notificationMessage.setIntygsId(CERTIFICATE_ID);
        notificationMessage.setUtkast(JsonNodeFactory.instance.textNode("UTKAST_JSON"));
        notificationMessage.setHandelse(EVENT_ENUM);
        notificationMessage.setHandelseTid(LocalDateTime.now());
        notificationMessage.setAmne(AmneskodCreator.create(SUBJECT_CODE, "Komplettering"));
        notificationMessage.setLogiskAdress(UNIT_ID);
        notificationMessage.setSistaSvarsDatum(LocalDate.now());
        notificationMessage.setSkickadeFragor(ArendeCount.getEmpty());
        notificationMessage.setMottagnaFragor(ArendeCount.getEmpty());
        return notificationMessage;
    }

    private Utlatande createUtlatande() {
        return LisjpUtlatandeV1.builder()
            .setId(CERTIFICATE_ID)
            .setTextVersion(TEXT_VERSION)
            .setGrundData(new GrundData())
            .build();
    }

    private Intyg createCertificateV3() {
        final var certificate = new Intyg();
        certificate.setIntygsId(NotificationRedeliveryUtil.getIIType(new IntygId(), CERTIFICATE_ID, UNIT_ID));
        certificate.setTyp(createCertificateType());
        certificate.setVersion(TEXT_VERSION);
        certificate.setPatient(NotificationRedeliveryUtil.getPatient(PATIENT_ID));
        certificate.setSkapadAv(NotificationRedeliveryUtil.getHosPersonal(createCareProvider(), createCareUnit(),
            createPersonInformation()));
        return certificate;
    }

    private TypAvIntyg createCertificateType() {
        TypAvIntyg certificateTypeV3 = new TypAvIntyg();
        certificateTypeV3.setCode(CERTIFICATE_TYPE_EXTERNAL);
        certificateTypeV3.setCodeSystem(KV_INTYGSTYP_CODE_SYSTEM);
        certificateTypeV3.setDisplayName(CERTIFICATE_DISPLAY_NAME);
        return certificateTypeV3;
    }

    private Vardgivare createCareProvider() {
        final var careProviderInfra = new se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare();
        careProviderInfra.setId(CARE_PROVIDER_ID);
        return careProviderInfra;
    }

    private Vardenhet createCareUnit() {
        final var careUnitInfra = new se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet();
        careUnitInfra.setId(UNIT_ID);
        return careUnitInfra;
    }

    private PersonInformation createPersonInformation() {
        final var personInformationType = new PersonInformation();
        personInformationType.setPersonHsaId(ISSUER_ID);
        return personInformationType;
    }
}
