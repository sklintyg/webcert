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
package se.inera.intyg.webcert.web.service.facade.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.service.receiver.CertificateReceiverService;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygReceiver;

@ExtendWith(MockitoExtension.class)
class CertificateRecipientConverterImplTest {

    @Mock
    CertificateReceiverService certificateReceiverService;

    @InjectMocks
    CertificateRecipientConverterImpl certificateRecipientConverter;

    private static final String ID = "RecipientId";
    private static final String ANOTHER_ID = "AnotherRecipientId";
    private static final String NAME = "RecipientName";
    private static final LocalDateTime SENT = LocalDateTime.now();


    @Nested
    class NoRecipient {

        @BeforeEach
        void setup() {
            when(certificateReceiverService.listPossibleReceiversWithApprovedInfo(anyString(), anyString()))
                .thenReturn(Collections.emptyList());
        }

        @Test
        void shouldReturnNullIfNoRecipient() {
            final var response = certificateRecipientConverter.get("type", "id", SENT);

            assertNull(response);
        }
    }

    @Nested
    class HasRecipient {

        @BeforeEach
        void setup() {
            final var receiver = new IntygReceiver();
            receiver.setId(ID);
            receiver.setName(NAME);
            receiver.setLocked(true);

            when(certificateReceiverService.listPossibleReceiversWithApprovedInfo(anyString(), anyString()))
                .thenReturn(List.of(receiver));
        }

        @Test
        void shouldReturnId() {
            final var response = certificateRecipientConverter.get("type", "id", SENT);

            assertEquals(ID, response.getId());
        }

        @Test
        void shouldReturnName() {
            final var response = certificateRecipientConverter.get("type", "id", SENT);

            assertEquals(NAME, response.getName());
        }

        @Test
        void shouldReturnSent() {
            final var response = certificateRecipientConverter.get("type", "id", SENT);

            assertEquals(SENT, response.getSent());
        }
    }

    @Nested
    class FilterMainRecipientOnLockStatus {

        final IntygReceiver receiver1 = new IntygReceiver();
        final IntygReceiver receiver2 = new IntygReceiver();

        @BeforeEach
        void setup() {
            receiver1.setId(ID);
            receiver2.setId(ANOTHER_ID);

            when(certificateReceiverService.listPossibleReceiversWithApprovedInfo(anyString(), anyString()))
                .thenReturn(List.of(receiver1, receiver2));
        }

        @Test
        void shouldReturnNullIfOnlyUnlockedReceivers() {
            receiver1.setLocked(false);
            receiver2.setLocked(false);

            final var response = certificateRecipientConverter.get("type", "id", SENT);
            assertNull(response);
        }

        @Test
        void shouldReturnFirstLockedReceiverIfAllLocked() {
            receiver1.setLocked(true);
            receiver2.setLocked(true);

            final var response = certificateRecipientConverter.get("type", "id", SENT);
            assertEquals(ID, response.getId());
        }

        @Test
        void shouldReturnFirstLockedReceiverIfMixed() {
            receiver1.setLocked(false);
            receiver2.setLocked(true);

            final var response = certificateRecipientConverter.get("type", "id", SENT);
            assertEquals(ANOTHER_ID, response.getId());
        }
    }
}
