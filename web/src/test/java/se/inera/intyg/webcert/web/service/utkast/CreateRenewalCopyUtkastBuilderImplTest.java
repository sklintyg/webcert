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
package se.inera.intyg.webcert.web.service.utkast;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import se.inera.intyg.common.fk7263.model.internal.Fk7263Utlatande;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.CreateDraftCopyHolder;
import se.inera.intyg.common.support.modules.support.api.dto.ValidateDraftResponse;
import se.inera.intyg.common.support.modules.support.api.dto.ValidationStatus;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.infra.pu.integration.api.model.Person;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.log.factory.LogRequestFactory;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateRenewalCopyRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.UtkastBuilderResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CreateRenewalCopyUtkastBuilderImplTest extends AbstractBuilderTest {

    private static final String INTYG_TYPE = "fk7263";

    private static final Personnummer PATIENT_NEW_SSN = createPnr("19121212-1414");
    private static final String INTYG_TYPE_VERSION = "2.2";

    private ModuleApi mockModuleApi;

    @Mock
    private LogService logService;

    @Mock
    private LogRequestFactory logRequestFactory;

    @InjectMocks
    private CreateRenewalCopyUtkastBuilder renewalBuilder = new CreateRenewalCopyUtkastBuilder();

    @Before
    public void expectCallToModuleRegistry() throws Exception {
        this.mockModuleApi = mock(ModuleApi.class);
        when(moduleRegistry.getModuleApi(anyString(), anyString())).thenReturn(mockModuleApi);
    }

    @Test
    public void testPopulateRenewalUtkastFromSignedIntyg() throws Exception {

        when(intygTextsService.getLatestVersionForSameMajorVersion(anyString(), anyString())).thenReturn("1.2");
        IntygContentHolder ich = createIntygContentHolder();
        when(mockIntygService.fetchIntygData(INTYG_ID, INTYG_TYPE)).thenReturn(ich);

        CreateRenewalCopyRequest renewalRequest = buildRenewalRequest();
        Person patientDetails = new Person(PATIENT_SSN, false, false, PATIENT_FNAME, PATIENT_MNAME, PATIENT_LNAME, "Postadr", "12345",
            "postort", false);

        when(mockModuleApi.createRenewalFromTemplate(any(CreateDraftCopyHolder.class), any())).thenReturn(INTYG_JSON);
        Utlatande utlatande = new Fk7263Utlatande();
        when(mockModuleApi.getUtlatandeFromJson(any(String.class))).thenReturn(utlatande);

        ValidateDraftResponse vdr = new ValidateDraftResponse(ValidationStatus.VALID, new ArrayList<>());
        when(mockModuleApi.validateDraft(anyString())).thenReturn(vdr);

        UtkastBuilderResponse builderResponse = renewalBuilder.populateCopyUtkastFromSignedIntyg(renewalRequest, patientDetails, false
        );

        assertNotNull(builderResponse.getUtkast());
        assertNotNull(builderResponse.getUtkast().getModel());
        assertEquals(INTYG_TYPE, builderResponse.getUtkast().getIntygsTyp());
        assertEquals("1.2", builderResponse.getUtkast().getIntygTypeVersion());
        assertEquals(PATIENT_SSN, builderResponse.getUtkast().getPatientPersonnummer());
        assertEquals(PATIENT_FNAME, builderResponse.getUtkast().getPatientFornamn());
        assertEquals(PATIENT_MNAME, builderResponse.getUtkast().getPatientMellannamn());
        assertEquals(PATIENT_LNAME, builderResponse.getUtkast().getPatientEfternamn());

        ArgumentCaptor<CreateDraftCopyHolder> requestCaptor = ArgumentCaptor.forClass(CreateDraftCopyHolder.class);
        verify(mockModuleApi).createRenewalFromTemplate(requestCaptor.capture(), any());

        // verify full name is set
        assertNotNull(requestCaptor.getValue().getPatient().getFullstandigtNamn());
        assertEquals(PATIENT_FNAME + " " + PATIENT_MNAME + " " + PATIENT_LNAME,
            requestCaptor.getValue().getPatient().getFullstandigtNamn());
    }

    @Test
    public void testPopulateRenewalUtkastFromOriginal() throws Exception {

        Utkast orgUtkast = createOriginalUtkast();
        when(mockUtkastRepository.findById(INTYG_ID)).thenReturn(Optional.of(orgUtkast));

        CreateRenewalCopyRequest renewalRequest = buildRenewalRequest();
        Person patientDetails = new Person(PATIENT_SSN, false, false, PATIENT_FNAME, PATIENT_MNAME, PATIENT_LNAME, "Postadr", "12345",
            "postort", false);

        when(mockModuleApi.createRenewalFromTemplate(any(CreateDraftCopyHolder.class), any())).thenReturn(INTYG_JSON);

        ValidateDraftResponse vdr = new ValidateDraftResponse(ValidationStatus.VALID, new ArrayList<>());
        when(mockModuleApi.validateDraft(anyString())).thenReturn(vdr);

        UtkastBuilderResponse builderResponse = renewalBuilder.populateCopyUtkastFromOrignalUtkast(renewalRequest, patientDetails,
            false
        );

        assertNotNull(builderResponse.getUtkast());
        assertNotNull(builderResponse.getUtkast().getModel());
        assertEquals(INTYG_TYPE, builderResponse.getUtkast().getIntygsTyp());
        assertEquals(PATIENT_SSN, builderResponse.getUtkast().getPatientPersonnummer());
        assertEquals(PATIENT_FNAME, builderResponse.getUtkast().getPatientFornamn());
        assertNotNull(builderResponse.getUtkast().getPatientMellannamn());
        assertEquals(PATIENT_LNAME, builderResponse.getUtkast().getPatientEfternamn());
    }

    @Test
    public void testPopulateRenewalUtkastFromOriginalWhenIntegratedAndWithUpdatedSSN() throws Exception {

        Utkast orgUtkast = createOriginalUtkast();
        when(mockUtkastRepository.findById(INTYG_ID)).thenReturn(Optional.of(orgUtkast));

        CreateRenewalCopyRequest renewalRequest = buildRenewalRequest();
        renewalRequest.setNyttPatientPersonnummer(PATIENT_NEW_SSN);
        renewalRequest.setDjupintegrerad(true);

        when(mockModuleApi.createRenewalFromTemplate(any(CreateDraftCopyHolder.class), any())).thenReturn(INTYG_JSON);

        ValidateDraftResponse vdr = new ValidateDraftResponse(ValidationStatus.VALID, new ArrayList<>());
        when(mockModuleApi.validateDraft(anyString())).thenReturn(vdr);

        UtkastBuilderResponse builderResponse = renewalBuilder.populateCopyUtkastFromOrignalUtkast(renewalRequest, null,
            false);

        assertNotNull(builderResponse.getUtkast());
        assertNotNull(builderResponse.getUtkast().getModel());
        assertEquals(INTYG_TYPE, builderResponse.getUtkast().getIntygsTyp());
        assertEquals(PATIENT_NEW_SSN, builderResponse.getUtkast().getPatientPersonnummer());
        assertEquals(PATIENT_FNAME, builderResponse.getUtkast().getPatientFornamn());
        assertNotNull(builderResponse.getUtkast().getPatientMellannamn());
        assertEquals(PATIENT_LNAME, builderResponse.getUtkast().getPatientEfternamn());
    }

    @Test
    public void testPopulateRenewalUtkastFromSignedIntygWithNoPatientDetails() throws Exception {

        IntygContentHolder ich = createIntygContentHolder();
        when(mockIntygService.fetchIntygData(INTYG_ID, INTYG_TYPE)).thenReturn(ich);

        CreateRenewalCopyRequest renewalRequest = buildRenewalRequest();

        when(mockModuleApi.createRenewalFromTemplate(any(CreateDraftCopyHolder.class), any())).thenReturn(INTYG_JSON);

        ValidateDraftResponse vdr = new ValidateDraftResponse(ValidationStatus.VALID, new ArrayList<>());
        when(mockModuleApi.validateDraft(anyString())).thenReturn(vdr);

        UtkastBuilderResponse builderResponse = renewalBuilder.populateCopyUtkastFromSignedIntyg(renewalRequest, null, false);

        assertNotNull(builderResponse.getUtkast());
        assertNotNull(builderResponse.getUtkast().getModel());
        assertEquals(INTYG_TYPE, builderResponse.getUtkast().getIntygsTyp());
        assertEquals(PATIENT_SSN, builderResponse.getUtkast().getPatientPersonnummer());
        assertEquals("Test", builderResponse.getUtkast().getPatientFornamn());
        assertNull(builderResponse.getUtkast().getPatientMellannamn());
        assertEquals("Testorsson", builderResponse.getUtkast().getPatientEfternamn());
    }

    @Test
    public void testExtractNamePartsFromFullName() {

        String[] res = renewalBuilder.extractNamePartsFromFullName(null);
        assertNotNull(res);

        res = renewalBuilder.extractNamePartsFromFullName("");
        assertNotNull(res);

        res = renewalBuilder.extractNamePartsFromFullName("  ");
        assertNotNull(res);
        assertEquals("", res[0]);
        assertEquals("", res[1]);

        res = renewalBuilder.extractNamePartsFromFullName("Adam");
        assertNotNull(res);
        assertEquals("Adam", res[0]);
        assertEquals("", res[1]);

        res = renewalBuilder.extractNamePartsFromFullName("Adam Caesarsson");
        assertNotNull(res);
        assertEquals("Adam", res[0]);
        assertEquals("Caesarsson", res[1]);

        res = renewalBuilder.extractNamePartsFromFullName("Adam Bertil Caesarsson");
        assertNotNull(res);
        assertEquals("Adam Bertil", res[0]);
        assertEquals("Caesarsson", res[1]);
    }

    private CreateRenewalCopyRequest buildRenewalRequest() {
        return new CreateRenewalCopyRequest(INTYG_ID, INTYG_TYPE, patient, hoSPerson);
    }

    private IntygContentHolder createIntygContentHolder() throws Exception {
        List<Status> status = new ArrayList<>();
        status.add(new Status(CertificateState.RECEIVED, "HSVARD", LocalDateTime.now()));
        status.add(new Status(CertificateState.SENT, "FKASSA", LocalDateTime.now()));
        Fk7263Utlatande utlatande = new CustomObjectMapper().readValue(new ClassPathResource(
            "IntygDraftServiceImplTest/utlatande.json").getFile(), Fk7263Utlatande.class);
        return IntygContentHolder.builder()
            .contents("<external-json/>")
            .utlatande(utlatande)
            .statuses(status)
            .revoked(false)
            .relations(new Relations())
            .deceased(false)
            .sekretessmarkering(false)
            .patientNameChangedInPU(false)
            .patientAddressChangedInPU(false)
            .testIntyg(false)
            .latestMajorTextVersion(true)
            .build();
    }

    private Utkast createOriginalUtkast() {

        Utkast orgUtkast = new Utkast();
        orgUtkast.setIntygsId(INTYG_COPY_ID);
        orgUtkast.setIntygsTyp(INTYG_TYPE);
        orgUtkast.setIntygTypeVersion(INTYG_TYPE_VERSION);
        orgUtkast.setPatientPersonnummer(PATIENT_SSN);
        orgUtkast.setPatientFornamn(PATIENT_FNAME);
        orgUtkast.setPatientMellannamn(PATIENT_MNAME);
        orgUtkast.setPatientEfternamn(PATIENT_LNAME);
        orgUtkast.setEnhetsId(VARDENHET_ID);
        orgUtkast.setEnhetsNamn(VARDENHET_NAME);
        orgUtkast.setVardgivarId(VARDGIVARE_ID);
        orgUtkast.setVardgivarNamn(VARDGIVARE_NAME);
        orgUtkast.setModel(INTYG_JSON);

        VardpersonReferens vpRef = new VardpersonReferens();
        vpRef.setHsaId(HOSPERSON_ID);
        vpRef.setNamn(HOSPERSON_NAME);

        orgUtkast.setSenastSparadAv(vpRef);
        orgUtkast.setSkapadAv(vpRef);

        return orgUtkast;
    }
}
