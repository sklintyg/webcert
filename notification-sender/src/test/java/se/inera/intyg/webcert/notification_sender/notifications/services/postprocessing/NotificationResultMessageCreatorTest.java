/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static se.inera.intyg.common.support.Constants.HSA_ID_OID;
import static se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationErrorTypeEnum.TECHNICAL_ERROR;
import static se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationErrorTypeEnum.WEBCERT_EXCEPTION;
import static se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationResultTypeEnum.ERROR;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.w3._2000._09.xmldsig_.SignatureType;
import se.inera.intyg.common.lisjp.v1.model.internal.LisjpUtlatandeV1;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.ModuleEntryPoint;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.common.service.notification.AmneskodCreator;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationRedeliveryMessage;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultMessage;
import se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationErrorTypeEnum;
import se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationResultTypeEnum;
import se.inera.intyg.webcert.notification_sender.notifications.util.NotificationRedeliveryUtil;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.fragasvar.model.Amne;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.UnderskriftType;
import se.riv.clinicalprocess.healthcond.certificate.v3.Arenden;
import se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultType;
import se.riv.infrastructure.directory.v1.PersonInformationType;

@RunWith(MockitoJUnitRunner.class)
public class NotificationResultMessageCreatorTest {

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @Mock
    private ModuleApi moduleApi;

    @Mock
    private ModuleEntryPoint moduleEntryPoint;

    @Spy
    private ObjectMapper objectMapper;

    @InjectMocks
    private NotificationResultMessageCreator notificationResultMessageCreator;

    private static final String CERTIFICATE_ID = "CERTIFICATE_ID";
    private static final String CERTIFICATE_TYPE_EXTERNAL = "LISJP";
    private static final String CERTIFICATE_TYPE_INTERNAL = "lisjp";
    private static final String TEXT_VERSION = "TEXT_VERSION";

    private static final HandelsekodEnum EVENT_ENUM = HandelsekodEnum.NYFRFM;
    private static final String PATIENT_ID = "191212121212";
    private static final String CARE_PROVIDER_ID = "CARE_PROVIDER_ID";
    private static final String USER_ID = "USER_ID";
    private static final String ISSUER_ID = "ISSUER_ID";

    private static final String SUBJECT_CODE = "KOMPLT";
    private static final Exception EXCEPTION = new NullPointerException("NullPointerException");

    private static final String LOGICAL_ADDRESS = "LOGICAL_ADDRESS";
    private static final String CORRELATION_ID = "CORRELATION_ID";

    private static final NotificationResultTypeEnum RESULT_TYPE_ENUM = ERROR;
    private static final NotificationErrorTypeEnum RESULT_ERROR_TYPE_ENUM = TECHNICAL_ERROR;
    private static final String RESULT_TEXT = "TECHNICAL_ERROR_TEXT";

    @Test
    public void testCreateFailureMessage() throws ModuleNotFoundException, IOException, ModuleException {
        final var notificationMessage = createNotificationMessage();

        doReturn(moduleApi).when(moduleRegistry).getModuleApi(notificationMessage.getIntygsTyp(), TEXT_VERSION);
        doReturn(createUtlatande()).when(moduleApi).getUtlatandeFromJson(notificationMessage.getUtkast());
        doReturn(moduleEntryPoint).when(moduleRegistry).getModuleEntryPoint(notificationMessage.getIntygsTyp());
        doReturn(CERTIFICATE_TYPE_EXTERNAL).when(moduleEntryPoint).getExternalId();

        var notificationResultMessage = notificationResultMessageCreator.createFailureMessage(notificationMessage,
            CORRELATION_ID, USER_ID, TEXT_VERSION, EXCEPTION);

        assertEquals(CORRELATION_ID, notificationResultMessage.getCorrelationId());
        assertNull(notificationResultMessage.getRedeliveryMessageBytes());

        assertEquals(EXCEPTION.getClass().getName(), notificationResultMessage.getResultType().getException());
        assertEquals(EXCEPTION.getMessage(), notificationResultMessage.getResultType().getNotificationResultText());
        assertEquals(NotificationResultTypeEnum.UNRECOVERABLE_ERROR, notificationResultMessage.getResultType().getNotificationResult());
        assertEquals(NotificationErrorTypeEnum.WEBCERT_EXCEPTION, notificationResultMessage.getResultType().getNotificationErrorType());

        assertNull(notificationResultMessage.getEvent().getId());
        assertEquals(CERTIFICATE_ID, notificationResultMessage.getEvent().getIntygsId());
        assertEquals(CERTIFICATE_TYPE_EXTERNAL, notificationResultMessage.getEvent().getCertificateType());
        assertEquals(TEXT_VERSION, notificationResultMessage.getEvent().getCertificateVersion());
        assertEquals(EVENT_ENUM, notificationResultMessage.getEvent().getCode());
        assertEquals(CARE_PROVIDER_ID, notificationResultMessage.getEvent().getVardgivarId());
        assertEquals(LOGICAL_ADDRESS, notificationResultMessage.getEvent().getEnhetsId());
        assertEquals(PATIENT_ID, notificationResultMessage.getEvent().getPersonnummer());
        assertEquals(ISSUER_ID, notificationResultMessage.getEvent().getCertificateIssuer());
        assertEquals(SUBJECT_CODE, notificationResultMessage.getEvent().getAmne().name());
        assertEquals(USER_ID, notificationResultMessage.getEvent().getHanteratAv());
    }

