/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.persistence.event.repository;

import java.time.LocalDateTime;
import se.inera.intyg.common.support.common.enumerations.EventKod;
import se.inera.intyg.webcert.persistence.event.model.UtkastEvent;

public class UtkastEventTestUtil {

    private UtkastEventTestUtil() {
    }

    public static final String INTYG_1_ID = "intygID1";
    public static final String INTYG_2_ID = "intygID2";

    public static final String ANVANDARE_1 = "SE123344332";

    public static final EventKod EVENT_KOD_SKAPAT = EventKod.SKAPAT;
    public static final EventKod EVENT_KOD_SIGNAT = EventKod.SIGNAT;
    public static final EventKod EVENT_KOD_SKICKAT = EventKod.SKICKAT;

    public static final String MEDDELANDE_1 = "Really important event.";

    public static UtkastEvent buildUtkastEvent(String intygsId) {
        return buildUtkastEvent(intygsId, ANVANDARE_1, EVENT_KOD_SKAPAT, MEDDELANDE_1);
    }

    public static UtkastEvent buildUtkastEvent(String intygsId, String anvandare) {
        return buildUtkastEvent(intygsId, anvandare, EVENT_KOD_SKAPAT, MEDDELANDE_1);
    }

    public static UtkastEvent buildUtkastEvent(String intygsId, EventKod eventKod) {
        return buildUtkastEvent(intygsId, ANVANDARE_1, eventKod, MEDDELANDE_1);
    }

    public static UtkastEvent buildUtkastEvent(String intygsId, String anvandare, EventKod eventKod) {
        return buildUtkastEvent(intygsId, anvandare, eventKod, MEDDELANDE_1);
    }

    public static UtkastEvent buildUtkastEvent(String intygsId, String anvandare, EventKod eventKod, String meddelande) {
        UtkastEvent utkastEvent = new UtkastEvent();
        utkastEvent.setId(1L);
        utkastEvent.setIntygsId(intygsId);
        utkastEvent.setAnvandare(anvandare);
        utkastEvent.setEventKod(eventKod);
        utkastEvent.setTimestamp(LocalDateTime.now());
        utkastEvent.setMeddelande(meddelande);

        return utkastEvent;
    }
}
