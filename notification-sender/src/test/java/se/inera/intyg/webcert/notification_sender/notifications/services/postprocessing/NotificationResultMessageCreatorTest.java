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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static se.inera.intyg.common.support.Constants.HSA_ID_OID;
import static se.inera.intyg.common.support.Constants.KV_INTYGSTYP_CODE_SYSTEM;
import static se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationErrorTypeEnum.TECHNICAL_ERROR;
import static se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationErrorTypeEnum.WEBCERT_EXCEPTION;
import static se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationResultTypeEnum.ERROR;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.apache.commons.lang3.StringUtils;
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
import se.inera.intyg.infra.integration.hsatk.model.PersonInformation;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.common.service.notification.AmneskodCreator;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultMessage;
import se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationErrorTypeEnum;
import se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationResultTypeEnum;
import se.inera.intyg.webcert.notification_sender.notifications.services.v3.CertificateStatusUpdateForCareCreator;
import se.inera.intyg.webcert.notification_sender.notifications.util.NotificationRedeliveryUtil;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.fragasvar.model.Amne;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.TypAvIntyg;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.UnderskriftType;
import se.riv.clinicalprocess.healthcond.certificate.v3.Arenden;
import se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultType;

@RunWith(MockitoJUnitRunner.class)
public class NotificationResultMessageCreatorTest {

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @Mock
    private ModuleApi moduleApi;

    @Mock
    private ModuleEntryPoint moduleEntryPoint;

    @Spy
    CertificateStatusUpdateForCareCreator certificateStatusUpdateForCareCreator;

    @Spy
    private ObjectMapper objectMapper;

    @InjectMocks
    private NotificationResultMessageCreator notificationResultMessageCreator;

    private static final String CERTIFICATE_ID = "CERTIFICATE_ID";
    private static final String CERTIFICATE_TYPE_EXTERNAL = "LISJP";
    private static final String CERTIFICATE_TYPE_INTERNAL = "lisjp";
    private static final String CERTIFICATE_DISPLAY_NAME = "Läkarintyg för sjukpenning";
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

    private static final long EVENT_ID = 1000L;
    private static final byte[] STATUS_UPDATE_XML = "STATUS_UPDATE_XML".getBytes();

    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

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
        assertNull(notificationResultMessage.getStatusUpdateXml());

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
    public void shallReturnNotificationResultErrorOnTemporaryException() throws ModuleNotFoundException, IOException, ModuleException {
        final var notificationMessage = createNotificationMessage();

        doReturn(moduleApi).when(moduleRegistry).getModuleApi(notificationMessage.getIntygsTyp(), TEXT_VERSION);
        doReturn(createUtlatande()).when(moduleApi).getUtlatandeFromJson(notificationMessage.getUtkast());
        doReturn(moduleEntryPoint).when(moduleRegistry).getModuleEntryPoint(notificationMessage.getIntygsTyp());
        doReturn(CERTIFICATE_TYPE_EXTERNAL).when(moduleEntryPoint).getExternalId();

        final var actualNotificationResultMessage = notificationResultMessageCreator.createFailureMessage(notificationMessage,
            CORRELATION_ID, USER_ID, TEXT_VERSION, new TemporaryException("Temporary exception!"));

        assertEquals(ERROR, actualNotificationResultMessage.getResultType().getNotificationResult());
    }

    @Test
    public void shallAddANotificationDateTimeForFailureMessages() throws ModuleNotFoundException, IOException, ModuleException {
        final var notificationMessage = createNotificationMessage();

        doReturn(moduleApi).when(moduleRegistry).getModuleApi(notificationMessage.getIntygsTyp(), TEXT_VERSION);
        doReturn(createUtlatande()).when(moduleApi).getUtlatandeFromJson(notificationMessage.getUtkast());
        doReturn(moduleEntryPoint).when(moduleRegistry).getModuleEntryPoint(notificationMessage.getIntygsTyp());
        doReturn(CERTIFICATE_TYPE_EXTERNAL).when(moduleEntryPoint).getExternalId();

        final var actualNotificationResultMessage = notificationResultMessageCreator.createFailureMessage(notificationMessage,
            CORRELATION_ID, USER_ID, TEXT_VERSION, new TemporaryException("Temporary exception!"));

        assertNotNull("Must include a LocalDateTime for when it was (unsuccessfully) sent",
            actualNotificationResultMessage.getNotificationSentTime());
    }

