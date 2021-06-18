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

import static se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO.FMB;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.web.event.CertificateEventService;
import se.inera.intyg.webcert.web.service.facade.GetCertificatesAvailableFunctions;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@Service
public class GetCertificatesAvailableFunctionsImpl implements GetCertificatesAvailableFunctions {

    @Autowired
    CertificateEventService certificateEventService;

    @Override
    public List<ResourceLinkDTO> get(Certificate certificate) {
        final var availableFunctions = new ArrayList<ResourceLinkDTO>();
        switch (certificate.getMetadata().getStatus()) {
            case UNSIGNED:
                availableFunctions.addAll(
                    getAvailableFunctionsForDraft(certificate)
                );
                break;
            case SIGNED:
                availableFunctions.addAll(
                    getAvailableFunctionsForCertificate(certificate)
                );
                break;
            case LOCKED:
                availableFunctions.addAll(
                    getAvailableFunctionsForLockedDraft(certificate)
                );
                break;
            default:
        }
        return availableFunctions;
    }

    private ArrayList<ResourceLinkDTO> getAvailableFunctionsForDraft(Certificate certificate) {
        final var resourceLinks = new ArrayList<ResourceLinkDTO>();

        resourceLinks.add(
            ResourceLinkDTO.create(
                ResourceLinkTypeDTO.EDIT_CERTIFICATE,
                "Ändra",
                "Ändrar intygsutkast",
                true
            )
        );

        resourceLinks.add(
            ResourceLinkDTO.create(
                ResourceLinkTypeDTO.PRINT_CERTIFICATE,
                "Skriv ut",
                "Laddar ned intygsutkastet för utskrift.",
                true
            )
        );

        resourceLinks.add(
            ResourceLinkDTO.create(
                ResourceLinkTypeDTO.REMOVE_CERTIFICATE,
                "Radera",
                "Raderar intygsutkast",
                true
            )
        );

        resourceLinks.add(
            ResourceLinkDTO.create(
                ResourceLinkTypeDTO.SIGN_CERTIFICATE,
                "Signera",
                "Signerar intygsutkast",
                true
            )
        );

        resourceLinks.add(
            ResourceLinkDTO.create(
                ResourceLinkTypeDTO.FORWARD_CERTIFICATE,
                "Vidarebefodra utkast",
                "Skapar ett e-postmeddelande i din e-postklient med en direktlänk till utkastet.",
                true
            )
        );

        if (certificate.getMetadata().getType().equalsIgnoreCase(LisjpEntryPoint.MODULE_ID)) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    FMB,
                    "FMB",
                    "Läs FMB - ett stöd för ifyllnad och bedömning",
                    true
                )
            );
        }

        return resourceLinks;
    }

    private ArrayList<ResourceLinkDTO> getAvailableFunctionsForCertificate(Certificate certificate) {
        final var resourceLinks = new ArrayList<ResourceLinkDTO>();

        resourceLinks.add(
            ResourceLinkDTO.create(
                ResourceLinkTypeDTO.PRINT_CERTIFICATE,
                "Skriv ut",
                "Laddar ned intyget för utskrift.",
                true
            )
        );

        resourceLinks.add(
            ResourceLinkDTO.create(
                ResourceLinkTypeDTO.REPLACE_CERTIFICATE,
                "Ersätt",
                "Skapar en kopia av detta intyg som du kan redigera.",
                true
            )
        );

        if (certificate.getMetadata().getType().equalsIgnoreCase(LisjpEntryPoint.MODULE_ID)) {
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

        resourceLinks.add(
            ResourceLinkDTO.create(
                ResourceLinkTypeDTO.REVOKE_CERTIFICATE,
                "Makulera",
                "Öppnar ett fönster där du kan välja att makulera intyget.",
                "<p>Om du går vidare kommer intyget skickas direkt till Försäkringskassans system vilket ska göras i samråd med patienten.</p>"
                    +
                    "<p>Upplys patienten om att även göra en ansökan om sjukpenning hos Försäkringskassan.</p>",
                true
            )
        );

        if (certificate.getMetadata().getType().equalsIgnoreCase(LisjpEntryPoint.MODULE_ID) && certificateEventService
            .isCertificateSent(certificate.getMetadata().getId())) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.SEND_CERTIFICATE,
                    "Skicka intyg",
                    "Öppnar ett fönster där du kan välja att skicka intyget till Försäkringskassan",
                    " ",
                    true
                )
            );
        }

        return resourceLinks;
    }

    private ArrayList<ResourceLinkDTO> getAvailableFunctionsForLockedDraft(Certificate certificate) {
        final var resourceLinks = new ArrayList<ResourceLinkDTO>();
        resourceLinks.add(
            ResourceLinkDTO.create(
                ResourceLinkTypeDTO.PRINT_CERTIFICATE,
                "Skriv ut",
                "Laddar ned intygsutkastet för utskrift.",
                true
            )
        );

        resourceLinks.add(
            ResourceLinkDTO.create(
                ResourceLinkTypeDTO.COPY_CERTIFICATE,
                "Kopiera",
                "Skapar en redigerbar kopia av utkastet på den enheten du är inloggad på.",
                true
            )
        );

        resourceLinks.add(
            ResourceLinkDTO.create(ResourceLinkTypeDTO.REVOKE_CERTIFICATE,
                "Makulera",
                "Öppnar ett fönster där du kan välja att makulera det låsta utkastet.",
                true
            )
        );

        return resourceLinks;
    }
}
