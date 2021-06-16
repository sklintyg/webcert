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

package se.inera.intyg.webcert.web.service.facade.impl;

import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.access.AccessEvaluationParameters;
import se.inera.intyg.webcert.web.service.access.CertificateAccessServiceHelper;
import se.inera.intyg.webcert.web.service.access.DraftAccessServiceHelper;
import se.inera.intyg.webcert.web.service.access.LockedDraftAccessServiceHelper;
import se.inera.intyg.webcert.web.service.facade.GetCertificationResourceLinks;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@Service
public class GetCertificationResourceLinksImpl implements GetCertificationResourceLinks {

    private final DraftAccessServiceHelper draftAccessServiceHelper;
    private final LockedDraftAccessServiceHelper lockedDraftAccessServiceHelper;
    private final CertificateAccessServiceHelper certificateAccessServiceHelper;

    @Autowired
    public GetCertificationResourceLinksImpl(
        DraftAccessServiceHelper draftAccessServiceHelper,
        LockedDraftAccessServiceHelper lockedDraftAccessServiceHelper,
        CertificateAccessServiceHelper certificateAccessServiceHelper) {
        this.draftAccessServiceHelper = draftAccessServiceHelper;
        this.lockedDraftAccessServiceHelper = lockedDraftAccessServiceHelper;
        this.certificateAccessServiceHelper = certificateAccessServiceHelper;
    }

    @Override
    public ResourceLinkDTO[] get(Certificate certificate) {
        final var resourceLinks = new ArrayList<ResourceLinkDTO>();
        final var accessEvaluationParameters = createAccessEvaluationParameters(certificate);
        switch (certificate.getMetadata().getStatus()) {
            case UNSIGNED:
                resourceLinks.addAll(
                    getResourceLinksForDraft(certificate, accessEvaluationParameters)
                );
                break;
            case SIGNED:
                resourceLinks.addAll(
                    getResourceLinksForCertificate(certificate, accessEvaluationParameters)
                );
                break;
            case LOCKED:
                resourceLinks.addAll(
                    getResourceLinksForLockedDraft(certificate, accessEvaluationParameters)
                );
                break;
            default:
        }
        return resourceLinks.toArray(new ResourceLinkDTO[0]);
    }

