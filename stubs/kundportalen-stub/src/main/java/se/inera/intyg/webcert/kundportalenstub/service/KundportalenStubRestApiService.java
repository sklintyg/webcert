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

package se.inera.intyg.webcert.kundportalenstub.service;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.kundportalenstub.state.KundportalenStubState;

@Service
public class KundportalenStubRestApiService {

    @Autowired
    private KundportalenStubState stubState;

    public List<Map<String, String>> getServices() {
        return stubState.getSServices();
    }

    public Boolean getSubscriptionInfo(String orgNumber, String serviceCode) {
        final var activeSubscriptions = stubState.getActiveSubscriptions();
        if (!activeSubscriptions.isEmpty()) {
            if (activeSubscriptions.containsKey(orgNumber)) {
                return activeSubscriptions.get(orgNumber).equals(serviceCode);
            }
            return false;
        }
        return stubState.getSubscriptionReturnValue();
    }
}