    @Test
    public void testCreateResultMessage() {
        final var statusUpdate = createStatusUpdateForCareWithUnsignedCertificate();

        var notificationResultMessage = notificationResultMessageCreator.createResultMessage(statusUpdate,
            CORRELATION_ID);

        assertEquals(CORRELATION_ID, notificationResultMessage.getCorrelationId());
        assertNotNull(notificationResultMessage.getRedeliveryMessageBytes());

        assertNull(notificationResultMessage.getEvent().getId());
        assertEquals(CERTIFICATE_ID, notificationResultMessage.getEvent().getIntygsId());
        assertEquals(CERTIFICATE_TYPE_EXTERNAL, notificationResultMessage.getEvent().getCertificateType());
        assertEquals(TEXT_VERSION, notificationResultMessage.getEvent().getCertificateVersion());
        assertEquals(EVENT_ENUM, notificationResultMessage.getEvent().getCode());
        assertEquals(CARE_PROVIDER_ID, notificationResultMessage.getEvent().getVardgivarId());
        assertEquals(LOGICAL_ADDRESS, notificationResultMessage.getEvent().getEnhetsId());
        assertEquals(PATIENT_ID, notificationResultMessage.getEvent().getPersonnummer());
        assertEquals(ISSUER_ID, notificationResultMessage.getEvent().getCertificateIssuer());
        assertEquals(SUBJECT_CODE, notificationResultMessage.getEvent().getAmne().name());
        assertEquals(USER_ID, notificationResultMessage.getEvent().getHanteratAv());
    }

    @Test
    public void shouldHaveProperRedeliveryMessageWhenSignedCertificate() throws JsonProcessingException {
        final var statusUpdate = createStatusUpdateForCareWithSignedCertificate();

        final var captureRedeliveryMessage = ArgumentCaptor.forClass(NotificationRedeliveryMessage.class);

        notificationResultMessageCreator.createResultMessage(statusUpdate, CORRELATION_ID);

        verify(objectMapper).writeValueAsBytes(captureRedeliveryMessage.capture());
        assertNull(captureRedeliveryMessage.getValue().getCert());
        assertFalse(captureRedeliveryMessage.getValue().hasCertificate());
        assertNotNull(captureRedeliveryMessage.getValue().getSent());
        assertNotNull(captureRedeliveryMessage.getValue().getReceived());
        assertEquals(PATIENT_ID, captureRedeliveryMessage.getValue().getPatient().getPersonId().getExtension());
    }

    @Test
    public void shouldHaveProperRedeliveryMessageWhenUnsignedCertificate() throws JsonProcessingException {
        final var statusUpdate = createStatusUpdateForCareWithUnsignedCertificate();

        final var captureRedeliveryMessage = ArgumentCaptor.forClass(NotificationRedeliveryMessage.class);

        notificationResultMessageCreator.createResultMessage(statusUpdate, CORRELATION_ID);

        verify(objectMapper).writeValueAsBytes(captureRedeliveryMessage.capture());
        assertNull(captureRedeliveryMessage.getValue().getPatient());
        assertTrue(captureRedeliveryMessage.getValue().hasCertificate());
        assertNotNull(captureRedeliveryMessage.getValue().getSent());
        assertNotNull(captureRedeliveryMessage.getValue().getReceived());
        assertEquals(CERTIFICATE_ID, captureRedeliveryMessage.getValue().getCert().getIntygsId().getExtension());
    }

