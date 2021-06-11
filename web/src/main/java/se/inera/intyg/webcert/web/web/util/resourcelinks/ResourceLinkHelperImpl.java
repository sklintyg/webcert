/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.common.support.modules.support.facade.dto.ResourceLinkDTO;
import se.inera.intyg.common.support.modules.support.facade.dto.ResourceLinkTypeDTO;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.access.AccessEvaluationParameters;
import se.inera.intyg.webcert.web.service.access.CertificateAccessServiceHelper;
import se.inera.intyg.webcert.web.service.access.DraftAccessServiceHelper;
import se.inera.intyg.webcert.web.service.access.LockedDraftAccessServiceHelper;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeListItem;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygModuleDTO;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.DraftHolder;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLink;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLinkType;

@Component
public class ResourceLinkHelperImpl implements ResourceLinkHelper {

    @Autowired
    private DraftAccessServiceHelper draftAccessServiceHelper;

    @Autowired
    private LockedDraftAccessServiceHelper lockedDraftAccessServiceHelper;

    @Autowired
    private CertificateAccessServiceHelper certificateAccessServiceHelper;

    @Override
    public void decorateIntygModuleWithValidActionLinks(List<IntygModuleDTO> intygModuleDTOList, Personnummer patient) {
        for (IntygModuleDTO intygModule : intygModuleDTOList) {
            decorateIntygModuleWithValidActionLinks(intygModule, patient);
        }
    }

    @Override
    public void decorateIntygModuleWithValidActionLinks(IntygModuleDTO intygModuleDTO, Personnummer patient) {
        if (draftAccessServiceHelper.isAllowedToCreateUtkast(intygModuleDTO.getId(), patient)) {
            intygModuleDTO.addLink(new ActionLink(ActionLinkType.SKAPA_UTKAST));
        }
    }

    @Override
    public void decorateUtkastWithValidActionLinks(DraftHolder draftHolder, String certificateType, String certificateTypeVersion,
        Vardenhet careUnit, Personnummer patient) {
        boolean isLocked = draftHolder.getStatus() != null && draftHolder.getStatus().equals(UtkastStatus.DRAFT_LOCKED);

        final AccessEvaluationParameters accessEvaluationParameters = AccessEvaluationParameters.create(certificateType,
            certificateTypeVersion, careUnit, patient, draftHolder.isTestIntyg());

        if (isLocked) {

            if (lockedDraftAccessServiceHelper.isAllowToInvalidate(accessEvaluationParameters)) {
                draftHolder.addLink(new ActionLink(ActionLinkType.MAKULERA_UTKAST));
            }

            if (lockedDraftAccessServiceHelper.isAllowToCopy(accessEvaluationParameters)) {
                draftHolder.addLink(new ActionLink(ActionLinkType.KOPIERA_UTKAST));
            }

            if (lockedDraftAccessServiceHelper.isAllowToPrint(accessEvaluationParameters)) {
                draftHolder.addLink(new ActionLink(ActionLinkType.SKRIV_UT_UTKAST));
            }

        } else {

            if (draftAccessServiceHelper.isAllowToEditUtkast(accessEvaluationParameters)) {
                draftHolder.addLink(new ActionLink(ActionLinkType.REDIGERA_UTKAST));
            }

            if (draftAccessServiceHelper.isAllowToDeleteUtkast(accessEvaluationParameters)) {
                draftHolder.addLink(new ActionLink(ActionLinkType.TA_BORT_UTKAST));
            }

            if (draftAccessServiceHelper.isAllowToPrintUtkast(accessEvaluationParameters)) {
                draftHolder.addLink(new ActionLink(ActionLinkType.SKRIV_UT_UTKAST));
            }

            if (certificateAccessServiceHelper.isAllowToApproveReceivers(accessEvaluationParameters)) {
                draftHolder.addLink(new ActionLink(ActionLinkType.GODKANNA_MOTTAGARE));
            }

            if (certificateAccessServiceHelper.isAllowToSend(accessEvaluationParameters)) {
                draftHolder.addLink(new ActionLink(ActionLinkType.SKICKA_INTYG));
            }

            final List<ActionLink> actionLinkList = getActionLinksForQuestions(accessEvaluationParameters);
            for (ActionLink actionLink : actionLinkList) {
                draftHolder.addLink(actionLink);
            }
        }
    }

