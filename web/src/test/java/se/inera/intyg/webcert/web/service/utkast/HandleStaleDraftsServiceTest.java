package se.inera.intyg.webcert.web.service.utkast;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.notification.NotificationService;

@ExtendWith(MockitoExtension.class)
class HandleStaleDraftsServiceTest {

  @Mock
  private NotificationService notificationService;

  @Mock
  private UtkastRepository utkastRepository;

  @InjectMocks
  private HandleStaleDraftsService handleStaleDraftsService;

  @Test
  void shouldDeleteDraftsAndSendNotifications() {
    final var draft1 = createUtkast("cert-id-1");
    final var draft2 = createUtkast("cert-id-2");
    final var draft3 = createUtkast("cert-id-3");
    final var drafts = List.of(draft1, draft2, draft3);

    handleStaleDraftsService.deleteAndNotify(drafts);

    verify(utkastRepository).deleteAllById(List.of("cert-id-1", "cert-id-2", "cert-id-3"));
    verify(notificationService).sendNotificationForDraftDeleted(draft1);
    verify(notificationService).sendNotificationForDraftDeleted(draft2);
    verify(notificationService).sendNotificationForDraftDeleted(draft3);
  }

  @Test
  void shouldDeleteSingleDraftAndSendNotification() {
    final var draft = createUtkast("cert-id-1");
    final var drafts = List.of(draft);

    handleStaleDraftsService.deleteAndNotify(drafts);

    verify(utkastRepository).deleteAllById(List.of("cert-id-1"));
    verify(notificationService).sendNotificationForDraftDeleted(draft);
  }

  @Test
  void shouldHandleEmptyListWithoutErrors() {
    final var drafts = Collections.<Utkast>emptyList();

    handleStaleDraftsService.deleteAndNotify(drafts);

    verify(utkastRepository).deleteAllById(Collections.emptyList());
    verifyNoInteractions(notificationService);
  }

  @Test
  void shouldDeleteDraftsInCorrectOrder() {
    final var draft1 = createUtkast("cert-id-1");
    final var draft2 = createUtkast("cert-id-2");
    final var drafts = List.of(draft1, draft2);

    handleStaleDraftsService.deleteAndNotify(drafts);

    verify(utkastRepository).deleteAllById(List.of("cert-id-1", "cert-id-2"));
  }

  private Utkast createUtkast(String certificateId) {
    final var utkast = new Utkast();
    utkast.setIntygsId(certificateId);
    return utkast;
  }
}