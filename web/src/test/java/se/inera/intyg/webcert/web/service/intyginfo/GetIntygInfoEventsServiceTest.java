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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.infra.intyginfo.dto.IntygInfoEvent;
import se.inera.intyg.infra.intyginfo.dto.IntygInfoEvent.Source;
import se.inera.intyg.infra.intyginfo.dto.IntygInfoEventType;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.handelse.model.HandelseMetaData;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;

@ExtendWith(MockitoExtension.class)
class GetIntygInfoEventsServiceTest {

    @Mock
    private HandelseRepository handelseRepository;

    @InjectMocks
    private GetIntygInfoEventsService getIntygInfoEventsService;

    @ParameterizedTest
    @CsvSource({
        "SKAPAT, IS101",
        "ANDRAT, IS102",
        "RADERA, IS103",
        "KFSIGN, IS105",
        "SIGNAT, IS106",
        "SKICKA, IS107",
        "MAKULE, IS108",
        "NYFRFM, IS109",
        "NYFRFV, IS110",
        "NYSVFM, IS111",
        "HANFRFM, IS112",
        "HANFRFV, IS113",
    })
    void shouldConvertEvent(String handelsekod, String eventType) {
        final var handelse = getHandelse(HandelsekodEnum.valueOf(handelsekod));
        final var expected = new IntygInfoEvent(Source.WEBCERT, handelse.getTimestamp(), IntygInfoEventType.valueOf(eventType));
        expected.addData("status", NotificationDeliveryStatusEnum.SUCCESS.toString());
        expected.addData("notificationId", "1");
        when(handelseRepository.findByIntygsId("ID")).thenReturn(List.of(handelse));

        final var events = getIntygInfoEventsService.get("ID");

        assertEquals(
            expected,
            events.getFirst()
        );
    }

    private static Handelse getHandelse(HandelsekodEnum skapat) {
        final var handelse = new Handelse();
        handelse.setId(1L);
        handelse.setCode(skapat);
        handelse.setTimestamp(LocalDateTime.now());
        handelse.setHandelseMetaData(new HandelseMetaData());
        handelse.getHandelseMetaData().setDeliveryStatus(NotificationDeliveryStatusEnum.SUCCESS);
        return handelse;
    }
}