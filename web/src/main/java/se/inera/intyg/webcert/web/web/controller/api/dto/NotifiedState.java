package se.inera.intyg.webcert.web.web.controller.api.dto;

import java.io.Serializable;

/**
 * Created by eriklupander on 2015-11-09.
 */
public class NotifiedState implements Serializable {
    private boolean notified;

    public boolean isNotified() {
        return notified;
    }

    public void setNotified(boolean notified) {
        this.notified = notified;
    }
}