    @Test
    public void redeliveryMessageShouldHaveProperlySetArenden() throws JsonProcessingException {
        final var statusUpdate = createStatusUpdateForCareWithArenden();

        final var captureRedeliveryMessage = ArgumentCaptor.forClass(NotificationRedeliveryMessage.class);

        notificationResultMessageCreator.createResultMessage(statusUpdate, CORRELATION_ID);

        verify(objectMapper).writeValueAsBytes(captureRedeliveryMessage.capture());
        assertEquals(1, captureRedeliveryMessage.getValue().getSent().getAnswered());
        assertEquals(2, captureRedeliveryMessage.getValue().getSent().getUnanswered());
        assertEquals(3, captureRedeliveryMessage.getValue().getSent().getHandled());
        assertEquals(4, captureRedeliveryMessage.getValue().getSent().getTotal());

        assertEquals(1, captureRedeliveryMessage.getValue().getReceived().getAnswered());
        assertEquals(2, captureRedeliveryMessage.getValue().getReceived().getUnanswered());
        assertEquals(3, captureRedeliveryMessage.getValue().getReceived().getHandled());
        assertEquals(4, captureRedeliveryMessage.getValue().getReceived().getTotal());
    }

    @Test
    public void shouldProperlyAddResultTypeToNotificationReslutMessage() {
        final var notificationResultMessage = new NotificationResultMessage();
        final var resultTypeV3 = createNotificationResultType();

        notificationResultMessageCreator.addToResultMessage(notificationResultMessage, resultTypeV3);

        assertEquals(RESULT_TYPE_ENUM, notificationResultMessage.getResultType().getNotificationResult());
        assertEquals(RESULT_ERROR_TYPE_ENUM, notificationResultMessage.getResultType().getNotificationErrorType());
        assertEquals(RESULT_TEXT, notificationResultMessage.getResultType().getNotificationResultText());
        assertNull(notificationResultMessage.getResultType().getException());
    }

    @Test
    public void shouldProperlyAddResultTypeToNotificationReslutMessageOnException() {
        final var notificationResultMessage = new NotificationResultMessage();

        notificationResultMessageCreator.addToResultMessage(notificationResultMessage, EXCEPTION);

        assertEquals(ERROR, notificationResultMessage.getResultType().getNotificationResult());
        assertEquals(WEBCERT_EXCEPTION, notificationResultMessage.getResultType().getNotificationErrorType());
        assertEquals(EXCEPTION.getMessage(), notificationResultMessage.getResultType().getNotificationResultText());
        assertEquals(EXCEPTION.getClass().getName(), notificationResultMessage.getResultType().getException());
    }

    @Test (expected = WebCertServiceException.class)
    public void shouldFailWhenJsonProcessingException() throws JsonProcessingException {
        final var statusUpdate = createStatusUpdateForCareWithUnsignedCertificate();

        doThrow(JsonProcessingException.class).when(objectMapper).writeValueAsBytes(any(NotificationRedeliveryMessage.class));

        notificationResultMessageCreator.createResultMessage(statusUpdate, CORRELATION_ID);
    }

    private NotificationMessage createNotificationMessage() {
        final var notificationMessage = new NotificationMessage();
        notificationMessage.setIntygsTyp(CERTIFICATE_TYPE_INTERNAL);
        notificationMessage.setIntygsId(CERTIFICATE_ID);
        notificationMessage.setUtkast(JsonNodeFactory.instance.textNode("UTKAST_JSON"));
        notificationMessage.setHandelse(EVENT_ENUM);
        notificationMessage.setHandelseTid(LocalDateTime.now());
        notificationMessage.setAmne(AmneskodCreator.create(SUBJECT_CODE, "Komplettering"));
        notificationMessage.setLogiskAdress(LOGICAL_ADDRESS);
        notificationMessage.setSistaSvarsDatum(LocalDate.now());
        return notificationMessage;
    }

    private Utlatande createUtlatande() {
        return LisjpUtlatandeV1.builder()
            .setId(CERTIFICATE_ID)
            .setTextVersion(TEXT_VERSION)
            .setGrundData(createGrundData())
            .build();
    }

    private GrundData createGrundData() {
        final var grundData = new GrundData();
        grundData.setPatient(createPatient());
        grundData.setSkapadAv(createHoSPersonal());
        return grundData;
    }

