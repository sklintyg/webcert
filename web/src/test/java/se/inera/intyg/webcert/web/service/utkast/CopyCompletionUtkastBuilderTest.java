/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.infra.integration.hsa.model.*;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.common.internal.*;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.*;
import se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper;
import se.inera.intyg.intygstyper.fk7263.model.internal.Utlatande;
import se.inera.intyg.webcert.integration.pu.model.Person;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.dto.CopyUtkastBuilderResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateCompletionCopyRequest;
import se.inera.intyg.webcert.web.service.utkast.util.CreateIntygsIdStrategy;

@RunWith(MockitoJUnitRunner.class)
public class CopyCompletionUtkastBuilderTest {

    private static final String INTYG_ID = "abc123";
    private static final String INTYG_COPY_ID = "def456";

    private static final String INTYG_JSON = "A bit of text representing json";

    private static final String INTYG_TYPE = "fk7263";

    private static final Personnummer PATIENT_SSN = new Personnummer("19121212-1212");
    private static final String PATIENT_FNAME = "Adam";
    private static final String PATIENT_MNAME = "Bertil";
    private static final String PATIENT_LNAME = "Caesarsson";

    private static final String VARDENHET_ID = "SE00001234-5678";
    private static final String VARDENHET_NAME = "Vårdenheten 1";

    private static final String VARDGIVARE_ID = "SE00001234-1234";
    private static final String VARDGIVARE_NAME = "Vårdgivaren 1";

    private static final String HOSPERSON_ID = "SE12345678-0001";
    private static final String HOSPERSON_NAME = "Dr Börje Dengroth";
    private static final String MEDDELANDE_ID = "13";

    @Mock
    private IntygService mockIntygService;

    @Mock
    private UtkastRepository mockUtkastRepository;

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @Mock
    private ArendeService arendeService;

    @Mock
    private WebCertUserService webcertUserService;

    @Spy
    private CreateIntygsIdStrategy mockIdStrategy = new CreateIntygsIdStrategy() {
        @Override
        public String createId() {
            return INTYG_COPY_ID;
        }
    };

    private ModuleApi mockModuleApi;

    private HoSPersonal hoSPerson;

    private Patient patient;

    @InjectMocks
    private CopyCompletionUtkastBuilder copyCompletionBuilder = new CopyCompletionUtkastBuilder();

    @Before
    public void setup() {
        hoSPerson = new HoSPersonal();
        hoSPerson.setPersonId(HOSPERSON_ID);
        hoSPerson.setFullstandigtNamn(HOSPERSON_NAME);

        Vardgivare vardgivare = new Vardgivare();
        vardgivare.setVardgivarid(VARDGIVARE_ID);
        vardgivare.setVardgivarnamn(VARDGIVARE_NAME);

        Vardenhet vardenhet = new Vardenhet();
        vardenhet.setEnhetsid(VARDENHET_ID);
        vardenhet.setEnhetsnamn(VARDENHET_NAME);
        vardenhet.setVardgivare(vardgivare);
        hoSPerson.setVardenhet(vardenhet);

        patient = new Patient();
        patient.setPersonId(PATIENT_SSN);
    }

    @Before
    public void expectCallToModuleRegistry() throws Exception {
        this.mockModuleApi = mock(ModuleApi.class);
        when(moduleRegistry.getModuleApi(INTYG_TYPE)).thenReturn(mockModuleApi);
    }

    @Before
    public void expectCallToWebcertUserService() {
        when(webcertUserService.getUser()).thenReturn(createWebcertUser());
        when(webcertUserService.isAuthorizedForUnit(VARDGIVARE_ID, VARDENHET_ID, true)).thenReturn(true);
    }

