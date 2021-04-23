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

public class SubscriptionInfo implements Serializable {

    @JsonProperty
    private MissingSubscriptionAction missingSubscriptionAction;
    @JsonProperty
    private List<String> unitHsaIdList;

    public SubscriptionInfo(MissingSubscriptionAction missingSubscriptionAction, List<String> unitHsaIdList) {
        this.missingSubscriptionAction = missingSubscriptionAction;
        this.unitHsaIdList = unitHsaIdList;
    }

    public SubscriptionInfo(MissingSubscriptionAction missingSubscriptionAction) {
        this.missingSubscriptionAction = missingSubscriptionAction;
        this.unitHsaIdList = new ArrayList<>();
    }

    public MissingSubscriptionAction getMissingSubscriptionAction() {
        return missingSubscriptionAction;
    }

    public void setMissingSubscriptionAction(MissingSubscriptionAction missingSubscriptionAction) {
        this.missingSubscriptionAction = missingSubscriptionAction;
    }

    public List<String> getUnitHsaIdList() {
        return unitHsaIdList;
    }

    public void setUnitHsaIdList(List<String> unitHsaIdList) {
        this.unitHsaIdList = unitHsaIdList;
    }
}
