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
package se.inera.intyg.webcert.integration.kundportalen.stub.state;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KundportalenStubState {

    private boolean subscriptionReturnValue = true;
    private int httpErrorCode = 0;
    private Map<String, List<String>> activeSubscriptions = new HashMap<>();

    @Value("#{${kundportalen.service.codes.eleg}}")
    private List<String> elegServiceCodes;

    @Value("#{${kundportalen.service.codes.siths}}")
    private List<String> sithsServiceCodes;

    public List<String> getServiceCodeList() {
        return Stream.concat(elegServiceCodes.stream(), sithsServiceCodes.stream()).collect(Collectors.toList());
    }

    public boolean getSubscriptionReturnValue() {
        return subscriptionReturnValue;
    }

    public void setSubscriptionReturnValue(boolean subscriptionReturnValue) {
        this.subscriptionReturnValue = subscriptionReturnValue;
    }

    public void setHttpErrorCode(int errorCode) {
        this.httpErrorCode = errorCode;
    }

    public int getHttpErrorCode() {
        return httpErrorCode;
    }

    public Map<String, List<String>> getActiveSubscriptions() {
        return activeSubscriptions;
    }

    public void setActiveSubscriptions(Map<String, List<String>> activeSubscriptions) {
        this.activeSubscriptions = activeSubscriptions;
    }

    public void clearActiveSubscriptions() {
        activeSubscriptions.clear();
    }
}