    @Test
    public void testCreateResultMessage() {
        final var statusUpdate = createStatusUpdateForCareWithUnsignedCertificate();

        var notificationResultMessage = notificationResultMessageCreator.createResultMessage(statusUpdate,
            CORRELATION_ID, LOGICAL_ADDRESS);

        assertEquals(CORRELATION_ID, notificationResultMessage.getCorrelationId());
        assertNull(notificationResultMessage.getStatusUpdateXml());
        assertNotNull(notificationResultMessage.getNotificationSentTime());

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
    public void redeliveryMessageXmlShouldHaveCorrectDataWhenSignedCertificate() throws JsonProcessingException {
        final var statusUpdate = createStatusUpdateForCareWithSignedCertificate();
        final var notificationResultMessage = new NotificationResultMessage();
        final var resultTypeV3 = new ResultType();

        final var captureStatusUpdateXml = ArgumentCaptor.forClass(String.class);

        notificationResultMessageCreator.addToResultMessage(notificationResultMessage, statusUpdate, resultTypeV3);

        verify(objectMapper).writeValueAsBytes(captureStatusUpdateXml.capture());
        assertNotNull(captureStatusUpdateXml.getValue());
        assertTrue(captureStatusUpdateXml.getValue().startsWith(XML_HEADER));
        assertTrue(captureStatusUpdateXml.getValue().contains(CERTIFICATE_ID));
        assertTrue(captureStatusUpdateXml.getValue().contains(EVENT_ENUM.name()));
        assertTrue(captureStatusUpdateXml.getValue().contains(PATIENT_ID));
        assertTrue(captureStatusUpdateXml.getValue().contains(USER_ID));
    }

    @Test
    public void redeliveryMessageXmlShouldHaveCorrectDataWhenUnsignedCertificate() throws JsonProcessingException {
        final var statusUpdate = createStatusUpdateForCareWithUnsignedCertificate();
        final var notificationResultMessage = new NotificationResultMessage();
        final var resultTypeV3 = new ResultType();

        final var captureStatusUpdateXml = ArgumentCaptor.forClass(String.class);

        notificationResultMessageCreator.addToResultMessage(notificationResultMessage, statusUpdate, resultTypeV3);

        verify(objectMapper).writeValueAsBytes(captureStatusUpdateXml.capture());
        assertNotNull(captureStatusUpdateXml.getValue());
        assertTrue(captureStatusUpdateXml.getValue().startsWith(XML_HEADER));
        assertTrue(captureStatusUpdateXml.getValue().contains(CERTIFICATE_ID));
        assertTrue(captureStatusUpdateXml.getValue().contains(EVENT_ENUM.name()));
        assertTrue(captureStatusUpdateXml.getValue().contains(PATIENT_ID));
        assertTrue(captureStatusUpdateXml.getValue().contains(USER_ID));
    }

    @Test
    public void redeliveryMessageXmlShouldHaveProperlySetArenden() throws JsonProcessingException {
        final var statusUpdate = createStatusUpdateForCareWithArenden();
        final var notificationResultMessage = new NotificationResultMessage();
        final var resultTypeV3 = new ResultType();

        final var captureStatusUpdateXml = ArgumentCaptor.forClass(String.class);

        notificationResultMessageCreator.addToResultMessage(notificationResultMessage, statusUpdate, resultTypeV3);

        verify(objectMapper).writeValueAsBytes(captureStatusUpdateXml.capture());
        assertNotNull(captureStatusUpdateXml.getValue());
        assertTrue(captureStatusUpdateXml.getValue().startsWith(XML_HEADER));
        assertEquals(2, StringUtils.countMatches(captureStatusUpdateXml.getValue(), "besvarade>1</"));
        assertEquals(2, StringUtils.countMatches(captureStatusUpdateXml.getValue(), "ejBesvarade>2</"));
        assertEquals(2, StringUtils.countMatches(captureStatusUpdateXml.getValue(), "hanterade>3</"));
        assertEquals(2, StringUtils.countMatches(captureStatusUpdateXml.getValue(), "totalt>4</"));
    }

    @Test
    public void shouldAddResultTypeToNotificationReslutMessage() {
        final var statusUpdate = createStatusUpdateForCareWithSignedCertificate();
        final var notificationResultMessage = new NotificationResultMessage();
        final var resultTypeV3 = createNotificationResultType();

        notificationResultMessageCreator.addToResultMessage(notificationResultMessage, statusUpdate, resultTypeV3);

        assertEquals(RESULT_TYPE_ENUM, notificationResultMessage.getResultType().getNotificationResult());
        assertEquals(RESULT_ERROR_TYPE_ENUM, notificationResultMessage.getResultType().getNotificationErrorType());
        assertEquals(RESULT_TEXT, notificationResultMessage.getResultType().getNotificationResultText());
        assertNull(notificationResultMessage.getResultType().getException());
    }

    @Test
    public void shouldAddResultTypeToNotificationReslutMessageOnException() {
        final var statusUpdate = createStatusUpdateForCareWithUnsignedCertificate();
        final var notificationResultMessage = new NotificationResultMessage();

        notificationResultMessageCreator.addToResultMessage(notificationResultMessage, statusUpdate, EXCEPTION);

        assertEquals(ERROR, notificationResultMessage.getResultType().getNotificationResult());
        assertEquals(WEBCERT_EXCEPTION, notificationResultMessage.getResultType().getNotificationErrorType());
        assertEquals(EXCEPTION.getMessage(), notificationResultMessage.getResultType().getNotificationResultText());
        assertEquals(EXCEPTION.getClass().getName(), notificationResultMessage.getResultType().getException());
    }

    @Test
    public void shouldSetRedeliveryMessageToNullIfExceptionInBytesConversion() throws JsonProcessingException {
        final var statusUpdate = createStatusUpdateForCareWithUnsignedCertificate();
        final var resultTypeV3 = createNotificationResultType();
        final var notificationResultMessage = new NotificationResultMessage();
        final var notNullBytes = "NOT_NULL_FOR_TESTING".getBytes();
        notificationResultMessage.setStatusUpdateXml(notNullBytes);

        doThrow(JsonProcessingException.class).when(objectMapper).writeValueAsBytes(any(String.class));

        notificationResultMessageCreator.addToResultMessage(notificationResultMessage, statusUpdate, resultTypeV3);

        assertNull(notificationResultMessage.getStatusUpdateXml());
    }

    @Test
    public void shouldSetReResultTypeNormallyIfExceptionInBytesConversion() throws JsonProcessingException {
        final var statusUpdate = createStatusUpdateForCareWithUnsignedCertificate();
        final var resultTypeV3 = createNotificationResultType();
        final var notificationResultMessage = new NotificationResultMessage();
        final var notNullBytes = "NOT_NULL_FOR_TESTING".getBytes();
        notificationResultMessage.setStatusUpdateXml(notNullBytes);

        doThrow(JsonProcessingException.class).when(objectMapper).writeValueAsBytes(any(String.class));

        notificationResultMessageCreator.addToResultMessage(notificationResultMessage, statusUpdate, resultTypeV3);

        assertEquals(TECHNICAL_ERROR, notificationResultMessage.getResultType().getNotificationErrorType());
        assertEquals(ERROR, notificationResultMessage.getResultType().getNotificationResult());
        assertEquals(RESULT_TEXT, notificationResultMessage.getResultType().getNotificationResultText());
    }

    @Test
    public void shouldReturnResultMessageWithCorrectData() {
        final var event = createEvent();
        final var notificationRedelivery = createNotificationRedelivery();
        final var exception = new NullPointerException("EXCEPTION");

        final var resultMessage = notificationResultMessageCreator.createFailureMessage(event,
            notificationRedelivery, exception);

        assertEquals(event, resultMessage.getEvent());
        assertEquals(EVENT_ID, resultMessage.getEvent().getId().longValue());
        assertEquals(STATUS_UPDATE_XML, resultMessage.getStatusUpdateXml());
        assertEquals(CORRELATION_ID, resultMessage.getCorrelationId());
        assertNotNull(resultMessage.getNotificationSentTime());
    }

    @Test
    public void shouldReturnResultMessageWithCorrectResultType() {
        final var event = createEvent();
        final var notificationRedelivery = createNotificationRedelivery();
        final var exception = new NullPointerException("EXCEPTION");

        final var resultMessage = notificationResultMessageCreator.createFailureMessage(event,
            notificationRedelivery, exception);

        assertEquals("java.lang.NullPointerException", resultMessage.getResultType().getException());
        assertEquals("EXCEPTION", resultMessage.getResultType().getNotificationResultText());
        assertEquals(NotificationErrorTypeEnum.WEBCERT_EXCEPTION, resultMessage.getResultType().getNotificationErrorType());
    }

    @Test
    public void shouldReturnResultTypeUnrecoverableErrorOnWebcertServiceEception() {
        final var event = createEvent();
        final var notificationRedelivery = createNotificationRedelivery();
        final var exception = new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, "EXCEPTION");

        final var resultMessage = notificationResultMessageCreator.createFailureMessage(event,
            notificationRedelivery, exception);

        assertEquals(NotificationResultTypeEnum.UNRECOVERABLE_ERROR, resultMessage.getResultType().getNotificationResult());
    }

