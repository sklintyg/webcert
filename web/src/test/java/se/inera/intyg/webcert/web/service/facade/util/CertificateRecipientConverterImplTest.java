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
}