    @Test
    public void testPopulateCompletionFromSignedIntyg() throws Exception {

        IntygContentHolder ich = createIntygContentHolder();
        when(mockIntygService.fetchIntygData(INTYG_ID, INTYG_TYPE, false)).thenReturn(ich);

        CreateCompletionCopyRequest copyRequest = buildCompletionRequest();
        copyRequest.setMeddelandeId("meddelandeId");
        Person patientDetails = new Person(PATIENT_SSN, false, PATIENT_FNAME, PATIENT_MNAME, PATIENT_LNAME, "Postadr", "12345", "postort");

        when(mockModuleApi.createNewInternalFromTemplate(any(CreateDraftCopyHolder.class), anyString())).thenReturn(INTYG_JSON);

        ValidateDraftResponse vdr = new ValidateDraftResponse(ValidationStatus.VALID, new ArrayList<ValidationMessage>());
        when(mockModuleApi.validateDraft(anyString())).thenReturn(vdr);

        CopyUtkastBuilderResponse builderResponse = copyCompletionBuilder.populateCopyUtkastFromSignedIntyg(copyRequest, patientDetails, true, false);

        assertNotNull(builderResponse.getUtkastCopy());
        assertNotNull(builderResponse.getUtkastCopy().getModel());
        assertEquals(INTYG_TYPE, builderResponse.getUtkastCopy().getIntygsTyp());
        assertEquals(PATIENT_SSN, builderResponse.getUtkastCopy().getPatientPersonnummer());
        assertEquals(PATIENT_FNAME, builderResponse.getUtkastCopy().getPatientFornamn());
        assertEquals(PATIENT_MNAME, builderResponse.getUtkastCopy().getPatientMellannamn());
        assertEquals(PATIENT_LNAME, builderResponse.getUtkastCopy().getPatientEfternamn());
        assertEquals(INTYG_ID, builderResponse.getUtkastCopy().getRelationIntygsId());

        ArgumentCaptor<CreateDraftCopyHolder> requestCaptor = ArgumentCaptor.forClass(CreateDraftCopyHolder.class);
        verify(mockModuleApi).createNewInternalFromTemplate(requestCaptor.capture(), anyString());

        // verify full name is set
        assertNotNull(requestCaptor.getValue().getPatient().getFullstandigtNamn());
        assertEquals(PATIENT_FNAME + " " + PATIENT_MNAME + " " + PATIENT_LNAME, requestCaptor.getValue().getPatient().getFullstandigtNamn());
    }

    @Test
    public void testPopulateCompletionFromOriginal() throws Exception {

        Utkast orgUtkast = createOriginalUtkast();
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(orgUtkast);

        CreateCompletionCopyRequest copyRequest = buildCompletionRequest();

        copyRequest.setMeddelandeId("meddelandeId");
        Person patientDetails = new Person(PATIENT_SSN, false, PATIENT_FNAME, PATIENT_MNAME, PATIENT_LNAME, "Postadr", "12345", "postort");

        when(mockModuleApi.createNewInternalFromTemplate(any(CreateDraftCopyHolder.class), anyString())).thenReturn(INTYG_JSON);

        ValidateDraftResponse vdr = new ValidateDraftResponse(ValidationStatus.VALID, new ArrayList<ValidationMessage>());
        when(mockModuleApi.validateDraft(anyString())).thenReturn(vdr);

        CopyUtkastBuilderResponse builderResponse = copyCompletionBuilder.populateCopyUtkastFromOrignalUtkast(copyRequest, patientDetails, true, false);

        assertNotNull(builderResponse.getUtkastCopy());
        assertNotNull(builderResponse.getUtkastCopy().getModel());
        assertEquals(INTYG_TYPE, builderResponse.getUtkastCopy().getIntygsTyp());
        assertEquals(PATIENT_SSN, builderResponse.getUtkastCopy().getPatientPersonnummer());
        assertEquals(PATIENT_FNAME, builderResponse.getUtkastCopy().getPatientFornamn());
        assertNotNull(builderResponse.getUtkastCopy().getPatientMellannamn());
        assertEquals(PATIENT_LNAME, builderResponse.getUtkastCopy().getPatientEfternamn());

        assertEquals(INTYG_ID, builderResponse.getUtkastCopy().getRelationIntygsId());
    }

    @Test
    public void testPopulateCompletionFromSignedIntygDecoratesWithReferensId() throws Exception {
        final String intygsTyp = "luse";
        final String meddelandeId = "meddelandeId";
        final String referensId = UUID.randomUUID().toString();
        Arende arende = new Arende();
        arende.setReferensId(referensId);
        when(moduleRegistry.getModuleApi(intygsTyp)).thenReturn(mockModuleApi);
        when(mockIntygService.fetchIntygData(INTYG_ID, intygsTyp, false)).thenReturn(createIntygContentHolder());
        when(mockModuleApi.createNewInternalFromTemplate(any(CreateDraftCopyHolder.class), anyString())).thenReturn(INTYG_JSON);
        when(mockModuleApi.validateDraft(anyString())).thenReturn(new ValidateDraftResponse(ValidationStatus.VALID, new ArrayList<ValidationMessage>()));
        when(arendeService.getArende(meddelandeId)).thenReturn(arende);

        CreateCompletionCopyRequest copyRequest = buildCompletionRequest();
        copyRequest.setMeddelandeId(meddelandeId);
        copyRequest.setTyp(intygsTyp);
        Person patientDetails = new Person(PATIENT_SSN, false, PATIENT_FNAME, PATIENT_MNAME, PATIENT_LNAME, "Postadr", "12345", "postort");

        copyCompletionBuilder.populateCopyUtkastFromSignedIntyg(copyRequest, patientDetails, true, false);

        ArgumentCaptor<CreateDraftCopyHolder> createDraftCopyHolderCaptor = ArgumentCaptor.forClass(CreateDraftCopyHolder.class);
        verify(mockModuleApi).createNewInternalFromTemplate(createDraftCopyHolderCaptor.capture(), anyString());

        assertNotNull(createDraftCopyHolderCaptor.getValue());
        assertEquals(meddelandeId, createDraftCopyHolderCaptor.getValue().getRelation().getMeddelandeId());
        assertEquals(RelationKod.KOMPLT, createDraftCopyHolderCaptor.getValue().getRelation().getRelationKod());
        assertEquals(referensId, createDraftCopyHolderCaptor.getValue().getRelation().getReferensId());
        assertEquals(INTYG_ID, createDraftCopyHolderCaptor.getValue().getRelation().getRelationIntygsId());
    }

