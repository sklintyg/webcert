/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.integration.interactions.createdraftcertificate.v3;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.notification.SchemaVersion;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.common.model.UtkastStatus;
import se.inera.intyg.webcert.integration.tak.model.TakResult;
import se.inera.intyg.webcert.integration.tak.service.TakService;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.web.integration.interactions.createdraftcertificate.BaseCreateDraftCertificateTest;
import se.inera.intyg.webcert.web.integration.registry.IntegreradeEnheterRegistry;
import se.inera.intyg.webcert.web.integration.registry.dto.IntegreradEnhetEntry;
import se.inera.intyg.webcert.web.integration.validators.ResultValidator;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftRequest;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.CreateDraftCertificateResponseType;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.CreateDraftCertificateType;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.Enhet;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.HosPersonal;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.TypAvIntyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v3.Patient;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CreateDraftCertificateResponderImplTest extends BaseCreateDraftCertificateTest {

    private static final String LOGICAL_ADDR = "1234567890";

    private static final String USER_HSAID = "SE1234567890";
    private static final String UNIT_HSAID = "SE0987654321";

    private static final String UTKAST_ID = "abc123";
    private static final String UTKAST_VERSION = "1";
    private static final String UTKAST_TYPE = "fk7263";
    private static final String UTKAST_JSON = "A bit of text representing json";
    @Mock
    private PatientDetailsResolver patientDetailsResolver;
    @Mock
    private UtkastService mockUtkastService;
    @Mock
    private CreateNewDraftRequestBuilder mockRequestBuilder;
    @Mock
    private CreateDraftCertificateValidator mockValidator;
    @Mock
    private IntegreradeEnheterRegistry mockIntegreradeEnheterService;
    @Mock
    private MonitoringLogService mockMonitoringLogService;
    @Mock
    private TakService takService;
    @Mock
    private IntygModuleRegistry moduleRegistry;

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @InjectMocks
    private CreateDraftCertificateResponderImpl responder;

    @Before
    public void setup() {
        super.setup();
        when(mockValidator.validateApplicationErrors(any(Intyg.class), any(IntygUser.class))).thenReturn(ResultValidator.newInstance());
        when(moduleRegistry.getModuleIdFromExternalId(any())).thenReturn(UTKAST_TYPE);
    }

    @Test
    public void testCreateDraftCertificateSuccess() {

        CreateNewDraftRequest draftRequest = createCreateNewDraftRequest(createVardenhet(createVardgivare()));
        CreateDraftCertificateType certificateType = createCertificateType();

        VardpersonReferens vardperson = createVardpersonReferens(
                certificateType.getIntyg().getSkapadAv().getPersonalId().getRoot(),
                certificateType.getIntyg().getSkapadAv().getFullstandigtNamn());

        Utkast utkast = createUtkast(UTKAST_ID, Long.parseLong(UTKAST_VERSION), UTKAST_TYPE, UtkastStatus.DRAFT_INCOMPLETE, UTKAST_JSON,
                vardperson);

        when(mockValidator.validate(any(Intyg.class))).thenReturn(new ResultValidator());
        when(mockRequestBuilder.buildCreateNewDraftRequest(any(Intyg.class), any(IntygUser.class))).thenReturn(draftRequest);
        when(mockUtkastService.createNewDraft(any(CreateNewDraftRequest.class))).thenReturn(utkast);
        when(takService.verifyTakningForCareUnit(any(String.class), any(String.class), any(SchemaVersion.class), any(IntygUser.class)))
                .thenReturn(new TakResult(true, Lists.emptyList()));
        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(eq(SekretessStatus.FALSE));

        CreateDraftCertificateResponseType response = responder.createDraftCertificate(LOGICAL_ADDR, certificateType);

        verify(mockUtkastService).createNewDraft(any(CreateNewDraftRequest.class));
        verify(mockIntegreradeEnheterService).putIntegreradEnhet(any(IntegreradEnhetEntry.class), eq(false), eq(true));
        verify(takService).verifyTakningForCareUnit(any(String.class), eq(UTKAST_TYPE), eq(SchemaVersion.VERSION_3), any(IntygUser.class));

        assertNotNull(response);
        assertEquals(response.getResult().getResultCode(), ResultCodeType.OK);
        assertEquals(response.getIntygsId().getRoot(), UNIT_HSAID);
        assertEquals(response.getIntygsId().getExtension(), UTKAST_ID);
    }

    @Test
    public void testCreateDraftCertificateTakningNotOK() {
        CreateDraftCertificateType certificateType = createCertificateType();

        when(mockValidator.validate(any(Intyg.class))).thenReturn(new ResultValidator());
        when(takService.verifyTakningForCareUnit(any(String.class), any(String.class), any(SchemaVersion.class), any(IntygUser.class)))
                .thenReturn(new TakResult(false, Lists.newArrayList("Den angivna enheten går ej att adressera för ärendekommunikation.")));

        CreateDraftCertificateResponseType response = responder.createDraftCertificate(LOGICAL_ADDR, certificateType);

        verify(takService).verifyTakningForCareUnit(any(String.class), eq(UTKAST_TYPE), eq(SchemaVersion.VERSION_3), any(IntygUser.class));

        assertNotNull(response);
        assertEquals(response.getResult().getResultCode(), ResultCodeType.ERROR);
        assertEquals(response.getResult().getErrorId(), ErrorIdType.APPLICATION_ERROR);
        assertEquals("Den angivna enheten går ej att adressera för ärendekommunikation.", response.getResult().getResultText());
    }

    @Test
    public void testCreateDraftCertificateValidationError() {
        ResultValidator resultValidator = mock(ResultValidator.class);
        CreateDraftCertificateType certificateType = createCertificateType();

        when(resultValidator.hasErrors()).thenReturn(true);
        when(mockValidator.validate(any(Intyg.class))).thenReturn(resultValidator);

        CreateDraftCertificateResponseType response = responder.createDraftCertificate(LOGICAL_ADDR, certificateType);

        assertNotNull(response);
        assertEquals(response.getResult().getResultCode(), ResultCodeType.ERROR);
        assertEquals(response.getResult().getErrorId(), ErrorIdType.VALIDATION_ERROR);
    }

    @Test
    public void testCreateDraftCertificateNoMIUs() {
        WebCertUser userWithoutMiu = buildWebCertUser();
        userWithoutMiu.setVardgivare(new ArrayList<>());
        when(webcertUserDetailsService.loadUserByHsaId(USER_HSAID)).thenReturn(userWithoutMiu);

        CreateDraftCertificateType certificateType = createCertificateType();

        when(mockValidator.validate(any(Intyg.class))).thenReturn(new ResultValidator());

        CreateDraftCertificateResponseType response = responder.createDraftCertificate(LOGICAL_ADDR, certificateType);

        verify(mockMonitoringLogService).logMissingMedarbetarUppdrag(USER_HSAID, UNIT_HSAID);

        assertNotNull(response);
        assertEquals(response.getResult().getResultCode(), ResultCodeType.ERROR);
        assertEquals(response.getResult().getErrorId(), ErrorIdType.VALIDATION_ERROR);
    }

    @Test
    public void testCreateDraftCertificateMultipleMIUs() {

        CreateNewDraftRequest draftRequest = createCreateNewDraftRequest(createVardenhet(createVardgivare()));
        CreateDraftCertificateType certificateType = createCertificateType();

        VardpersonReferens vardperson = createVardpersonReferens(
                certificateType.getIntyg().getSkapadAv().getPersonalId().getRoot(),
                certificateType.getIntyg().getSkapadAv().getFullstandigtNamn());

        Utkast utkast = createUtkast(UTKAST_ID, Long.parseLong(UTKAST_VERSION), UTKAST_TYPE, UtkastStatus.DRAFT_INCOMPLETE, UTKAST_JSON,
                vardperson);

        when(mockValidator.validate(any(Intyg.class))).thenReturn(new ResultValidator());
        when(mockRequestBuilder.buildCreateNewDraftRequest(any(Intyg.class), any(IntygUser.class))).thenReturn(draftRequest);
        when(mockUtkastService.createNewDraft(any(CreateNewDraftRequest.class))).thenReturn(utkast);
        when(takService.verifyTakningForCareUnit(any(String.class), any(String.class), any(SchemaVersion.class), any(IntygUser.class)))
                .thenReturn(new TakResult(true, Lists.emptyList()));

        CreateDraftCertificateResponseType response = responder.createDraftCertificate(LOGICAL_ADDR, certificateType);

        verify(mockUtkastService).createNewDraft(any(CreateNewDraftRequest.class));
        verify(mockIntegreradeEnheterService).putIntegreradEnhet(any(IntegreradEnhetEntry.class), eq(false), eq(true));

        assertNotNull(response);
        assertEquals(response.getResult().getResultCode(), ResultCodeType.OK);
        assertEquals(response.getIntygsId().getExtension(), UTKAST_ID);
    }

    @Test
    public void testCreateDraftCertificateVardenhetAlredyExistsInRegistry() {
        CreateNewDraftRequest draftRequest = createCreateNewDraftRequest(createVardenhet(createVardgivare()));
        CreateDraftCertificateType certificateType = createCertificateType();

        VardpersonReferens vardperson = createVardpersonReferens(
                certificateType.getIntyg().getSkapadAv().getPersonalId().getRoot(),
                certificateType.getIntyg().getSkapadAv().getFullstandigtNamn());

        Utkast utkast = createUtkast(UTKAST_ID, Long.parseLong(UTKAST_VERSION), UTKAST_TYPE, UtkastStatus.DRAFT_INCOMPLETE, UTKAST_JSON,
                vardperson);

        when(mockValidator.validate(any(Intyg.class))).thenReturn(new ResultValidator());
        when(mockRequestBuilder.buildCreateNewDraftRequest(any(Intyg.class), any(IntygUser.class))).thenReturn(draftRequest);
        when(mockUtkastService.createNewDraft(any(CreateNewDraftRequest.class))).thenReturn(utkast);
        when(takService.verifyTakningForCareUnit(any(String.class), any(String.class), any(SchemaVersion.class), any(IntygUser.class)))
                .thenReturn(new TakResult(true, Lists.emptyList()));

        CreateDraftCertificateResponseType response = responder.createDraftCertificate(LOGICAL_ADDR, certificateType);

        verify(mockUtkastService).createNewDraft(any(CreateNewDraftRequest.class));
        verify(mockIntegreradeEnheterService).putIntegreradEnhet(any(IntegreradEnhetEntry.class), eq(false), eq(true));

        assertNotNull(response);
        assertEquals(response.getResult().getResultCode(), ResultCodeType.OK);
        assertEquals(response.getIntygsId().getExtension(), UTKAST_ID);
    }

    private VardpersonReferens createVardpersonReferens(String hsaId, String name) {
        VardpersonReferens vardperson = new VardpersonReferens();
        vardperson.setHsaId(hsaId);
        vardperson.setNamn(name);
        return vardperson;
    }

    private CreateNewDraftRequest createCreateNewDraftRequest(Vardenhet vardenhet) {
        CreateNewDraftRequest draftRequest = new CreateNewDraftRequest(UTKAST_ID, null, null, new HoSPersonal(), null);
        draftRequest.getHosPerson().setVardenhet(vardenhet);
        return draftRequest;
    }

    private Vardenhet createVardenhet(Vardgivare vardgivare) {
        Vardenhet vardenhet = new Vardenhet();
        vardenhet.setEnhetsid("SE1234567890-1A01");
        vardenhet.setEnhetsnamn("Vardenheten");
        vardenhet.setVardgivare(vardgivare);
        return vardenhet;
    }

    private Vardgivare createVardgivare() {
        Vardgivare vardgivare = new Vardgivare();
        vardgivare.setVardgivarid("SE1234567890-2B01");
        vardgivare.setVardgivarnamn("Vardgivaren");
        return vardgivare;
    }

    private CreateDraftCertificateType createCertificateType() {

        // Type
        TypAvIntyg utlTyp = new TypAvIntyg();
        utlTyp.setCode("fk7263");

        // HoSPerson
        HsaId userHsaId = new HsaId();
        userHsaId.setExtension(USER_HSAID);
        userHsaId.setRoot("USERHSAID");

        HsaId unitHsaId = new HsaId();
        unitHsaId.setExtension(UNIT_HSAID);
        unitHsaId.setRoot("UNITHSAID");

        Enhet hosEnhet = new Enhet();
        hosEnhet.setEnhetsId(unitHsaId);

        HosPersonal hosPerson = new HosPersonal();
        hosPerson.setFullstandigtNamn("Abel Baker");
        hosPerson.setPersonalId(userHsaId);
        hosPerson.setEnhet(hosEnhet);

        // Patient
        PersonId personId = new PersonId();
        personId.setRoot("PERSNR");
        personId.setExtension("19121212-1212");

        Patient patType = new Patient();
        patType.setPersonId(personId);
        patType.setFornamn("Adam");
        patType.setMellannamn("Cesarsson");
        patType.setEfternamn("Eriksson");

        Intyg utlatande = new Intyg();
        utlatande.setTypAvIntyg(utlTyp);
        utlatande.setSkapadAv(hosPerson);
        utlatande.setPatient(patType);
        utlatande.setRef("Test-ref");

        CreateDraftCertificateType certificateType = new CreateDraftCertificateType();
        certificateType.setIntyg(utlatande);

        return certificateType;
    }

    private Utkast createUtkast(String intygId, long version, String type, UtkastStatus status, String model,
            VardpersonReferens vardperson) {

        Utkast utkast = new Utkast();
        utkast.setIntygsId(intygId);
        utkast.setVersion(version);
        utkast.setIntygsTyp(type);
        utkast.setStatus(status);
        utkast.setModel(model);
        utkast.setSkapadAv(vardperson);
        utkast.setSenastSparadAv(vardperson);

        return utkast;
    }
}
