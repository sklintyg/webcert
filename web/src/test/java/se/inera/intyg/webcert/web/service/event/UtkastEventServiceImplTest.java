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

package se.inera.intyg.webcert.web.service.event;


import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.support.common.enumerations.EventKod;
import se.inera.intyg.webcert.persistence.event.model.UtkastEvent;
import se.inera.intyg.webcert.persistence.event.repository.UtkastEventRepository;
import se.inera.intyg.webcert.web.event.UtkastEventServiceImpl;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@RunWith(MockitoJUnitRunner.class)
public class UtkastEventServiceImplTest {

    private static final String INTYG_ID = "1234";
    private static final String ANVANDARE_HSA_ID = "anvandareHsaId";
    private static final EventKod EVENT_KOD_SKAPAT = EventKod.SKAPAT;
    private static final String MEDDELANDE = "Inte köra, bara testa";

    @Mock
    private UtkastEventRepository utkastEventRepository;

    @Mock
    private WebCertUserService webCertUserService;

    @Mock
    private WebCertUser webcertUser;

    @InjectMocks
    private UtkastEventServiceImpl utkastEventService;

    @Before
    public void setup() throws Exception {
    }

    @Test
    public void testGetAllUtkastEventsByIntygsId() {
     //  when(utkastEventRepository.findByIntygsId(anyString())).thenReturn(new ArrayList<>());

        List<UtkastEvent> list = new ArrayList<UtkastEvent>();
        UtkastEvent event = new UtkastEvent();

        list.add(event);

        when(utkastEventRepository.findByIntygsId(anyString())).thenReturn(list);

        //test
        List<UtkastEvent> eventList = utkastEventService.getUtkastEvents("intygsId1");

        assertEquals(1, eventList.size());
        verify(utkastEventRepository, times(1)).findByIntygsId("intygsId1");
    }

    @Test
    public void testNoUtkastEvents() {
        when(utkastEventRepository.findByIntygsId(anyString())).thenReturn(new ArrayList<>());
        List<UtkastEvent> eventList = utkastEventService.getUtkastEvents("not_found");
        assertEquals(0, eventList.size());
    }

    @Test
    public void testSaveUtkastEvent() {

        UtkastEvent utkastEvent = createUtkastEvent();
        utkastEventService.createUtkastEvent(utkastEvent.getIntygsId(), "hsaId1", EventKod.SKAPAT, "hej");
        verify(utkastEventRepository).save(any(UtkastEvent.class));

    }

    private UtkastEvent createUtkastEvent() {
        UtkastEvent utkastEvent = new UtkastEvent();
        utkastEvent.setIntygsId(INTYG_ID);
        utkastEvent.setAnvandare(ANVANDARE_HSA_ID);
        utkastEvent.setEventKod(EVENT_KOD_SKAPAT);
        utkastEvent.setMeddelande(MEDDELANDE);
        return utkastEvent;
    }

}
