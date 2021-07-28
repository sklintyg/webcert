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

import static se.inera.intyg.common.support.facade.model.CertificateRelationType.COMPLEMENTED;
import static se.inera.intyg.common.support.facade.model.CertificateRelationType.REPLACED;
import static se.inera.intyg.common.support.facade.model.CertificateStatus.SIGNED;
import static se.inera.intyg.common.support.facade.model.CertificateStatus.UNSIGNED;
import static se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO.FMB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.webcert.web.service.facade.GetCertificatesAvailableFunctions;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@Service
public class GetCertificatesAvailableFunctionsImpl implements GetCertificatesAvailableFunctions {

    private final AuthoritiesHelper authoritiesHelper;

    @Autowired
    public GetCertificatesAvailableFunctionsImpl(AuthoritiesHelper authoritiesHelper) {
        this.authoritiesHelper = authoritiesHelper;
    }

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

        if (isSignedAndSendDirectly(certificate)) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.SIGN_CERTIFICATE,
                    "Signera och skicka",
                    "Intyget skickas direkt till intygsmottagare",
                    true
                )
            );
        } else {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.SIGN_CERTIFICATE,
                    "Signera intyget",
                    "Signerar intygsutkast",
                    true
                )
            );
        }

        if (isMessagingAvailable(certificate) && isComplementingCertificate(certificate)) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.QUESTIONS,
                    "Ärendekommunikation",
                    "Hantera kompletteringsbegäran, frågor och svar",
                    true
                )
            );
        }

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

        if (isReplaceCertificateAvailable(certificate)) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.REPLACE_CERTIFICATE,
                    "Ersätt",
                    "Skapar en kopia av detta intyg som du kan redigera.",
                    true
                )
            );
        }

        if (isReplaceCertificateContinueAvailable(certificate)) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.REPLACE_CERTIFICATE_CONTINUE,
                    "Ersätt",
                    "Skapar en kopia av detta intyg som du kan redigera.",
                    true
                )
            );
        }

        if (isRenewCertificateAvailable(certificate)) {
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
                true
            )
        );

        if (isSendCertificateAvailable(certificate)) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.SEND_CERTIFICATE,
                    "Skicka till Försäkringskassan",
                    "Öppnar ett fönster där du kan välja att skicka intyget till Försäkringskassan",
                    "<p>Om du går vidare kommer intyget skickas direkt till "
                        + "Försäkringskassans system vilket ska göras i samråd med patienten.</p>"
                        +
                        "<p>Upplys patienten om att även göra en ansökan om sjukpenning hos Försäkringskassan.</p>",
                    true
                )
            );
        }

        if (isMessagingAvailable(certificate)) {
            if (isSent(certificate)) {
                resourceLinks.add(
                    ResourceLinkDTO.create(
                        ResourceLinkTypeDTO.QUESTIONS,
                        "Ärendekommunikation",
                        "Hantera kompletteringsbegäran, frågor och svar",
                        true
                    )
                );
            } else {
                resourceLinks.add(
                    ResourceLinkDTO.create(
                        ResourceLinkTypeDTO.QUESTIONS_NOT_AVAILABLE,
                        "Ärendekommunikation",
                        "Hantera kompletteringsbegäran, frågor och svar",
                        true
                    )
                );
            }
        }

        if (isMessagingAvailable(certificate) && isSent(certificate)) {
            resourceLinks.add(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.CREATE_QUESTIONS,
                    "Ny fråga",
                    "Här kan du ställa en ny fråga till Försäkringskassan.",
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

    private boolean isSent(Certificate certificate) {
        return certificate.getMetadata().isSent();
    }

    private boolean isSignedAndSendDirectly(Certificate certificate) {
        return authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_SIGNERA_SKICKA_DIREKT, certificate.getMetadata().getType())
            || isComplementingCertificate(certificate);
    }

    private boolean isComplementingCertificate(Certificate certificate) {
        return certificate.getMetadata().getRelations() != null && certificate.getMetadata().getRelations().getParent() != null
            && certificate.getMetadata().getRelations().getParent().getType() == COMPLEMENTED;
    }

    private boolean isSendCertificateAvailable(Certificate certificate) {
        if (isSent(certificate)) {
            return false;
        }

        if (isReplacementSigned(certificate)) {
            return false;
        }

        return certificate.getMetadata().getType().equalsIgnoreCase(LisjpEntryPoint.MODULE_ID);
    }

    private boolean isReplaceCertificateAvailable(Certificate certificate) {
        return !(isReplacementUnsigned(certificate) || isReplacementSigned(certificate));
    }

    private boolean isReplaceCertificateContinueAvailable(Certificate certificate) {
        return isReplacementUnsigned(certificate);
    }

    private boolean isRenewCertificateAvailable(Certificate certificate) {
        if (isReplacementSigned(certificate)) {
            return false;
        }

        return authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_FORNYA_INTYG, certificate.getMetadata().getType());
    }

    private boolean isReplacementUnsigned(Certificate certificate) {
        return includesChildRelation(certificate.getMetadata().getRelations(), REPLACED, UNSIGNED);
    }

    private boolean isReplacementSigned(Certificate certificate) {
        return includesChildRelation(certificate.getMetadata().getRelations(), REPLACED, SIGNED);
    }

    private boolean includesChildRelation(CertificateRelations relations, CertificateRelationType type, CertificateStatus status) {
        if (missingChildRelations(relations)) {
            return false;
        }

        return Arrays.stream(relations.getChildren()).anyMatch(
            relation -> relation.getType().equals(type) && relation.getStatus().equals(status)
        );
    }

    private boolean missingChildRelations(CertificateRelations relations) {
        return relations == null || relations.getChildren() == null;
    }

    private boolean isMessagingAvailable(Certificate certificate) {
        return authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_HANTERA_FRAGOR, certificate.getMetadata().getType());
    }
}
