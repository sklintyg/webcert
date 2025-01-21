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
package se.inera.intyg.webcert.web.web.util.resourcelink;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
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
import se.inera.intyg.webcert.web.service.access.AccessEvaluationParameters;
import se.inera.intyg.webcert.web.service.access.CertificateAccessServiceHelper;
import se.inera.intyg.webcert.web.service.access.DraftAccessServiceHelper;
import se.inera.intyg.webcert.web.service.access.LockedDraftAccessServiceHelper;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeListItem;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygModuleDTO;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.DraftHolder;
import se.inera.intyg.webcert.web.web.util.resourcelinks.ResourceLinkHelperImpl;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLink;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLinkType;

@RunWith(MockitoJUnitRunner.class)
public class ResourceLinkHelperImplTest {

    @Mock
    private DraftAccessServiceHelper draftAccessServiceHelper;

    @Mock
    private LockedDraftAccessServiceHelper lockedDraftAccessServiceHelper;

    @Mock
    private CertificateAccessServiceHelper certificateAccessServiceHelper;

    @InjectMocks
    private ResourceLinkHelperImpl resourceLinkHelper;

    @Test
    public void validActionsForIntygModuleWithAccessAllowed() {
        final String intygsTyp = "intygstyp";
        final Personnummer personnummer = Personnummer.createPersonnummer("191212121212").get();

        doReturn(true).when(draftAccessServiceHelper).isAllowedToCreateUtkast(intygsTyp, personnummer);

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

        doReturn(false).when(draftAccessServiceHelper)
            .isAllowedToCreateUtkast(intygsTyp, personnummer);

        final ActionLink expectedActionLink = new ActionLink();

        final IntygModuleDTO intygModuleDTO = createIntygModuleDTO(intygsTyp);
        final List<IntygModuleDTO> intygModuleDTOList = Arrays.asList(intygModuleDTO);
        resourceLinkHelper.decorateIntygModuleWithValidActionLinks(intygModuleDTO, personnummer);

        final List<ActionLink> actualLinks = intygModuleDTO.getLinks();

        assertNotNull(actualLinks);
        assertEquals("Should be no links", 0, actualLinks.size());
    }

    private IntygModuleDTO createIntygModuleDTO(String intygsTyp) {
        return new IntygModuleDTO(new IntygModule(intygsTyp, "", "", "", "", "", "", "", ""));
    }

    @Test
    public void validActionsForLockedDraftHolderWithAccessAllowed() {
        final String intygsTyp = "intygstyp";
        final String intygsTypVersion = "intygstypVersion";
        final Personnummer patient = Personnummer.createPersonnummer("191212121212").get();
        final Vardenhet vardenhet = mock(Vardenhet.class);

        doReturn(true).when(lockedDraftAccessServiceHelper).isAllowToInvalidate(any(AccessEvaluationParameters.class));
        doReturn(true).when(lockedDraftAccessServiceHelper).isAllowToCopy(any(AccessEvaluationParameters.class));
        doReturn(true).when(lockedDraftAccessServiceHelper).isAllowToPrint(any(AccessEvaluationParameters.class));

        final List<ActionLink> expectedLinks = new ArrayList<>();
        expectedLinks.add(new ActionLink(ActionLinkType.MAKULERA_UTKAST));
        expectedLinks.add(new ActionLink(ActionLinkType.KOPIERA_UTKAST));
        expectedLinks.add(new ActionLink(ActionLinkType.SKRIV_UT_UTKAST));

        final DraftHolder draftHolder = new DraftHolder();
        draftHolder.setStatus(UtkastStatus.DRAFT_LOCKED);

        resourceLinkHelper.decorateUtkastWithValidActionLinks(draftHolder, intygsTyp, intygsTypVersion, vardenhet, patient);

        final List<ActionLink> actualLinks = draftHolder.getLinks();

        assertLinks(expectedLinks, actualLinks);
    }

