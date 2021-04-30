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
package se.inera.intyg.webcert.web.service.subscription;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Feature;
import se.inera.intyg.webcert.web.web.controller.integration.dto.SubscriptionInfo;
import se.inera.intyg.webcert.web.web.controller.integration.dto.SubscriptionAction;
import se.inera.intyg.infra.security.common.model.UserOriginType;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    @Override
    public SubscriptionInfo fetchSubscriptionInfo(String origin, Map<String, Feature> features, List<String> careProviderHsaIdList) {
        final var missingSubscriptionAction = determineSubscriptionAction(origin, features);
        if (missingSubscriptionAction != SubscriptionAction.NONE_SUBSCRIPTION_FEATURES_NOT_ACTIVE) {
            //TODO: Call Kundportalen
            return new SubscriptionInfo(missingSubscriptionAction, careProviderHsaIdList);
        }

        return SubscriptionInfo.createSubscriptionInfoFeaturesNotActive();
    }

    @Override
    public SubscriptionAction determineSubscriptionAction(String origin, Map<String, Feature> features) {
        if (origin.equals(UserOriginType.NORMAL.name())) {
            if (Boolean.TRUE.equals(features.get(AuthoritiesConstants.FEATURE_SUBSCRIPTION_PAST_ADJUSTMENT_PERIOD).getGlobal())) {
                return SubscriptionAction.MISSING_SUBSCRIPTION_BLOCK;
            } else if (Boolean.TRUE.equals(features.get(AuthoritiesConstants.FEATURE_SUBSCRIPTION_DURING_ADJUSTMENT_PERIOD).getGlobal())) {
                return SubscriptionAction.MISSING_SUBSCRIPTION_WARN;
            }
        }

        return SubscriptionAction.NONE_SUBSCRIPTION_FEATURES_NOT_ACTIVE;
    }

}
