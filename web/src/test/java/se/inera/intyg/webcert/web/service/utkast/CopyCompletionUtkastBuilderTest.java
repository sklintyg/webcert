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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import se.inera.intyg.common.fk7263.model.internal.Fk7263Utlatande;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.CreateDraftCopyHolder;
import se.inera.intyg.common.support.modules.support.api.dto.ValidateDraftResponse;
import se.inera.intyg.common.support.modules.support.api.dto.ValidationStatus;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.infra.pu.integration.api.model.Person;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.facade.util.DefaultTypeAheadProvider;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.log.factory.LogRequestFactory;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateCompletionCopyRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.UtkastBuilderResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CopyCompletionUtkastBuilderTest extends AbstractBuilderTest {

    private static final String INTYG_TYPE = "fk7263";

    private static final String MEDDELANDE_ID = "13";
    private static final String KOMMENTAR = "Kommentar";
    private static final String INTYG_TYPE_VERSION = "1.0";

    @Mock
    private ArendeService arendeService;

    private ModuleApi mockModuleApi;

    @Mock
    private LogService logService;

    @Mock
    private LogRequestFactory logRequestFactory;

    @Mock
    private DefaultTypeAheadProvider defaultTypeAheadProvider;

    @InjectMocks
    private CopyCompletionUtkastBuilder copyCompletionBuilder = new CopyCompletionUtkastBuilder();

    @Before
    public void expectCallToModuleRegistry() throws Exception {
        this.mockModuleApi = mock(ModuleApi.class);
        when(moduleRegistry.getModuleApi(INTYG_TYPE, INTYG_TYPE_VERSION)).thenReturn(mockModuleApi);
    }

    @Test
    public void testPopulateCompletionFromSignedIntyg() throws Exception {

        IntygContentHolder ich = createIntygContentHolder();
        when(mockIntygService.fetchIntygData(INTYG_ID, INTYG_TYPE)).thenReturn(ich);

        CreateCompletionCopyRequest copyRequest = buildCompletionRequest();
        copyRequest.setMeddelandeId("meddelandeId");
        Person patientDetails = new Person(PATIENT_SSN, false, false, PATIENT_FNAME, PATIENT_MNAME, PATIENT_LNAME, "Postadr", "12345",
            "postort");

        ValidateDraftResponse vdr = new ValidateDraftResponse(ValidationStatus.VALID, new ArrayList<>());
        when(mockModuleApi.validateDraft(isNull(), eq(defaultTypeAheadProvider))).thenReturn(vdr);
        Utlatande utlatande = new Fk7263Utlatande();
        when(mockModuleApi.getUtlatandeFromJson(any(String.class))).thenReturn(utlatande);

        UtkastBuilderResponse builderResponse = copyCompletionBuilder.populateCopyUtkastFromSignedIntyg(copyRequest, patientDetails,
            true);

        assertNotNull(builderResponse.getUtkast());
        assertNotNull(builderResponse.getUtkast().getModel());
        assertEquals(INTYG_TYPE, builderResponse.getUtkast().getIntygsTyp());
        assertEquals(PATIENT_SSN, builderResponse.getUtkast().getPatientPersonnummer());
        assertEquals(PATIENT_FNAME, builderResponse.getUtkast().getPatientFornamn());
        assertEquals(PATIENT_MNAME, builderResponse.getUtkast().getPatientMellannamn());
        assertEquals(PATIENT_LNAME, builderResponse.getUtkast().getPatientEfternamn());
        assertEquals(INTYG_ID, builderResponse.getUtkast().getRelationIntygsId());

        ArgumentCaptor<CreateDraftCopyHolder> requestCaptor = ArgumentCaptor.forClass(CreateDraftCopyHolder.class);
        verify(mockModuleApi).createCompletionFromTemplate(requestCaptor.capture(), any(), eq(KOMMENTAR));

        // verify full name is set
        assertNotNull(requestCaptor.getValue().getPatient().getFullstandigtNamn());
        assertEquals(PATIENT_FNAME + " " + PATIENT_MNAME + " " + PATIENT_LNAME,
            requestCaptor.getValue().getPatient().getFullstandigtNamn());
    }

    @Test
    public void testPopulateCompletionFromOriginal() throws Exception {

        Utkast orgUtkast = createOriginalUtkast();
        when(mockUtkastRepository.findById(INTYG_ID)).thenReturn(Optional.of(orgUtkast));

        CreateCompletionCopyRequest copyRequest = buildCompletionRequest();

        copyRequest.setMeddelandeId("meddelandeId");
        Person patientDetails = new Person(PATIENT_SSN, false, false, PATIENT_FNAME, PATIENT_MNAME, PATIENT_LNAME, "Postadr", "12345",
            "postort");

        ValidateDraftResponse vdr = new ValidateDraftResponse(ValidationStatus.VALID, new ArrayList<>());
        when(mockModuleApi.validateDraft(isNull(), eq(defaultTypeAheadProvider))).thenReturn(vdr);

        UtkastBuilderResponse builderResponse = copyCompletionBuilder.populateCopyUtkastFromOrignalUtkast(copyRequest, patientDetails,
            true);

        assertNotNull(builderResponse.getUtkast());
        assertNotNull(builderResponse.getUtkast().getModel());
        assertEquals(INTYG_TYPE, builderResponse.getUtkast().getIntygsTyp());
        assertEquals(PATIENT_SSN, builderResponse.getUtkast().getPatientPersonnummer());
        assertEquals(PATIENT_FNAME, builderResponse.getUtkast().getPatientFornamn());
        assertNotNull(builderResponse.getUtkast().getPatientMellannamn());
        assertEquals(PATIENT_LNAME, builderResponse.getUtkast().getPatientEfternamn());

        assertEquals(INTYG_ID, builderResponse.getUtkast().getRelationIntygsId());
    }

    @Test
    public void testPopulateCompletionFromSignedIntygDecoratesWithReferensId() throws Exception {
        final String intygsTyp = "luse";
        final String intygsTypVersion = "1.0";
        final String meddelandeId = "meddelandeId";
        final String referensId = UUID.randomUUID().toString();
        Arende arende = new Arende();
        arende.setReferensId(referensId);
        when(moduleRegistry.getModuleApi(intygsTyp, intygsTypVersion)).thenReturn(mockModuleApi);
        when(mockIntygService.fetchIntygData(INTYG_ID, intygsTyp)).thenReturn(createIntygContentHolder());
        when(mockModuleApi.validateDraft(isNull(), eq(defaultTypeAheadProvider)))
            .thenReturn(new ValidateDraftResponse(ValidationStatus.VALID, new ArrayList<>()));
        Utlatande utlatande = new Fk7263Utlatande();
        when(mockModuleApi.getUtlatandeFromJson(any(String.class))).thenReturn(utlatande);
        when(arendeService.getArende(meddelandeId)).thenReturn(arende);

        CreateCompletionCopyRequest copyRequest = buildCompletionRequest();
        copyRequest.setMeddelandeId(meddelandeId);
        copyRequest.setTyp(intygsTyp);
        copyRequest.setOriginalIntygTyp(intygsTyp);
        Person patientDetails = new Person(PATIENT_SSN, false, false, PATIENT_FNAME, PATIENT_MNAME, PATIENT_LNAME, "Postadr", "12345",
            "postort");

        copyCompletionBuilder.populateCopyUtkastFromSignedIntyg(copyRequest, patientDetails, true);

        ArgumentCaptor<CreateDraftCopyHolder> createDraftCopyHolderCaptor = ArgumentCaptor.forClass(CreateDraftCopyHolder.class);
        verify(mockModuleApi).createCompletionFromTemplate(createDraftCopyHolderCaptor.capture(), any(), eq(KOMMENTAR));

        assertNotNull(createDraftCopyHolderCaptor.getValue());
        assertEquals(meddelandeId, createDraftCopyHolderCaptor.getValue().getRelation().getMeddelandeId());
        assertEquals(RelationKod.KOMPLT, createDraftCopyHolderCaptor.getValue().getRelation().getRelationKod());
        assertEquals(referensId, createDraftCopyHolderCaptor.getValue().getRelation().getReferensId());
        assertEquals(INTYG_ID, createDraftCopyHolderCaptor.getValue().getRelation().getRelationIntygsId());
    }

    @Test
    public void testPopulateCompletionFromOriginalDecoratesWithReferensId() throws Exception {
        final String intygsTyp = "lisjp";
        final String intygsTypVersion = "1.0";
        final String meddelandeId = "meddelandeId";
        final String referensId = UUID.randomUUID().toString();
        Arende arende = new Arende();
        arende.setReferensId(referensId);
        when(moduleRegistry.getModuleApi(intygsTyp, intygsTypVersion)).thenReturn(mockModuleApi);
        when(mockUtkastRepository.findById(INTYG_ID)).thenReturn(Optional.of(createOriginalUtkast()));
        when(mockModuleApi.validateDraft(isNull(), eq(defaultTypeAheadProvider)))
            .thenReturn(new ValidateDraftResponse(ValidationStatus.VALID, new ArrayList<>()));
        when(arendeService.getArende(meddelandeId)).thenReturn(arende);
        Utlatande utlatande = new Fk7263Utlatande();
        when(mockModuleApi.getUtlatandeFromJson(any(String.class))).thenReturn(utlatande);

        CreateCompletionCopyRequest copyRequest = buildCompletionRequest();
        copyRequest.setMeddelandeId(meddelandeId);
        copyRequest.setTyp(intygsTyp);
        copyRequest.setOriginalIntygTyp(intygsTyp);
        Person patientDetails = new Person(PATIENT_SSN, false, false, PATIENT_FNAME, PATIENT_MNAME, PATIENT_LNAME, "Postadr", "12345",
            "postort");

        copyCompletionBuilder.populateCopyUtkastFromOrignalUtkast(copyRequest, patientDetails, true);

        ArgumentCaptor<CreateDraftCopyHolder> createDraftCopyHolderCaptor = ArgumentCaptor.forClass(CreateDraftCopyHolder.class);
        verify(mockModuleApi).createCompletionFromTemplate(createDraftCopyHolderCaptor.capture(), any(), eq(KOMMENTAR));

        assertNotNull(createDraftCopyHolderCaptor.getValue());
        assertEquals(meddelandeId, createDraftCopyHolderCaptor.getValue().getRelation().getMeddelandeId());
        assertEquals(RelationKod.KOMPLT, createDraftCopyHolderCaptor.getValue().getRelation().getRelationKod());
        assertEquals(referensId, createDraftCopyHolderCaptor.getValue().getRelation().getReferensId());
        assertEquals(INTYG_ID, createDraftCopyHolderCaptor.getValue().getRelation().getRelationIntygsId());
    }

    private CreateCompletionCopyRequest buildCompletionRequest() {
        return new CreateCompletionCopyRequest(INTYG_ID, INTYG_TYPE, MEDDELANDE_ID, patient, hoSPerson, KOMMENTAR);
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
            // .setReplacedByRelation(null)
            // .setComplementedByRelation(null)
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