    @Test
    public void noValidActionsForLockedDraftHolderWithAccessAllowed() {
        final String intygsTyp = "intygstyp";
        final String intygsTypVersion = "intygstypVersion";
        final Personnummer patient = Personnummer.createPersonnummer("191212121212").get();
        final Vardenhet vardenhet = mock(Vardenhet.class);

        doReturn(false).when(lockedDraftAccessServiceHelper).isAllowToInvalidate(any(AccessEvaluationParameters.class));
        doReturn(false).when(lockedDraftAccessServiceHelper).isAllowToCopy(any(AccessEvaluationParameters.class));
        doReturn(false).when(lockedDraftAccessServiceHelper).isAllowToPrint(any(AccessEvaluationParameters.class));

        final List<ActionLink> expectedLinks = new ArrayList<>();

        final DraftHolder draftHolder = new DraftHolder();
        draftHolder.setStatus(UtkastStatus.DRAFT_LOCKED);

        resourceLinkHelper.decorateUtkastWithValidActionLinks(draftHolder, intygsTyp, intygsTypVersion, vardenhet, patient);

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
        final String intygsTypVersion = "intygsTypVersion";
        final Personnummer patient = Personnummer.createPersonnummer("191212121212").get();
        final Vardenhet vardenhet = mock(Vardenhet.class);

        doReturn(true).when(draftAccessServiceHelper).isAllowToEditUtkast(any(AccessEvaluationParameters.class));
        doReturn(true).when(draftAccessServiceHelper).isAllowToDeleteUtkast(any(AccessEvaluationParameters.class));
        doReturn(true).when(draftAccessServiceHelper).isAllowToPrintUtkast(any(AccessEvaluationParameters.class));
        doReturn(true).when(certificateAccessServiceHelper).isAllowToCreateQuestion(any(AccessEvaluationParameters.class));
        doReturn(true).when(certificateAccessServiceHelper).isAllowToReadQuestions(any(AccessEvaluationParameters.class));
        doReturn(true).when(certificateAccessServiceHelper).isAllowToAnswerAdminQuestion(any(AccessEvaluationParameters.class));
        doReturn(true).when(certificateAccessServiceHelper)
            .isAllowToAnswerComplementQuestion(any(AccessEvaluationParameters.class), anyBoolean());
        doReturn(true).when(certificateAccessServiceHelper).isAllowToSetComplementAsHandled(any(AccessEvaluationParameters.class));
        doReturn(true).when(certificateAccessServiceHelper).isAllowToSetQuestionAsHandled(any(AccessEvaluationParameters.class));
        doReturn(true).when(certificateAccessServiceHelper).isAllowToForwardQuestions(any(AccessEvaluationParameters.class));
        doReturn(true).when(certificateAccessServiceHelper).isAllowToApproveReceivers(any(AccessEvaluationParameters.class));
        doReturn(true).when(certificateAccessServiceHelper).isAllowToSend(any(AccessEvaluationParameters.class));

        final List<ActionLink> expectedLinks = new ArrayList<>();
        expectedLinks.add(new ActionLink(ActionLinkType.REDIGERA_UTKAST));
        expectedLinks.add(new ActionLink(ActionLinkType.TA_BORT_UTKAST));
        expectedLinks.add(new ActionLink(ActionLinkType.SKRIV_UT_UTKAST));
        expectedLinks.add(new ActionLink(ActionLinkType.GODKANNA_MOTTAGARE));
        expectedLinks.add(new ActionLink(ActionLinkType.SKICKA_INTYG));
        expectedLinks.add(new ActionLink(ActionLinkType.SKAPA_FRAGA));
        expectedLinks.add(new ActionLink(ActionLinkType.LASA_FRAGA));
        expectedLinks.add(new ActionLink(ActionLinkType.BESVARA_FRAGA));
        expectedLinks.add(new ActionLink(ActionLinkType.BESVARA_KOMPLETTERING));
        expectedLinks.add(new ActionLink(ActionLinkType.BESVARA_KOMPLETTERING_MED_MEDDELANDE));
        expectedLinks.add(new ActionLink(ActionLinkType.VIDAREBEFODRA_FRAGA));
        expectedLinks.add(new ActionLink(ActionLinkType.MARKERA_KOMPLETTERING_SOM_HANTERAD));
        expectedLinks.add(new ActionLink(ActionLinkType.MARKERA_FRAGA_SOM_HANTERAD));

        final DraftHolder draftHolder = new DraftHolder();

        resourceLinkHelper.decorateUtkastWithValidActionLinks(draftHolder, intygsTyp, intygsTypVersion, vardenhet, patient);

        final List<ActionLink> actualLinks = draftHolder.getLinks();

        assertLinks(expectedLinks, actualLinks);
    }

