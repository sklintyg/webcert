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
package se.inera.intyg.webcert.web.service.utkast;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
import se.inera.intyg.infra.integration.pu.model.Person;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.utkast.dto.CopyUtkastBuilderResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateCompletionCopyRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CopyCompletionUtkastBuilderTest extends AbstractBuilderTest {

    private static final String INTYG_TYPE = "fk7263";

    private static final String MEDDELANDE_ID = "13";
    private static final String KOMMENTAR = "Kommentar";
    private static final String INTYG_TYPE_VERSION = "1.0";

    @Mock
    private ArendeService arendeService;

    private ModuleApi mockModuleApi;

    @InjectMocks
    private CopyCompletionUtkastBuilder copyCompletionBuilder = new CopyCompletionUtkastBuilder();

    @Before
    public void expectCallToModuleRegistry() throws Exception {
        this.mockModuleApi = mock(ModuleApi.class);
        when(moduleRegistry.getModuleApi(INTYG_TYPE)).thenReturn(mockModuleApi);
    }

    @Test
    public void testPopulateCompletionFromSignedIntyg() throws Exception {

        IntygContentHolder ich = createIntygContentHolder();
        when(mockIntygService.fetchIntygData(INTYG_ID, INTYG_TYPE, false)).thenReturn(ich);

        CreateCompletionCopyRequest copyRequest = buildCompletionRequest();
        copyRequest.setMeddelandeId("meddelandeId");
        Person patientDetails = new Person(PATIENT_SSN, false, false, PATIENT_FNAME, PATIENT_MNAME, PATIENT_LNAME, "Postadr", "12345",
                "postort");

        ValidateDraftResponse vdr = new ValidateDraftResponse(ValidationStatus.VALID, new ArrayList<>());
        when(mockModuleApi.validateDraft(isNull())).thenReturn(vdr);
        Utlatande utlatande = new Fk7263Utlatande();
        when(mockModuleApi.getUtlatandeFromJson(any(String.class))).thenReturn(utlatande);

        CopyUtkastBuilderResponse builderResponse = copyCompletionBuilder.populateCopyUtkastFromSignedIntyg(copyRequest, patientDetails,
                true, false, false);

        assertNotNull(builderResponse.getUtkastCopy());
        assertNotNull(builderResponse.getUtkastCopy().getModel());
        assertEquals(INTYG_TYPE, builderResponse.getUtkastCopy().getIntygsTyp());
        assertEquals(PATIENT_SSN, builderResponse.getUtkastCopy().getPatientPersonnummer());
        assertEquals(PATIENT_FNAME, builderResponse.getUtkastCopy().getPatientFornamn());
        assertEquals(PATIENT_MNAME, builderResponse.getUtkastCopy().getPatientMellannamn());
        assertEquals(PATIENT_LNAME, builderResponse.getUtkastCopy().getPatientEfternamn());
        assertEquals(INTYG_ID, builderResponse.getUtkastCopy().getRelationIntygsId());

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
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(orgUtkast);

        CreateCompletionCopyRequest copyRequest = buildCompletionRequest();

        copyRequest.setMeddelandeId("meddelandeId");
        Person patientDetails = new Person(PATIENT_SSN, false, false, PATIENT_FNAME, PATIENT_MNAME, PATIENT_LNAME, "Postadr", "12345",
                "postort");

        ValidateDraftResponse vdr = new ValidateDraftResponse(ValidationStatus.VALID, new ArrayList<>());
        when(mockModuleApi.validateDraft(isNull())).thenReturn(vdr);

        CopyUtkastBuilderResponse builderResponse = copyCompletionBuilder.populateCopyUtkastFromOrignalUtkast(copyRequest, patientDetails,
                true, false, false);

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
        when(mockModuleApi.validateDraft(isNull()))
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

        copyCompletionBuilder.populateCopyUtkastFromSignedIntyg(copyRequest, patientDetails, true, false, false);

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
        final String meddelandeId = "meddelandeId";
        final String referensId = UUID.randomUUID().toString();
        Arende arende = new Arende();
        arende.setReferensId(referensId);
        when(moduleRegistry.getModuleApi(intygsTyp)).thenReturn(mockModuleApi);
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(createOriginalUtkast());
        when(mockModuleApi.validateDraft(isNull()))
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

        copyCompletionBuilder.populateCopyUtkastFromOrignalUtkast(copyRequest, patientDetails, true, false, false);

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
                .setContents("<external-json/>")
                .setUtlatande(utlatande)
                .setStatuses(status)
                .setRevoked(false)
                .setRelations(new Relations())
              //  .setReplacedByRelation(null)
              //  .setComplementedByRelation(null)
                .setDeceased(false)
                .setSekretessmarkering(false)
                .setPatientNameChangedInPU(false)
                .setPatientAddressChangedInPU(false)
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
