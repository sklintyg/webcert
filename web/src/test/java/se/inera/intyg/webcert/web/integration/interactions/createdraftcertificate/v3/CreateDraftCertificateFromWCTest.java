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

package se.inera.intyg.webcert.web.integration.interactions.createdraftcertificate.v3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.fk7263.rest.Fk7263ModuleApi;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.notification.SchemaVersion;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.webcert.integration.tak.model.TakResult;
import se.inera.intyg.webcert.integration.tak.service.TakService;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.web.integration.interactions.createdraftcertificate.BaseCreateDraftCertificateTest;
import se.inera.intyg.webcert.web.integration.registry.IntegreradeEnheterRegistry;
import se.inera.intyg.webcert.web.integration.registry.dto.IntegreradEnhetEntry;
import se.inera.intyg.webcert.web.integration.validators.ResultValidator;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftRequest;
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

@RunWith(MockitoJUnitRunner.class)
public class CreateDraftCertificateFromWCTest extends BaseCreateDraftCertificateTest {

    private static final String USER_HSAID = "SE1234567890";
    private static final String UNIT_HSAID = "SE0987654321";
    private static final String UTKAST_ID = "abc123";
    private static final String UTKAST_VERSION = "1";
    private static final String UTKAST_TYPE = "fk7263";
    private static final String UTKAST_JSON = "A bit of text representing json";
    private static final String INTYG_TYPE_VERSION = "1.0";
    private static final String CERTIFICATE_TYPE = "CERTIFICATE_TYPE";
    private static final String ISSUER_NAME = "ISSUER_NAME";
    private static final String FACILITY_NAME = "FACILITY_NAME";
    private static final String PERSON_ID = "191212121212";

    @Mock
    private UtkastService mockUtkastService;
    @Mock
    private CreateNewDraftRequestBuilder mockRequestBuilder;
    @Mock
    private CreateDraftCertificateValidator mockValidator;
    @Mock
    private IntegreradeEnheterRegistry mockIntegreradeEnheterService;
    @Mock
    private TakService takService;
    @Mock
    private IntygModuleRegistry moduleRegistry;
    @Mock
    private IntygTextsService intygTextsService;
    @InjectMocks
    private CreateDraftCertificateFromWC createDraftCertificateFromWC;

    @Before
    public void setup() throws ModuleNotFoundException {
        when(moduleRegistry.getModuleIdFromExternalId(any())).thenReturn(UTKAST_TYPE);
        when(mockUtkastService.checkIfPersonHasExistingIntyg(any(), any(), any())).thenReturn(ImmutableMap.of(
            "utkast", ImmutableMap.of(),
            "intyg", ImmutableMap.of()));
        when(intygTextsService.getLatestVersion(any(String.class))).thenReturn(INTYG_TYPE_VERSION);
    }

    @Test
    public void testCreateDraftCertificateSuccess() throws ModuleNotFoundException {
        final var vardperson = createVardpersonReferens(
            createCertificateType().getIntyg().getSkapadAv().getPersonalId().getRoot(),
            createCertificateType().getIntyg().getSkapadAv().getFullstandigtNamn());
        final var utkast = createUtkast(Long.parseLong(UTKAST_VERSION), vardperson);

        when(moduleRegistry.getModuleApi(any(), any())).thenReturn(new Fk7263ModuleApi());
        when(mockValidator.validateCertificateErrors(any(Intyg.class), any(IntygUser.class))).thenReturn(new ResultValidator());
        when(mockRequestBuilder.buildCreateNewDraftRequest(any(Intyg.class), any(String.class), any(IntygUser.class)))
            .thenReturn(createCreateNewDraftRequest(createVardenhet(createVardgivare())));
        when(mockUtkastService.createNewDraft(any(CreateNewDraftRequest.class))).thenReturn(utkast);
        when(takService.verifyTakningForCareUnit(any(String.class), any(String.class), any(SchemaVersion.class), any(IntygUser.class)))
            .thenReturn(new TakResult(true, Collections.emptyList()));

        final var response = createDraftCertificateFromWC.create(buildIntyg(), getIntygUser(USER_HSAID));

        verify(mockUtkastService).createNewDraft(any(CreateNewDraftRequest.class));
        verify(mockIntegreradeEnheterService).putIntegreradEnhet(any(IntegreradEnhetEntry.class), eq(false), eq(true));
        verify(takService).verifyTakningForCareUnit(any(String.class), eq(UTKAST_TYPE), eq(SchemaVersion.VERSION_3), any(IntygUser.class));

        assertNotNull(response);
        assertEquals(ResultCodeType.OK, response.getResult().getResultCode());
        assertEquals(UNIT_HSAID, response.getIntygsId().getRoot());
        assertEquals(UTKAST_ID, response.getIntygsId().getExtension());
    }