    @Test
    public void shouldReturnResultTypeErrorOnOtherThanWebcertServiceEception() {
        final var event = createEvent();
        final var notificationRedelivery = createNotificationRedelivery();
        final var exception = new NullPointerException("EXCEPTION");

        final var resultMessage = notificationResultMessageCreator.createFailureMessage(event,
            notificationRedelivery, exception);

        assertEquals(NotificationResultTypeEnum.ERROR, resultMessage.getResultType().getNotificationResult());
    }

    private Handelse createEvent() {
        final var event = new Handelse();
        event.setId(EVENT_ID);
        event.setCode(HandelsekodEnum.SKAPAT);
        event.setIntygsId("INTYGS_ID");
        event.setEnhetsId("ENHETS_ID");
        event.setDeliveryStatus(NotificationDeliveryStatusEnum.SUCCESS);
        return event;
    }

    private NotificationRedelivery createNotificationRedelivery() {
        final var notificationRedelivery = new NotificationRedelivery();
        notificationRedelivery.setCorrelationId(CORRELATION_ID);
        notificationRedelivery.setEventId(1000L);
        notificationRedelivery.setMessage(STATUS_UPDATE_XML);
        notificationRedelivery.setRedeliveryTime(LocalDateTime.now());
        return notificationRedelivery;
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
        certificate.setTyp(createCertificateType());
        certificate.setVersion(TEXT_VERSION);
        certificate.setPatient(NotificationRedeliveryUtil.getPatient(PATIENT_ID));
        certificate.setSkapadAv(NotificationRedeliveryUtil.getHosPersonal(createCareProviderInfra(), createCareUnitInfra(),
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

    private se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare createCareProviderInfra() {
        final var careProviderInfra = new se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare();
        careProviderInfra.setId(CARE_PROVIDER_ID);
        return careProviderInfra;
    }

    private se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet createCareUnitInfra() {
        final var careUnitInfra = new se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet();
        careUnitInfra.setId(LOGICAL_ADDRESS);
        return careUnitInfra;
    }

    private PersonInformation createPersonInformation() {
        final var personInformationType = new PersonInformation();
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
