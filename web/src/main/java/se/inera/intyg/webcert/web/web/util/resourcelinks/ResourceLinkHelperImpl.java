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

package se.inera.intyg.webcert.web.web.util.resourcelinks;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.access.CertificateAccessService;
import se.inera.intyg.webcert.web.service.access.DraftAccessService;
import se.inera.intyg.webcert.web.service.access.LockedDraftAccessService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeListItem;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygModuleDTO;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.DraftHolder;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLink;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLinkType;

/**
 * Implementation of ResourceLinkHelper.
 */
@Component
public class ResourceLinkHelperImpl implements ResourceLinkHelper {

    @Autowired
    private DraftAccessService draftAccessService;

    @Autowired
    private LockedDraftAccessService lockedDraftAccessService;

    @Autowired
    private CertificateAccessService certificateAccessService;

    @Override
    public void decorateIntygModuleWithValidActionLinks(List<IntygModuleDTO> intygModuleDTOList, Personnummer patient) {
        for (IntygModuleDTO intygModule : intygModuleDTOList) {
            decorateIntygModuleWithValidActionLinks(intygModule, patient);
        }
    }

    @Override
    public void decorateIntygModuleWithValidActionLinks(IntygModuleDTO intygModuleDTO, Personnummer patient) {
        if (draftAccessService.allowToCreateDraft(intygModuleDTO.getId(), patient).isAllowed()) {
            intygModuleDTO.addLink(new ActionLink(ActionLinkType.SKAPA_UTKAST));
        }
    }

    @Override
    public void decorateUtkastWithValidActionLinks(DraftHolder draftHolder, String certificateType, Vardenhet careUnit,
        Personnummer patient) {
        boolean isLocked = draftHolder.getStatus() != null ? draftHolder.getStatus().equals(UtkastStatus.DRAFT_LOCKED) : false;
        if (isLocked) {

            if (lockedDraftAccessService.allowedToInvalidateLockedUtkast(certificateType, careUnit, patient).isAllowed()) {
                draftHolder.addLink(new ActionLink(ActionLinkType.MAKULERA_UTKAST));
            }

            if (lockedDraftAccessService.allowedToCopyLockedUtkast(certificateType, careUnit, patient).isAllowed()) {
                draftHolder.addLink(new ActionLink(ActionLinkType.KOPIERA_UTKAST));
            }

            if (lockedDraftAccessService.allowToPrint(certificateType, careUnit, patient).isAllowed()) {
                draftHolder.addLink(new ActionLink(ActionLinkType.SKRIV_UT_UTKAST));
            }

        } else {

            if (draftAccessService.allowToEditDraft(certificateType, careUnit, patient).isAllowed()) {
                draftHolder.addLink(new ActionLink(ActionLinkType.REDIGERA_UTKAST));
            }

            if (draftAccessService.allowToDeleteDraft(certificateType, careUnit, patient).isAllowed()) {
                draftHolder.addLink(new ActionLink(ActionLinkType.TA_BORT_UTKAST));
            }

            if (draftAccessService.allowToPrintDraft(certificateType, careUnit, patient).isAllowed()) {
                draftHolder.addLink(new ActionLink(ActionLinkType.SKRIV_UT_UTKAST));
            }

            // Add action links related to questions, as the utkast can be part of a kompletteringsbegäran.
            final List<ActionLink> actionLinkList = getActionLinksForQuestions(certificateType, careUnit, patient);
            for (ActionLink actionLink : actionLinkList) {
                draftHolder.addLink(actionLink);
            }
        }
    }

    @Override
    public void decorateIntygWithValidActionLinks(IntygContentHolder intygContentHolder) {
        final String intygsTyp = intygContentHolder.getUtlatande().getTyp();
        final Vardenhet vardenhet = intygContentHolder.getUtlatande().getGrundData().getSkapadAv().getVardenhet();
        final Personnummer personnummer = intygContentHolder.getUtlatande().getGrundData().getPatient().getPersonId();

        if (certificateAccessService.allowToRenew(intygsTyp, vardenhet, personnummer).isAllowed()) {
            intygContentHolder.addLink(new ActionLink(ActionLinkType.FORNYA_INTYG));
        }

        if (certificateAccessService.allowToInvalidate(intygsTyp, vardenhet, personnummer).isAllowed()) {
            intygContentHolder.addLink(new ActionLink(ActionLinkType.MAKULERA_INTYG));
        }

        if (certificateAccessService.allowToPrint(intygsTyp, vardenhet, personnummer, false).isAllowed()) {
            intygContentHolder.addLink(new ActionLink(ActionLinkType.SKRIV_UT_INTYG));
        }

        if (certificateAccessService.allowToReplace(intygsTyp, vardenhet, personnummer).isAllowed()) {
            intygContentHolder.addLink(new ActionLink(ActionLinkType.ERSATT_INTYG));
        }

        if (certificateAccessService.allowToSend(intygsTyp, vardenhet, personnummer).isAllowed()) {
            intygContentHolder.addLink(new ActionLink(ActionLinkType.SKICKA_INTYG));
        }

        final List<ActionLink> actionLinkList = getActionLinksForQuestions(intygsTyp, vardenhet, personnummer);
        for (ActionLink actionLink : actionLinkList) {
            intygContentHolder.addLink(actionLink);
        }
    }

