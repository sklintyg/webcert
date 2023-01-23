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
package se.inera.intyg.webcert.integration.kundportalen.stub.service;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.integration.kundportalen.stub.state.KundportalenStubState;

@Service
public class KundportalenStubSettingsApiService {

    private final KundportalenStubState stubState;

    public KundportalenStubSettingsApiService(KundportalenStubState stubState) {
        this.stubState = stubState;
    }

    public void setSubscriptionReturnValue(Boolean returnValue) {
        stubState.setSubscriptionReturnValue(returnValue);
        clearActiveSubscriptions();
    }

    public Boolean getSubscriptionReturnValue() {
        return stubState.getSubscriptionReturnValue();
    }

    public void setActiveSubscription(String orgNumber, String serviceCode) {
        final var activeSubscriptions = stubState.getActiveSubscriptions();
        if (activeSubscriptions.containsKey(orgNumber)) {
            final var serviceCodes = activeSubscriptions.get(orgNumber);
            if (!serviceCodes.contains(serviceCode)) {
                serviceCodes.add(serviceCode);
            }
        } else {
            activeSubscriptions.put(orgNumber, List.of(serviceCode));
        }
    }

    public void removeActiveSubscriptions(String orgNumber) {
        final var activeSubscriptions = stubState.getActiveSubscriptions();
        activeSubscriptions.remove(orgNumber);
    }

    public void clearActiveSubscriptions() {
        final var activeSubscriptions = stubState.getActiveSubscriptions();
        activeSubscriptions.clear();
    }

    public Map<String, List<String>> getActiveSubscriptions() {
        return stubState.getActiveSubscriptions();
    }

    public void setHttpError(int errorCode) {
        stubState.setHttpErrorCode(errorCode);
    }

    public void clearHttpError() {
        stubState.setHttpErrorCode(0);
    }
}