    @Test
    public void noValidActionsForDraftHolderWithAccessAllowed() {
        final String intygsTyp = "intygstyp";
        final String intygsTypVersion = "intygsTypVersion";
        final Personnummer patient = Personnummer.createPersonnummer("191212121212").get();
        final Vardenhet vardenhet = mock(Vardenhet.class);

        doReturn(false).when(draftAccessServiceHelper).isAllowToEditUtkast(any(AccessEvaluationParameters.class));
        doReturn(false).when(draftAccessServiceHelper).isAllowToDeleteUtkast(any(AccessEvaluationParameters.class));
        doReturn(false).when(draftAccessServiceHelper).isAllowToPrintUtkast(any(AccessEvaluationParameters.class));
        doReturn(false).when(certificateAccessServiceHelper).isAllowToApproveReceivers(any(AccessEvaluationParameters.class));
        doReturn(false).when(certificateAccessServiceHelper).isAllowToSend(any(AccessEvaluationParameters.class));
        doReturn(false).when(certificateAccessServiceHelper).isAllowToCreateQuestion(any(AccessEvaluationParameters.class));
        doReturn(false).when(certificateAccessServiceHelper).isAllowToReadQuestions(any(AccessEvaluationParameters.class));
        doReturn(false).when(certificateAccessServiceHelper).isAllowToAnswerAdminQuestion(any(AccessEvaluationParameters.class));
        doReturn(false).when(certificateAccessServiceHelper)
            .isAllowToAnswerComplementQuestion(any(AccessEvaluationParameters.class), anyBoolean());
        doReturn(false).when(certificateAccessServiceHelper).isAllowToSetComplementAsHandled(any(AccessEvaluationParameters.class));
        doReturn(false).when(certificateAccessServiceHelper).isAllowToSetQuestionAsHandled(any(AccessEvaluationParameters.class));
        doReturn(false).when(certificateAccessServiceHelper).isAllowToForwardQuestions(any(AccessEvaluationParameters.class));

        final List<ActionLink> expectedLinks = new ArrayList<>();

        final DraftHolder draftHolder = new DraftHolder();

        resourceLinkHelper.decorateUtkastWithValidActionLinks(draftHolder, intygsTyp, intygsTypVersion, vardenhet, patient);

        final List<ActionLink> actualLinks = draftHolder.getLinks();

        assertLinks(expectedLinks, actualLinks);
    }

