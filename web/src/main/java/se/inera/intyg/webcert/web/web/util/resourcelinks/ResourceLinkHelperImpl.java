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
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygModuleDTO;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.DraftHolder;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLink;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLinkType;

@Component
public class ResourceLinkHelperImpl implements ResourceLinkHelper {

    @Autowired
    private DraftAccessService draftAccessService;

    @Autowired
    private LockedDraftAccessService lockedDraftAccessService;

    @Autowired
    private CertificateAccessService certificateAccessService;

    @Override
    public void decorateWithValidActionLinks(List<IntygModuleDTO> intygModules, Personnummer personnummer) {
        for (IntygModuleDTO intygModule : intygModules) {
            decorateWithValidActionLinks(intygModule, personnummer);
        }
    }

    @Override
    public void decorateWithValidActionLinks(IntygModuleDTO intygModule, Personnummer personnummer) {
        if (draftAccessService.allowToCreateDraft(intygModule.getId(), personnummer).isAllowed()) {
            final ActionLink actionLink = new ActionLink();
            actionLink.setType(ActionLinkType.SKAPA_UTKAST);
            actionLink.setUrl("testurl");
            intygModule.addLink(actionLink);
        }
    }

    @Override
    public void decorateWithValidActionLinks(DraftHolder utkast, String intygsTyp, String enhetsId, Personnummer personnummer) {
        boolean isLocked = utkast.getStatus() != null ? utkast.getStatus().equals(UtkastStatus.DRAFT_LOCKED) : false;
        // TODO Manage print when revoked.
        if (isLocked) {
            if (lockedDraftAccessService.allowedToInvalidateLockedUtkast(intygsTyp, enhetsId, personnummer).isAllowed()) {
                final ActionLink actionLink = new ActionLink();
                actionLink.setType(ActionLinkType.MAKULERA_UTKAST);
                actionLink.setUrl("testurl");
                utkast.addLink(actionLink);
            }
            if (lockedDraftAccessService.allowedToCopyLockedUtkast(intygsTyp, enhetsId, personnummer).isAllowed()) {
                final ActionLink actionLink = new ActionLink();
                actionLink.setType(ActionLinkType.KOPIERA_UTKAST);
                actionLink.setUrl("testurl");
                utkast.addLink(actionLink);
            }
            if (lockedDraftAccessService.allowToPrint(intygsTyp, enhetsId, personnummer).isAllowed()) {
                final ActionLink actionLink = new ActionLink();
                actionLink.setType(ActionLinkType.SKRIV_UT_UTKAST);
                actionLink.setUrl("testurl");
                utkast.addLink(actionLink);
            }
        } else {
            if (draftAccessService.allowToDeleteDraft(intygsTyp, enhetsId, personnummer).isAllowed()) {
                final ActionLink actionLink = new ActionLink();
                actionLink.setType(ActionLinkType.TA_BORT_UTKAST);
                actionLink.setUrl("testurl");
                utkast.addLink(actionLink);
            }
            if (draftAccessService.allowToPrintDraft(intygsTyp, enhetsId, personnummer).isAllowed()) {
                final ActionLink actionLink = new ActionLink();
                actionLink.setType(ActionLinkType.SKRIV_UT_UTKAST);
                actionLink.setUrl("testurl");
                utkast.addLink(actionLink);
            }
        }
    }

