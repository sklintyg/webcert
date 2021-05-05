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

package se.inera.intyg.webcert.kundportalenstub.state;

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
    private Map<String, String> activeSubscriptions = new HashMap<>();

    @Value("#{${kundportalenstub.service.codes.eleg}}")
    private List<Map<String, String>> kundportalenElegServices;

    @Value("#{${kundportalenstub.service.codes.siths}}")
    private List<Map<String, String>> kundportalenSithsServices;

    public List<Map<String, String>> getSServices() {
        return Stream.concat(kundportalenElegServices.stream(), kundportalenSithsServices.stream()).collect(Collectors.toList());
    }

    public boolean getSubscriptionReturnValue() {
        return subscriptionReturnValue;
    }

    public void setSubscriptionReturnValue(boolean subscriptionReturnValue) {
        this.subscriptionReturnValue = subscriptionReturnValue;
    }

    public Map<String, String> getActiveSubscriptions() {
        return activeSubscriptions;
    }

    public void setActiveSubscriptions(Map<String, String> activeSubscriptions) {
        this.activeSubscriptions = activeSubscriptions;
    }

    public void clearActiveSubscriptions() {
        activeSubscriptions.clear();
    }
}