    @Test
    public void validActionsForIntygContentHolderWithAccessAllowed() {
        final String intygsTyp = "intygstyp";
        final Personnummer patient = Personnummer.createPersonnummer("191212121212").get();
        final Vardenhet vardenhet = mock(Vardenhet.class);

        doReturn(true).when(certificateAccessServiceHelper).isAllowToRenew(any(AccessEvaluationParameters.class));
        doReturn(true).when(certificateAccessServiceHelper).isAllowToInvalidate(any(AccessEvaluationParameters.class));
        doReturn(true).when(certificateAccessServiceHelper).isAllowToPrint(any(AccessEvaluationParameters.class), anyBoolean());
        doReturn(true).when(certificateAccessServiceHelper).isAllowToReplace(any(AccessEvaluationParameters.class));
        doReturn(true).when(certificateAccessServiceHelper).isAllowToSend(any(AccessEvaluationParameters.class));
        doReturn(true).when(certificateAccessServiceHelper).isAllowToCreateDraftFromSignedTemplate(any(AccessEvaluationParameters.class));

        doReturn(true).when(certificateAccessServiceHelper).isAllowToCreateQuestion(any(AccessEvaluationParameters.class));
        doReturn(true).when(certificateAccessServiceHelper).isAllowToReadQuestions(any(AccessEvaluationParameters.class));
        doReturn(true).when(certificateAccessServiceHelper).isAllowToAnswerAdminQuestion(any(AccessEvaluationParameters.class));
        doReturn(true).when(certificateAccessServiceHelper)
            .isAllowToAnswerComplementQuestion(any(AccessEvaluationParameters.class), anyBoolean());
        doReturn(true).when(certificateAccessServiceHelper).isAllowToSetComplementAsHandled(any(AccessEvaluationParameters.class));
        doReturn(true).when(certificateAccessServiceHelper).isAllowToSetQuestionAsHandled(any(AccessEvaluationParameters.class));
        doReturn(true).when(certificateAccessServiceHelper).isAllowToForwardQuestions(any(AccessEvaluationParameters.class));
        doReturn(true).when(certificateAccessServiceHelper).isAllowToApproveReceivers(any(AccessEvaluationParameters.class));

        final List<ActionLink> expectedLinks = new ArrayList<>();
        expectedLinks.add(new ActionLink(ActionLinkType.FORNYA_INTYG));
        expectedLinks.add(new ActionLink(ActionLinkType.MAKULERA_INTYG));
        expectedLinks.add(new ActionLink(ActionLinkType.SKRIV_UT_INTYG));
        expectedLinks.add(new ActionLink(ActionLinkType.ERSATT_INTYG));
        expectedLinks.add(new ActionLink(ActionLinkType.SKICKA_INTYG));
        expectedLinks.add(new ActionLink(ActionLinkType.GODKANNA_MOTTAGARE));
        expectedLinks.add(new ActionLink(ActionLinkType.SKAPA_UTKAST_FRAN_INTYG));
        expectedLinks.add(new ActionLink(ActionLinkType.SKAPA_FRAGA));
        expectedLinks.add(new ActionLink(ActionLinkType.LASA_FRAGA));
        expectedLinks.add(new ActionLink(ActionLinkType.BESVARA_FRAGA));
        expectedLinks.add(new ActionLink(ActionLinkType.BESVARA_KOMPLETTERING));
        expectedLinks.add(new ActionLink(ActionLinkType.BESVARA_KOMPLETTERING_MED_MEDDELANDE));
        expectedLinks.add(new ActionLink(ActionLinkType.VIDAREBEFODRA_FRAGA));
        expectedLinks.add(new ActionLink(ActionLinkType.MARKERA_KOMPLETTERING_SOM_HANTERAD));
        expectedLinks.add(new ActionLink(ActionLinkType.MARKERA_FRAGA_SOM_HANTERAD));

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
            .revoked(false)
            .deceased(false)
            .sekretessmarkering(false)
            .patientNameChangedInPU(false)
            .patientAddressChangedInPU(false)
            .utlatande(utlatande)
            .testIntyg(false)
            .relations(new Relations())
            .latestMajorTextVersion(true)
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

        doReturn(false).when(certificateAccessServiceHelper).isAllowToRenew(any(AccessEvaluationParameters.class));
        doReturn(false).when(certificateAccessServiceHelper).isAllowToInvalidate(any(AccessEvaluationParameters.class));
        doReturn(false).when(certificateAccessServiceHelper).isAllowToPrint(any(AccessEvaluationParameters.class), anyBoolean());
        doReturn(false).when(certificateAccessServiceHelper).isAllowToReplace(any(AccessEvaluationParameters.class));
        doReturn(false).when(certificateAccessServiceHelper).isAllowToSend(any(AccessEvaluationParameters.class));
        doReturn(false).when(certificateAccessServiceHelper).isAllowToCreateDraftFromSignedTemplate(any(AccessEvaluationParameters.class));

        doReturn(false).when(certificateAccessServiceHelper).isAllowToCreateQuestion(any(AccessEvaluationParameters.class));
        doReturn(false).when(certificateAccessServiceHelper).isAllowToReadQuestions(any(AccessEvaluationParameters.class));
        doReturn(false).when(certificateAccessServiceHelper).isAllowToAnswerAdminQuestion(any(AccessEvaluationParameters.class));
        doReturn(false).when(certificateAccessServiceHelper)
            .isAllowToAnswerComplementQuestion(any(AccessEvaluationParameters.class), anyBoolean());
        doReturn(false).when(certificateAccessServiceHelper).isAllowToSetComplementAsHandled(any(AccessEvaluationParameters.class));
        doReturn(false).when(certificateAccessServiceHelper).isAllowToSetQuestionAsHandled(any(AccessEvaluationParameters.class));
        doReturn(false).when(certificateAccessServiceHelper).isAllowToForwardQuestions(any(AccessEvaluationParameters.class));
        doReturn(false).when(certificateAccessServiceHelper).isAllowToApproveReceivers(any(AccessEvaluationParameters.class));

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
            .revoked(false)
            .deceased(false)
            .sekretessmarkering(false)
            .patientNameChangedInPU(false)
            .patientAddressChangedInPU(false)
            .utlatande(utlatande)
            .testIntyg(false)
            .relations(new Relations())
            .latestMajorTextVersion(true)
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

        doReturn(true).when(certificateAccessServiceHelper).isAllowToRead(any(AccessEvaluationParameters.class));
        doReturn(true).when(certificateAccessServiceHelper).isAllowToRenew(any(AccessEvaluationParameters.class));

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

        doReturn(false).when(certificateAccessServiceHelper).isAllowToRead(any(AccessEvaluationParameters.class));
        doReturn(false).when(certificateAccessServiceHelper).isAllowToRenew(any(AccessEvaluationParameters.class));

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

        doReturn(true).when(certificateAccessServiceHelper).isAllowToForwardQuestions(any(AccessEvaluationParameters.class));

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
}
