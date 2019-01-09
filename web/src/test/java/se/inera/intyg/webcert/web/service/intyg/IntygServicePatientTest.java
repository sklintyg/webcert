/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.intyg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.WebServiceException;

import org.apache.commons.io.IOUtils;
import org.apache.cxf.helpers.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.core.io.ClassPathResource;

import com.fasterxml.jackson.databind.ObjectMapper;

import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateMetaData;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateResponse;
import se.inera.intyg.common.ts_bas.model.internal.TsBasUtlatande;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.persistence.utkast.model.Signatur;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.converter.IntygDraftsConverter;
import se.inera.intyg.webcert.web.service.intyg.converter.IntygModuleFacade;
import se.inera.intyg.webcert.web.service.intyg.decorator.IntygRelationHelper;
import se.inera.intyg.webcert.web.service.intyg.decorator.UtkastIntygDecorator;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.relation.CertificateRelationService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v3.ListCertificatesForCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v3.ListCertificatesForCareResponseType;

@RunWith(MockitoJUnitRunner.class)
public class IntygServicePatientTest {

    private static final String HSA_ID = "HSA-123";
    private static final String CREATED_BY_NAME = "Läkare Läkarsson";
    private static final String CERTIFICATE_ID = "with-address";
    private static final String CERTIFICATE_TYPE = "ts-bas";
    private static final String LOGICAL_ADDRESS = "<logicalAddress>";
    private static final String PERSON_ID = "19121212-1212";
    private static final String PATIENT_JSON_FORMATTER = "{\"grundData\": { \"patient\": {\"%s\": \"%s\"}}}";

    private static final Personnummer PERSNR = Personnummer.createPersonnummer(PERSON_ID).get();

    private ListCertificatesForCareResponseType listResponse;
    private VardpersonReferens vardpersonReferens;

    private String json;

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @Mock
    private ModuleApi moduleApi;

    @Mock
    private ListCertificatesForCareResponderInterface listCertificatesForCareResponder;

    @Mock
    private IntygRelationHelper intygRelationHelper;

    @Mock
    private IntygModuleFacade moduleFacade;

    @Mock
    private UtkastRepository utkastRepository;

    @Mock
    private LogService logservice;

    @Mock
    private WebCertUser webcertUser;

    @Mock
    private WebCertUserService webCertUserService;

    @Mock
    private MonitoringLogService mockMonitoringservice;

    @Mock
    private CertificateRelationService certificateRelationService;

    @Mock
    private PatientDetailsResolver patientDetailsResolver;

    @Mock
    private UtkastIntygDecorator utkastIntygDecorator;

    @Spy
    private ObjectMapper objectMapper = new CustomObjectMapper();

    @InjectMocks
    private IntygDraftsConverter intygConverter = new IntygDraftsConverter();

    @InjectMocks
    private IntygServiceImpl intygService;

    @Before
    public void setupIntygstjanstResponse() throws Exception {
        vardpersonReferens = new VardpersonReferens();
        vardpersonReferens.setHsaId(HSA_ID);
        vardpersonReferens.setNamn(CREATED_BY_NAME);

        json = FileUtils.getStringFromFile(new ClassPathResource("IntygServiceTest/utlatande-address.json").getFile());
        TsBasUtlatande utlatande = objectMapper.readValue(json, TsBasUtlatande.class);

        CertificateResponse certificateResponse = new CertificateResponse(json, utlatande, buildCertificateMetaData(), false);
        when(moduleFacade.getCertificate(any(String.class), any(String.class))).thenReturn(certificateResponse);
        when(moduleFacade.getUtlatandeFromInternalModel(anyString(),anyString())).thenReturn(utlatande);
    }

    @Before
    public void setupIntygstjanstListResponse() throws Exception {
        ClassPathResource response = new ClassPathResource("IntygServiceTest/response-list-certificates.xml");

        JAXBContext context = JAXBContext.newInstance(ListCertificatesForCareResponseType.class);
        listResponse = context.createUnmarshaller()
                .unmarshal(new StreamSource(response.getInputStream()), ListCertificatesForCareResponseType.class)
                .getValue();

        when(intygRelationHelper.getRelationsForIntyg(anyString())).thenReturn(new Relations());
        when(certificateRelationService.getRelations(anyString())).thenReturn(new Relations());
    }

    @Before
    public void setupDefaultAuthorization() {
        Set<String> set = new HashSet<>();
        set.add("ts-bas");

        when(webCertUserService.getUser()).thenReturn(webcertUser);
        when(webcertUser.getOrigin()).thenReturn(UserOriginType.NORMAL.name());
        when(webCertUserService.isAuthorizedForUnit(any(String.class), any(String.class), eq(true))).thenReturn(true);
    }