    @Override
    public void decorateIntygWithValidActionLinks(IntygContentHolder intygContentHolder) {
        final var certificateType = intygContentHolder.getUtlatande().getTyp();
        final var certificateTypeVersion = intygContentHolder.getUtlatande().getTextVersion();
        final var vardenhet = intygContentHolder.getUtlatande().getGrundData().getSkapadAv().getVardenhet();
        final var personnummer = intygContentHolder.getUtlatande().getGrundData().getPatient().getPersonId();
        final var accessEvaluationParameters = AccessEvaluationParameters.create(certificateType, certificateTypeVersion,
            vardenhet, personnummer, intygContentHolder.isTestIntyg());

        if (certificateAccessServiceHelper.isAllowToRenew(accessEvaluationParameters)) {
            intygContentHolder.addLink(new ActionLink(ActionLinkType.FORNYA_INTYG));
        }

        if (certificateAccessServiceHelper.isAllowToInvalidate(accessEvaluationParameters)) {
            intygContentHolder.addLink(new ActionLink(ActionLinkType.MAKULERA_INTYG));
        }

        if (certificateAccessServiceHelper.isAllowToPrint(accessEvaluationParameters, false)) {
            intygContentHolder.addLink(new ActionLink(ActionLinkType.SKRIV_UT_INTYG));
        }

        if (certificateAccessServiceHelper.isAllowToReplace(accessEvaluationParameters)) {
            intygContentHolder.addLink(new ActionLink(ActionLinkType.ERSATT_INTYG));
        }

        if (certificateAccessServiceHelper.isAllowToSend(accessEvaluationParameters)) {
            intygContentHolder.addLink(new ActionLink(ActionLinkType.SKICKA_INTYG));
        }

        if (certificateAccessServiceHelper.isAllowToApproveReceivers(accessEvaluationParameters)) {
            intygContentHolder.addLink(new ActionLink(ActionLinkType.GODKANNA_MOTTAGARE));
        }

        if (certificateAccessServiceHelper.isAllowToCreateDraftFromSignedTemplate(accessEvaluationParameters)) {
            intygContentHolder.addLink(new ActionLink(ActionLinkType.SKAPA_UTKAST_FRAN_INTYG));
        }

        final List<ActionLink> actionLinkList = getActionLinksForQuestions(accessEvaluationParameters);
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

        final AccessEvaluationParameters accessEvaluationParameters = AccessEvaluationParameters.create(
            listIntygEntry.getIntygType(), listIntygEntry.getIntygTypeVersion(), vardenhet, patient, listIntygEntry.isTestIntyg());

        if (certificateAccessServiceHelper.isAllowToRead(accessEvaluationParameters)) {
            listIntygEntry.addLink(new ActionLink(ActionLinkType.LASA_INTYG));
        }

        if (certificateAccessServiceHelper.isAllowToRenew(accessEvaluationParameters)) {
            listIntygEntry.addLink(new ActionLink(ActionLinkType.FORNYA_INTYG));
        }
    }

    @Override
    public void decorateArendeWithValidActionLinks(List<ArendeListItem> arendeListItems, Vardenhet careUnit) {
        for (ArendeListItem arendeListItem : arendeListItems) {
            final AccessEvaluationParameters accessEvaluationParameters = AccessEvaluationParameters.create(
                arendeListItem.getIntygTyp(),
                null,
                careUnit,
                Personnummer.createPersonnummer(arendeListItem.getPatientId()).get(),
                arendeListItem.isTestIntyg());

            if (certificateAccessServiceHelper.isAllowToForwardQuestions(accessEvaluationParameters)) {
                arendeListItem.addLink(new ActionLink(ActionLinkType.VIDAREBEFODRA_FRAGA));
            }
        }
    }

