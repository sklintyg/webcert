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
package se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions;

import static se.inera.intyg.common.support.facade.model.CertificateRelationType.COMPLEMENTED;
import static se.inera.intyg.common.support.facade.model.CertificateRelationType.REPLACED;
import static se.inera.intyg.common.support.facade.model.CertificateStatus.SIGNED;

import java.util.Arrays;
import java.util.Objects;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

public class CertificateRenewFunction {

    private static final String RENEW_NAME = "Förnya";
    private static final String RENEW_DESCRIPTION = "Skapar en redigerbar kopia av intyget på den enhet som du är inloggad på.";
    public static final String EVENTUAL_COMPLEMENTARY_REQUEST_WONT_BE_MARKED_READY = "Eventuell kompletteringsbegäran kommer inte "
        + "att klarmarkeras.";
    public static final String EVENTUAL_COMPLEMENTARY_WILL_BE_MARKED_READY = "Eventuell kompletteringsbegäran kommer att klarmarkeras.";


    public static boolean validate(String certificateType, CertificateRelations relations,
        CertificateStatus status, AuthoritiesHelper authoritiesHelper) {
        if (isReplacementSigned(relations) || hasBeenComplementedBySignedCertificate(relations)) {
            return false;
        }

        return validateStatus(status) && authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_FORNYA_INTYG, certificateType);
    }

    public static ResourceLinkDTO createResourceLink(String loggedInUnitId, String savedUnitId, String certificateType) {
        return ResourceLinkDTO.create(
            ResourceLinkTypeDTO.RENEW_CERTIFICATE,
            RENEW_NAME,
            RENEW_DESCRIPTION,
            getRenewBody(loggedInUnitId, savedUnitId, certificateType),
            true
        );
    }

    private static boolean validateStatus(CertificateStatus status) {
        return status == CertificateStatus.SIGNED;
    }

    private static boolean isReplacementSigned(CertificateRelations relations) {
        return includesChildRelation(relations, REPLACED, SIGNED);
    }

    private static boolean includesChildRelation(CertificateRelations relations, CertificateRelationType type, CertificateStatus status) {
        if (missingChildRelations(relations)) {
            return false;
        }

        return Arrays.stream(relations.getChildren()).anyMatch(
            relation -> relation.getType().equals(type) && relation.getStatus().equals(status)
        );
    }

    private static boolean missingChildRelations(CertificateRelations relations) {
        return relations == null || relations.getChildren() == null;
    }

    private static boolean hasBeenComplementedBySignedCertificate(CertificateRelations relations) {
        if (relations != null) {
            return Arrays.stream(relations.getChildren()).anyMatch(
                relation -> relation.getType().equals(
                    COMPLEMENTED) && relation.getStatus() == SIGNED
            );
        }
        return false;
    }

    private static String getRenewBody(String loggedInUnitId, String savedUnitId, String certificateType) {
        if (isLisjpCertificate(certificateType)) {
            final var complementaryText =
                isUserAndCertificateFromSameCareUnit(loggedInUnitId, savedUnitId)
                    ? EVENTUAL_COMPLEMENTARY_WILL_BE_MARKED_READY
                    : EVENTUAL_COMPLEMENTARY_REQUEST_WONT_BE_MARKED_READY;

            return String.format(
                "Förnya intyg kan användas vid förlängning av en sjukskrivning. När ett intyg förnyas skapas ett nytt intygsutkast"
                    + " med viss information från det ursprungliga intyget.<br><br>\n"
                    + "Uppgifterna i det nya intygsutkastet går att ändra innan det signeras.<br><br>\n"
                    + "De uppgifter som inte kommer med till det nya utkastet är:<br><br>\n"
                    + "<ul>\n"
                    + "<li>Sjukskrivningsperiod och grad.</li>\n"
                    + "<li>Valet om man vill ha kontakt med Försäkringskassan.</li>\n"
                    + "<li>Referenser som intyget baseras på.</li>\n"
                    + "</ul>\n"
                    + "<br>%s<br><br>\n"
                    + "Det nya utkastet skapas på den enhet du är inloggad på.", complementaryText);
        } else {
            return
                "Förnya intyg kan användas vid förlängning av en sjukskrivning. När ett intyg förnyas skapas ett nytt intygsutkast"
                    + " med viss information från det ursprungliga intyget.<br><br>"
                    + "Uppgifterna i det nya intygsutkastet går att ändra innan det signeras.<br><br>"
                    + "De uppgifter som inte kommer med till det nya utkastet är:<br><br>"
                    + "<ul>"
                    + "<li>Valet om diagnos ska förmedlas till arbetsgivaren</li>"
                    + "<li>Valet om funktionsnedsättning ska förmedlas till arbetsgivaren</li>"
                    + "<li>Sjukskrivningsperiod och grad</li>"
                    + "<li>Valet om man vill ha kontakt med arbetsgivaren</li>"
                    + "<li>Referenser som intyget baseras på</li>"
                    + "</ul>"
                    + "<br>Det nya utkastet skapas på den enhet du är inloggad på.";
        }
    }

    private static Boolean isLisjpCertificate(String certificateType) {
        return Objects.equals(certificateType, LisjpEntryPoint.MODULE_ID);
    }

    private static Boolean isUserAndCertificateFromSameCareUnit(String loggedInCareUnitId, String savedUnitId) {
        return Objects.equals(loggedInCareUnitId, savedUnitId);
    }
}
