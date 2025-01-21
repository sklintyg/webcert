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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
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
import se.inera.intyg.common.support.common.enumerations.EventCode;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.event.model.CertificateEvent;
import se.inera.intyg.webcert.persistence.event.repository.CertificateEventRepository;
import se.inera.intyg.webcert.persistence.utkast.model.Signatur;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.event.CertificateEventServiceImpl;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;

@RunWith(MockitoJUnitRunner.class)
public class CertificateEventServiceImplTest {

    private static final String UTKAST_CERTIFICATE_ID = "1234";
    private static final String INTYG_CERTIFICATE_ID = "5678";
    private static final String CERTIFICATE_TYPE = "lisjp";
    private static final String HSA_ID = "userHsaId";
    private static final EventCode EVENT_CODE_SKAPAT = EventCode.SKAPAT;
    private static final String MESSAGE = "Testing, testing";

    @Mock
    private CertificateEventRepository eventRepository;

    @Mock
    private UtkastRepository utkastRepository;

    @Mock
    private IntygService intygService;

    @Mock
    private ArendeService arendeService;

    @InjectMocks
    private CertificateEventServiceImpl eventService;


    @Test
    public void testGetEvents() {
        List<CertificateEvent> list = new ArrayList<>();
        list.add(getCertificateEvent(UTKAST_CERTIFICATE_ID));

        when(eventRepository.findByCertificateId(anyString())).thenReturn(list);

        List<CertificateEvent> eventList = eventService.getCertificateEvents(UTKAST_CERTIFICATE_ID);

        assertEquals(1, eventList.size());
        verify(eventRepository, times(1)).findByCertificateId(UTKAST_CERTIFICATE_ID);
    }


