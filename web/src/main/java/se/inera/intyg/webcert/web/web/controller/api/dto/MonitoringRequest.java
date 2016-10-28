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

package se.inera.intyg.webcert.web.web.controller.api.dto;

import java.util.Map;

public class MonitoringRequest {

    public static final String WIDTH = "width";
    public static final String HEIGHT = "height";
    public static final String INTYG_ID = "intygId";
    public static final String INTYG_TYPE = "intygType";
    private MonitoringRequestEvent event;
    private Map<String, String> info;

    public MonitoringRequestEvent getEvent() {
        return event;
    }

    public void setEvent(MonitoringRequestEvent event) {
        this.event = event;
    }

    public Map<String, String> getInfo() {
        return info;
    }

    public void setInfo(Map<String, String> info) {
        this.info = info;
    }

    public boolean isValid() {
        if (event == null) {
            return false;
        }
        switch (event) {
        case SCREEN_RESOLUTION:
            return info != null && info.get(WIDTH) != null && info.get(HEIGHT) != null;
        case DIAGNOSKODVERK_CHANGED:
            return info != null && info.get(INTYG_ID) != null && info.get(INTYG_TYPE) != null;
        default:
            return true;
        }
    }

    public enum MonitoringRequestEvent {
        SCREEN_RESOLUTION,
        DIAGNOSKODVERK_CHANGED
    }
}
