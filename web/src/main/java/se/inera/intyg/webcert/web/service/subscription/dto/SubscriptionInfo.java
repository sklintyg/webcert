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
import java.util.ArrayList;
import java.util.List;

public class SubscriptionInfo implements Serializable {

    @JsonProperty
    private SubscriptionAction subscriptionAction;

    @JsonProperty
    private List<String> careProvidersMissingSubscription;

    @JsonProperty
    private String subscriptionAdaptationStartDate;

    @JsonProperty
    private String requireSubscriptionStartDate;

    public SubscriptionInfo() {

    }

    public SubscriptionInfo(String subscriptionAdaptationStartDate, String requireSubscriptionStartDate) {
        this.subscriptionAdaptationStartDate = subscriptionAdaptationStartDate;
        this.requireSubscriptionStartDate = requireSubscriptionStartDate;
        this.subscriptionAction = SubscriptionAction.NONE;
        this.careProvidersMissingSubscription = new ArrayList<>();
    }

    public SubscriptionAction getSubscriptionAction() {
        return subscriptionAction;
    }

    public void setSubscriptionAction(SubscriptionAction subscriptionAction) {
        this.subscriptionAction = subscriptionAction;
    }

    public List<String> getCareProvidersMissingSubscription() {
        return careProvidersMissingSubscription;
    }

    public void setCareProvidersMissingSubscription(
        List<String> careProvidersMissingSubscription) {
        this.careProvidersMissingSubscription = careProvidersMissingSubscription;
    }

    public String getSubscriptionAdaptationStartDate() {
        return subscriptionAdaptationStartDate;
    }

    public void setSubscriptionAdaptationStartDate(String subscriptionAdaptationStartDate) {
        this.subscriptionAdaptationStartDate = subscriptionAdaptationStartDate;
    }

    public String getRequireSubscriptionStartDate() {
        return requireSubscriptionStartDate;
    }

    public void setRequireSubscriptionStartDate(String requireSubscriptionStartDate) {
        this.requireSubscriptionStartDate = requireSubscriptionStartDate;
    }
}