    @Override
    public void decorateCertificateWithValidActionLinks(Certificate certificate) {
        final Vardenhet vardenhet = new Vardenhet();
        vardenhet.setEnhetsid(certificate.getMetadata().getUnit().getUnitId());
        vardenhet.setVardgivare(new Vardgivare());
        vardenhet.getVardgivare().setVardgivarid(certificate.getMetadata().getCareProvider().getUnitId());

        final AccessEvaluationParameters accessEvaluationParameters = AccessEvaluationParameters.create(
            certificate.getMetadata().getType(), certificate.getMetadata().getTypeVersion(), vardenhet,
            Personnummer.createPersonnummer(certificate.getMetadata().getPatient().getPersonId().getId()).get(),
            certificate.getMetadata().isTestCertificate());

        switch (certificate.getMetadata().getStatus()) {
            case UNSIGNED:
                decorateUnsignedCertificateWithValidActionLinks(certificate, accessEvaluationParameters);
                break;
            case SIGNED:
                decorateSignedCertificateWithValidActionLinks(certificate, accessEvaluationParameters);
                break;
            case LOCKED:
                decorateLockedCertificateWithValidActionLinks(certificate, accessEvaluationParameters);
                break;
            default:
                certificate.setLinks(new ResourceLinkDTO[0]);
        }
    }

    private void decorateUnsignedCertificateWithValidActionLinks(Certificate certificate,
        AccessEvaluationParameters accessEvaluationParameters) {
        final var resourceLinks = new ArrayList<ResourceLinkDTO>();
        if (draftAccessServiceHelper.isAllowToEditUtkast(accessEvaluationParameters)) {
            resourceLinks.add(ResourceLinkDTO.create(ResourceLinkTypeDTO.EDIT_CERTIFICATE, "Ändra", "Ändrar intygsutkast", true));
        }
        if (draftAccessServiceHelper.isAllowToPrintUtkast(accessEvaluationParameters)) {
            resourceLinks.add(
                ResourceLinkDTO.create(ResourceLinkTypeDTO.PRINT_CERTIFICATE, "Skriv ut", "Laddar ned intygsutkastet för utskrift.", true));
        }
        if (draftAccessServiceHelper.isAllowToDeleteUtkast(accessEvaluationParameters)) {
            resourceLinks.add(ResourceLinkDTO.create(ResourceLinkTypeDTO.REMOVE_CERTIFICATE, "Radera", "Raderar intygsutkast", true));
        }
        if (draftAccessServiceHelper.isAllowToSign(accessEvaluationParameters, certificate.getMetadata().getId())) {
            resourceLinks.add(ResourceLinkDTO.create(ResourceLinkTypeDTO.SIGN_CERTIFICATE, "Signera", "Signerar intygsutkast", true));
        }
        if (draftAccessServiceHelper.isAllowedToForwardUtkast(accessEvaluationParameters)) {
            resourceLinks.add(ResourceLinkDTO.create(ResourceLinkTypeDTO.FORWARD_CERTIFICATE, "Vidarebefodra utkast",
                        "Skapar ett e-postmeddelande i din e-postklient med en direktlänk till utkastet.",
                        true));
        }
//        if (certificateAccessService.allowToSend(accessEvaluationParameters).isAllowed()) {
//            resourceLinks
//                .add(ResourceLinkDTO.create(ResourceLinkTypeDTO.SEND_CERTIFICATE, "Skicka", "Skickar intyget", true));
//        }
        certificate.setLinks(resourceLinks.toArray(new ResourceLinkDTO[0]));
    }

    private void decorateSignedCertificateWithValidActionLinks(Certificate certificate,
        AccessEvaluationParameters accessEvaluationParameters) {
        final var resourceLinks = new ArrayList<ResourceLinkDTO>();
        if (certificateAccessServiceHelper.isAllowToPrint(accessEvaluationParameters, false)) {
            resourceLinks.add(
                ResourceLinkDTO.create(ResourceLinkTypeDTO.PRINT_CERTIFICATE, "Skriv ut", "Laddar ned intyget för utskrift.", true));
        }
        if (certificateAccessServiceHelper.isAllowToReplace(accessEvaluationParameters)) {
            resourceLinks.add(
                ResourceLinkDTO.create(ResourceLinkTypeDTO.REPLACE_CERTIFICATE, "Ersätt", "Skapar en kopia av detta intyg som du kan redigera.", true));
        }
        if (certificateAccessServiceHelper.isAllowToRenew(accessEvaluationParameters)) {
            resourceLinks.add(
                ResourceLinkDTO.create(ResourceLinkTypeDTO.REPLACE_CERTIFICATE, "Förnya", "Skapar en redigerbar kopia av intyget på den enhet som du är inloggad på.", true));
        }
        if (certificateAccessServiceHelper.isAllowToInvalidate(accessEvaluationParameters)) {
            resourceLinks.add(
                ResourceLinkDTO.create(ResourceLinkTypeDTO.REVOKE_CERTIFICATE, "Makulera", "Öppnar ett fönster där du kan välja att makulera intyget.", true));
        }
        certificate.setLinks(resourceLinks.toArray(new ResourceLinkDTO[0]));
    }

