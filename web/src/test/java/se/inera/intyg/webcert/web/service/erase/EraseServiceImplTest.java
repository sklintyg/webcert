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
package se.inera.intyg.webcert.web.service.erase;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import se.inera.intyg.webcert.persistence.arende.repository.ArendeDraftRepository;
import se.inera.intyg.webcert.persistence.arende.repository.ArendeRepository;
import se.inera.intyg.webcert.persistence.event.repository.CertificateEventFailedLoadRepository;
import se.inera.intyg.webcert.persistence.event.repository.CertificateEventProcessedRepository;
import se.inera.intyg.webcert.persistence.event.repository.CertificateEventRepository;
import se.inera.intyg.webcert.persistence.fragasvar.repository.FragaSvarRepository;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.persistence.integreradenhet.repository.IntegreradEnhetRepository;
import se.inera.intyg.webcert.persistence.legacy.repository.MigreratMedcertIntygRepository;
import se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliveryRepository;
import se.inera.intyg.webcert.persistence.privatlakaravtal.repository.GodkantAvtalRepository;
import se.inera.intyg.webcert.persistence.referens.repository.ReferensRepository;
import se.inera.intyg.webcert.persistence.utkast.repository.PagaendeSigneringRepository;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;

@ExtendWith(MockitoExtension.class)
class EraseServiceImplTest {

    @Mock
    private IntegreradEnhetRepository integreradEnhetRepository;
    @Mock
    private GodkantAvtalRepository godkantAvtalRepository;
    @Mock
    private ArendeDraftRepository arendeDraftRepository;
    @Mock
    private CertificateEventFailedLoadRepository certificateEventFailedLoadRepository;
    @Mock
    private CertificateEventProcessedRepository certificateEventProcessedRepository;
    @Mock
    private PagaendeSigneringRepository pagaendeSigneringRepository;
    @Mock
    private NotificationRedeliveryRepository notificationRedeliveryRepository;
    @Mock
    private ReferensRepository referensRepository;
    @Mock
    private MigreratMedcertIntygRepository migreratMedcertIntygRepository;
    @Mock
    private FragaSvarRepository fragaSvarRepository;
    @Mock
    private ArendeRepository arendeRepository;
    @Mock
    private HandelseRepository handelseRepository;
    @Mock
    private CertificateEventRepository certificateEventRepository;
    @Mock
    private UtkastRepository utkastRepository;

    @InjectMocks
    private EraseServiceImpl eraseService;

    @Captor
    ArgumentCaptor<List<String>> certIdCaptor;

    @Captor
    ArgumentCaptor<List<Long>> eventIdCaptor;

    private final List<List<String>> certIds = new ArrayList<>();
    private final List<List<Long>> eventIds = new ArrayList<>();

    private static final int PAGE = 0;
    private static final int ERASE_SIZE = 4;

    private static final String CARE_PROVIDER_ID = "CARE_PROVIDER_ID";
    private static final Pageable ERASE_PAGEABLE = PageRequest.of(PAGE, ERASE_SIZE, Sort.by(Direction.ASC, "skapad", "intygsId"));

    @AfterEach
    public void cleanup() {
        certIds.clear();
        eventIds.clear();
    }

    @Test
    public void shouldCallForNewCertificateIdsForEachBatch() {
        setupPageMock(true);
        setupEraseByCareProviderIdMocks();
        setupEraseByCertificateIdMocks(false);

        eraseService.eraseCertificates(CARE_PROVIDER_ID, ERASE_PAGEABLE.getPageSize());

        verify(utkastRepository, times(3)).findCertificateIdsForCareProvider(CARE_PROVIDER_ID, ERASE_PAGEABLE);
    }

    @Test
    public void shouldEraseIntegratedUnitsOnceBasedOnCareProviderId() {
        setupPageMock(true);
        setupEraseByCareProviderIdMocks();
        setupEraseByCertificateIdMocks(false);

        eraseService.eraseCertificates(CARE_PROVIDER_ID, ERASE_PAGEABLE.getPageSize());

        verify(integreradEnhetRepository, times(1)).eraseIntegratedUnitsByCareProviderId(CARE_PROVIDER_ID);
    }

