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
package se.inera.intyg.webcert.notification_sender.mocks;

import java.time.LocalDateTime;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;

/**
 * Created by eriklupander on 2016-07-06.
 */
public class NotificationStubEntry {

    public String intygsId;
    public String handelseTyp;
    public LocalDateTime handelseTid;
    public HsaId userId;

    public NotificationStubEntry(String intygsId, String handelseTyp,
        LocalDateTime handelseTid, HsaId userId) {
        this.intygsId = intygsId;
        this.handelseTyp = handelseTyp;
        this.handelseTid = handelseTid;
        this.userId = userId;
    }
}