    private HoSPersonal createHoSPersonal() {
        HoSPersonal hosPersonal = new HoSPersonal();
        hosPersonal.setPersonId(ISSUER_ID);
        hosPersonal.setVardenhet(createCareUnit());
        return hosPersonal;
    }

    private Vardenhet createCareUnit() {
        final var careUnit = new Vardenhet();
        careUnit.setVardgivare(createCareProvider());
        return careUnit;
    }

    private Vardgivare createCareProvider() {
        final var careProvider = new Vardgivare();
        careProvider.setVardgivarid(CARE_PROVIDER_ID);
        return careProvider;
    }

    private Patient createPatient() {
        final var patient = new Patient();
        patient.setPersonId(Personnummer.createPersonnummer(PATIENT_ID).orElse(null));
        return patient;
    }

    private CertificateStatusUpdateForCareType createStatusUpdateForCareWithSignedCertificate() {
        final var statusUpdate = createStatusUpdateForCareWithUnsignedCertificate();
        final var underskriftType = new UnderskriftType();
        underskriftType.setSignature(new SignatureType());
        statusUpdate.getIntyg().setUnderskrift(underskriftType);
        statusUpdate.getIntyg().setSigneringstidpunkt(LocalDateTime.now());
        return statusUpdate;
    }

    private CertificateStatusUpdateForCareType createStatusUpdateForCareWithArenden() {
        final var statusUpdate = createStatusUpdateForCareWithUnsignedCertificate();
        statusUpdate.setSkickadeFragor(createArendenV3());
        statusUpdate.setMottagnaFragor(createArendenV3());
        return statusUpdate;
    }

    private CertificateStatusUpdateForCareType createStatusUpdateForCareWithUnsignedCertificate() {
        final var statusUpdate = new CertificateStatusUpdateForCareType();
        statusUpdate.setIntyg(createCertificateV3());
        statusUpdate.setHandelse(NotificationRedeliveryUtil.getEventV3(EVENT_ENUM, LocalDateTime.now(), ArendeAmne.fromAmne(
            Amne.KOMPLETTERING_AV_LAKARINTYG).orElse(null), LocalDate.now()));
        statusUpdate.setHanteratAv(NotificationRedeliveryUtil.getIIType(new HsaId(), USER_ID, HSA_ID_OID));
        statusUpdate.setRef("REFERENCE");
        return statusUpdate;
    }

    private Intyg createCertificateV3() {
        final var certificate = new Intyg();
        certificate.setIntygsId(NotificationRedeliveryUtil.getIIType(new IntygId(), CERTIFICATE_ID, LOGICAL_ADDRESS));
        certificate.setTyp(NotificationRedeliveryUtil.getCertificateType(CERTIFICATE_TYPE_EXTERNAL));
        certificate.setVersion(TEXT_VERSION);
        certificate.setPatient(NotificationRedeliveryUtil.getPatient(PATIENT_ID));
        certificate.setSkapadAv(NotificationRedeliveryUtil.getHosPersonal(createCareProviderInfra(), createCareUnitInfra(),
            createPersonInformationType()));
        return certificate;
    }

    private se.inera.intyg.infra.integration.hsa.model.Vardgivare createCareProviderInfra() {
        final var careProviderInfra = new se.inera.intyg.infra.integration.hsa.model.Vardgivare();
        careProviderInfra.setId(CARE_PROVIDER_ID);
        return careProviderInfra;
    }

    private se.inera.intyg.infra.integration.hsa.model.Vardenhet createCareUnitInfra() {
        final var careUnitInfra = new se.inera.intyg.infra.integration.hsa.model.Vardenhet();
        careUnitInfra.setId(LOGICAL_ADDRESS);
        return careUnitInfra;
    }

    private PersonInformationType createPersonInformationType() {
        final var personInformationType = new PersonInformationType();
        personInformationType.setPersonHsaId(ISSUER_ID);
        return personInformationType;
    }

    private ResultType createNotificationResultType() {
        final var resultTypeV3 = new ResultType();
        resultTypeV3.setResultCode(ResultCodeType.ERROR);
        resultTypeV3.setErrorId(ErrorIdType.TECHNICAL_ERROR);
        resultTypeV3.setResultText(RESULT_TEXT);
        return resultTypeV3;
    }

    private Arenden createArendenV3() {
        final var arendenV3 = new Arenden();
        arendenV3.setBesvarade(1);
        arendenV3.setEjBesvarade(2);
        arendenV3.setHanterade(3);
        arendenV3.setTotalt(4);
        return arendenV3;
    }
}
