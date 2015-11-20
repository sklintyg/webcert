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
