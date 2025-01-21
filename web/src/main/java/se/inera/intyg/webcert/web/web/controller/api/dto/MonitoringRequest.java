/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
    public static final String NET_ID_VERSION = "netIdVersion";
    public static final String INTYG_ID = "intygId";
    public static final String INTYG_TYPE = "intygType";
    public static final String CAREGIVER_ID = "caregiverId";
    public static final String CARE_UNIT_ID = "careUnitId";
    public static final String USER_CLIENT_CONTEXT = "userClientContext";
    public static final String MAIN_DIAGNOSIS_CODE = "mainDiagnosisCode";
    public static final String ERROR_MESSAGE = "errorMessage";
    public static final String IP = "ip";
    public static final String CONNECTIVITY = "connectivity";

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
            case BROWSER_INFO:
                return info != null && info.get(WIDTH) != null && info.get(HEIGHT) != null && info.get(NET_ID_VERSION) != null;
            case DIAGNOSKODVERK_CHANGED:
                return info != null && info.get(INTYG_ID) != null && info.get(INTYG_TYPE) != null;
            case SIGNING_FAILED:
                return info != null && info.get(ERROR_MESSAGE) != null && info.get(INTYG_ID) != null;
            case IDP_CONNECTIVITY_CHECK:
                return info != null && info.get(IP) != null && info.get(CONNECTIVITY) != null;
            case SRS_LOADED:
                return info != null && info.get(INTYG_ID) != null && info.get(USER_CLIENT_CONTEXT) != null
                    && info.get(CARE_UNIT_ID) != null && info.get(CAREGIVER_ID) != null && info.get(MAIN_DIAGNOSIS_CODE) != null;
            case SRS_PANEL_ACTIVATED:
            case SRS_CONSENT_ANSWERED:
            case SRS_QUESTION_ANSWERED:
            case SRS_CALCULATE_CLICKED:
            case SRS_HIDE_QUESTIONS_CLICKED:
            case SRS_SHOW_QUESTIONS_CLICKED:
            case SRS_MEASURES_SHOW_MORE_CLICKED:
            case SRS_MEASURES_EXPAND_ONE_CLICKED:
            case SRS_MEASURES_LINK_CLICKED:
            case SRS_STATISTICS_ACTIVATED:
            case SRS_STATISTICS_LINK_CLICKED:
            case SRS_MEASURES_DISPLAYED:
                return info != null && info.get(INTYG_ID) != null && info.get(USER_CLIENT_CONTEXT) != null
                    && info.get(CARE_UNIT_ID) != null && info.get(CAREGIVER_ID) != null;
        }
        return true;
    }

    public enum MonitoringRequestEvent {
        BROWSER_INFO,
        DIAGNOSKODVERK_CHANGED,
        SIGNING_FAILED,
        IDP_CONNECTIVITY_CHECK,
        SRS_LOADED,
        SRS_PANEL_ACTIVATED,
        SRS_CONSENT_ANSWERED,
        SRS_QUESTION_ANSWERED,
        SRS_CALCULATE_CLICKED,
        SRS_HIDE_QUESTIONS_CLICKED,
        SRS_SHOW_QUESTIONS_CLICKED,
        SRS_MEASURES_SHOW_MORE_CLICKED,
        SRS_MEASURES_EXPAND_ONE_CLICKED,
        SRS_MEASURES_LINK_CLICKED,
        SRS_STATISTICS_ACTIVATED,
        SRS_STATISTICS_LINK_CLICKED,
        SRS_MEASURES_DISPLAYED
    }
}
