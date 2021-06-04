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
    private SubscriptionAction subscriptionAction;
    @JsonProperty
    private AuthenticationMethodEnum authenticationMethod;
    @JsonProperty
    private List<String> unitHsaIdList; //TODO: Rename to careProviderList or move to enhet/mottagning as hasSubscription or similar
    @JsonProperty
    private List<String> acknowledgedWarnings;
    @JsonProperty
    private String subscriptionBlockStartDate;

    public SubscriptionInfo(SubscriptionAction subscriptionAction, List<String> unitHsaIdList,
        AuthenticationMethodEnum authenticationMethod, String subscriptionBlockStartDate) {
        this.subscriptionAction = subscriptionAction;
        this.authenticationMethod = authenticationMethod;
        this.unitHsaIdList = unitHsaIdList;
        this.acknowledgedWarnings = new ArrayList<>();
        this.subscriptionBlockStartDate = subscriptionBlockStartDate;
    }

    public static SubscriptionInfo createSubscriptionInfoNoAction() {
        return new SubscriptionInfo(SubscriptionAction.NONE, new ArrayList<>(), null, null);
    }

    public SubscriptionAction getSubscriptionAction() {
        return subscriptionAction;
    }

    public void setSubscriptionAction(SubscriptionAction subscriptionAction) {
        this.subscriptionAction = subscriptionAction;
    }

    public AuthenticationMethodEnum getAuthenticationMethod() {
        return authenticationMethod;
    }

    public void setAuthenticationMethod(AuthenticationMethodEnum authenticationMethod) {
        this.authenticationMethod = authenticationMethod;
    }

    public List<String> getUnitHsaIdList() {
        return unitHsaIdList;
    }

    public void setUnitHsaIdList(List<String> unitHsaIdList) {
        this.unitHsaIdList = unitHsaIdList;
    }

    public List<String> getAcknowledgedWarnings() {
        return acknowledgedWarnings;
    }

    public void setAcknowledgedWarnings(List<String> acknowledgedWarnings) {
        this.acknowledgedWarnings = acknowledgedWarnings;
    }

    public String getAdjustmentPeriodStartDate() {
        return subscriptionBlockStartDate;
    }

    public void setAdjustmentPeriodStartDate(String subscriptionBlockStartDate) {
        this.subscriptionBlockStartDate = subscriptionBlockStartDate;
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
        return subscriptionAction == that.subscriptionAction && Objects.equals(unitHsaIdList, that.unitHsaIdList)
            && Objects.equals(acknowledgedWarnings, that.acknowledgedWarnings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriptionAction, unitHsaIdList);
    }
}