    @Test
    public void shouldEraseGodkantAvtalOnceBasedOnCareProviderId() {
        setupPageMock(true);
        setupEraseByCareProviderIdMocks();
        setupEraseByCertificateIdMocks(false);

        eraseService.eraseCertificates(CARE_PROVIDER_ID, ERASE_PAGEABLE.getPageSize());

        verify(godkantAvtalRepository, times(1)).eraseGodkantAvtalByCareProviderId(CARE_PROVIDER_ID);
    }

    @Test
    public void shouldEraseArendeDraftsInBatches() {
        setupPageMock(true);
        setupEraseByCareProviderIdMocks();
        setupEraseByCertificateIdMocks(false);

        eraseService.eraseCertificates(CARE_PROVIDER_ID, ERASE_PAGEABLE.getPageSize());

        assertAll(
            () -> verify(arendeDraftRepository, times(3)).eraseArendeDraftsByCertificateIds(certIdCaptor.capture()),
            () -> assertIterableEquals(certIds.get(0), certIdCaptor.getAllValues().get(0)),
            () -> assertIterableEquals(certIds.get(1), certIdCaptor.getAllValues().get(1)),
            () -> assertIterableEquals(certIds.get(2), certIdCaptor.getAllValues().get(2))
        );
    }

    @Test
    public void shouldEraseCertificateEventFailedLoadInBatches() {
        setupPageMock(true);
        setupEraseByCareProviderIdMocks();
        setupEraseByCertificateIdMocks(false);

        eraseService.eraseCertificates(CARE_PROVIDER_ID, ERASE_PAGEABLE.getPageSize());

        assertAll(
            () -> verify(certificateEventFailedLoadRepository, times(3)).eraseEventsFailedByCertificateIds(certIdCaptor.capture()),
            () -> assertIterableEquals(certIds.get(0), certIdCaptor.getAllValues().get(0)),
            () -> assertIterableEquals(certIds.get(1), certIdCaptor.getAllValues().get(1)),
            () -> assertIterableEquals(certIds.get(2), certIdCaptor.getAllValues().get(2))
        );
    }

    @Test
    public void shouldEraseCertificateEventProcessedInBatches() {
        setupPageMock(true);
        setupEraseByCareProviderIdMocks();
        setupEraseByCertificateIdMocks(false);

        eraseService.eraseCertificates(CARE_PROVIDER_ID, ERASE_PAGEABLE.getPageSize());

        assertAll(
            () -> verify(certificateEventProcessedRepository, times(3)).eraseEventsProcessedByCertificateIds(certIdCaptor.capture()),
            () -> assertIterableEquals(certIds.get(0), certIdCaptor.getAllValues().get(0)),
            () -> assertIterableEquals(certIds.get(1), certIdCaptor.getAllValues().get(1)),
            () -> assertIterableEquals(certIds.get(2), certIdCaptor.getAllValues().get(2))
        );
    }

    @Test
    public void shouldErasePagaendeSigneringInBatches() {
        setupPageMock(true);
        setupEraseByCareProviderIdMocks();
        setupEraseByCertificateIdMocks(false);

        eraseService.eraseCertificates(CARE_PROVIDER_ID, ERASE_PAGEABLE.getPageSize());

        assertAll(
            () -> verify(pagaendeSigneringRepository, times(3)).erasePagaendeSigneringByCertificateIds(certIdCaptor.capture()),
            () -> assertIterableEquals(certIds.get(0), certIdCaptor.getAllValues().get(0)),
            () -> assertIterableEquals(certIds.get(1), certIdCaptor.getAllValues().get(1)),
            () -> assertIterableEquals(certIds.get(2), certIdCaptor.getAllValues().get(2))
        );
    }