    private ArrayList<ResourceLinkDTO> getResourceLinksForDraft(Certificate certificate,
        AccessEvaluationParameters accessEvaluationParameters) {
        final var resourceLinks = new ArrayList<ResourceLinkDTO>();
        if (draftAccessServiceHelper.isAllowToEditUtkast(accessEvaluationParameters)) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.EDIT_CERTIFICATE,
                    "Ändra",
                    "Ändrar intygsutkast",
                    true
                )
            );
        }
        if (draftAccessServiceHelper.isAllowToPrintUtkast(accessEvaluationParameters)) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.PRINT_CERTIFICATE,
                    "Skriv ut",
                    "Laddar ned intygsutkastet för utskrift.",
                    true
                )
            );
        }
        if (draftAccessServiceHelper.isAllowToDeleteUtkast(accessEvaluationParameters)) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.REMOVE_CERTIFICATE,
                    "Radera",
                    "Raderar intygsutkast",
                    true
                )
            );
        }
        if (draftAccessServiceHelper.isAllowToSign(accessEvaluationParameters, certificate.getMetadata().getId())) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.SIGN_CERTIFICATE,
                    "Signera",
                    "Signerar intygsutkast",
                    true
                )
            );
        }
        if (draftAccessServiceHelper.isAllowedToForwardUtkast(accessEvaluationParameters)) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.FORWARD_CERTIFICATE,
                    "Vidarebefodra utkast",
                    "Skapar ett e-postmeddelande i din e-postklient med en direktlänk till utkastet.",
                    true
                )
            );
        }
        return resourceLinks;
    }

    private ArrayList<ResourceLinkDTO> getResourceLinksForCertificate(Certificate certificate,
        AccessEvaluationParameters accessEvaluationParameters) {
        final var resourceLinks = new ArrayList<ResourceLinkDTO>();
        if (certificateAccessServiceHelper.isAllowToPrint(accessEvaluationParameters, false)) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.PRINT_CERTIFICATE,
                    "Skriv ut",
                    "Laddar ned intyget för utskrift.",
                    true
                )
            );
        }
        if (certificateAccessServiceHelper.isAllowToReplace(accessEvaluationParameters)) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.REPLACE_CERTIFICATE,
                    "Ersätt",
                    "Skapar en kopia av detta intyg som du kan redigera.",
                    true
                )
            );
        }
        if (certificateAccessServiceHelper.isAllowToRenew(accessEvaluationParameters)) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.RENEW_CERTIFICATE,
                    "Förnya",
                    "Skapar en redigerbar kopia av intyget på den enhet som du är inloggad på.",
                    "Förnya intyg kan användas vid förlängning av en sjukskrivning. När ett intyg förnyas skapas ett nytt intygsutkast"
                        + " med viss information från det ursprungliga intyget.<br><br>\n"
                        + "Uppgifterna i det nya intygsutkastet går att ändra innan det signeras.<br><br>\n"
                        + "De uppgifter som inte kommer med till det nya utkastet är:<br><br>\n"
                        + "<ul>\n"
                        + "<li>Sjukskrivningsperiod och grad.</li>\n"
                        + "<li>Valet om man vill ha kontakt med Försäkringskassan.</li>\n"
                        + "<li>Referenser som intyget baseras på.</li>\n"
                        + "</ul>\n"
                        + "<br>Eventuell kompletteringsbegäran kommer att klarmarkeras.<br><br>\n"
                        + "Det nya utkastet skapas på den enhet du är inloggad på.",
                    true
                )
            );
        }
        if (certificateAccessServiceHelper.isAllowToInvalidate(accessEvaluationParameters)) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.REVOKE_CERTIFICATE,
                    "Makulera",
                    "Öppnar ett fönster där du kan välja att makulera intyget.",
                    true
                )
            );
        }
        return resourceLinks;
    }

    private ArrayList<ResourceLinkDTO> getResourceLinksForLockedDraft(Certificate certificate,
        AccessEvaluationParameters accessEvaluationParameters) {
        final var resourceLinks = new ArrayList<ResourceLinkDTO>();
        if (lockedDraftAccessServiceHelper.isAllowToPrint(accessEvaluationParameters)) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.PRINT_CERTIFICATE,
                    "Skriv ut",
                    "Laddar ned intygsutkastet för utskrift.",
                    true
                )
            );
        }
        if (lockedDraftAccessServiceHelper.isAllowToCopy(accessEvaluationParameters)) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.COPY_CERTIFICATE,
                    "Kopiera",
                    "Skapar en redigerbar kopia av utkastet på den enheten du är inloggad på.",
                    true
                )
            );
        }
        if (lockedDraftAccessServiceHelper.isAllowToInvalidate(accessEvaluationParameters)) {
            resourceLinks.add(
                ResourceLinkDTO.create(ResourceLinkTypeDTO.REVOKE_CERTIFICATE,
                    "Makulera",
                    "Öppnar ett fönster där du kan välja att makulera det låsta utkastet.",
                    true
                )
            );
        }
        return resourceLinks;
    }

    private AccessEvaluationParameters createAccessEvaluationParameters(Certificate certificate) {
        final var unit = createUnit(certificate);

        return AccessEvaluationParameters.create(
            certificate.getMetadata().getType(),
            certificate.getMetadata().getTypeVersion(),
            unit,
            Personnummer.createPersonnummer(certificate.getMetadata().getPatient().getPersonId().getId()).orElseThrow(),
            certificate.getMetadata().isTestCertificate()
        );
    }

    private Vardenhet createUnit(Certificate certificate) {
        final var unit = new Vardenhet();
        unit.setEnhetsid(certificate.getMetadata().getUnit().getUnitId());
        unit.setVardgivare(new Vardgivare());
        unit.getVardgivare().setVardgivarid(certificate.getMetadata().getCareProvider().getUnitId());
        return unit;
    }
}
