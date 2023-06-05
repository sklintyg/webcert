/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.unansweredCommunication;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.UnansweredCommunicationRequest;

@ExtendWith(MockitoExtension.class)
class UnansweredCommunicationServiceImplTest {

    @Mock
    private ArendeService arendeService;
    @InjectMocks
    private UnansweredCommunicationServiceImpl unansweredCommunicationService;

    private UnansweredCommunicationRequest request;
    private static final String PATIENT_ID = "191212121212";

    @BeforeEach
    void setUp() {
        request = new UnansweredCommunicationRequest(List.of(PATIENT_ID), 0);
    }

    @Test
    void shouldThrowIfPatientIdsIsNull() {
        final var request = new UnansweredCommunicationRequest(Collections.emptyList(), 0);
        assertThrows(IllegalArgumentException.class, () -> unansweredCommunicationService.get(request));
    }

    @Test
    void shouldThrowIfPatientIdsIsEmpty() {
        final var request = new UnansweredCommunicationRequest(Collections.emptyList(), 0);
        assertThrows(IllegalArgumentException.class, () -> unansweredCommunicationService.get(request));
    }

    @Test
    void shouldThrowIfMaxDaysOfUnansweredCommunicationIsNull() {
        final var request = new UnansweredCommunicationRequest(Collections.emptyList(), null);
        assertThrows(IllegalArgumentException.class, () -> unansweredCommunicationService.get(request));
    }

    @Test
    void shouldReturnEmptyResponseIfNoResultFromArendeService() {
        doReturn(Collections.emptyList()).when(arendeService).getArendenForPatientsWithTimestampAfterDate(
            eq(request.getPatientIds()),
            any()
        );
        final var result = unansweredCommunicationService.get(request);
        Assertions.assertEquals(0, result.getUnansweredQAsMap().size());
    }

    @Test
    void shouldFilterOnAmneIfPaminnelse() {
        final var arende = getArende(ArendeAmne.PAMINN, Status.PENDING_INTERNAL_ACTION, null);
        doReturn(List.of(arende)).when(arendeService).getArendenForPatientsWithTimestampAfterDate(
            eq(request.getPatientIds()),
            any()
        );
        final var result = unansweredCommunicationService.get(request);
        Assertions.assertEquals(0, result.getUnansweredQAsMap().size());
    }

    @Test
    void shouldFilterOnStatusIfNotObesvarad() {
        final var arende = getArende(ArendeAmne.KOMPLT, Status.ANSWERED, null);
        doReturn(List.of(arende)).when(arendeService).getArendenForPatientsWithTimestampAfterDate(
            eq(request.getPatientIds()),
            any()
        );
        final var result = unansweredCommunicationService.get(request);
        Assertions.assertEquals(0, result.getUnansweredQAsMap().size());
    }

    @Test
    void shouldReturnCertificateId() {
        final var expecteCertificateId = "expecteCertificateId";
        final var arende = getArende(ArendeAmne.KOMPLT, Status.PENDING_INTERNAL_ACTION, expecteCertificateId);
        doReturn(List.of(arende)).when(arendeService).getArendenForPatientsWithTimestampAfterDate(
            eq(request.getPatientIds()),
            any()
        );
        final var result = unansweredCommunicationService.get(request);
        Assertions.assertTrue(result.getUnansweredQAsMap().containsKey(expecteCertificateId));
    }

    @Test
    void shouldIncrementComplemented() {
        final var expecteCertificateId = "expecteCertificateId";
        final var arende = getArende(ArendeAmne.KOMPLT, Status.PENDING_INTERNAL_ACTION, expecteCertificateId);
        doReturn(List.of(arende)).when(arendeService).getArendenForPatientsWithTimestampAfterDate(
            eq(request.getPatientIds()),
            any()
        );
        final var result = unansweredCommunicationService.get(request);
        Assertions.assertEquals(1, result.getUnansweredQAsMap().get(expecteCertificateId).getComplement());
    }