    @Override
    public void decorateIntygWithValidActionLinks(List<ListIntygEntry> listIntygEntryList, Personnummer patient) {
        for (ListIntygEntry intyg : listIntygEntryList) {
            decorateIntygWithValidActionLinks(intyg, patient);
        }
    }

    @Override
    public void decorateIntygWithValidActionLinks(ListIntygEntry listIntygEntry, Personnummer patient) {
        final Vardenhet vardenhet = new Vardenhet();
        vardenhet.setEnhetsid(listIntygEntry.getVardenhetId());
        vardenhet.setVardgivare(new Vardgivare());
        vardenhet.getVardgivare().setVardgivarid(listIntygEntry.getVardgivarId());

        if (certificateAccessService.allowToRead(listIntygEntry.getIntygType(), vardenhet, patient).isAllowed()) {
            listIntygEntry.addLink(new ActionLink(ActionLinkType.LASA_INTYG));
        }

        if (certificateAccessService.allowToRenew(listIntygEntry.getIntygType(), vardenhet, patient).isAllowed()) {
            listIntygEntry.addLink(new ActionLink(ActionLinkType.FORNYA_INTYG));
        }
    }

    @Override
    public void decorateArendeWithValidActionLinks(List<ArendeListItem> arendeListItems, Vardenhet careUnit) {
        for (ArendeListItem arendeListItem : arendeListItems) {
            if (certificateAccessService.allowToForwardQuestions(arendeListItem.getIntygTyp(), careUnit,
                Personnummer.createPersonnummer(arendeListItem.getPatientId()).get()).isAllowed()) {
                arendeListItem.addLink(new ActionLink(ActionLinkType.VIDAREBEFODRA_FRAGA));
            }
        }
    }

    private List<ActionLink> getActionLinksForQuestions(String intygsTyp, Vardenhet vardenhet, Personnummer personnummer) {
        final List<ActionLink> actionLinkList = new ArrayList<>();

        if (certificateAccessService.allowToCreateQuestion(intygsTyp, vardenhet, personnummer).isAllowed()) {
            actionLinkList.add(new ActionLink(ActionLinkType.SKAPA_FRAGA));
        }

        if (certificateAccessService.allowToReadQuestions(intygsTyp, vardenhet, personnummer).isAllowed()) {
            actionLinkList.add(new ActionLink(ActionLinkType.LASA_FRAGA));
        }

        if (certificateAccessService.allowToAnswerAdminQuestion(intygsTyp, vardenhet, personnummer).isAllowed()) {
            actionLinkList.add(new ActionLink(ActionLinkType.BESVARA_FRAGA));
        }

        if (certificateAccessService.allowToAnswerComplementQuestion(intygsTyp, vardenhet, personnummer, true).isAllowed()) {
            actionLinkList.add(new ActionLink(ActionLinkType.BESVARA_KOMPLETTERING));
        }

        if (certificateAccessService.allowToAnswerComplementQuestion(intygsTyp, vardenhet, personnummer, false).isAllowed()) {
            actionLinkList.add(new ActionLink(ActionLinkType.BESVARA_KOMPLETTERING_MED_MEDDELANDE));
        }

        if (certificateAccessService.allowToForwardQuestions(intygsTyp, vardenhet, personnummer).isAllowed()) {
            actionLinkList.add(new ActionLink(ActionLinkType.VIDAREBEFODRA_FRAGA));
        }

        if (certificateAccessService.allowToSetComplementAsHandled(intygsTyp, vardenhet, personnummer).isAllowed()) {
            actionLinkList.add(new ActionLink(ActionLinkType.MARKERA_KOMPLETTERING_SOM_HANTERAD));
        }

        return actionLinkList;
    }
}
