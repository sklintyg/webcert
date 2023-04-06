/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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

import java.util.Optional;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Feature;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@Component
public class SrsFunctionImpl implements SrsFunction {

    @Override
    public Optional<ResourceLinkDTO> get(Certificate certificate, WebCertUser user) {
        return isSRSActive(certificate, user) ? Optional.of(getResourceLink(true)) : Optional.empty();
    }

    private ResourceLinkDTO getResourceLink(boolean enabled) {
        return ResourceLinkDTO.create(
            ResourceLinkTypeDTO.SRS,
            "Risk och råd",
            "Risk och råd",
            enabled
        );
    }

    private boolean isSRSActive(Certificate certificate, WebCertUser user) {
        final var feature = user.getFeatures().get(AuthoritiesConstants.FEATURE_SRS);
        if (feature == null) {
            return false;
        }

        return isDraft(certificate) && isFeatureActive(certificate.getMetadata().getType(), feature);
    }

    private static boolean isDraft(Certificate certificate) {
        return certificate.getMetadata().getStatus() == CertificateStatus.UNSIGNED;
    }

    private static boolean isFeatureActive(String certificateType, Feature feature) {
        return isCertificateTypeDefined(certificateType, feature) && feature.getGlobal();
    }

    private static boolean isCertificateTypeDefined(String certificateType, Feature feature) {
        return feature.getIntygstyper().isEmpty() || feature.getIntygstyper().contains(certificateType);
    }
}