    @Test
    void shouldIncrementMultipleComplemented() {
        final var expecteCertificateId = "expecteCertificateId";
        final var arende = getArende(ArendeAmne.KOMPLT, Status.PENDING_INTERNAL_ACTION, expecteCertificateId);
        doReturn(List.of(arende, arende, arende)).when(arendeService).getArendenForPatientsWithTimestampAfterDate(
            eq(request.getPatientIds()),
            any()
        );
        final var result = unansweredCommunicationService.get(request);
        Assertions.assertEquals(3, result.getUnansweredQAsMap().get(expecteCertificateId).getComplement());
    }

    @Test
    void shouldIncrementMultipleOther() {
        final var expecteCertificateId = "expecteCertificateId";
        final var arende = getArende(ArendeAmne.KONTKT, Status.PENDING_INTERNAL_ACTION, expecteCertificateId);
        doReturn(List.of(arende, arende, arende)).when(arendeService).getArendenForPatientsWithTimestampAfterDate(
            eq(request.getPatientIds()),
            any()
        );
        final var result = unansweredCommunicationService.get(request);
        Assertions.assertEquals(3, result.getUnansweredQAsMap().get(expecteCertificateId).getOthers());
    }

    @Test
    void shouldIncrementOther() {
        final var expecteCertificateId = "expecteCertificateId";
        final var arende = getArende(ArendeAmne.KONTKT, Status.PENDING_INTERNAL_ACTION, expecteCertificateId);
        doReturn(List.of(arende)).when(arendeService).getArendenForPatientsWithTimestampAfterDate(
            eq(request.getPatientIds()),
            any()
        );
        
        final var result = unansweredCommunicationService.get(request);
        Assertions.assertEquals(1, result.getUnansweredQAsMap().get(expecteCertificateId).getOthers());
    }

    @Test
    void shouldIncrementComplementAndOther() {
        final var expecteCertificateId = "expecteCertificateId";
        final var arendeOther = getArende(ArendeAmne.KONTKT, Status.PENDING_INTERNAL_ACTION, expecteCertificateId);
        final var arendeComplement = getArende(ArendeAmne.KOMPLT, Status.PENDING_INTERNAL_ACTION, expecteCertificateId);
        final var notValidArende = getArende(ArendeAmne.PAMINN, Status.PENDING_INTERNAL_ACTION, expecteCertificateId);

        doReturn(List.of(arendeOther, arendeComplement, notValidArende)).when(arendeService).getArendenForPatientsWithTimestampAfterDate(
            eq(request.getPatientIds()),
            any()
        );

        final var result = unansweredCommunicationService.get(request);
        Assertions.assertEquals(1, result.getUnansweredQAsMap().get(expecteCertificateId).getOthers());
        Assertions.assertEquals(1, result.getUnansweredQAsMap().get(expecteCertificateId).getComplement());
    }

    @Test
    void shouldIncrementComplementAndOtherForMultipleCertificates() {
        final var certificateIdOne = "certificateIdOne";
        final var certificateITwo = "certificateITwo";
        final var arendeOther = getArende(ArendeAmne.KONTKT, Status.PENDING_INTERNAL_ACTION, certificateIdOne);
        final var arendeComplement = getArende(ArendeAmne.KOMPLT, Status.PENDING_INTERNAL_ACTION, certificateITwo);
        final var notValidArende = getArende(ArendeAmne.PAMINN, Status.PENDING_INTERNAL_ACTION, certificateITwo);

        doReturn(List.of(arendeOther, arendeComplement, notValidArende)).when(arendeService).getArendenForPatientsWithTimestampAfterDate(
            eq(request.getPatientIds()),
            any()
        );

        final var result = unansweredCommunicationService.get(request);
        Assertions.assertEquals(1, result.getUnansweredQAsMap().get(certificateIdOne).getOthers());
        Assertions.assertEquals(1, result.getUnansweredQAsMap().get(certificateITwo).getComplement());
        Assertions.assertEquals(2, result.getUnansweredQAsMap().size());
    }

    private Arende getArende(ArendeAmne amne, Status status, String certificateId) {
        final var arende = new Arende();
        arende.setAmne(amne);
        arende.setStatus(status);
        arende.setSvarPaId(PATIENT_ID);
        arende.setIntygsId(certificateId);
        return arende;
    }
}