    private void decorateLockedCertificateWithValidActionLinks(Certificate certificate,
        AccessEvaluationParameters accessEvaluationParameters) {
        final var resourceLinks = new ArrayList<ResourceLinkDTO>();
        certificate.setLinks(resourceLinks.toArray(new ResourceLinkDTO[0]));
        if (lockedDraftAccessServiceHelper.isAllowToPrint(accessEvaluationParameters)) {
            resourceLinks.add(
                ResourceLinkDTO.create(ResourceLinkTypeDTO.PRINT_CERTIFICATE, "Skriv ut", "Laddar ned intygsutkastet för utskrift.", true));
        }
        if (lockedDraftAccessServiceHelper.isAllowToCopy(accessEvaluationParameters)) {
            resourceLinks.add(
                ResourceLinkDTO
                    .create(ResourceLinkTypeDTO.COPY_CERTIFICATE, "Kopiera",
                        "Skapar en redigerbar kopia av utkastet på den enheten du är inloggad på.", true));
        }
        if (lockedDraftAccessServiceHelper.isAllowToInvalidate(accessEvaluationParameters)) {
            resourceLinks.add(
                ResourceLinkDTO.create(ResourceLinkTypeDTO.REVOKE_CERTIFICATE, "Makulera",
                    "Öppnar ett fönster där du kan välja att makulera det låsta utkastet.", true));
        }
        certificate.setLinks(resourceLinks.toArray(new ResourceLinkDTO[0]));
    }

    private List<ActionLink> getActionLinksForQuestions(AccessEvaluationParameters accessEvaluationParameters) {
        final List<ActionLink> actionLinkList = new ArrayList<>();

        if (certificateAccessServiceHelper.isAllowToCreateQuestion(accessEvaluationParameters)) {
            actionLinkList.add(new ActionLink(ActionLinkType.SKAPA_FRAGA));
        }

        if (certificateAccessServiceHelper.isAllowToReadQuestions(accessEvaluationParameters)) {
            actionLinkList.add(new ActionLink(ActionLinkType.LASA_FRAGA));
        }

        if (certificateAccessServiceHelper.isAllowToAnswerAdminQuestion(accessEvaluationParameters)) {
            actionLinkList.add(new ActionLink(ActionLinkType.BESVARA_FRAGA));
        }

        if (certificateAccessServiceHelper.isAllowToAnswerComplementQuestion(accessEvaluationParameters, true)) {
            actionLinkList.add(new ActionLink(ActionLinkType.BESVARA_KOMPLETTERING));
        }

        if (certificateAccessServiceHelper.isAllowToAnswerComplementQuestion(accessEvaluationParameters, false)) {
            actionLinkList.add(new ActionLink(ActionLinkType.BESVARA_KOMPLETTERING_MED_MEDDELANDE));
        }

        if (certificateAccessServiceHelper.isAllowToForwardQuestions(accessEvaluationParameters)) {
            actionLinkList.add(new ActionLink(ActionLinkType.VIDAREBEFODRA_FRAGA));
        }

        if (certificateAccessServiceHelper.isAllowToSetComplementAsHandled(accessEvaluationParameters)) {
            actionLinkList.add(new ActionLink(ActionLinkType.MARKERA_KOMPLETTERING_SOM_HANTERAD));
        }

        if (certificateAccessServiceHelper.isAllowToSetQuestionAsHandled(accessEvaluationParameters)) {
            actionLinkList.add(new ActionLink(ActionLinkType.MARKERA_FRAGA_SOM_HANTERAD));
        }

        return actionLinkList;
    }
}
