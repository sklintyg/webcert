/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.access;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.security.authorities.AuthoritiesException;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.web.util.access.AccessResultExceptionHelper;
import se.inera.intyg.webcert.web.web.util.access.AccessResultExceptionHelperImpl;

@ExtendWith(MockitoExtension.class)
class LockedDraftAccessServiceHelperTest {

  @Mock private LockedDraftAccessService lockedDraftAccessService;

  @Spy
  private AccessResultExceptionHelper accessResultExceptionHelper =
      new AccessResultExceptionHelperImpl();

  @InjectMocks private LockedDraftAccessServiceHelper lockedDraftAccessServiceHelper;

  private Utkast draft;

  @BeforeEach
  void setup() {
    draft = new Utkast();
    draft.setIntygsTyp("certificateType");
    draft.setIntygTypeVersion("certificateTypeVersion");
    draft.setPatientPersonnummer(Personnummer.createPersonnummer("191212121212").get());
    draft.setTestIntyg(false);
  }

  @Test
  void shallThrowExceptionIfNoAccessToRead() {
    try {
      doReturn(createNoAccessResult())
          .when(lockedDraftAccessService)
          .allowToRead(any(AccessEvaluationParameters.class));
      lockedDraftAccessServiceHelper.validateAccessToRead(draft);
      assertTrue(false, "Should throw exception if no access");
    } catch (AuthoritiesException ex) {
      assertTrue(true);
    }
  }

  @Test
  void shallNotThrowExeptionIfAllowAccessToRead() {
    doReturn(createAccessResult())
        .when(lockedDraftAccessService)
        .allowToRead(any(AccessEvaluationParameters.class));
    lockedDraftAccessServiceHelper.validateAccessToRead(draft);
    assertTrue(true);
  }

