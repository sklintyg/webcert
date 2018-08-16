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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

import se.inera.intyg.common.db.model.internal.DbUtlatande;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.CreateDraftCopyHolder;
import se.inera.intyg.common.support.modules.support.api.dto.ValidateDraftResponse;
import se.inera.intyg.common.support.modules.support.api.dto.ValidationStatus;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.infra.integration.pu.model.Person;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.utkast.dto.CopyUtkastBuilderResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateUtkastFromTemplateRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CreateUtkastCopyBuilderTest extends AbstractBuilderTest {

    private static final String INTYG_TYPE_1 = "db";
    private static final String INTYG_TYPE_2 = "doi";

    private ModuleApi mockModuleApi1;
    private ModuleApi mockModuleApi2;

    @InjectMocks
    private CreateUtkastCopyBuilder createUtkastCopyBuilder = new CreateUtkastCopyBuilder();

    @Before
    public void expectCallToModuleRegistry() throws Exception {
        this.mockModuleApi1 = mock(ModuleApi.class);
        this.mockModuleApi2 = mock(ModuleApi.class);
        when(moduleRegistry.getModuleApi(INTYG_TYPE_1)).thenReturn(mockModuleApi1);
        when(moduleRegistry.getModuleApi(INTYG_TYPE_2)).thenReturn(mockModuleApi2);
    }

    @Test
    public void testPopulateRenewalUtkastFromSignedIntyg() throws Exception {

        IntygContentHolder ich = createIntygContentHolder();
        when(mockIntygService.fetchIntygData(INTYG_ID, INTYG_TYPE_1, false)).thenReturn(ich);

        CreateUtkastFromTemplateRequest createUtkastFromTemplateRequest = buildCreateUtkastFromTemplateRequest();
        Person patientDetails = new Person(PATIENT_SSN, false, false, PATIENT_FNAME, PATIENT_MNAME, PATIENT_LNAME, "Postadr", "12345",
                "postort");

        when(mockModuleApi2.createNewInternalFromTemplate(any(CreateDraftCopyHolder.class), any())).thenReturn(INTYG_JSON);

        ValidateDraftResponse vdr = new ValidateDraftResponse(ValidationStatus.VALID, new ArrayList<>());
        when(mockModuleApi2.validateDraft(anyString())).thenReturn(vdr);

        CopyUtkastBuilderResponse builderResponse = createUtkastCopyBuilder
                .populateCopyUtkastFromSignedIntyg(createUtkastFromTemplateRequest, patientDetails, false,
                        false, false);

        assertNotNull(builderResponse.getUtkastCopy());
        assertNotNull(builderResponse.getUtkastCopy().getModel());
        assertEquals(INTYG_TYPE_2, builderResponse.getUtkastCopy().getIntygsTyp());
        assertEquals(PATIENT_SSN, builderResponse.getUtkastCopy().getPatientPersonnummer());
        assertEquals(PATIENT_FNAME, builderResponse.getUtkastCopy().getPatientFornamn());
        assertEquals(PATIENT_MNAME, builderResponse.getUtkastCopy().getPatientMellannamn());
        assertEquals(PATIENT_LNAME, builderResponse.getUtkastCopy().getPatientEfternamn());

        ArgumentCaptor<CreateDraftCopyHolder> requestCaptor = ArgumentCaptor.forClass(CreateDraftCopyHolder.class);
        verify(mockModuleApi2).createNewInternalFromTemplate(requestCaptor.capture(), any());

        // verify full name is set
        assertNotNull(requestCaptor.getValue().getPatient().getFullstandigtNamn());
        assertEquals(PATIENT_FNAME + " " + PATIENT_MNAME + " " + PATIENT_LNAME,
                requestCaptor.getValue().getPatient().getFullstandigtNamn());

        assertNull(builderResponse.getUtkastCopy().getRelationKod());
        assertNull(builderResponse.getUtkastCopy().getRelationIntygsId());
    }

    @Test
    public void testPopulateCopyUtkastFromOriginalUtkast() throws Exception {

        Utkast orgUtkast = createOriginalUtkast();
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(orgUtkast);

        CreateUtkastFromTemplateRequest createUtkastFromTemplateRequest = buildCreateUtkastFromTemplateRequest();
        Person patientDetails = new Person(PATIENT_SSN, false, false, PATIENT_FNAME, PATIENT_MNAME, PATIENT_LNAME, "Postadr", "12345",
                "postort");

        when(mockModuleApi2.createNewInternalFromTemplate(any(CreateDraftCopyHolder.class), any())).thenReturn(INTYG_JSON);

        ValidateDraftResponse vdr = new ValidateDraftResponse(ValidationStatus.VALID, new ArrayList<>());
        when(mockModuleApi2.validateDraft(anyString())).thenReturn(vdr);

        CopyUtkastBuilderResponse builderResponse = createUtkastCopyBuilder
                .populateCopyUtkastFromOrignalUtkast(createUtkastFromTemplateRequest, patientDetails, true,
                        false, false);

        assertNotNull(builderResponse.getUtkastCopy());
        assertNotNull(builderResponse.getUtkastCopy().getModel());
        assertEquals(INTYG_TYPE_2, builderResponse.getUtkastCopy().getIntygsTyp());
        assertEquals(PATIENT_SSN, builderResponse.getUtkastCopy().getPatientPersonnummer());
        assertEquals(PATIENT_FNAME, builderResponse.getUtkastCopy().getPatientFornamn());
        assertNotNull(builderResponse.getUtkastCopy().getPatientMellannamn());
        assertEquals(PATIENT_LNAME, builderResponse.getUtkastCopy().getPatientEfternamn());

        assertEquals(RelationKod.KOPIA, builderResponse.getUtkastCopy().getRelationKod());
        assertEquals(INTYG_ID, builderResponse.getUtkastCopy().getRelationIntygsId());
    }

    private CreateUtkastFromTemplateRequest buildCreateUtkastFromTemplateRequest() {
        return new CreateUtkastFromTemplateRequest(INTYG_ID, INTYG_TYPE_2, patient, hoSPerson, INTYG_TYPE_1);
    }

    private IntygContentHolder createIntygContentHolder() throws Exception {
        List<Status> status = new ArrayList<>();
        status.add(new Status(CertificateState.RECEIVED, "HSVARD", LocalDateTime.now()));
        status.add(new Status(CertificateState.SENT, "SKV", LocalDateTime.now()));
        DbUtlatande utlatande = new CustomObjectMapper().readValue(new ClassPathResource(
                "IntygDraftServiceImplTest/db-utlatande.json").getFile(), DbUtlatande.class);
        return IntygContentHolder.builder()
                .setContents("<external-json/>")
                .setUtlatande(utlatande)
                .setStatuses(status)
                .setRevoked(false)
                .setRelations(new Relations())
                .setDeceased(false)
                .setSekretessmarkering(false)
                .setPatientNameChangedInPU(false)
                .setPatientAddressChangedInPU(false)
                .build();
    }

    private Utkast createOriginalUtkast() {

        Utkast orgUtkast = new Utkast();
        orgUtkast.setIntygsId(INTYG_COPY_ID);
        orgUtkast.setIntygsTyp(INTYG_TYPE_1);
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