    @Test
    public void shouldEraseNotificationRedeliveryInBatches() {
        setupPageMock(true);
        setupEraseByCareProviderIdMocks();
        setupEraseByCertificateIdMocks(false);

        eraseService.eraseCertificates(CARE_PROVIDER_ID, ERASE_PAGEABLE.getPageSize());

        assertAll(
            () -> verify(handelseRepository, times(3)).findHandelseIdsByCertificateIds(certIdCaptor.capture()),
            () -> verify(notificationRedeliveryRepository, times(3)).eraseRedeliveriesForEventIds(eventIdCaptor.capture()),
            () -> assertIterableEquals(certIds.get(0), certIdCaptor.getAllValues().get(0)),
            () -> assertIterableEquals(certIds.get(1), certIdCaptor.getAllValues().get(1)),
            () -> assertIterableEquals(certIds.get(2), certIdCaptor.getAllValues().get(2)),
            () -> assertIterableEquals(eventIdCaptor.getAllValues().get(0), eventIds.get(0)),
            () -> assertIterableEquals(eventIdCaptor.getAllValues().get(1), eventIds.get(1)),
            () -> assertIterableEquals(eventIdCaptor.getAllValues().get(2), eventIds.get(2))
        );
    }

    @Test
    public void shouldEraseReferensInBatches() {
        setupPageMock(true);
        setupEraseByCareProviderIdMocks();
        setupEraseByCertificateIdMocks(false);

        eraseService.eraseCertificates(CARE_PROVIDER_ID, ERASE_PAGEABLE.getPageSize());

        assertAll(
            () -> verify(referensRepository, times(3)).eraseReferenserByCertificateIds(certIdCaptor.capture()),
            () -> assertIterableEquals(certIds.get(0), certIdCaptor.getAllValues().get(0)),
            () -> assertIterableEquals(certIds.get(1), certIdCaptor.getAllValues().get(1)),
            () -> assertIterableEquals(certIds.get(2), certIdCaptor.getAllValues().get(2))
        );
    }

    @Test
    public void shouldEraseMigreratMedcertIntygInBatches() {
        setupPageMock(true);
        setupEraseByCareProviderIdMocks();
        setupEraseByCertificateIdMocks(false);

        eraseService.eraseCertificates(CARE_PROVIDER_ID, ERASE_PAGEABLE.getPageSize());

        assertAll(
            () -> verify(migreratMedcertIntygRepository, times(3)).eraseMedcertCertificatesByCertificateIds(certIdCaptor.capture()),
            () -> assertIterableEquals(certIds.get(0), certIdCaptor.getAllValues().get(0)),
            () -> assertIterableEquals(certIds.get(1), certIdCaptor.getAllValues().get(1)),
            () -> assertIterableEquals(certIds.get(2), certIdCaptor.getAllValues().get(2))
        );
    }

    @Test
    public void shouldEraseFragaSvarIntygInBatches() {
        setupPageMock(true);
        setupEraseByCareProviderIdMocks();
        setupEraseByCertificateIdMocks(false);

        eraseService.eraseCertificates(CARE_PROVIDER_ID, ERASE_PAGEABLE.getPageSize());

        assertAll(
            () -> verify(fragaSvarRepository, times(3)).eraseFragaSvarByCertificateIds(certIdCaptor.capture()),
            () -> assertIterableEquals(certIds.get(0), certIdCaptor.getAllValues().get(0)),
            () -> assertIterableEquals(certIds.get(1), certIdCaptor.getAllValues().get(1)),
            () -> assertIterableEquals(certIds.get(2), certIdCaptor.getAllValues().get(2))
        );
    }

    @Test
    public void shouldEraseArendenInBatches() {
        setupPageMock(true);
        setupEraseByCareProviderIdMocks();
        setupEraseByCertificateIdMocks(false);

        eraseService.eraseCertificates(CARE_PROVIDER_ID, ERASE_PAGEABLE.getPageSize());

        assertAll(
            () -> verify(arendeRepository, times(3)).eraseArendenByCertificateIds(certIdCaptor.capture()),
            () -> assertIterableEquals(certIds.get(0), certIdCaptor.getAllValues().get(0)),
            () -> assertIterableEquals(certIds.get(1), certIdCaptor.getAllValues().get(1)),
            () -> assertIterableEquals(certIds.get(2), certIdCaptor.getAllValues().get(2))
        );
    }