    @Test
    public void testCreateDraftCertificateTakningNotOK() throws ModuleNotFoundException {
        when(moduleRegistry.getModuleApi(any(), any())).thenReturn(new Fk7263ModuleApi());
        when(mockValidator.validateCertificateErrors(any(Intyg.class), any(IntygUser.class))).thenReturn(new ResultValidator());
        when(takService.verifyTakningForCareUnit(any(String.class), any(String.class), any(SchemaVersion.class), any(IntygUser.class)))
            .thenReturn(new TakResult(false, Lists.newArrayList("Den angivna enheten går ej att adressera för ärendekommunikation.")));
        final var response = createDraftCertificateFromWC.create(buildIntyg(), getIntygUser(USER_HSAID));

        verify(takService).verifyTakningForCareUnit(any(String.class), eq(UTKAST_TYPE), eq(SchemaVersion.VERSION_3), any(IntygUser.class));

        assertNotNull(response);
        assertEquals(ResultCodeType.ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdType.APPLICATION_ERROR, response.getResult().getErrorId());
        assertEquals("Den angivna enheten går ej att adressera för ärendekommunikation.", response.getResult().getResultText());
    }

    @Test
    public void testCreateDraftCertificateValidationError() {
        final var resultValidator = mock(ResultValidator.class);
        when(resultValidator.hasErrors()).thenReturn(true);
        when(mockValidator.validateCertificateErrors(any(Intyg.class), any(IntygUser.class))).thenReturn(resultValidator);

        final var response = createDraftCertificateFromWC.create(buildIntyg(), new IntygUser(USER_HSAID));

        assertNotNull(response);
        assertEquals(ResultCodeType.ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdType.APPLICATION_ERROR, response.getResult().getErrorId());
    }

    @Test
    public void testCreateDraftCertificateMultipleMIUs() throws ModuleNotFoundException {
        final var vardperson = createVardpersonReferens(
            createCertificateType().getIntyg().getSkapadAv().getPersonalId().getRoot(),
            createCertificateType().getIntyg().getSkapadAv().getFullstandigtNamn());

        Utkast utkast = createUtkast(Long.parseLong(UTKAST_VERSION), vardperson);

        when(moduleRegistry.getModuleApi(any(), any())).thenReturn(new Fk7263ModuleApi());
        when(mockValidator.validateCertificateErrors(any(Intyg.class), any(IntygUser.class))).thenReturn(new ResultValidator());
        when(mockRequestBuilder.buildCreateNewDraftRequest(any(Intyg.class), any(String.class), any(IntygUser.class)))
            .thenReturn(createCreateNewDraftRequest(createVardenhet(createVardgivare())));
        when(mockUtkastService.createNewDraft(any(CreateNewDraftRequest.class))).thenReturn(utkast);
        when(takService.verifyTakningForCareUnit(any(String.class), any(String.class), any(SchemaVersion.class), any(IntygUser.class)))
            .thenReturn(new TakResult(true, Collections.emptyList()));

        final var response = createDraftCertificateFromWC.create(buildIntyg(), getIntygUser(USER_HSAID));

        verify(mockUtkastService).createNewDraft(any(CreateNewDraftRequest.class));
        verify(mockIntegreradeEnheterService).putIntegreradEnhet(any(IntegreradEnhetEntry.class), eq(false), eq(true));

        assertNotNull(response);
        assertEquals(ResultCodeType.OK, response.getResult().getResultCode());
        assertEquals(UTKAST_ID, response.getIntygsId().getExtension());
    }