    @Test
    public void testPopulateCompletionFromOriginalDecoratesWithReferensId() throws Exception {
        final String intygsTyp = "lisjp";
        final String meddelandeId = "meddelandeId";
        final String referensId = UUID.randomUUID().toString();
        Arende arende = new Arende();
        arende.setReferensId(referensId);
        when(moduleRegistry.getModuleApi(intygsTyp)).thenReturn(mockModuleApi);
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(createOriginalUtkast());
        when(mockModuleApi.validateDraft(anyString())).thenReturn(new ValidateDraftResponse(ValidationStatus.VALID, new ArrayList<ValidationMessage>()));
        when(arendeService.getArende(meddelandeId)).thenReturn(arende);
        when(mockModuleApi.createNewInternalFromTemplate(any(CreateDraftCopyHolder.class), anyString())).thenReturn(INTYG_JSON);
        when(webcertUserService.isAuthorizedForUnit(any(String.class), any(boolean.class))).thenReturn(true);

        CreateCompletionCopyRequest copyRequest = buildCompletionRequest();
        copyRequest.setMeddelandeId(meddelandeId);
        copyRequest.setTyp(intygsTyp);
        Person patientDetails = new Person(PATIENT_SSN, false, PATIENT_FNAME, PATIENT_MNAME, PATIENT_LNAME, "Postadr", "12345", "postort");

        copyCompletionBuilder.populateCopyUtkastFromOrignalUtkast(copyRequest, patientDetails, true, false);

        ArgumentCaptor<CreateDraftCopyHolder> createDraftCopyHolderCaptor = ArgumentCaptor.forClass(CreateDraftCopyHolder.class);
        verify(mockModuleApi).createNewInternalFromTemplate(createDraftCopyHolderCaptor.capture(), anyString());

        assertNotNull(createDraftCopyHolderCaptor.getValue());
        assertEquals(meddelandeId, createDraftCopyHolderCaptor.getValue().getRelation().getMeddelandeId());
        assertEquals(RelationKod.KOMPLT, createDraftCopyHolderCaptor.getValue().getRelation().getRelationKod());
        assertEquals(referensId, createDraftCopyHolderCaptor.getValue().getRelation().getReferensId());
        assertEquals(INTYG_ID, createDraftCopyHolderCaptor.getValue().getRelation().getRelationIntygsId());
    }

    private CreateCompletionCopyRequest buildCompletionRequest() {
        return new CreateCompletionCopyRequest(INTYG_ID, INTYG_TYPE, MEDDELANDE_ID, patient, hoSPerson);
    }

    private IntygContentHolder createIntygContentHolder() throws Exception {
        List<Status> status = new ArrayList<>();
        status.add(new Status(CertificateState.RECEIVED, "HV", LocalDateTime.now()));
        status.add(new Status(CertificateState.SENT, "FK", LocalDateTime.now()));
        Utlatande utlatande = new CustomObjectMapper().readValue(new ClassPathResource(
                "IntygDraftServiceImplTest/utlatande.json").getFile(), Utlatande.class);
        return new IntygContentHolder("<external-json/>", utlatande, status, false, null);
    }

    private Utkast createOriginalUtkast() {

        Utkast orgUtkast = new Utkast();
        orgUtkast.setIntygsId(INTYG_COPY_ID);
        orgUtkast.setIntygsTyp(INTYG_TYPE);
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

    private WebCertUser createWebcertUser() {
        WebCertUser user = new WebCertUser();
        user.setHsaId(HOSPERSON_ID);
        user.setNamn(HOSPERSON_NAME);
        se.inera.intyg.infra.integration.hsa.model.Vardgivare vGivare = new se.inera.intyg.infra.integration.hsa.model.Vardgivare();
        vGivare.setId(VARDGIVARE_ID);
        vGivare.setNamn(VARDENHET_NAME);
        user.setVardgivare(Arrays.asList(vGivare));
        AbstractVardenhet vardenhet = new se.inera.intyg.infra.integration.hsa.model.Vardenhet();
        vardenhet.setId(VARDENHET_ID);
        vardenhet.setNamn(VARDENHET_NAME);
        user.setValdVardenhet(vardenhet);
        return user;
    }
}
