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
package se.inera.intyg.webcert.web.web.controller.integration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import se.inera.intyg.webcert.web.service.subscription.AuthenticationMethodEnum;

public class SubscriptionInfo implements Serializable {

    @JsonProperty
    private SubscriptionState subscriptionState;
    @JsonProperty
    private AuthenticationMethodEnum authenticationMethod;
    @JsonProperty
    private List<String> careProviderHsaIdList;
    @JsonProperty
    private List<String> acknowledgedWarnings;
    @JsonProperty
    private String requireSubscriptionStartDate;

    public SubscriptionInfo(SubscriptionState subscriptionState, List<String> careProviderHsaIdList,
        AuthenticationMethodEnum authenticationMethod, String requireSubscriptionStartDate) {
        this.subscriptionState = subscriptionState;
        this.authenticationMethod = authenticationMethod;
        this.careProviderHsaIdList = careProviderHsaIdList;
        this.acknowledgedWarnings = new ArrayList<>();
        this.requireSubscriptionStartDate = requireSubscriptionStartDate;
    }

    public static SubscriptionInfo createSubscriptionInfoNoAction() {
        return new SubscriptionInfo(SubscriptionState.NONE, new ArrayList<>(), null, null);
    }

    public SubscriptionState getSubscriptionState() {
        return subscriptionState;
    }

    public void setSubscriptionState(SubscriptionState subscriptionState) {
        this.subscriptionState = subscriptionState;
    }

    public AuthenticationMethodEnum getAuthenticationMethod() {
        return authenticationMethod;
    }

    public void setAuthenticationMethod(AuthenticationMethodEnum authenticationMethod) {
        this.authenticationMethod = authenticationMethod;
    }

    public List<String> getCareProviderHsaIdList() {
        return careProviderHsaIdList;
    }

    public void setCareProviderHsaIdList(List<String> careProviderHsaIdList) {
        this.careProviderHsaIdList = careProviderHsaIdList;
    }

    public List<String> getAcknowledgedWarnings() {
        return acknowledgedWarnings;
    }

    public void setAcknowledgedWarnings(List<String> acknowledgedWarnings) {
        this.acknowledgedWarnings = acknowledgedWarnings;
    }

    public String getRequireSubscriptionStartDate() {
        return requireSubscriptionStartDate;
    }

    public void setRequireSubscriptionStartDate(String requireSubscriptionStartDate) {
        this.requireSubscriptionStartDate = requireSubscriptionStartDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SubscriptionInfo that = (SubscriptionInfo) o;
        return subscriptionState == that.subscriptionState && Objects.equals(careProviderHsaIdList, that.careProviderHsaIdList)
            && Objects.equals(acknowledgedWarnings, that.acknowledgedWarnings) && Objects.equals(requireSubscriptionStartDate,
            that.requireSubscriptionStartDate) && Objects.equals(authenticationMethod, that.authenticationMethod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriptionState, authenticationMethod, careProviderHsaIdList, acknowledgedWarnings,
            requireSubscriptionStartDate);
    }
}
