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

package se.inera.intyg.webcert.web.service.intyginfo;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import se.inera.intyg.infra.intyginfo.dto.IntygInfoEvent;
import se.inera.intyg.infra.intyginfo.dto.IntygInfoEvent.Source;
import se.inera.intyg.infra.intyginfo.dto.IntygInfoEventType;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;

@Component
@RequiredArgsConstructor
public class GetIntygInfoEventsService {

    private final HandelseRepository handelseRepository;

    public List<IntygInfoEvent> get(String id) {
        final var handelses = handelseRepository.findByIntygsId(id);
        return handelses.stream()
            .map(GetIntygInfoEventsService::getEvent)
            .filter(Objects::nonNull)
            .toList();
    }

    private static IntygInfoEvent getEvent(Handelse handelse) {
        IntygInfoEvent event = getEvent(handelse, null);
        if (event != null) {
            final var status = handelse.getHandelseMetaData().getDeliveryStatus().toString();
            final var id = handelse.getId().toString();
            event.addData("status", status);
            event.addData("notificationId", id);
        }
        return event;
    }

    private static IntygInfoEvent getEvent(Handelse handelse, IntygInfoEvent event) {
        switch (handelse.getCode()) {
            case SKAPAT:
                event = new IntygInfoEvent(Source.WEBCERT, handelse.getTimestamp(), IntygInfoEventType.IS101);
                break;
            case ANDRAT:
                event = new IntygInfoEvent(Source.WEBCERT, handelse.getTimestamp(), IntygInfoEventType.IS102);
                break;
            case RADERA:
                event = new IntygInfoEvent(Source.WEBCERT, handelse.getTimestamp(), IntygInfoEventType.IS103);
                break;
            case KFSIGN:
                event = new IntygInfoEvent(Source.WEBCERT, handelse.getTimestamp(), IntygInfoEventType.IS105);
                break;
            case SIGNAT:
                event = new IntygInfoEvent(Source.WEBCERT, handelse.getTimestamp(), IntygInfoEventType.IS106);
                break;
            case SKICKA:
                event = new IntygInfoEvent(Source.WEBCERT, handelse.getTimestamp(), IntygInfoEventType.IS107);
                break;
            case MAKULE:
                event = new IntygInfoEvent(Source.WEBCERT, handelse.getTimestamp(), IntygInfoEventType.IS108);
                break;
            case NYFRFM:
                event = new IntygInfoEvent(Source.WEBCERT, handelse.getTimestamp(), IntygInfoEventType.IS109);
                break;
            case NYFRFV:
                event = new IntygInfoEvent(Source.WEBCERT, handelse.getTimestamp(), IntygInfoEventType.IS110);
                break;
            case NYSVFM:
                event = new IntygInfoEvent(Source.WEBCERT, handelse.getTimestamp(), IntygInfoEventType.IS111);
                break;
            case HANFRFM:
                event = new IntygInfoEvent(Source.WEBCERT, handelse.getTimestamp(), IntygInfoEventType.IS112);
                break;
            case HANFRFV:
                event = new IntygInfoEvent(Source.WEBCERT, handelse.getTimestamp(), IntygInfoEventType.IS113);
                break;
        }
        return event;
    }

}
