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
package se.inera.intyg.webcert.web.service.subscription.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import se.inera.intyg.webcert.web.service.subscription.enumerations.SubscriptionState;

public class SubscriptionInfo implements Serializable {

    @JsonProperty
    private SubscriptionState subscriptionState;
    @JsonProperty
    private String requireSubscriptionStartDate;

    public SubscriptionInfo(SubscriptionState subscriptionState, String requireSubscriptionStartDate) {
        this.subscriptionState = subscriptionState;
        this.requireSubscriptionStartDate = requireSubscriptionStartDate;
    }

    public SubscriptionState getSubscriptionState() {
        return subscriptionState;
    }

    public void setSubscriptionState(SubscriptionState subscriptionState) {
        this.subscriptionState = subscriptionState;
    }

    public String getRequireSubscriptionStartDate() {
        return requireSubscriptionStartDate;
    }

    public void setRequireSubscriptionStartDate(String requireSubscriptionStartDate) {
        this.requireSubscriptionStartDate = requireSubscriptionStartDate;
    }
}
