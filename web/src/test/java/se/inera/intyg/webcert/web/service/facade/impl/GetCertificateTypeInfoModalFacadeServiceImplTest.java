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
package se.inera.intyg.webcert.web.service.facade.impl;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@ExtendWith(MockitoExtension.class)
class GetCertificateTypeInfoModalFacadeServiceImplTest {

    private static final String CERTIFICATE_TYPE = "db";
    private static final String PATIENT_ID = "191212121212";
    private static final String EXPECTED_TITLE = "Signerat dödsbevis på annan vårdenhet";
    private static final String EXPECTED_DESCRIPTION = "<p><strong>Vårdgivare</strong><br/>Test Provider</p>";

    @Mock
    private CertificateTypeInfoModalService certificateTypeInfoModalService;

    @Mock
    private LogService logService;

    @Mock
    private WebCertUserService webCertUserService;

    @InjectMocks
    private GetCertificateTypeInfoModalFacadeServiceImpl service;

    private Personnummer personnummer;
    private WebCertUser user;

    @BeforeEach
    void setUp() {
        personnummer = Personnummer.createPersonnummer(PATIENT_ID).orElseThrow();
        user = new WebCertUser();
        when(webCertUserService.getUser()).thenReturn(user);
    }

    @Test
    void shouldReturnModalWhenModalExists() {
        final var modal = CertificateTypeInfoModal.builder()
            .title(EXPECTED_TITLE)
            .description(EXPECTED_DESCRIPTION)
            .link("Visa vårdenhetens namn och HSA-id")
            .build();

        when(certificateTypeInfoModalService.get(eq(CERTIFICATE_TYPE), eq(personnummer)))
            .thenReturn(Optional.of(modal));

        final var result = service.get(CERTIFICATE_TYPE, personnummer);

        assertAll(
            () -> assertNotNull(result),
            () -> assertEquals(EXPECTED_TITLE, result.getTitle()),
            () -> assertEquals(EXPECTED_DESCRIPTION, result.getDescription())
        );
    }

    @Test
    void shouldReturnNullWhenNoModalExists() {
        when(certificateTypeInfoModalService.get(eq(CERTIFICATE_TYPE), eq(personnummer)))
            .thenReturn(Optional.empty());

        final var result = service.get(CERTIFICATE_TYPE, personnummer);

        assertNull(result);
    }

    @Test
    void shouldHandleDoiCertificateType() {
        final var certificateType = "doi";
        final var expectedTitle = "Signerat dödsorsaksintyg på annan vårdenhet";

        final var modal = CertificateTypeInfoModal.builder()
            .title(expectedTitle)
            .description(EXPECTED_DESCRIPTION)
            .link("Visa vårdenhetens namn och HSA-id")
            .build();

        when(certificateTypeInfoModalService.get(eq(certificateType), eq(personnummer)))
            .thenReturn(Optional.of(modal));

        final var result = service.get(certificateType, personnummer);

        assertAll(
            () -> assertNotNull(result),
            () -> assertEquals(expectedTitle, result.getTitle())
        );
    }

    @Nested
    class Logging {

        @Test
        void shouldLogReadLevelOneWhenModalExists() {
            final var modal = CertificateTypeInfoModal.builder()
                .title(EXPECTED_TITLE)
                .description(EXPECTED_DESCRIPTION)
                .link("Visa vårdenhetens namn och HSA-id")
                .build();

            when(certificateTypeInfoModalService.get(eq(CERTIFICATE_TYPE), eq(personnummer)))
                .thenReturn(Optional.of(modal));

            service.get(CERTIFICATE_TYPE, personnummer);

            verify(logService).logReadLevelOne(eq(user), eq(PATIENT_ID));
        }

        @Test
        void shouldLogReadLevelOneEvenWhenNoModalExists() {
            when(certificateTypeInfoModalService.get(eq(CERTIFICATE_TYPE), eq(personnummer)))
                .thenReturn(Optional.empty());

            service.get(CERTIFICATE_TYPE, personnummer);

            verify(logService).logReadLevelOne(eq(user), eq(PATIENT_ID));
        }

        @Test
        void shouldLogReadLevelOneWithCorrectPatientId() {
            final var differentPatientId = "199001011234";
            final var differentPersonnummer = Personnummer.createPersonnummer(differentPatientId).orElseThrow();

            final var modal = CertificateTypeInfoModal.builder()
                .title(EXPECTED_TITLE)
                .description(EXPECTED_DESCRIPTION)
                .build();

            when(certificateTypeInfoModalService.get(eq(CERTIFICATE_TYPE), eq(differentPersonnummer)))
                .thenReturn(Optional.of(modal));

            service.get(CERTIFICATE_TYPE, differentPersonnummer);

            verify(logService).logReadLevelOne(eq(user), eq(differentPatientId));
        }
    }
}