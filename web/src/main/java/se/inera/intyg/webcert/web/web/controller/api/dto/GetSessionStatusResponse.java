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

import java.io.Serializable;

/**
 * Created by marced on 09/03/16.
 */
public class GetSessionStatusResponse implements Serializable {

    private static final long serialVersionUID = 3147429809057414979L;
    /**
     * Is this request associated with a session at all?.
     */
    private boolean hasSession;

    /**
     * Is this request associated with a session that has a Spring securityContext in it?.
     */
    private boolean isAuthenticated;

    /**
     * How many seconds until the session expires (0 if no session).
     */
    private long secondsUntilExpire;

    public GetSessionStatusResponse(boolean hasSession, boolean isAuthenticated, long secondsUntilExpire) {
        this.hasSession = hasSession;
        this.isAuthenticated = isAuthenticated;
        this.secondsUntilExpire = secondsUntilExpire;
    }

    public boolean isHasSession() {
        return hasSession;
    }

    public void setHasSession(boolean hasSession) {
        this.hasSession = hasSession;
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        isAuthenticated = authenticated;
    }

    public long getSecondsUntilExpire() {
        return secondsUntilExpire;
    }

    public void setSecondsUntilExpire(long secondsUntilExpire) {
        this.secondsUntilExpire = secondsUntilExpire;
    }
}