  @Test
  void shallAllowIfAllowAccessToRead() {
    doReturn(createAccessResult())
        .when(lockedDraftAccessService)
        .allowToRead(any(AccessEvaluationParameters.class));
    final var actualResult = lockedDraftAccessServiceHelper.isAllowToRead(draft);
    assertEquals(true, actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToRead() {
    doReturn(createNoAccessResult())
        .when(lockedDraftAccessService)
        .allowToRead(any(AccessEvaluationParameters.class));
    final var actualResult = lockedDraftAccessServiceHelper.isAllowToRead(draft);
    assertEquals(false, actualResult);
  }

  @Test
  void shallAllowIfAllowAccessToReadPassingAccessEvaluationParameters() {
    doReturn(createAccessResult())
        .when(lockedDraftAccessService)
        .allowToRead(any(AccessEvaluationParameters.class));
    final var actualResult =
        lockedDraftAccessServiceHelper.isAllowToRead(mock(AccessEvaluationParameters.class));
    assertEquals(true, actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToReadPassingAccessEvaluationParameters() {
    doReturn(createNoAccessResult())
        .when(lockedDraftAccessService)
        .allowToRead(any(AccessEvaluationParameters.class));
    final var actualResult =
        lockedDraftAccessServiceHelper.isAllowToRead(mock(AccessEvaluationParameters.class));
    assertEquals(false, actualResult);
  }

  @Test
  void shallThrowExceptionIfNoAccessToCopy() {
    try {
      doReturn(createNoAccessResult())
          .when(lockedDraftAccessService)
          .allowToCopy(any(AccessEvaluationParameters.class));
      lockedDraftAccessServiceHelper.validateAccessToCopy(draft);
      assertTrue(false, "Should throw exception if no access");
    } catch (AuthoritiesException ex) {
      assertTrue(true);
    }
  }

  @Test
  void shallNotThrowExeptionIfAllowAccessToCopy() {
    doReturn(createAccessResult())
        .when(lockedDraftAccessService)
        .allowToCopy(any(AccessEvaluationParameters.class));
    lockedDraftAccessServiceHelper.validateAccessToCopy(draft);
    assertTrue(true);
  }

  @Test
  void shallAllowIfAllowAccessToCopy() {
    doReturn(createAccessResult())
        .when(lockedDraftAccessService)
        .allowToCopy(any(AccessEvaluationParameters.class));
    final var actualResult = lockedDraftAccessServiceHelper.isAllowToCopy(draft);
    assertEquals(true, actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToCopy() {
    doReturn(createNoAccessResult())
        .when(lockedDraftAccessService)
        .allowToCopy(any(AccessEvaluationParameters.class));
    final var actualResult = lockedDraftAccessServiceHelper.isAllowToCopy(draft);
    assertEquals(false, actualResult);
  }

  @Test
  void shallAllowIfAllowAccessToCopyPassingAccessEvaluationParameters() {
    doReturn(createAccessResult())
        .when(lockedDraftAccessService)
        .allowToCopy(any(AccessEvaluationParameters.class));
    final var actualResult =
        lockedDraftAccessServiceHelper.isAllowToCopy(mock(AccessEvaluationParameters.class));
    assertEquals(true, actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToCopyPassingAccessEvaluationParameters() {
    doReturn(createNoAccessResult())
        .when(lockedDraftAccessService)
        .allowToCopy(any(AccessEvaluationParameters.class));
    final var actualResult =
        lockedDraftAccessServiceHelper.isAllowToCopy(mock(AccessEvaluationParameters.class));
    assertEquals(false, actualResult);
  }

  @Test
  void shallThrowExceptionIfNoAccessToInvalidate() {
    try {
      doReturn(createNoAccessResult())
          .when(lockedDraftAccessService)
          .allowToInvalidate(any(AccessEvaluationParameters.class));
      lockedDraftAccessServiceHelper.validateAccessToInvalidate(draft);
      assertTrue(false, "Should throw exception if no access");
    } catch (AuthoritiesException ex) {
      assertTrue(true);
    }
  }

  @Test
  void shallNotThrowExeptionIfAllowAccessToInvalidate() {
    doReturn(createAccessResult())
        .when(lockedDraftAccessService)
        .allowToInvalidate(any(AccessEvaluationParameters.class));
    lockedDraftAccessServiceHelper.validateAccessToInvalidate(draft);
    assertTrue(true);
  }

  @Test
  void shallAllowIfAllowAccessToInvalidate() {
    doReturn(createAccessResult())
        .when(lockedDraftAccessService)
        .allowToInvalidate(any(AccessEvaluationParameters.class));
    final var actualResult = lockedDraftAccessServiceHelper.isAllowToInvalidate(draft);
    assertEquals(true, actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToInvalidate() {
    doReturn(createNoAccessResult())
        .when(lockedDraftAccessService)
        .allowToInvalidate(any(AccessEvaluationParameters.class));
    final var actualResult = lockedDraftAccessServiceHelper.isAllowToInvalidate(draft);
    assertEquals(false, actualResult);
  }

  @Test
  void shallAllowIfAllowAccessToInvalidatePassingAccessEvaluationParameters() {
    doReturn(createAccessResult())
        .when(lockedDraftAccessService)
        .allowToInvalidate(any(AccessEvaluationParameters.class));
    final var actualResult =
        lockedDraftAccessServiceHelper.isAllowToInvalidate(mock(AccessEvaluationParameters.class));
    assertEquals(true, actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToInvalidatePassingAccessEvaluationParameters() {
    doReturn(createNoAccessResult())
        .when(lockedDraftAccessService)
        .allowToInvalidate(any(AccessEvaluationParameters.class));
    final var actualResult =
        lockedDraftAccessServiceHelper.isAllowToInvalidate(mock(AccessEvaluationParameters.class));
    assertEquals(false, actualResult);
  }

  @Test
  void shallThrowExceptionIfNoAccessToPrint() {
    try {
      doReturn(createNoAccessResult())
          .when(lockedDraftAccessService)
          .allowToPrint(any(AccessEvaluationParameters.class));
      lockedDraftAccessServiceHelper.validateAccessToPrint(draft);
      assertTrue(false, "Should throw exception if no access");
    } catch (AuthoritiesException ex) {
      assertTrue(true);
    }
  }

  @Test
  void shallNotThrowExeptionIfAllowAccessToPrint() {
    doReturn(createAccessResult())
        .when(lockedDraftAccessService)
        .allowToPrint(any(AccessEvaluationParameters.class));
    lockedDraftAccessServiceHelper.validateAccessToPrint(draft);
    assertTrue(true);
  }

  @Test
  void shallAllowIfAllowAccessToPrint() {
    doReturn(createAccessResult())
        .when(lockedDraftAccessService)
        .allowToPrint(any(AccessEvaluationParameters.class));
    final var actualResult = lockedDraftAccessServiceHelper.isAllowToPrint(draft);
    assertEquals(true, actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToPrint() {
    doReturn(createNoAccessResult())
        .when(lockedDraftAccessService)
        .allowToPrint(any(AccessEvaluationParameters.class));
    final var actualResult = lockedDraftAccessServiceHelper.isAllowToPrint(draft);
    assertEquals(false, actualResult);
  }

  @Test
  void shallAllowIfAllowAccessToPrintPassingAccessEvaluationParameters() {
    doReturn(createAccessResult())
        .when(lockedDraftAccessService)
        .allowToPrint(any(AccessEvaluationParameters.class));
    final var actualResult =
        lockedDraftAccessServiceHelper.isAllowToPrint(mock(AccessEvaluationParameters.class));
    assertEquals(true, actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToPrintPassingAccessEvaluationParameters() {
    doReturn(createNoAccessResult())
        .when(lockedDraftAccessService)
        .allowToPrint(any(AccessEvaluationParameters.class));
    final var actualResult =
        lockedDraftAccessServiceHelper.isAllowToPrint(mock(AccessEvaluationParameters.class));
    assertEquals(false, actualResult);
  }

  private AccessResult createAccessResult() {
    return AccessResult.noProblem();
  }

  private AccessResult createNoAccessResult() {
    return AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION, "No Access");
  }
}
