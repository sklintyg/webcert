/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.notificationstub.v2.stat;

import java.time.LocalDateTime;

/**
 * Created by eriklupander on 2016-07-05.
 */
public class NotificationStubEntry {
    public String intygsId;
    public String handelseKod;
    public LocalDateTime handelseTid;

    public NotificationStubEntry(String intygsId, String handelseKod, LocalDateTime handelseTid) {
        this.intygsId = intygsId;
        this.handelseKod = handelseKod;
        this.handelseTid = handelseTid;
    }

    public String getIntygsId() {
        return intygsId;
    }

    public String getHandelseKod() {
        return handelseKod;
    }

    public LocalDateTime getHandelseTid() {
        return handelseTid;
    }
}
