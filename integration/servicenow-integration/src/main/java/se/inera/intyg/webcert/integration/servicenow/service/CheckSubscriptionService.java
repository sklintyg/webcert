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

package se.inera.intyg.webcert.integration.servicenow.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.integration.api.subscription.AuthenticationMethodEnum;

@Service
public class CheckSubscriptionService {

    @Value("#{${servicenow.service.codes.eleg}}")
    private List<String> elegServiceCodes;

    @Value("#{${servicenow.service.codes.siths}}")
    private List<String> sithsServiceCodes;

    public boolean isMissing(List<String> activeServiceCodes, AuthenticationMethodEnum authMethod) {
        if (activeServiceCodes.isEmpty()) {
            return true;
        }
        if (authMethod == AuthenticationMethodEnum.ELEG) {
            return activeServiceCodes.stream().noneMatch(serviceCode -> elegServiceCodes.contains(serviceCode));
        }
        return activeServiceCodes.stream().noneMatch(serviceCode -> sithsServiceCodes.contains(serviceCode));
    }
}
