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
package se.inera.intyg.webcert.web.service.facade.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
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
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions.SendCertificateFunction;
import se.inera.intyg.webcert.web.service.receiver.CertificateReceiverService;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygReceiver;

@ExtendWith(MockitoExtension.class)
class CertificateRecipientConverterImplTest {

  @Mock
  CertificateReceiverService certificateReceiverService;
  @Mock
  SendCertificateFunction sendCertificateFunction;
  @Mock
  AuthoritiesHelper authoritiesHelper;

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

      when(sendCertificateFunction.isSendCertificateBlockedForCertificateVersion(anyString(), anyBoolean()))
          .thenReturn(false);

      when(authoritiesHelper.isFeatureActive(anyString(), anyString()))
          .thenReturn(true);
    }

    @Test
    void shouldReturnNullIfNoRecipient() {
      final var response = certificateRecipientConverter.get("type", "id", SENT, true);

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

      when(sendCertificateFunction.isSendCertificateBlockedForCertificateVersion(anyString(), anyBoolean()))
          .thenReturn(false);

      when(authoritiesHelper.isFeatureActive(anyString(), anyString()))
          .thenReturn(true);
    }

    @Test
    void shouldReturnId() {
      final var response = certificateRecipientConverter.get("type", "id", SENT, true);

      assertEquals(ID, response.getId());
    }

    @Test
    void shouldReturnName() {
      final var response = certificateRecipientConverter.get("type", "id", SENT, true);

      assertEquals(NAME, response.getName());
    }

    @Test
    void shouldReturnSent() {
      final var response = certificateRecipientConverter.get("type", "id", SENT, true);

      assertEquals(SENT, response.getSent());
    }
  }

  @Nested
  class FilterMainRecipientOnLockStatus  {
    final IntygReceiver receiver1 = new IntygReceiver();
    final IntygReceiver receiver2 = new IntygReceiver();

    @BeforeEach
    void setup() {
      receiver1.setId(ID);
      receiver2.setId(ANOTHER_ID);

      when(certificateReceiverService.listPossibleReceiversWithApprovedInfo(anyString(), anyString()))
          .thenReturn(List.of(receiver1, receiver2));

      when(sendCertificateFunction.isSendCertificateBlockedForCertificateVersion(anyString(), anyBoolean()))
          .thenReturn(false);

      when(authoritiesHelper.isFeatureActive(anyString(), anyString()))
          .thenReturn(true);
    }

    @Test
    void shouldReturnNullIfOnlyUnlockedReceivers() {
      receiver1.setLocked(false);
      receiver2.setLocked(false);

      final var response = certificateRecipientConverter.get("type", "id", SENT, true);
      assertNull(response);
    }

    @Test
    void shouldReturnFirstLockedReceiverIfAllLocked() {
      receiver1.setLocked(true);
      receiver2.setLocked(true);

      final var response = certificateRecipientConverter.get("type", "id", SENT, true);
      assertEquals(ID, response.getId());
    }

    @Test
    void shouldReturnFirstLockedReceiverIfMixed() {
      receiver1.setLocked(false);
      receiver2.setLocked(true);

      final var response = certificateRecipientConverter.get("type", "id", SENT, true);
      assertEquals(ANOTHER_ID, response.getId());
    }
  }

  @Nested
  class FeatureActivation {
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
    void shouldReturnNullIfFeatureIsNotActivated() {
      final var response = certificateRecipientConverter.get("type", "id", LocalDateTime.now(), true);

      assertNull(response);
    }

    @Test
    void shouldReturnRecipientIfFeatureIsActivated() {
      when(authoritiesHelper.isFeatureActive(anyString(), anyString()))
          .thenReturn(true);

      final var response = certificateRecipientConverter.get("type", "id", LocalDateTime.now(), true);

      assertNotNull(response);
    }
  }

  @Nested
  class SendBlockedForVersionCheck {
    @BeforeEach
    void setup() {
      final var receiver = new IntygReceiver();
      receiver.setId(ID);
      receiver.setName(NAME);
      receiver.setLocked(true);

      when(certificateReceiverService.listPossibleReceiversWithApprovedInfo(anyString(), anyString()))
          .thenReturn(List.of(receiver));

      when(authoritiesHelper.isFeatureActive(anyString(), anyString()))
          .thenReturn(true);
    }

    @Test
    void shouldReturnNullIfSendFunctionReturnsTsIsBlocked() {
      when(sendCertificateFunction.isSendCertificateBlockedForCertificateVersion(anyString(), anyBoolean()))
          .thenReturn(true);

      final var response = certificateRecipientConverter.get("type", "id", LocalDateTime.now(), true);

      assertNull(response);
    }

    @Test
    void shouldReturnRecipientIfSendFunctionReturnsTsNotBlocked() {
      final var response = certificateRecipientConverter.get("type", "id", LocalDateTime.now(), true);

      assertNotNull(response);
    }
  }
}