    @Test
    public void testGetEventsWithAdditionalMessages() {
        LocalDateTime earlierEventTimestamp = LocalDateTime.parse("2018-01-01T00:00:00");
        LocalDateTime latestEventTimestamp = LocalDateTime.parse("2019-01-01T00:00:00");
        LocalDateTime newMessageTimestamp = LocalDateTime.parse("2020-01-01T00:00:00");

        CertificateEvent ce1 = getCertificateEvent(UTKAST_CERTIFICATE_ID);
        ce1.setTimestamp(earlierEventTimestamp);
        ce1.setEventCode(EventCode.NYFRFM);

        CertificateEvent ce2 = getCertificateEvent(UTKAST_CERTIFICATE_ID);
        ce2.setTimestamp(latestEventTimestamp);
        ce2.setEventCode(EventCode.KOMPLBEGARAN);

        List<CertificateEvent> events = new ArrayList<>();
        events.add(ce1);
        events.add(ce2);

        Arende ce1Arende = new Arende();
        ce1Arende.setIntygsId(UTKAST_CERTIFICATE_ID);
        ce1Arende.setAmne(ArendeAmne.KOMPLT);
        ce1Arende.setTimestamp(latestEventTimestamp);
        Arende newArende = new Arende();
        newArende.setIntygsId(UTKAST_CERTIFICATE_ID);
        newArende.setAmne(ArendeAmne.PAMINN);
        newArende.setPaminnelseMeddelandeId("1");
        newArende.setTimestamp(newMessageTimestamp);

        IntygContentHolder intyg = getIntygContentHolder();

        when(eventRepository.findByCertificateId(anyString())).thenReturn(events);
        when(intygService.fetchIntygDataForInternalUse(anyString(), anyBoolean())).thenReturn(intyg);
        when(arendeService.getArendenInternal(anyString())).thenReturn(Arrays.asList(ce1Arende, newArende));
        when(eventRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        List<CertificateEvent> eventList = eventService.getCertificateEvents(UTKAST_CERTIFICATE_ID);

        verify(eventRepository).findByCertificateId(UTKAST_CERTIFICATE_ID);
        verify(intygService).fetchIntygDataForInternalUse(UTKAST_CERTIFICATE_ID, true);
        assertTrue("Expects a NYFRFM event", eventList.stream()
            .anyMatch(certificateEvent -> certificateEvent.getEventCode() == EventCode.NYFRFM
                && earlierEventTimestamp.equals(certificateEvent.getTimestamp())));
        assertTrue("Expects a KOMPLBEGARAN event", eventList.stream()
            .anyMatch(certificateEvent -> certificateEvent.getEventCode() == EventCode.KOMPLBEGARAN
                && latestEventTimestamp.equals(certificateEvent.getTimestamp())));
        assertTrue("Expects a PAMINNELSE event", eventList.stream()
            .anyMatch(certificateEvent -> certificateEvent.getEventCode() == EventCode.PAMINNELSE
                && newMessageTimestamp.equals(certificateEvent.getTimestamp())));
    }

    @Test
    public void testGenerateEventsForUtkast() {

        Utkast utkast = getCertificate();

        when(eventRepository.findByCertificateId(anyString())).thenReturn(Collections.emptyList());
        when(utkastRepository.findById(anyString())).thenReturn(Optional.of(utkast));
        when(eventRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        List<CertificateEvent> result = eventService.getCertificateEvents(UTKAST_CERTIFICATE_ID);

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        assertEquals(EventCode.SKAPAT, result.get(0).getEventCode());
        assertEquals(EventCode.KFSIGN, result.get(1).getEventCode());
        verify(utkastRepository).findById(UTKAST_CERTIFICATE_ID);
        verifyNoInteractions(intygService);
    }

    @Test
    public void testGenerateEventsForUtkastWithArende() {

        Utkast utkast = getCertificate();
        utkast.setSignatur(new Signatur());
        Arende arende = getArende(utkast.getIntygsId());

        when(eventRepository.findByCertificateId(anyString())).thenReturn(Collections.emptyList());
        when(utkastRepository.findById(anyString())).thenReturn(Optional.of(utkast));
        when(arendeService.getArendenInternal(anyString())).thenReturn(List.of(arende));
        when(eventRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        List<CertificateEvent> result = eventService.getCertificateEvents(UTKAST_CERTIFICATE_ID);

        assertFalse(result.isEmpty());
        assertEquals(3, result.size());
        assertEquals(EventCode.SKAPAT, result.get(0).getEventCode());
        assertEquals(EventCode.KFSIGN, result.get(1).getEventCode());
        assertEquals(EventCode.KOMPLBEGARAN, result.get(2).getEventCode());
        verify(utkastRepository).findById(UTKAST_CERTIFICATE_ID);
        verifyNoInteractions(intygService);
    }


    @Test
    public void testGenerateEventsForIntyg() {

        IntygContentHolder intyg = getIntygContentHolder();

        when(eventRepository.findByCertificateId(anyString())).thenReturn(Collections.emptyList());
        when(utkastRepository.findById(anyString())).thenReturn(Optional.empty());
        when(intygService.fetchIntygDataForInternalUse(anyString(), anyBoolean())).thenReturn(intyg);
        when(eventRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        List<CertificateEvent> result = eventService.getCertificateEvents(INTYG_CERTIFICATE_ID);

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        assertEquals(EventCode.SIGNAT, result.get(0).getEventCode());
        assertEquals(EventCode.SKICKAT, result.get(1).getEventCode());
        verify(utkastRepository).findById(INTYG_CERTIFICATE_ID);
        verify(intygService).fetchIntygDataForInternalUse(INTYG_CERTIFICATE_ID, true);
    }

    @Test
    public void testGenerateSentEventForIntygSentFromOutsideWebcert() {

        final var intyg = getIntygContentHolder();

        final var signedEvent = new CertificateEvent();
        signedEvent.setEventCode(EventCode.SIGNAT);
        signedEvent.setCertificateId(INTYG_CERTIFICATE_ID);
        signedEvent.setTimestamp(LocalDateTime.now().minus(1, ChronoUnit.DAYS));

        final List<CertificateEvent> events = new ArrayList<>();
        events.add(signedEvent);

        when(eventRepository.findByCertificateId(anyString())).thenReturn(events);
        when(intygService.fetchIntygDataForInternalUse(anyString(), anyBoolean())).thenReturn(intyg);
        when(eventRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        List<CertificateEvent> result = eventService.getCertificateEvents(INTYG_CERTIFICATE_ID);

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        assertEquals(EventCode.SIGNAT, result.get(0).getEventCode());
        assertEquals(EventCode.SKICKAT, result.get(1).getEventCode());
    }

    @Test
    public void testDontGenerateSentEventForIntygSentFromWebcert() {

        final var intyg = getIntygContentHolder();

        final var signedEvent = new CertificateEvent();
        signedEvent.setEventCode(EventCode.SIGNAT);
        signedEvent.setCertificateId(INTYG_CERTIFICATE_ID);
        signedEvent.setTimestamp(LocalDateTime.now().minus(1, ChronoUnit.DAYS));

        final var sentEvent = new CertificateEvent();
        sentEvent.setEventCode(EventCode.SKICKAT);
        sentEvent.setCertificateId(INTYG_CERTIFICATE_ID);
        sentEvent.setTimestamp(LocalDateTime.now().minus(1, ChronoUnit.HOURS));

        final List<CertificateEvent> events = new ArrayList<>();
        events.add(signedEvent);
        events.add(sentEvent);

        when(eventRepository.findByCertificateId(anyString())).thenReturn(events);
        when(intygService.fetchIntygDataForInternalUse(anyString(), anyBoolean())).thenReturn(intyg);

        List<CertificateEvent> result = eventService.getCertificateEvents(INTYG_CERTIFICATE_ID);

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        assertEquals(EventCode.SIGNAT, result.get(0).getEventCode());
        assertEquals(EventCode.SKICKAT, result.get(1).getEventCode());
    }

    @Test
    public void testGenerateEventsForIntygWithArende() {

        IntygContentHolder intyg = getIntygContentHolder();
        Arende arende = getArende(INTYG_CERTIFICATE_ID);

        when(eventRepository.findByCertificateId(anyString())).thenReturn(Collections.emptyList());
        when(utkastRepository.findById(anyString())).thenReturn(Optional.empty());
        when(intygService.fetchIntygDataForInternalUse(anyString(), anyBoolean())).thenReturn(intyg);
        when(arendeService.getArendenInternal(anyString())).thenReturn(List.of(arende));
        when(eventRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        List<CertificateEvent> result = eventService.getCertificateEvents(INTYG_CERTIFICATE_ID);

        assertFalse(result.isEmpty());
        assertEquals(3, result.size());
        assertEquals(EventCode.SIGNAT, result.get(0).getEventCode());
        assertEquals(EventCode.SKICKAT, result.get(1).getEventCode());
        assertEquals(EventCode.KOMPLBEGARAN, result.get(2).getEventCode());
        verify(utkastRepository).findById(INTYG_CERTIFICATE_ID);
        verify(intygService).fetchIntygDataForInternalUse(INTYG_CERTIFICATE_ID, true);
    }

    @Test
    public void testGenerateNoEventsForIntygWithArende() {
        final var timestampWithMilliseconds = LocalDateTime.parse("2021-01-01T00:00:00.750");
        final var timestampWithoutMilliseconds = LocalDateTime.parse("2021-01-01T00:00:01");

        final var intyg = getIntygContentHolder();
        final var arende = getArende(INTYG_CERTIFICATE_ID);
        arende.setTimestamp(timestampWithoutMilliseconds);

        final var event = getCertificateEvent(INTYG_CERTIFICATE_ID);
        event.setTimestamp(timestampWithMilliseconds);
        event.setEventCode(EventCode.KOMPLBEGARAN);

        final var eventList = new ArrayList<CertificateEvent>();
        eventList.add(event);

        when(eventRepository.findByCertificateId(anyString())).thenReturn(eventList);
        when(intygService.fetchIntygDataForInternalUse(anyString(), anyBoolean())).thenReturn(intyg);
        when(arendeService.getArendenInternal(anyString())).thenReturn(List.of(arende));

        final var result = eventService.getCertificateEvents(INTYG_CERTIFICATE_ID);

        assertFalse(result.isEmpty());
        assertTrue("Expects a KOMPLBEGARAN event",
            result.stream().anyMatch(certificateEvent -> certificateEvent.getEventCode() == EventCode.KOMPLBEGARAN));
    }

    @Test
    public void testNoEventsGenerated() {

        IntygContentHolder intyg = getIntygContentHolderGeneratingNoEvents();

        when(eventRepository.findByCertificateId(anyString())).thenReturn(Collections.emptyList());
        when(utkastRepository.findById(anyString())).thenReturn(Optional.empty());
        when(intygService.fetchIntygDataForInternalUse(anyString(), anyBoolean())).thenReturn(intyg);

        List<CertificateEvent> result = eventService.getCertificateEvents(INTYG_CERTIFICATE_ID);

        assertTrue(result.isEmpty());
        verify(utkastRepository).findById(INTYG_CERTIFICATE_ID);
        verify(intygService).fetchIntygDataForInternalUse(INTYG_CERTIFICATE_ID, true);
        verify(eventRepository, times(0)).save(any());
    }

    @Test
    public void testSaveCertificateEvent() {
        CertificateEvent certificateEvent = getCertificateEvent(UTKAST_CERTIFICATE_ID);
        eventService.createCertificateEvent(certificateEvent.getCertificateId(), HSA_ID, EventCode.SKAPAT, MESSAGE);

        verify(eventRepository).save(any(CertificateEvent.class));
    }

    private CertificateEvent getCertificateEvent(String certificateId) {
        CertificateEvent certificateEvent = new CertificateEvent();
        certificateEvent.setCertificateId(certificateId);
        certificateEvent.setUser(HSA_ID);
        certificateEvent.setEventCode(EVENT_CODE_SKAPAT);
        certificateEvent.setMessage(MESSAGE);

        return certificateEvent;
    }

    public static Utkast getCertificate() {

        VardpersonReferens vp = new VardpersonReferens();
        vp.setHsaId(HSA_ID);

        Utkast utkast = new Utkast();
        utkast.setIntygsId(UTKAST_CERTIFICATE_ID);
        utkast.setSkapadAv(vp);
        utkast.setIntygsTyp(CERTIFICATE_TYPE);
        utkast.setStatus(UtkastStatus.DRAFT_COMPLETE);
        utkast.setKlartForSigneringDatum(LocalDateTime.parse("2020-01-01T10:05:00"));
        utkast.setSkapad(LocalDateTime.parse("2020-01-01T10:00:00"));

        return utkast;
    }

    private Arende getArende(String id) {

        Arende arende = new Arende();
        arende.setIntygsId(id);
        arende.setIntygTyp(CERTIFICATE_TYPE);
        arende.setAmne(ArendeAmne.KOMPLT);
        arende.setTimestamp(LocalDateTime.now());

        return arende;
    }

    private IntygContentHolder getIntygContentHolder() {

        List<se.inera.intyg.common.support.model.Status> status = new ArrayList<>();
        status.add(new se.inera.intyg.common.support.model.Status(CertificateState.RECEIVED, "HSVARD", LocalDateTime.now()));
        status.add(new se.inera.intyg.common.support.model.Status(CertificateState.SENT, "FKASSA", LocalDateTime.now()));

        return IntygContentHolder.builder()
            .contents("<external-json/>")
            .utlatande(getUtlatande())
            .statuses(status)
            .revoked(false)
            .relations(new Relations())
            .deceased(false)
            .sekretessmarkering(false)
            .patientNameChangedInPU(false)
            .patientAddressChangedInPU(false)
            .testIntyg(false)
            .latestMajorTextVersion(true)
            .build();
    }

    private IntygContentHolder getIntygContentHolderGeneratingNoEvents() {

        IntygContentHolder certificate = IntygContentHolder.builder()
            .contents("<external-json/>")
            .utlatande(getUtlatande())
            .statuses(null)
            .revoked(false)
            .relations(new Relations())
            .deceased(false)
            .sekretessmarkering(false)
            .patientNameChangedInPU(false)
            .patientAddressChangedInPU(false)
            .testIntyg(false)
            .latestMajorTextVersion(true)
            .build();

        certificate.getUtlatande().getGrundData().setSigneringsdatum(null);

        return certificate;
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