    @Test
    public void shouldEraseHandelserInBatches() {
        setupPageMock(true);
        setupEraseByCareProviderIdMocks();
        setupEraseByCertificateIdMocks(false);

        eraseService.eraseCertificates(CARE_PROVIDER_ID, ERASE_PAGEABLE.getPageSize());

        assertAll(
            () -> verify(handelseRepository, times(3)).eraseHandelseByCertificateIds(certIdCaptor.capture()),
            () -> assertIterableEquals(certIds.get(0), certIdCaptor.getAllValues().get(0)),
            () -> assertIterableEquals(certIds.get(1), certIdCaptor.getAllValues().get(1)),
            () -> assertIterableEquals(certIds.get(2), certIdCaptor.getAllValues().get(2))
        );
    }

    @Test
    public void shouldEraseCertificateEventsInBatches() {
        setupPageMock(true);
        setupEraseByCareProviderIdMocks();
        setupEraseByCertificateIdMocks(false);

        eraseService.eraseCertificates(CARE_PROVIDER_ID, ERASE_PAGEABLE.getPageSize());

        assertAll(
            () -> verify(certificateEventRepository, times(3)).eraseCertificateEventsByCertificateIds(certIdCaptor.capture()),
            () -> assertIterableEquals(certIds.get(0), certIdCaptor.getAllValues().get(0)),
            () -> assertIterableEquals(certIds.get(1), certIdCaptor.getAllValues().get(1)),
            () -> assertIterableEquals(certIds.get(2), certIdCaptor.getAllValues().get(2))
        );
    }

    @Test
    public void shouldEraseUtkastInBatches() {
        setupPageMock(true);
        setupEraseByCareProviderIdMocks();
        setupEraseByCertificateIdMocks(false);

        eraseService.eraseCertificates(CARE_PROVIDER_ID, ERASE_PAGEABLE.getPageSize());

        assertAll(
            () -> verify(utkastRepository, times(3)).eraseCertificatesByCertificateIds(certIdCaptor.capture()),
            () -> assertIterableEquals(certIds.get(0), certIdCaptor.getAllValues().get(0)),
            () -> assertIterableEquals(certIds.get(1), certIdCaptor.getAllValues().get(1)),
            () -> assertIterableEquals(certIds.get(2), certIdCaptor.getAllValues().get(2))
        );
    }

    @Test
    public void shouldRethrowAnyCaughtException() {
        setupPageMock(true);
        setupEraseByCareProviderIdMocks();
        setupEraseByCertificateIdMocks(true);

        final var exception = assertThrows(IllegalArgumentException.class, () -> eraseService
            .eraseCertificates(CARE_PROVIDER_ID, ERASE_PAGEABLE.getPageSize()));

        assertEquals("TestException", exception.getMessage());
    }

    @Test
    public void shouldNotMakeCertificateEraseCallsIfCareProviderWithoutCertificates() {
        setupEraseByCareProviderIdMocks();
        setupPageMock(false);

        eraseService.eraseCertificates(CARE_PROVIDER_ID, ERASE_PAGEABLE.getPageSize());

        assertAll(
            () -> verify(integreradEnhetRepository, times(1)).eraseIntegratedUnitsByCareProviderId(CARE_PROVIDER_ID),
            () -> verify(godkantAvtalRepository, times(1)).eraseGodkantAvtalByCareProviderId(CARE_PROVIDER_ID),
            () -> verify(utkastRepository, times(1)).findCertificateIdsForCareProvider(any(String.class), any(Pageable.class)),
            () -> verifyNoInteractions(arendeDraftRepository),
            () -> verifyNoInteractions(certificateEventFailedLoadRepository),
            () -> verifyNoInteractions(certificateEventProcessedRepository),
            () -> verifyNoInteractions(pagaendeSigneringRepository),
            () -> verifyNoInteractions(notificationRedeliveryRepository),
            () -> verifyNoInteractions(referensRepository),
            () -> verifyNoInteractions(migreratMedcertIntygRepository),
            () -> verifyNoInteractions(fragaSvarRepository),
            () -> verifyNoInteractions(arendeRepository),
            () -> verifyNoInteractions(handelseRepository),
            () -> verifyNoInteractions(certificateEventRepository),
            () -> verifyNoMoreInteractions(utkastRepository)
        );
    }

