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

package se.inera.intyg.webcert.web.web.util.resourcelink;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.modules.registry.IntygModule;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.access.AccessResult;
import se.inera.intyg.webcert.web.service.access.AccessResultCode;
import se.inera.intyg.webcert.web.service.access.CertificateAccessService;
import se.inera.intyg.webcert.web.service.access.DraftAccessService;
import se.inera.intyg.webcert.web.service.access.LockedDraftAccessService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeListItem;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygModuleDTO;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.DraftHolder;
import se.inera.intyg.webcert.web.web.util.resourcelinks.ResourceLinkHelperImpl;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLink;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLinkType;

@RunWith(MockitoJUnitRunner.class)
public class ResourceLinkHelperImplTest {

    @Mock
    private DraftAccessService draftAccessService;

    @Mock
    private LockedDraftAccessService lockedDraftAccessService;

    @Mock
    private CertificateAccessService certificateAccessService;

    @InjectMocks
    private ResourceLinkHelperImpl resourceLinkHelper;

    @Test
    public void validActionsForIntygModuleWithAccessAllowed() {
        final String intygsTyp = "intygstyp";
        final Personnummer personnummer = Personnummer.createPersonnummer("191212121212").get();

        doReturn(AccessResult.noProblem()).when(draftAccessService).allowToCreateDraft(intygsTyp, personnummer);

        final ActionLink expectedActionLink = new ActionLink();
        expectedActionLink.setType(ActionLinkType.SKAPA_UTKAST);

        final IntygModuleDTO intygModuleDTO = createIntygModuleDTO(intygsTyp);
        final List<IntygModuleDTO> intygModuleDTOList = Arrays.asList(intygModuleDTO);
        resourceLinkHelper.decorateIntygModuleWithValidActionLinks(intygModuleDTO, personnummer);

        final List<ActionLink> actualLinks = intygModuleDTO.getLinks();

        assertNotNull(actualLinks);
        assertEquals("Should be one link", 1, actualLinks.size());
        assertEquals("ActionLink type should be same", expectedActionLink.getType(), actualLinks.get(0).getType());
    }

    @Test
    public void noValidActionsForIntygModuleWithAccessAllowed() {
        final String intygsTyp = "intygstyp";
        final Personnummer personnummer = Personnummer.createPersonnummer("191212121212").get();

        doReturn(AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION, "No access")).when(draftAccessService)
            .allowToCreateDraft(intygsTyp, personnummer);

        final ActionLink expectedActionLink = new ActionLink();

        final IntygModuleDTO intygModuleDTO = createIntygModuleDTO(intygsTyp);
        final List<IntygModuleDTO> intygModuleDTOList = Arrays.asList(intygModuleDTO);
        resourceLinkHelper.decorateIntygModuleWithValidActionLinks(intygModuleDTO, personnummer);

        final List<ActionLink> actualLinks = intygModuleDTO.getLinks();

