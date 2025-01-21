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

package se.inera.intyg.webcert.web.service.facade.internalapi.availablefunction;

import static se.inera.intyg.webcert.web.service.facade.internalapi.availablefunction.AvailableFunctionUtils.isReplacedOrComplemented;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.webcert.web.service.facade.internalapi.AvailableFunctions;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.AvailableFunctionDTO;

@Component
public class CertificateSendFunction implements AvailableFunctions {

    private final AuthoritiesHelper authoritiesHelper;

    public CertificateSendFunction(AuthoritiesHelper authoritiesHelper) {
        this.authoritiesHelper = authoritiesHelper;
    }

    @Override
    public List<AvailableFunctionDTO> get(Certificate certificate) {
        final var availableFunctions = new ArrayList<AvailableFunctionDTO>();

        if (isSendFunctionActive(certificate)) {
            availableFunctions.add(AvailableFunctionFactory.send(!certificate.getMetadata().isSent()));
        }

        return availableFunctions;
    }

    private boolean isSendFunctionActive(Certificate certificate) {
        final var type = certificate.getMetadata().getType();
        final var latestMajorVersion = certificate.getMetadata().isLatestMajorVersion();
        final var recipient = certificate.getMetadata().getRecipient();
        final var relations = certificate.getMetadata().getRelations();

        return isSendFeatureActive(type)
            && isVersionAbleToSend(latestMajorVersion, type)
            && recipient != null
            && !isReplacedOrComplemented(relations);
    }

    private boolean isVersionAbleToSend(boolean latestMajorVersion, String type) {
        if (latestMajorVersion) {
            return true;
        }

        return !isFeatureInactiveOlderVersionsActive(type);
    }

    private boolean isFeatureInactiveOlderVersionsActive(String type) {
        return authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_INACTIVATE_PREVIOUS_MAJOR_VERSION, type);
    }

    private boolean isSendFeatureActive(String type) {
        return authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_SKICKA_INTYG, type);
    }

}