    private void setupEraseByCertificateIdMocks(boolean withException) {
        doReturn(2).when(arendeDraftRepository).eraseArendeDraftsByCertificateIds(any());
        doReturn(2).when(certificateEventFailedLoadRepository).eraseEventsFailedByCertificateIds(any());
        doReturn(2).when(certificateEventProcessedRepository).eraseEventsProcessedByCertificateIds(any());
        doReturn(2).when(pagaendeSigneringRepository).erasePagaendeSigneringByCertificateIds(any());
        doReturn(2).when(notificationRedeliveryRepository).eraseRedeliveriesForEventIds(any());
        doReturn(2).when(referensRepository).eraseReferenserByCertificateIds(any());
        doReturn(2).when(migreratMedcertIntygRepository).eraseMedcertCertificatesByCertificateIds(any());
        doReturn(2).when(fragaSvarRepository).eraseFragaSvarByCertificateIds(any());
        doReturn(2).when(arendeRepository).eraseArendenByCertificateIds(any());
        doReturn(2).when(handelseRepository).eraseHandelseByCertificateIds(any());
        doReturn(2).when(certificateEventRepository).eraseCertificateEventsByCertificateIds(any());

        doReturn(getHandelseIds(), getHandelseIds(), getHandelseIds()).when(handelseRepository).findHandelseIdsByCertificateIds(any());

        if (!withException) {
            doReturn(4, 4, 2).when(utkastRepository).eraseCertificatesByCertificateIds(any());
        } else {
            when(utkastRepository.eraseCertificatesByCertificateIds(any())).thenReturn(4, 4)
                .thenThrow(new IllegalArgumentException("TestException"));
        }
    }

    private void setupEraseByCareProviderIdMocks() {
        doReturn(2).when(integreradEnhetRepository).eraseIntegratedUnitsByCareProviderId(any(String.class));
        doReturn(2).when(godkantAvtalRepository).eraseGodkantAvtalByCareProviderId(any(String.class));
    }

    private void setupPageMock(boolean careProviderHasCertificates) {
        if (careProviderHasCertificates) {
            final var page1 = new PageImpl<>(getCertificateIds(4), ERASE_PAGEABLE, 10L);
            final var page2 = new PageImpl<>(getCertificateIds(4), ERASE_PAGEABLE, 6L);
            final var page3 = new PageImpl<>(getCertificateIds(2), ERASE_PAGEABLE, 2L);

            doReturn(page1, page2, page3).when(utkastRepository).findCertificateIdsForCareProvider(any(String.class), any(Pageable.class));
        } else {
            final var emptyPage = new PageImpl<>(getCertificateIds(0), ERASE_PAGEABLE, 0L);
            doReturn(emptyPage).when(utkastRepository).findCertificateIdsForCareProvider(any(String.class), any(Pageable.class));
        }
    }

    private List<String> getCertificateIds(int count) {
        final var certificateIds = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            certificateIds.add(UUID.randomUUID().toString());
        }
        certIds.add(certificateIds);
        return certificateIds;
    }

    private List<Long> getHandelseIds() {
        final var max = 12345;
        final var min = 1234;
        final var random = new Random();
        final var ids = new ArrayList<Long>();
        for (int i = 0; i < 3; i++) {
            ids.add(min + (long) (random.nextDouble() * (max - min)));
        }
        eventIds.add(ids);
        return ids;
    }
}
