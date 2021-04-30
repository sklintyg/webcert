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
import java.util.List;
import java.util.Objects;

public class SubscriptionInfo implements Serializable {

    @JsonProperty
    private SubscriptionAction subscriptionAction;
    @JsonProperty
    private List<String> unitHsaIdList;

    public SubscriptionInfo(SubscriptionAction subscriptionAction, List<String> unitHsaIdList) {
        this.subscriptionAction = subscriptionAction;
        this.unitHsaIdList = unitHsaIdList;
    }

    public static SubscriptionInfo createSubscriptionInfoFeaturesNotActive() {
        return new SubscriptionInfo(SubscriptionAction.NONE_SUBSCRIPTION_FEATURES_NOT_ACTIVE, null);
    }

    public SubscriptionAction getSubscriptionAction() {
        return subscriptionAction;
    }

    public void setSubscriptionAction(SubscriptionAction subscriptionAction) {
        this.subscriptionAction = subscriptionAction;
    }

    public List<String> getUnitHsaIdList() {
        return unitHsaIdList;
    }

    public void setUnitHsaIdList(List<String> unitHsaIdList) {
        this.unitHsaIdList = unitHsaIdList;
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
        return subscriptionAction == that.subscriptionAction && Objects.equals(unitHsaIdList, that.unitHsaIdList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriptionAction, unitHsaIdList);
    }
}
