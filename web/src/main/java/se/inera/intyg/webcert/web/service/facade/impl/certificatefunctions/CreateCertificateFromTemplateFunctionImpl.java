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
import java.util.Optional;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@Component
public class CreateCertificateFromTemplateFunctionImpl implements CreateCertificateFromTemplateFunction {

    private static final String CREATE_AG7804_NAME = "Skapa AG7804";

    private static final String CREATE_AG7804_DESCRIPTION = "Skapar ett intyg till arbetsgivaren utifrån Försäkringskassans intyg.";
    private static final String CREATE_AG7804_BODY = "<div><div class=\"ic-alert ic-alert--status ic-alert--info\">\n"
        + "<i class=\"ic-alert__icon ic-info-icon\"></i>\n"
        + "Kom ihåg att stämma av med patienten om hen vill att du skickar Läkarintyget för sjukpenning till Försäkringskassan. "
        + "Gör detta i så fall först.</div>"
        + "<p class='iu-pt-400'>Skapa ett Läkarintyg om arbetsförmåga - arbetsgivaren (AG7804)"
        + " utifrån ett Läkarintyg för sjukpenning innebär att "
        + "informationsmängder som är gemensamma för båda intygen automatiskt förifylls.\n"
        + "</p></div>";

    private static final String CREATE_DOI_NAME = "Skapa dödsorsaksintyg";
    private static final String CREATE_DOI_DESCRIPTION = "Skapar ett dödsorsaksintyg utifrån dödsbeviset.";
    private static final String CREATE_DOI_BODY = "Skapa ett dödsorsaksintyg utifrån ett dödsbevis innebär att informationsmängder som är "
        + "gemensamma för båda intygen, automatiskt förifylls.";

    @Override
    public Optional<ResourceLinkDTO> get(Certificate certificate, WebCertUser webCertUser) {
        if (isCreateAg7804FromFk7804(certificate, webCertUser)) {
            return Optional.of(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.CREATE_CERTIFICATE_FROM_TEMPLATE,
                    CREATE_AG7804_NAME,
                    CREATE_AG7804_DESCRIPTION,
                    CREATE_AG7804_BODY,
                    true
                )
            );
        }

        if (isCreateDoiFromDb(certificate, webCertUser)) {
            return Optional.of(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.CREATE_CERTIFICATE_FROM_TEMPLATE,
                    CREATE_DOI_NAME,
                    CREATE_DOI_DESCRIPTION,
                    CREATE_DOI_BODY,
                    true
                )
            );
        }

        return Optional.empty();
    }

    private boolean isCreateAg7804FromFk7804(Certificate certificate, WebCertUser webCertUser) {
        if (notSigned(certificate)
            || notCorrectCertificateType(certificate, LisjpEntryPoint.MODULE_ID)
            || isReplacementSigned(certificate)
            || isDjupintegration(webCertUser)
            || hasBeenComplementedBySignedCertificate(certificate)
            || isRevoked(certificate)
        ) {
            return false;
        }

        return true;
    }

    private boolean isCreateDoiFromDb(Certificate certificate, WebCertUser webCertUser) {
        if (notSigned(certificate)
            || notCorrectCertificateType(certificate, DbModuleEntryPoint.MODULE_ID)
            || isReplacementSigned(certificate)
            || isDjupintegration(webCertUser)
            || isRevoked(certificate)
        ) {
            return false;
        }

        return true;
    }

    private static boolean notSigned(Certificate certificate) {
        return certificate.getMetadata().getStatus() != SIGNED;
    }

    private static boolean notCorrectCertificateType(Certificate certificate, String certificateType) {
        return !certificate.getMetadata().getType().equalsIgnoreCase(certificateType);
    }

    private boolean isReplacementSigned(Certificate certificate) {
        return includesChildRelation(certificate.getMetadata().getRelations(), REPLACED, SIGNED);
    }

    private boolean isDjupintegration(WebCertUser webCertUser) {
        return webCertUser != null && webCertUser.getOrigin().contains("DJUPINTEGRATION");
    }

    private boolean isRevoked(Certificate certificate) {
        return certificate.getMetadata().getStatus() == CertificateStatus.REVOKED;
    }

    private boolean includesChildRelation(CertificateRelations relations, CertificateRelationType type, CertificateStatus status) {
        if (missingChildRelations(relations)) {
            return false;
        }

        return Arrays.stream(relations.getChildren()).anyMatch(
            relation -> relation.getType().equals(type) && relation.getStatus().equals(status)
        );
    }

    private boolean hasBeenComplementedBySignedCertificate(Certificate certificate) {
        if (certificate.getMetadata().getRelations() != null) {
            return Arrays.stream(certificate.getMetadata().getRelations().getChildren()).anyMatch(
                relation -> relation.getType().equals(COMPLEMENTED) && relation.getStatus() == SIGNED
            );
        }
        return false;
    }

    private boolean missingChildRelations(CertificateRelations relations) {
        return relations == null || relations.getChildren() == null;
    }
}
