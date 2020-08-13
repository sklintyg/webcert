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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import se.inera.intyg.common.fk7263.model.internal.Fk7263Utlatande;
import se.inera.intyg.common.support.common.enumerations.EventKod;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.webcert.persistence.event.model.UtkastEvent;
import se.inera.intyg.webcert.persistence.event.repository.UtkastEventRepository;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.web.event.UtkastEventServiceImpl;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;

@RunWith(MockitoJUnitRunner.class)
public class UtkastEventServiceImplTest {

    private static final String INTYG_ID = "1234";
    private static final String INTYG_TYP = "typ";
    private static final String HSA_ID = "anvandareHsaId";
    private static final EventKod EVENT_KOD_SKAPAT = EventKod.SKAPAT;
    private static final String MEDDELANDE = "Inte köra, bara testa";

    @Mock
    private UtkastEventRepository eventRepository;

    @Mock
    private IntygService intygstjanst;

    @Mock
    private UtkastService utkastService;

    @InjectMocks
    private UtkastEventServiceImpl eventService;


    @Test
    public void testGetEvents() {
        List<UtkastEvent> list = new ArrayList<UtkastEvent>();
        list.add(getUtkastEvent());

        when(eventRepository.findByIntygsId(anyString())).thenReturn(list);

        List<UtkastEvent> eventList = eventService.getUtkastEvents(INTYG_ID, INTYG_TYP);

        assertEquals(1, eventList.size());
        verify(eventRepository, times(1)).findByIntygsId(INTYG_ID);
    }

    @Test
    public void testGenerateEventsForUtkast() {

        Utkast utkast = getUtkast();

        when(eventRepository.findByIntygsId(anyString())).thenReturn(Collections.emptyList());
        when(utkastService.getOptionalDraft(anyString(), anyString(), anyBoolean())).thenReturn(Optional.of(utkast));
        when(eventRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        List<UtkastEvent> result = eventService.getUtkastEvents(INTYG_ID, INTYG_TYP);

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        assertEquals(EventKod.SKAPAT, result.get(0).getEventKod());
        assertEquals(EventKod.KFSIGN, result.get(1).getEventKod());
        verify(utkastService).getOptionalDraft(INTYG_ID, INTYG_TYP, true);
        verifyNoInteractions(intygstjanst);
    }

    @Test
    public void testGenerateEventsForIntyg() {

        IntygContentHolder intyg = getIntygContentHolder();

        when(eventRepository.findByIntygsId(anyString())).thenReturn(Collections.emptyList());
        when(utkastService.getOptionalDraft(anyString(), anyString(), anyBoolean())).thenReturn(Optional.empty());
        when(intygstjanst.fetchIntygData(INTYG_ID, INTYG_TYP, false)).thenReturn(intyg);
        when(eventRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        List<UtkastEvent> result = eventService.getUtkastEvents(INTYG_ID, INTYG_TYP);

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        assertEquals(EventKod.SIGNAT, result.get(0).getEventKod());
        assertEquals(EventKod.SKICKAT, result.get(1).getEventKod());
        verify(intygstjanst).fetchIntygData(INTYG_ID, INTYG_TYP, false);
    }

    @Test
    public void testNoEventsCouldBeGenerated() {

        IntygContentHolder intyg = getIntygContentHolderGeneratingNoEvents();

        when(eventRepository.findByIntygsId(anyString())).thenReturn(Collections.emptyList());
        when(utkastService.getOptionalDraft(anyString(), anyString(), anyBoolean())).thenReturn(Optional.empty());
        when(intygstjanst.fetchIntygData(INTYG_ID, INTYG_TYP, false)).thenReturn(intyg);

        List<UtkastEvent> result = eventService.getUtkastEvents(INTYG_ID, INTYG_TYP);

        assertTrue(result.isEmpty());
        verify(utkastService).getOptionalDraft(INTYG_ID, INTYG_TYP, true);
        verify(intygstjanst).fetchIntygData(INTYG_ID, INTYG_TYP, false);
        verify(eventRepository, times(0)).save(any());
    }

    @Test
    public void testSaveUtkastEvent() {
        UtkastEvent utkastEvent = getUtkastEvent();
        eventService.createUtkastEvent(utkastEvent.getIntygsId(), HSA_ID, EventKod.SKAPAT, MEDDELANDE);

        verify(eventRepository).save(any(UtkastEvent.class));
    }

    private UtkastEvent getUtkastEvent() {
        UtkastEvent utkastEvent = new UtkastEvent();
        utkastEvent.setIntygsId(INTYG_ID);
        utkastEvent.setAnvandare(HSA_ID);
        utkastEvent.setEventKod(EVENT_KOD_SKAPAT);
        utkastEvent.setMeddelande(MEDDELANDE);

        return utkastEvent;
    }

    public static Utkast getUtkast() {

        VardpersonReferens vp = new VardpersonReferens();
        vp.setHsaId(HSA_ID);

        Utkast utkast = new Utkast();
        utkast.setIntygsId(INTYG_ID);
        utkast.setSkapadAv(vp);
        utkast.setIntygsTyp(INTYG_TYP);
        utkast.setStatus(UtkastStatus.DRAFT_COMPLETE);
        utkast.setKlartForSigneringDatum(LocalDateTime.parse("2020-01-01T10:05:00"));
        utkast.setSkapad(LocalDateTime.parse("2020-01-01T10:00:00"));

        return utkast;
    }

    private IntygContentHolder getIntygContentHolder() {

        List<se.inera.intyg.common.support.model.Status> status = new ArrayList<>();
        status.add(new se.inera.intyg.common.support.model.Status(CertificateState.RECEIVED, "HSVARD", LocalDateTime.now()));
        status.add(new se.inera.intyg.common.support.model.Status(CertificateState.SENT, "FKASSA", LocalDateTime.now()));

        return IntygContentHolder.builder()
            .setContents("<external-json/>")
            .setUtlatande(getUtlatande())
            .setStatuses(status)
            .setRevoked(false)
            .setDeceased(false)
            .setSekretessmarkering(false)
            .setPatientNameChangedInPU(false)
            .setPatientAddressChangedInPU(false)
            .setTestIntyg(false)
            .build();
    }

    private IntygContentHolder getIntygContentHolderGeneratingNoEvents() {

        IntygContentHolder intyg = IntygContentHolder.builder()
            .setContents("<external-json/>")
            .setUtlatande(getUtlatande())
            .setStatuses(null)
            .setRevoked(false)
            .setDeceased(false)
            .setSekretessmarkering(false)
            .setPatientNameChangedInPU(false)
            .setPatientAddressChangedInPU(false)
            .setTestIntyg(false)
            .build();

        intyg.getUtlatande().getGrundData().setSigneringsdatum(null);

        return intyg;
    }


    private Fk7263Utlatande getUtlatande() {
        // create mocked Utlatande from intygstjansten
        try {
            return new CustomObjectMapper().readValue(new ClassPathResource(
                "FragaSvarServiceImplTest/utlatande.json").getFile(), Fk7263Utlatande.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
