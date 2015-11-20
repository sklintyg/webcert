package se.inera.intyg.webcert.web.web.controller.moduleapi.dto;

import java.io.Serializable;

/**
 * Created by eriklupander on 2015-11-09.
 */
public class DispatchState implements Serializable {
    private boolean dispatched;

    public boolean isDispatched() {
        return dispatched;
    }

    public void setDispatched(boolean dispatched) {
        this.dispatched = dispatched;
    }
}