        assertNotNull(actualLinks);
        assertEquals("Should be no links", 0, actualLinks.size());
    }

    private IntygModuleDTO createIntygModuleDTO(String intygsTyp) {
        return new IntygModuleDTO(new IntygModule(intygsTyp, "", "", "", "", "", "", "", "", false, false));
    }

    @Test
    public void validActionsForLockedDraftHolderWithAccessAllowed() {
        final String intygsTyp = "intygstyp";
        final Personnummer patient = Personnummer.createPersonnummer("191212121212").get();
        final Vardenhet vardenhet = mock(Vardenhet.class);

        doReturn(AccessResult.noProblem()).when(lockedDraftAccessService).allowedToInvalidateLockedUtkast(intygsTyp, vardenhet, patient);
        doReturn(AccessResult.noProblem()).when(lockedDraftAccessService).allowedToCopyLockedUtkast(intygsTyp, vardenhet, patient);
        doReturn(AccessResult.noProblem()).when(lockedDraftAccessService).allowToPrint(intygsTyp, vardenhet, patient);

        final List<ActionLink> expectedLinks = new ArrayList<>();
        expectedLinks.add(new ActionLink(ActionLinkType.MAKULERA_UTKAST));
        expectedLinks.add(new ActionLink(ActionLinkType.KOPIERA_UTKAST));
        expectedLinks.add(new ActionLink(ActionLinkType.SKRIV_UT_UTKAST));

        final DraftHolder draftHolder = new DraftHolder();
        draftHolder.setStatus(UtkastStatus.DRAFT_LOCKED);

        resourceLinkHelper.decorateUtkastWithValidActionLinks(draftHolder, intygsTyp, vardenhet, patient);

        final List<ActionLink> actualLinks = draftHolder.getLinks();

        assertLinks(expectedLinks, actualLinks);
    }

    @Test
    public void noValidActionsForLockedDraftHolderWithAccessAllowed() {
        final String intygsTyp = "intygstyp";
        final Personnummer patient = Personnummer.createPersonnummer("191212121212").get();
        final Vardenhet vardenhet = mock(Vardenhet.class);

        doReturn(AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION, "No access")).when(lockedDraftAccessService)
            .allowedToInvalidateLockedUtkast(intygsTyp, vardenhet, patient);
        doReturn(AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION, "No access")).when(lockedDraftAccessService)
            .allowedToCopyLockedUtkast(intygsTyp, vardenhet, patient);
        doReturn(AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION, "No access")).when(lockedDraftAccessService)
            .allowToPrint(intygsTyp, vardenhet, patient);

        final List<ActionLink> expectedLinks = new ArrayList<>();

        final DraftHolder draftHolder = new DraftHolder();
        draftHolder.setStatus(UtkastStatus.DRAFT_LOCKED);

        resourceLinkHelper.decorateUtkastWithValidActionLinks(draftHolder, intygsTyp, vardenhet, patient);

        final List<ActionLink> actualLinks = draftHolder.getLinks();

        assertLinks(expectedLinks, actualLinks);
    }

    private void assertLinks(List<ActionLink> expectedLinks, List<ActionLink> actualLinks) {
        assertNotNull(actualLinks);
        assertEquals(expectedLinks.size(), actualLinks.size());
        for (int i = 0; i < expectedLinks.size(); i++) {
            assertEquals(expectedLinks.get(i), actualLinks.get(i));
        }
    }

    @Test
    public void validActionsForDraftHolderWithAccessAllowed() {
        final String intygsTyp = "intygstyp";
        final Personnummer patient = Personnummer.createPersonnummer("191212121212").get();
        final Vardenhet vardenhet = mock(Vardenhet.class);

        doReturn(AccessResult.noProblem()).when(draftAccessService).allowToEditDraft(intygsTyp, vardenhet, patient);
        doReturn(AccessResult.noProblem()).when(draftAccessService).allowToDeleteDraft(intygsTyp, vardenhet, patient);
        doReturn(AccessResult.noProblem()).when(draftAccessService).allowToPrintDraft(intygsTyp, vardenhet, patient);
        doReturn(AccessResult.noProblem()).when(certificateAccessService).allowToCreateQuestion(intygsTyp, vardenhet, patient);
        doReturn(AccessResult.noProblem()).when(certificateAccessService).allowToReadQuestions(intygsTyp, vardenhet, patient);
        doReturn(AccessResult.noProblem()).when(certificateAccessService).allowToAnswerAdminQuestion(intygsTyp, vardenhet, patient);
        doReturn(AccessResult.noProblem()).when(certificateAccessService).allowToAnswerComplementQuestion(intygsTyp, vardenhet, patient,
                true);
        doReturn(AccessResult.noProblem()).when(certificateAccessService).allowToSetComplementAsHandled(intygsTyp, vardenhet, patient);
        doReturn(AccessResult.noProblem()).when(certificateAccessService).allowToAnswerComplementQuestion(intygsTyp, vardenhet, patient,
            false);
        doReturn(AccessResult.noProblem()).when(certificateAccessService).allowToForwardQuestions(intygsTyp, vardenhet, patient);

        final List<ActionLink> expectedLinks = new ArrayList<>();
        expectedLinks.add(new ActionLink(ActionLinkType.REDIGERA_UTKAST));
        expectedLinks.add(new ActionLink(ActionLinkType.TA_BORT_UTKAST));
        expectedLinks.add(new ActionLink(ActionLinkType.SKRIV_UT_UTKAST));
        expectedLinks.add(new ActionLink(ActionLinkType.SKAPA_FRAGA));
        expectedLinks.add(new ActionLink(ActionLinkType.LASA_FRAGA));
        expectedLinks.add(new ActionLink(ActionLinkType.BESVARA_FRAGA));
        expectedLinks.add(new ActionLink(ActionLinkType.BESVARA_KOMPLETTERING));
        expectedLinks.add(new ActionLink(ActionLinkType.BESVARA_KOMPLETTERING_MED_MEDDELANDE));
        expectedLinks.add(new ActionLink(ActionLinkType.VIDAREBEFODRA_FRAGA));
        expectedLinks.add(new ActionLink(ActionLinkType.MARKERA_KOMPLETTERING_SOM_HANTERAD));

        final DraftHolder draftHolder = new DraftHolder();

        resourceLinkHelper.decorateUtkastWithValidActionLinks(draftHolder, intygsTyp, vardenhet, patient);

        final List<ActionLink> actualLinks = draftHolder.getLinks();

        assertLinks(expectedLinks, actualLinks);
    }

    @Test
    public void noValidActionsForDraftHolderWithAccessAllowed() {
        final String intygsTyp = "intygstyp";
        final Personnummer patient = Personnummer.createPersonnummer("191212121212").get();
        final Vardenhet vardenhet = mock(Vardenhet.class);

        doReturn(AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION, "No access")).when(draftAccessService)
            .allowToEditDraft(intygsTyp, vardenhet, patient);
        doReturn(AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION, "No access")).when(draftAccessService)
            .allowToDeleteDraft(intygsTyp, vardenhet, patient);
        doReturn(AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION, "No access")).when(draftAccessService)
            .allowToPrintDraft(intygsTyp, vardenhet, patient);
        doReturn(AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION, "No access")).when(certificateAccessService)
            .allowToCreateQuestion(intygsTyp, vardenhet, patient);
        doReturn(AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION, "No access")).when(certificateAccessService)
            .allowToReadQuestions(intygsTyp, vardenhet, patient);
        doReturn(AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION, "No access")).when(certificateAccessService)
            .allowToAnswerAdminQuestion(intygsTyp, vardenhet, patient);
        doReturn(AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION, "No access")).when(certificateAccessService)
            .allowToAnswerComplementQuestion(intygsTyp, vardenhet, patient,
                true);
        doReturn(AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION, "No access")).when(certificateAccessService)
            .allowToAnswerComplementQuestion(intygsTyp, vardenhet, patient,
                false);
        doReturn(AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION, "No access")).when(certificateAccessService)
            .allowToSetComplementAsHandled(intygsTyp, vardenhet, patient);
        doReturn(AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION, "No access")).when(certificateAccessService)
            .allowToForwardQuestions(intygsTyp, vardenhet, patient);

        final List<ActionLink> expectedLinks = new ArrayList<>();

        final DraftHolder draftHolder = new DraftHolder();

        resourceLinkHelper.decorateUtkastWithValidActionLinks(draftHolder, intygsTyp, vardenhet, patient);

        final List<ActionLink> actualLinks = draftHolder.getLinks();

        assertLinks(expectedLinks, actualLinks);
    }

    @Test
    public void validActionsForIntygContentHolderWithAccessAllowed() {
        final String intygsTyp = "intygstyp";
        final Personnummer patient = Personnummer.createPersonnummer("191212121212").get();
        final Vardenhet vardenhet = mock(Vardenhet.class);

        doReturn(AccessResult.noProblem()).when(certificateAccessService).allowToRenew(intygsTyp, vardenhet, patient);
        doReturn(AccessResult.noProblem()).when(certificateAccessService).allowToInvalidate(intygsTyp, vardenhet, patient);
        doReturn(AccessResult.noProblem()).when(certificateAccessService).allowToPrint(intygsTyp, vardenhet, patient, false);
        doReturn(AccessResult.noProblem()).when(certificateAccessService).allowToReplace(intygsTyp, vardenhet, patient);
        doReturn(AccessResult.noProblem()).when(certificateAccessService).allowToSend(intygsTyp, vardenhet, patient);

        doReturn(AccessResult.noProblem()).when(certificateAccessService).allowToCreateQuestion(intygsTyp, vardenhet, patient);
        doReturn(AccessResult.noProblem()).when(certificateAccessService).allowToReadQuestions(intygsTyp, vardenhet, patient);
        doReturn(AccessResult.noProblem()).when(certificateAccessService).allowToAnswerAdminQuestion(intygsTyp, vardenhet, patient);
        doReturn(AccessResult.noProblem()).when(certificateAccessService).allowToAnswerComplementQuestion(intygsTyp, vardenhet, patient,
            true);
        doReturn(AccessResult.noProblem()).when(certificateAccessService).allowToAnswerComplementQuestion(intygsTyp, vardenhet, patient,
            false);
        doReturn(AccessResult.noProblem()).when(certificateAccessService).allowToSetComplementAsHandled(intygsTyp, vardenhet, patient);
        doReturn(AccessResult.noProblem()).when(certificateAccessService).allowToForwardQuestions(intygsTyp, vardenhet, patient);

        final List<ActionLink> expectedLinks = new ArrayList<>();
        expectedLinks.add(new ActionLink(ActionLinkType.FORNYA_INTYG));
        expectedLinks.add(new ActionLink(ActionLinkType.MAKULERA_INTYG));
        expectedLinks.add(new ActionLink(ActionLinkType.SKRIV_UT_INTYG));
        expectedLinks.add(new ActionLink(ActionLinkType.ERSATT_INTYG));
        expectedLinks.add(new ActionLink(ActionLinkType.SKICKA_INTYG));
        expectedLinks.add(new ActionLink(ActionLinkType.SKAPA_FRAGA));
        expectedLinks.add(new ActionLink(ActionLinkType.LASA_FRAGA));
        expectedLinks.add(new ActionLink(ActionLinkType.BESVARA_FRAGA));
        expectedLinks.add(new ActionLink(ActionLinkType.BESVARA_KOMPLETTERING));
        expectedLinks.add(new ActionLink(ActionLinkType.BESVARA_KOMPLETTERING_MED_MEDDELANDE));
        expectedLinks.add(new ActionLink(ActionLinkType.VIDAREBEFODRA_FRAGA));
        expectedLinks.add(new ActionLink(ActionLinkType.MARKERA_KOMPLETTERING_SOM_HANTERAD));

        final Utlatande utlatande = mock(Utlatande.class);
        doReturn(intygsTyp).when(utlatande).getTyp();
        final GrundData grundData = mock(GrundData.class);
        doReturn(grundData).when(utlatande).getGrundData();
        final HoSPersonal skapadAv = mock(HoSPersonal.class);
        doReturn(skapadAv).when(grundData).getSkapadAv();
        doReturn(vardenhet).when(skapadAv).getVardenhet();
        final Patient patientMock = mock(Patient.class);
        doReturn(patientMock).when(grundData).getPatient();
        doReturn(patient).when(patientMock).getPersonId();

        final IntygContentHolder intygContentHolder = IntygContentHolder.builder()
            .setRevoked(false)
            .setDeceased(false)
            .setSekretessmarkering(false)
            .setPatientNameChangedInPU(false)
            .setPatientAddressChangedInPU(false)
            .setUtlatande(utlatande)
            .build();

        resourceLinkHelper.decorateIntygWithValidActionLinks(intygContentHolder);

        final List<ActionLink> actualLinks = intygContentHolder.getLinks();

        assertLinks(expectedLinks, actualLinks);
    }

    @Test
    public void noValidActionsForIntygContentHolderWithAccessAllowed() {
        final String intygsTyp = "intygstyp";
        final Personnummer patient = Personnummer.createPersonnummer("191212121212").get();
        final Vardenhet vardenhet = mock(Vardenhet.class);

        doReturn(AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION, "No access")).when(certificateAccessService)
            .allowToRenew(intygsTyp, vardenhet, patient);
        doReturn(AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION, "No access")).when(certificateAccessService)
            .allowToInvalidate(intygsTyp, vardenhet, patient);
        doReturn(AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION, "No access")).when(certificateAccessService)
            .allowToPrint(intygsTyp, vardenhet, patient, false);
        doReturn(AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION, "No access")).when(certificateAccessService)
            .allowToReplace(intygsTyp, vardenhet, patient);
        doReturn(AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION, "No access")).when(certificateAccessService)
            .allowToSend(intygsTyp, vardenhet, patient);

        doReturn(AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION, "No access")).when(certificateAccessService)
            .allowToCreateQuestion(intygsTyp, vardenhet, patient);
        doReturn(AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION, "No access")).when(certificateAccessService)
            .allowToReadQuestions(intygsTyp, vardenhet, patient);
        doReturn(AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION, "No access")).when(certificateAccessService)
            .allowToAnswerAdminQuestion(intygsTyp, vardenhet, patient);
        doReturn(AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION, "No access")).when(certificateAccessService)
            .allowToAnswerComplementQuestion(intygsTyp, vardenhet, patient,
                true);
        doReturn(AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION, "No access")).when(certificateAccessService)
            .allowToAnswerComplementQuestion(intygsTyp, vardenhet, patient,
                false);
        doReturn(AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION, "No access")).when(certificateAccessService)
            .allowToSetComplementAsHandled(intygsTyp, vardenhet, patient);
        doReturn(AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION, "No access")).when(certificateAccessService)
            .allowToForwardQuestions(intygsTyp, vardenhet, patient);

        final List<ActionLink> expectedLinks = new ArrayList<>();

        final Utlatande utlatande = mock(Utlatande.class);
        doReturn(intygsTyp).when(utlatande).getTyp();
        final GrundData grundData = mock(GrundData.class);
        doReturn(grundData).when(utlatande).getGrundData();
        final HoSPersonal skapadAv = mock(HoSPersonal.class);
        doReturn(skapadAv).when(grundData).getSkapadAv();
        doReturn(vardenhet).when(skapadAv).getVardenhet();
        final Patient patientMock = mock(Patient.class);
        doReturn(patientMock).when(grundData).getPatient();
        doReturn(patient).when(patientMock).getPersonId();

        final IntygContentHolder intygContentHolder = IntygContentHolder.builder()
            .setRevoked(false)
            .setDeceased(false)
            .setSekretessmarkering(false)
            .setPatientNameChangedInPU(false)
            .setPatientAddressChangedInPU(false)
            .setUtlatande(utlatande)
            .build();

        resourceLinkHelper.decorateIntygWithValidActionLinks(intygContentHolder);

        final List<ActionLink> actualLinks = intygContentHolder.getLinks();

        assertLinks(expectedLinks, actualLinks);
    }

    @Test
    public void validActionsForListIntygEntryWithAccessAllowed() {
        final String intygsTyp = "intygstyp";
        final Personnummer patient = Personnummer.createPersonnummer("191212121212").get();
        final Vardenhet vardenhet = mock(Vardenhet.class);

        doReturn(AccessResult.noProblem()).when(certificateAccessService).allowToRead(anyString(), any(Vardenhet.class),
            any(Personnummer.class));
        doReturn(AccessResult.noProblem()).when(certificateAccessService).allowToRenew(anyString(), any(Vardenhet.class),
            any(Personnummer.class));

        final List<ActionLink> expectedLinks = new ArrayList<>();
        expectedLinks.add(new ActionLink(ActionLinkType.LASA_INTYG));
        expectedLinks.add(new ActionLink(ActionLinkType.FORNYA_INTYG));

        final ListIntygEntry listIntygEntry = new ListIntygEntry();
        listIntygEntry.setVardenhetId("vardenhetsid");
        listIntygEntry.setVardenhetId("vardgivareid");
        listIntygEntry.setIntygType(intygsTyp);

        final List<ListIntygEntry> listIntygEntryList = Arrays.asList(listIntygEntry);

        resourceLinkHelper.decorateIntygWithValidActionLinks(listIntygEntryList, patient);

        final List<ActionLink> actualLinks = listIntygEntry.getLinks();

        assertLinks(expectedLinks, actualLinks);
    }

    @Test
    public void noValidActionsForListIntygEntryWithAccessAllowed() {
        final String intygsTyp = "intygstyp";
        final Personnummer patient = Personnummer.createPersonnummer("191212121212").get();
        final Vardenhet vardenhet = mock(Vardenhet.class);

        doReturn(AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION, "No access")).when(certificateAccessService).allowToRead(
            anyString(), any(Vardenhet.class),
            any(Personnummer.class));
        doReturn(AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION, "No access")).when(certificateAccessService).allowToRenew(
            anyString(), any(Vardenhet.class),
            any(Personnummer.class));

        final List<ActionLink> expectedLinks = new ArrayList<>();

        final ListIntygEntry listIntygEntry = new ListIntygEntry();
        listIntygEntry.setVardenhetId("vardenhetsid");
        listIntygEntry.setVardenhetId("vardgivareid");
        listIntygEntry.setIntygType(intygsTyp);

        final List<ListIntygEntry> listIntygEntryList = Arrays.asList(listIntygEntry);

        resourceLinkHelper.decorateIntygWithValidActionLinks(listIntygEntryList, patient);

        final List<ActionLink> actualLinks = listIntygEntry.getLinks();

        assertLinks(expectedLinks, actualLinks);
    }

    @Test
    public void validActionsForArendeListItemWithAccessAllowed() {
        final String intygsTyp = "intygstyp";
        final Personnummer patient = Personnummer.createPersonnummer("191212121212").get();
        final Vardenhet vardenhet = mock(Vardenhet.class);

        doReturn(AccessResult.noProblem()).when(certificateAccessService).allowToForwardQuestions(anyString(), any(Vardenhet.class),
            any(Personnummer.class));

        final List<ActionLink> expectedLinks = new ArrayList<>();
        expectedLinks.add(new ActionLink(ActionLinkType.VIDAREBEFODRA_FRAGA));

        final ArendeListItem arendeListItem = new ArendeListItem();
        arendeListItem.setIntygTyp(intygsTyp);
        arendeListItem.setPatientId("191212121212");

        final List<ArendeListItem> arendeListItemList = Arrays.asList(arendeListItem);

        resourceLinkHelper.decorateArendeWithValidActionLinks(arendeListItemList, vardenhet);

        final List<ActionLink> actualLinks = arendeListItem.getLinks();

        assertLinks(expectedLinks, actualLinks);
    }

    @Test
    public void noValidActionsForArendeListItemWithAccessAllowed() {
        final String intygsTyp = "intygstyp";
        final Personnummer patient = Personnummer.createPersonnummer("191212121212").get();
        final Vardenhet vardenhet = mock(Vardenhet.class);

        doReturn(AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION, "No access")).when(certificateAccessService)
            .allowToForwardQuestions(anyString(), any(Vardenhet.class),
                any(Personnummer.class));

        final List<ActionLink> expectedLinks = new ArrayList<>();

        final ArendeListItem arendeListItem = new ArendeListItem();
        arendeListItem.setIntygTyp(intygsTyp);
        arendeListItem.setPatientId("191212121212");

        final List<ArendeListItem> arendeListItemList = Arrays.asList(arendeListItem);

        resourceLinkHelper.decorateArendeWithValidActionLinks(arendeListItemList, vardenhet);

        final List<ActionLink> actualLinks = arendeListItem.getLinks();

        assertLinks(expectedLinks, actualLinks);
    }
}