    @Override
    public void decorateWithValidActionLinks(IntygContentHolder intygAsExternal) {
        final String intygsTyp = intygAsExternal.getUtlatande().getTyp();
        final Vardenhet vardenhet = intygAsExternal.getUtlatande().getGrundData().getSkapadAv().getVardenhet();
        final Personnummer personnummer = intygAsExternal.getUtlatande().getGrundData().getPatient().getPersonId();
        if (certificateAccessService.allowToRenew(intygsTyp, vardenhet, personnummer, false).isAllowed()) {
            final ActionLink actionLink = new ActionLink();
            actionLink.setType(ActionLinkType.FORNYA_INTYG);
            actionLink.setUrl("testurl");
            intygAsExternal.addLink(actionLink);
        }
        if (certificateAccessService.allowToInvalidate(intygsTyp, vardenhet, personnummer).isAllowed()) {
            final ActionLink actionLink = new ActionLink();
            actionLink.setType(ActionLinkType.MAKULERA_INTYG);
            actionLink.setUrl("testurl");
            intygAsExternal.addLink(actionLink);
        }
        if (certificateAccessService.allowToPrint(intygsTyp, vardenhet, personnummer, false).isAllowed()) {
            final ActionLink actionLink = new ActionLink();
            actionLink.setType(ActionLinkType.SKRIV_UT_INTYG);
            actionLink.setUrl("testurl");
            intygAsExternal.addLink(actionLink);
        }
        if (certificateAccessService.allowToReplace(intygsTyp, vardenhet, personnummer).isAllowed()) {
            final ActionLink actionLink = new ActionLink();
            actionLink.setType(ActionLinkType.ERSATT_INTYG);
            actionLink.setUrl("testurl");
            intygAsExternal.addLink(actionLink);
        }
        if (certificateAccessService.allowToSend(intygsTyp, vardenhet, personnummer).isAllowed()) {
            final ActionLink actionLink = new ActionLink();
            actionLink.setType(ActionLinkType.SKICKA_INTYG);
            actionLink.setUrl("testurl");
            intygAsExternal.addLink(actionLink);
        }
        if (certificateAccessService.allowToCreateQuestion(intygsTyp, vardenhet, personnummer).isAllowed()) {
            final ActionLink actionLink = new ActionLink();
            actionLink.setType(ActionLinkType.SKAPA_FRAGA);
            actionLink.setUrl("testurl");
            intygAsExternal.addLink(actionLink);
        }
        if (certificateAccessService.allowToReadQuestions(intygsTyp, vardenhet, personnummer).isAllowed()) {
            final ActionLink actionLink = new ActionLink();
            actionLink.setType(ActionLinkType.LASA_FRAGA);
            actionLink.setUrl("testurl");
            intygAsExternal.addLink(actionLink);
        }
        if (certificateAccessService.allowToAnswerComplementQuestion(intygsTyp, vardenhet, personnummer, false).isAllowed()) {
            final ActionLink actionLink = new ActionLink();
            actionLink.setType(ActionLinkType.BESVARA_FRAGA);
            actionLink.setUrl("testurl");
            intygAsExternal.addLink(actionLink);
        }
        if (certificateAccessService.allowToAnswerComplementQuestion(intygsTyp, vardenhet, personnummer, true).isAllowed()) {
            final ActionLink actionLink = new ActionLink();
            actionLink.setType(ActionLinkType.BESVARA_KOMPLETTERING);
            actionLink.setUrl("testurl");
            intygAsExternal.addLink(actionLink);
        }
        if (certificateAccessService.allowToForwardQuestions(intygsTyp, vardenhet, personnummer).isAllowed()) {
            final ActionLink actionLink = new ActionLink();
            actionLink.setType(ActionLinkType.VIDAREBEFODRA_FRAGA);
            actionLink.setUrl("testurl");
            intygAsExternal.addLink(actionLink);
        }
    }

    @Override
    public void decorateIntygWithValidActionLinks(List<ListIntygEntry> allIntyg, Personnummer personNummer) {
        for (ListIntygEntry intyg : allIntyg) {
            decorateWithValidActionLinks(intyg, personNummer);
        }
    }

    @Override
    public void decorateWithValidActionLinks(ListIntygEntry intygEntry, Personnummer personNummer) {
        final Vardenhet vardenhet = new Vardenhet();
        vardenhet.setEnhetsid(intygEntry.getVardenhetId());
        vardenhet.setVardgivare(new Vardgivare());
        vardenhet.getVardgivare().setVardgivarid(intygEntry.getVardgivarId());

        if (certificateAccessService.allowToRead(intygEntry.getIntygType(), vardenhet, personNummer).isAllowed()) {
            final ActionLink actionLink = new ActionLink();
            actionLink.setType(ActionLinkType.LASA_INTYG);
            actionLink.setUrl("testurl");
            intygEntry.addLink(actionLink);
        }

        if (certificateAccessService.allowToRenew(intygEntry.getIntygType(), vardenhet, personNummer, false).isAllowed()) {
            final ActionLink actionLink = new ActionLink();
            actionLink.setType(ActionLinkType.FORNYA_INTYG);
            actionLink.setUrl("testurl");
            intygEntry.addLink(actionLink);
        }
    }
}
