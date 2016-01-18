/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.intyg.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by erik on 15-04-08.
 *
 * This DTO wraps a List of IntygItem and adds a simple flag indicating whether the list was built from
 * intygstjansten ("online") data or local webcert utkast ("offline")  data.
 *
 * The default is offline == false.
 */
public class IntygItemListResponse {

    private List<IntygItem> intygItemList = new ArrayList<>();
    private boolean offlineMode = false;

    public IntygItemListResponse(List<IntygItem> intygItems, boolean offlineMode) {
        intygItemList = intygItems;
        this.offlineMode = offlineMode;
    }

    public List<IntygItem> getIntygItemList() {
        return intygItemList;
    }

    public void setIntygItemList(List<IntygItem> intygItemList) {
        this.intygItemList = intygItemList;
    }

    public boolean isOfflineMode() {
        return offlineMode;
    }

    public void setOfflineMode(boolean offlineMode) {
        this.offlineMode = offlineMode;
    }
}