    @Before
    public void setupLogicalAddress() {
        intygService.setLogicalAddress(LOGICAL_ADDRESS);
    }

    @Before
    public void IntygServiceConverter() throws Exception {
        when(moduleRegistry.getModuleApi(any(String.class))).thenReturn(moduleApi);
        json = FileUtils.getStringFromFile(new ClassPathResource("IntygServiceTest/utlatande-address.json").getFile());
        TsBasUtlatande utlatande = objectMapper.readValue(json, TsBasUtlatande.class);
        when(moduleApi.getUtlatandeFromJson(anyString())).thenReturn(utlatande);

        // use reflection to set IntygDraftsConverter in IntygService
        Field field = IntygServiceImpl.class.getDeclaredField("intygConverter");
        field.setAccessible(true);
        field.set(intygService, intygConverter);
    }

    @Before
    public void setupPatientDetailsResolver() {
        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.FALSE);
        when(patientDetailsResolver.isPatientAddressChanged(any(Patient.class), any(Patient.class))).thenReturn(false);
        when(patientDetailsResolver.isPatientNamedChanged(any(Patient.class), any(Patient.class))).thenReturn(false);
    }

    @Test
    public void testThatCompletePatientAddressIsUsed() throws Exception {
        // Given
        Patient patient = buildPatient(false, false);
        when(patientDetailsResolver.resolvePatient(any(Personnummer.class), anyString())).thenReturn(patient);

        // When
        IntygContentHolder intygData = intygService.fetchIntygDataWithRelations(CERTIFICATE_ID, CERTIFICATE_TYPE,false);
        Patient oldPatient = intygData.getUtlatande().getGrundData().getPatient();

        // Then
        assertFalse("isPatientAddressChangedInPU should be false",intygData.isPatientAddressChangedInPU());
        assertFalse("isPatientNameChangedInPU should be false",intygData.isPatientNameChangedInPU());
        assertEquals(patient.getPostadress(), oldPatient.getPostadress());
        assertEquals(patient.getPostort(), oldPatient.getPostort());
        assertEquals(patient.getPostnummer(), oldPatient.getPostnummer());
        JSONAssert.assertEquals(String.format(PATIENT_JSON_FORMATTER,"postadress",patient.getPostadress()),intygData.getContents(), JSONCompareMode.LENIENT);
        JSONAssert.assertEquals(String.format(PATIENT_JSON_FORMATTER,"postort",patient.getPostort()),intygData.getContents(), JSONCompareMode.LENIENT);
        JSONAssert.assertEquals(String.format(PATIENT_JSON_FORMATTER,"postnummer",patient.getPostnummer()),intygData.getContents(), JSONCompareMode.LENIENT);
    }

    @Test
    public void testThatPatientAddressIsChangedInPu() throws Exception {
        // Given
        Patient patient = buildPatient(false, false);
        patient.setPostadress("NyGatuAdress");
        when(patientDetailsResolver.resolvePatient(any(Personnummer.class), anyString())).thenReturn(patient);
        when(patientDetailsResolver.isPatientAddressChanged(any(Patient.class), any(Patient.class))).thenReturn(true);

        // When
        IntygContentHolder intygData = intygService.fetchIntygData(CERTIFICATE_ID,CERTIFICATE_TYPE, false);
        Patient oldPatient = intygData.getUtlatande().getGrundData().getPatient();

        // Then
        assertTrue("isPatientAddressChangedInPU should be true",intygData.isPatientAddressChangedInPU());
        assertFalse("isPatientNameChangedInPU should be false",intygData.isPatientNameChangedInPU());
        assertNotEquals(patient.getPostadress(), oldPatient.getPostadress());
        assertEquals(patient.getPostort(), oldPatient.getPostort());
        assertEquals(patient.getPostnummer(), oldPatient.getPostnummer());
        JSONAssert.assertNotEquals(String.format(PATIENT_JSON_FORMATTER,"postadress",patient.getPostadress()),intygData.getContents(), JSONCompareMode.LENIENT);
        JSONAssert.assertEquals(String.format(PATIENT_JSON_FORMATTER,"postort",patient.getPostort()),intygData.getContents(), JSONCompareMode.LENIENT);
        JSONAssert.assertEquals(String.format(PATIENT_JSON_FORMATTER,"postnummer",patient.getPostnummer()),intygData.getContents(), JSONCompareMode.LENIENT);
    }

    @Test
    public void testThatPatientNameIsChangedInPu() throws Exception {
        // Given
        Patient patient = buildPatient(false, false);
        patient.setEfternamn("Nygift");
        when(patientDetailsResolver.resolvePatient(any(Personnummer.class), anyString())).thenReturn(patient);
        when(patientDetailsResolver.isPatientNamedChanged(any(Patient.class), any(Patient.class))).thenReturn(true);

        // When
        IntygContentHolder intygData = intygService.fetchIntygData(CERTIFICATE_ID,CERTIFICATE_TYPE, false);
        Patient oldPatient = intygData.getUtlatande().getGrundData().getPatient();

        // Then
        assertFalse("isPatientAddressChangedInPU should be false",intygData.isPatientAddressChangedInPU());
        assertTrue("isPatientNameChangedInPU should be true",intygData.isPatientNameChangedInPU());
        assertEquals(patient.getFornamn(), oldPatient.getFornamn());
        assertNotEquals(patient.getEfternamn(), oldPatient.getEfternamn());
        JSONAssert.assertEquals(String.format(PATIENT_JSON_FORMATTER,"fornamn",patient.getFornamn()),intygData.getContents(), JSONCompareMode.LENIENT);
        JSONAssert.assertNotEquals(String.format(PATIENT_JSON_FORMATTER,"efternamn",patient.getEfternamn()),intygData.getContents(), JSONCompareMode.LENIENT);
    }

    @Test
    public void testThatCompletePatientAddressIsUsedWhenIntygtjanstIsUnavailable() throws Exception {
        // Given
        when(moduleFacade.getCertificate(CERTIFICATE_ID, CERTIFICATE_TYPE)).thenThrow(new WebServiceException());
        when(utkastRepository.findByIntygsIdAndIntygsTyp(CERTIFICATE_ID, CERTIFICATE_TYPE)).thenReturn(getIntyg(CERTIFICATE_ID,
                LocalDateTime.now(), null));

        Patient patient = buildPatient(false, false);
        when(patientDetailsResolver.resolvePatient(any(Personnummer.class), anyString())).thenReturn(patient);

        // When
        IntygContentHolder intygData = intygService.fetchIntygData(CERTIFICATE_ID, CERTIFICATE_TYPE, false);
        Patient oldPatient = intygData.getUtlatande().getGrundData().getPatient();

        // Then
        assertFalse("isPatientAddressChangedInPU should be false",intygData.isPatientAddressChangedInPU());
        assertFalse("isPatientNameChangedInPU should be false",intygData.isPatientNameChangedInPU());
        assertEquals(patient.getPostadress(), oldPatient.getPostadress());
        assertEquals(patient.getPostort(), oldPatient.getPostort());
        assertEquals(patient.getPostnummer(), oldPatient.getPostnummer());
        JSONAssert.assertEquals(String.format(PATIENT_JSON_FORMATTER,"postadress",patient.getPostadress()),intygData.getContents(), JSONCompareMode.LENIENT);
        JSONAssert.assertEquals(String.format(PATIENT_JSON_FORMATTER,"postort",patient.getPostort()),intygData.getContents(), JSONCompareMode.LENIENT);
        JSONAssert.assertEquals(String.format(PATIENT_JSON_FORMATTER,"postnummer",patient.getPostnummer()),intygData.getContents(), JSONCompareMode.LENIENT);
    }

    @Test
    public void testThatPatientAddressIsChangedInPuWhenIntygtjanstIsUnavailable() throws Exception {
        // Given
        when(moduleFacade.getCertificate(anyString(), anyString())).thenThrow(new WebServiceException());
        when(utkastRepository.findByIntygsIdAndIntygsTyp(anyString(), anyString())).thenReturn(getIntyg(CERTIFICATE_ID,
                LocalDateTime.now(), null));

        Patient patient = buildPatient(false, false);
        patient.setPostadress("NyGatuAdress");
        when(patientDetailsResolver.resolvePatient(any(Personnummer.class), anyString())).thenReturn(patient);
        when(patientDetailsResolver.isPatientAddressChanged(any(Patient.class), any(Patient.class))).thenReturn(true);

        // When
        IntygContentHolder intygData = intygService.fetchIntygData(CERTIFICATE_ID, CERTIFICATE_TYPE, false);
        Patient oldPatient = intygData.getUtlatande().getGrundData().getPatient();

        // Then
        assertTrue("isPatientAddressChangedInPU should be true",intygData.isPatientAddressChangedInPU());
        assertFalse("isPatientNameChangedInPU should be false",intygData.isPatientNameChangedInPU());
        assertNotEquals(patient.getPostadress(), oldPatient.getPostadress());
        assertEquals(patient.getPostort(), oldPatient.getPostort());
        assertEquals(patient.getPostnummer(), oldPatient.getPostnummer());
        JSONAssert.assertNotEquals(String.format(PATIENT_JSON_FORMATTER,"postadress",patient.getPostadress()),intygData.getContents(), JSONCompareMode.LENIENT);
        JSONAssert.assertEquals(String.format(PATIENT_JSON_FORMATTER,"postort",patient.getPostort()),intygData.getContents(), JSONCompareMode.LENIENT);
        JSONAssert.assertEquals(String.format(PATIENT_JSON_FORMATTER,"postnummer",patient.getPostnummer()),intygData.getContents(), JSONCompareMode.LENIENT);
    }

    @Test
    public void testThatPatientNameIsChangedInPuWhenIntygtjanstIsUnavailable() throws Exception {
        // Given
        when(moduleFacade.getCertificate(anyString(), anyString())).thenThrow(new WebServiceException());
        when(utkastRepository.findByIntygsIdAndIntygsTyp(anyString(), anyString())).thenReturn(getIntyg(CERTIFICATE_ID,
                LocalDateTime.now(), null));

        Patient patient = buildPatient(false, false);
        patient.setEfternamn("NyGift");
        when(patientDetailsResolver.resolvePatient(any(Personnummer.class), anyString())).thenReturn(patient);
        when(patientDetailsResolver.isPatientNamedChanged(any(Patient.class), any(Patient.class))).thenReturn(true);

        // When
        IntygContentHolder intygData = intygService.fetchIntygData(CERTIFICATE_ID, CERTIFICATE_TYPE, false);
        Patient oldPatient = intygData.getUtlatande().getGrundData().getPatient();

        // Then
        assertFalse("isPatientAddressChangedInPU should be false",intygData.isPatientAddressChangedInPU());
        assertTrue("isPatientNameChangedInPU should be true",intygData.isPatientNameChangedInPU());
        assertEquals(patient.getFornamn(), oldPatient.getFornamn());
        assertNotEquals(patient.getEfternamn(), oldPatient.getEfternamn());
        JSONAssert.assertEquals(String.format(PATIENT_JSON_FORMATTER,"fornamn",patient.getFornamn()),intygData.getContents(), JSONCompareMode.LENIENT);
        JSONAssert.assertNotEquals(String.format(PATIENT_JSON_FORMATTER,"efternamn",patient.getEfternamn()),intygData.getContents(), JSONCompareMode.LENIENT);
    }

    private Utkast getIntyg(String intygsId, LocalDateTime sendDate, LocalDateTime revokeDate) throws IOException {
        String json = IOUtils.toString(new ClassPathResource(
                "IntygServiceTest/utlatande-address.json").getInputStream(), "UTF-8");

        Utkast utkast = new Utkast();
        utkast.setModel(json);
        utkast.setIntygsId(intygsId);
        utkast.setIntygsTyp(CERTIFICATE_TYPE);
        utkast.setSkickadTillMottagareDatum(sendDate);
        utkast.setAterkalladDatum(revokeDate);
        utkast.setStatus(UtkastStatus.SIGNED);
        utkast.setPatientPersonnummer(PERSNR);

        Signatur signatur = new Signatur(LocalDateTime.now(), HSA_ID, CERTIFICATE_ID, "", "", "");
        utkast.setSignatur(signatur);

        return utkast;
    }

    private Utkast getDraft(String intygsId) throws IOException {
        return getDraft(intygsId, UtkastStatus.DRAFT_INCOMPLETE);
    }

    private Utkast getDraft(String intygsId, UtkastStatus utkastStatus) throws IOException {
        Utkast utkast = new Utkast();
        String json = IOUtils.toString(new ClassPathResource(
                "IntygServiceTest/utlatande-address.json").getInputStream(), "UTF-8");
        utkast.setModel(json);
        utkast.setIntygsId(intygsId);
        utkast.setIntygsTyp(CERTIFICATE_TYPE);
        utkast.setPatientPersonnummer(PERSNR);
        utkast.setStatus(utkastStatus);

        return utkast;
    }

    private Patient buildPatient(boolean sekretessMarkering, boolean avliden) {
        Patient patient = new Patient();
        patient.setPersonId(PERSNR);
        patient.setFornamn("fornamn");
        patient.setMellannamn("mellannamn");
        patient.setEfternamn("efternamn");
        patient.setPostadress("Testgatan");
        patient.setPostort("Testistan");
        patient.setPostnummer("12345");
        patient.setSekretessmarkering(sekretessMarkering);
        patient.setAvliden(avliden);

        return patient;
    }

    protected CertificateMetaData buildCertificateMetaData() {
        CertificateMetaData metaData = new CertificateMetaData();
        metaData.setStatus(new ArrayList<>());
        Status statusSigned = new Status(CertificateState.RECEIVED, "TRANSP", LocalDateTime.now());
        metaData.getStatus().add(statusSigned);
        metaData.setSignDate(LocalDateTime.now());
        return metaData;
    }

}