    @Test
    public void testCreateDraftCertificateVardenhetAlredyExistsInRegistry() throws ModuleNotFoundException {
        final var vardperson = createVardpersonReferens(
            createCertificateType().getIntyg().getSkapadAv().getPersonalId().getRoot(),
            createCertificateType().getIntyg().getSkapadAv().getFullstandigtNamn());

        final var utkast = createUtkast(Long.parseLong(UTKAST_VERSION), vardperson);

        when(moduleRegistry.getModuleApi(any(), any())).thenReturn(new Fk7263ModuleApi());
        when(mockValidator.validateCertificateErrors(any(Intyg.class), any(IntygUser.class))).thenReturn(new ResultValidator());
        when(mockRequestBuilder.buildCreateNewDraftRequest(any(Intyg.class), any(String.class), any(IntygUser.class)))
            .thenReturn(createCreateNewDraftRequest(createVardenhet(createVardgivare())));
        when(mockUtkastService.createNewDraft(any(CreateNewDraftRequest.class))).thenReturn(utkast);
        when(takService.verifyTakningForCareUnit(any(String.class), any(String.class), any(SchemaVersion.class), any(IntygUser.class)))
            .thenReturn(new TakResult(true, Collections.emptyList()));

        final var response = createDraftCertificateFromWC.create(buildIntyg(), getIntygUser(USER_HSAID));

        verify(mockUtkastService).createNewDraft(any(CreateNewDraftRequest.class));
        verify(mockIntegreradeEnheterService).putIntegreradEnhet(any(IntegreradEnhetEntry.class), eq(false), eq(true));

        assertNotNull(response);
        assertEquals(ResultCodeType.OK, response.getResult().getResultCode());
        assertEquals(UTKAST_ID, response.getIntygsId().getExtension());
    }

    @Test
    public void shouldReturnErrorIfModuleApiThrows() throws ModuleNotFoundException {
        when(mockValidator.validateCertificateErrors(any(Intyg.class), any(IntygUser.class))).thenReturn(new ResultValidator());
        when(moduleRegistry.getModuleApi(any(), any())).thenThrow(ModuleNotFoundException.class);
        final var response = createDraftCertificateFromWC.create(buildIntyg(), getIntygUser(USER_HSAID));

        assertNotNull(response);
        assertEquals(ResultCodeType.ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdType.APPLICATION_ERROR, response.getResult().getErrorId());
        assertEquals("Internal error. Could not get module api.", response.getResult().getResultText());
    }

    private VardpersonReferens createVardpersonReferens(String hsaId, String name) {
        VardpersonReferens vardperson = new VardpersonReferens();
        vardperson.setHsaId(hsaId);
        vardperson.setNamn(name);
        return vardperson;
    }

    private CreateNewDraftRequest createCreateNewDraftRequest(Vardenhet vardenhet) {
        CreateNewDraftRequest draftRequest = new CreateNewDraftRequest(UTKAST_ID, null, INTYG_TYPE_VERSION, null, new HoSPersonal(), null);
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

    private Intyg buildIntyg() {
        final var intyg = new Intyg();
        intyg.setTypAvIntyg(new TypAvIntyg());
        intyg.getTypAvIntyg().setCode(CERTIFICATE_TYPE);
        intyg.setSkapadAv(new HosPersonal());
        intyg.getSkapadAv().setFullstandigtNamn(ISSUER_NAME);
        intyg.getSkapadAv().setEnhet(new Enhet());
        intyg.getSkapadAv().getEnhet().setEnhetsnamn(FACILITY_NAME);
        intyg.getSkapadAv().getEnhet().setEnhetsId(new HsaId());
        intyg.getSkapadAv().getEnhet().getEnhetsId().setExtension(UNIT_HSAID);
        intyg.getSkapadAv().setPersonalId(new HsaId());
        intyg.getSkapadAv().getPersonalId().setExtension(USER_HSAID);
        intyg.setPatient(new Patient());
        intyg.getPatient().setPersonId(new PersonId());
        intyg.getPatient().getPersonId().setExtension(PERSON_ID);
        return intyg;
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

    private Utkast createUtkast(long version, VardpersonReferens vardperson) {
        return createUtkast(version, UTKAST_JSON, vardperson);
    }

    private Utkast createUtkast(long version, String model,
        VardpersonReferens vardperson) {

        Utkast utkast = new Utkast();
        utkast.setIntygsId(CreateDraftCertificateFromWCTest.UTKAST_ID);
        utkast.setVersion(version);
        utkast.setIntygsTyp(CreateDraftCertificateFromWCTest.UTKAST_TYPE);
        utkast.setIntygTypeVersion(CreateDraftCertificateFromWCTest.INTYG_TYPE_VERSION);
        utkast.setStatus(UtkastStatus.DRAFT_INCOMPLETE);
        utkast.setModel(model);
        utkast.setSkapadAv(vardperson);
        utkast.setSenastSparadAv(vardperson);

        return utkast;
    }
}
