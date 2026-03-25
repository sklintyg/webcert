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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
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
class DraftAccessServiceHelperTest {

  @Mock private DraftAccessService draftAccessService;

  @Spy
  private final AccessResultExceptionHelper accessResultExceptionHelper =
      new AccessResultExceptionHelperImpl();

  @InjectMocks private DraftAccessServiceHelper draftAccessServiceHelper;

  private Utkast draft;

  @BeforeEach
  void setup() {
    draft = new Utkast();
    draft.setIntygsTyp("certificateType");
    draft.setIntygTypeVersion("certificateTypeVersion");
    draft.setPatientPersonnummer(Personnummer.createPersonnummer("191212121212").orElseThrow());
    draft.setTestIntyg(false);
  }

  @Test
  void shallThrowExceptionIfNoAccessToCreate() {
    try {
      doReturn(createNoAccessResult())
          .when(draftAccessService)
          .allowToCreateDraft(any(AccessEvaluationParameters.class));
      draftAccessServiceHelper.validateAllowToCreateUtkast(
          draft.getIntygsTyp(), draft.getPatientPersonnummer());
      fail();
    } catch (AuthoritiesException ex) {
      assertTrue(true);
    }
  }

  @Test
  void shallNotThrowExeptionIfAllowAccessToCreate() {
    doReturn(createAccessResult())
        .when(draftAccessService)
        .allowToCreateDraft(any(AccessEvaluationParameters.class));
    draftAccessServiceHelper.validateAllowToCreateUtkast(
        draft.getIntygsTyp(), draft.getPatientPersonnummer());
    assertTrue(true);
  }

  @Test
  void shallAllowIfAllowAccessToCreate() {
    doReturn(createAccessResult())
        .when(draftAccessService)
        .allowToCreateDraft(any(AccessEvaluationParameters.class));
    final var actualResult =
        draftAccessServiceHelper.isAllowedToCreateUtkast(
            draft.getIntygsTyp(), draft.getPatientPersonnummer());
    assertTrue(actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToCreate() {
    doReturn(createNoAccessResult())
        .when(draftAccessService)
        .allowToCreateDraft(any(AccessEvaluationParameters.class));
    final var actualResult =
        draftAccessServiceHelper.isAllowedToCreateUtkast(
            draft.getIntygsTyp(), draft.getPatientPersonnummer());
    assertFalse(actualResult);
  }

  @Test
  void shallThrowExceptionIfNoAccessToRead() {
    try {
      doReturn(createNoAccessResult())
          .when(draftAccessService)
          .allowToReadDraft(any(AccessEvaluationParameters.class));
      draftAccessServiceHelper.validateAllowToReadUtkast(draft);
      fail();
    } catch (AuthoritiesException ex) {
      assertTrue(true);
    }
  }

  @Test
  void shallNotThrowExeptionIfAllowAccessToRead() {
    doReturn(createAccessResult())
        .when(draftAccessService)
        .allowToReadDraft(any(AccessEvaluationParameters.class));
    draftAccessServiceHelper.validateAllowToReadUtkast(draft);
    assertTrue(true);
  }

  @Test
  void shallAllowIfAllowAccessToRead() {
    doReturn(createAccessResult())
        .when(draftAccessService)
        .allowToReadDraft(any(AccessEvaluationParameters.class));
    final var actualResult = draftAccessServiceHelper.isAllowedToReadUtkast(draft);
    assertTrue(actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToRead() {
    doReturn(createNoAccessResult())
        .when(draftAccessService)
        .allowToReadDraft(any(AccessEvaluationParameters.class));
    final var actualResult = draftAccessServiceHelper.isAllowedToReadUtkast(draft);
    assertFalse(actualResult);
  }

  @Test
  void shallThrowExceptionIfNoAccessToEdit() {
    try {
      doReturn(createNoAccessResult())
          .when(draftAccessService)
          .allowToEditDraft(any(AccessEvaluationParameters.class));
      draftAccessServiceHelper.validateAllowToEditUtkast(draft);
      fail();
    } catch (AuthoritiesException ex) {
      assertTrue(true);
    }
  }

  @Test
  void shallNotThrowExeptionIfAllowAccessToEdit() {
    doReturn(createAccessResult())
        .when(draftAccessService)
        .allowToEditDraft(any(AccessEvaluationParameters.class));
    draftAccessServiceHelper.validateAllowToEditUtkast(draft);
    assertTrue(true);
  }

  @Test
  void shallAllowIfAllowAccessToEdit() {
    doReturn(createAccessResult())
        .when(draftAccessService)
        .allowToEditDraft(any(AccessEvaluationParameters.class));
    final var actualResult = draftAccessServiceHelper.isAllowToEditUtkast(draft);
    assertTrue(actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToEdit() {
    doReturn(createNoAccessResult())
        .when(draftAccessService)
        .allowToEditDraft(any(AccessEvaluationParameters.class));
    final var actualResult = draftAccessServiceHelper.isAllowToEditUtkast(draft);
    assertFalse(actualResult);
  }

  @Test
  void shallAllowIfAllowAccessToEditCertificatePassingAccessEvaluationParameters() {
    doReturn(createAccessResult())
        .when(draftAccessService)
        .allowToEditDraft(any(AccessEvaluationParameters.class));
    final var actualResult =
        draftAccessServiceHelper.isAllowToEditUtkast(mock(AccessEvaluationParameters.class));
    assertTrue(actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToEditCertificatePassingAccessEvaluationParameters() {
    doReturn(createNoAccessResult())
        .when(draftAccessService)
        .allowToEditDraft(any(AccessEvaluationParameters.class));
    final var actualResult =
        draftAccessServiceHelper.isAllowToEditUtkast(mock(AccessEvaluationParameters.class));
    assertFalse(actualResult);
  }

  @Test
  void shallThrowExceptionIfNoAccessToDelete() {
    try {
      doReturn(createNoAccessResult())
          .when(draftAccessService)
          .allowToDeleteDraft(any(AccessEvaluationParameters.class));
      draftAccessServiceHelper.validateAllowToDeleteUtkast(draft);
      fail();
    } catch (AuthoritiesException ex) {
      assertTrue(true);
    }
  }

  @Test
  void shallNotThrowExeptionIfAllowAccessToDelete() {
    doReturn(createAccessResult())
        .when(draftAccessService)
        .allowToDeleteDraft(any(AccessEvaluationParameters.class));
    draftAccessServiceHelper.validateAllowToDeleteUtkast(draft);
    assertTrue(true);
  }

  @Test
  void shallAllowIfAllowAccessToDelete() {
    doReturn(createAccessResult())
        .when(draftAccessService)
        .allowToDeleteDraft(any(AccessEvaluationParameters.class));
    final var actualResult = draftAccessServiceHelper.isAllowToDeleteUtkast(draft);
    assertTrue(actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToDelete() {
    doReturn(createNoAccessResult())
        .when(draftAccessService)
        .allowToDeleteDraft(any(AccessEvaluationParameters.class));
    final var actualResult = draftAccessServiceHelper.isAllowToDeleteUtkast(draft);
    assertFalse(actualResult);
  }

  @Test
  void shallAllowIfAllowAccessToDeleteCertificatePassingAccessEvaluationParameters() {
    doReturn(createAccessResult())
        .when(draftAccessService)
        .allowToDeleteDraft(any(AccessEvaluationParameters.class));
    final var actualResult =
        draftAccessServiceHelper.isAllowToDeleteUtkast(mock(AccessEvaluationParameters.class));
    assertTrue(actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToDeleteCertificatePassingAccessEvaluationParameters() {
    doReturn(createNoAccessResult())
        .when(draftAccessService)
        .allowToDeleteDraft(any(AccessEvaluationParameters.class));
    final var actualResult =
        draftAccessServiceHelper.isAllowToDeleteUtkast(mock(AccessEvaluationParameters.class));
    assertFalse(actualResult);
  }

  @Test
  void shallThrowExceptionIfNoAccessToPrint() {
    try {
      doReturn(createNoAccessResult())
          .when(draftAccessService)
          .allowToPrintDraft(any(AccessEvaluationParameters.class));
      draftAccessServiceHelper.validateAllowToPrintUtkast(draft);
      fail();
    } catch (AuthoritiesException ex) {
      assertTrue(true);
    }
  }

  @Test
  void shallNotThrowExeptionIfAllowAccessToPrint() {
    doReturn(createAccessResult())
        .when(draftAccessService)
        .allowToPrintDraft(any(AccessEvaluationParameters.class));
    draftAccessServiceHelper.validateAllowToPrintUtkast(draft);
    assertTrue(true);
  }

  @Test
  void shallAllowIfAllowAccessToPrint() {
    doReturn(createAccessResult())
        .when(draftAccessService)
        .allowToPrintDraft(any(AccessEvaluationParameters.class));
    final var actualResult = draftAccessServiceHelper.isAllowToPrintUtkast(draft);
    assertTrue(actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToPrint() {
    doReturn(createNoAccessResult())
        .when(draftAccessService)
        .allowToPrintDraft(any(AccessEvaluationParameters.class));
    final var actualResult = draftAccessServiceHelper.isAllowToPrintUtkast(draft);
    assertFalse(actualResult);
  }

  @Test
  void shallAllowIfAllowAccessToPrintCertificatePassingAccessEvaluationParameters() {
    doReturn(createAccessResult())
        .when(draftAccessService)
        .allowToPrintDraft(any(AccessEvaluationParameters.class));
    final var actualResult =
        draftAccessServiceHelper.isAllowToPrintUtkast(mock(AccessEvaluationParameters.class));
    assertTrue(actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToPrintCertificatePassingAccessEvaluationParameters() {
    doReturn(createNoAccessResult())
        .when(draftAccessService)
        .allowToPrintDraft(any(AccessEvaluationParameters.class));
    final var actualResult =
        draftAccessServiceHelper.isAllowToPrintUtkast(mock(AccessEvaluationParameters.class));
    assertFalse(actualResult);
  }

  @Test
  void shallThrowExceptionIfNoAccessToForward() {
    try {
      doReturn(createNoAccessResult())
          .when(draftAccessService)
          .allowToForwardDraft(any(AccessEvaluationParameters.class));
      draftAccessServiceHelper.validateAllowToForwardDraft(draft);
      fail();
    } catch (AuthoritiesException ex) {
      assertTrue(true);
    }
  }

  @Test
  void shallNotThrowExeptionIfAllowAccessToForward() {
    doReturn(createAccessResult())
        .when(draftAccessService)
        .allowToForwardDraft(any(AccessEvaluationParameters.class));
    draftAccessServiceHelper.validateAllowToForwardDraft(draft);
    assertTrue(true);
  }

  @Test
  void shallAllowIfAllowAccessToForward() {
    doReturn(createAccessResult())
        .when(draftAccessService)
        .allowToForwardDraft(any(AccessEvaluationParameters.class));
    final var actualResult = draftAccessServiceHelper.isAllowedToForwardUtkast(draft);
    assertTrue(actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToForward() {
    doReturn(createNoAccessResult())
        .when(draftAccessService)
        .allowToForwardDraft(any(AccessEvaluationParameters.class));
    final var actualResult = draftAccessServiceHelper.isAllowedToForwardUtkast(draft);
    assertFalse(actualResult);
  }

  @Test
  void shallAllowIfAllowAccessToForwardCertificatePassingAccessEvaluationParameters() {
    doReturn(createAccessResult())
        .when(draftAccessService)
        .allowToForwardDraft(any(AccessEvaluationParameters.class));
    final var actualResult =
        draftAccessServiceHelper.isAllowedToForwardUtkast(mock(AccessEvaluationParameters.class));
    assertTrue(actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToForwardCertificatePassingAccessEvaluationParameters() {
    doReturn(createNoAccessResult())
        .when(draftAccessService)
        .allowToForwardDraft(any(AccessEvaluationParameters.class));
    final var actualResult =
        draftAccessServiceHelper.isAllowedToForwardUtkast(mock(AccessEvaluationParameters.class));
    assertFalse(actualResult);
  }

  @Test
  void shallThrowExceptionIfNoAccessToReadyForSign() {
    try {
      doReturn(createNoAccessResult())
          .when(draftAccessService)
          .allowToReadyForSign(any(AccessEvaluationParameters.class));
      draftAccessServiceHelper.validateAllowToReadyForSign(draft);
      fail();
    } catch (AuthoritiesException ex) {
      assertTrue(true);
    }
  }

  @Test
  void shallNotThrowExeptionIfAllowAccessToReadyForSign() {
    doReturn(createAccessResult())
        .when(draftAccessService)
        .allowToReadyForSign(any(AccessEvaluationParameters.class));
    draftAccessServiceHelper.validateAllowToReadyForSign(draft);
    assertTrue(true);
  }

  @Test
  void shallAllowIfAllowAccessToReadyForSign() {
    doReturn(createAccessResult())
        .when(draftAccessService)
        .allowToReadyForSign(any(AccessEvaluationParameters.class));
    final var actualResult = draftAccessServiceHelper.isAllowedToReadyForSign(draft);
    assertTrue(actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToReadyForSign() {
    doReturn(createNoAccessResult())
        .when(draftAccessService)
        .allowToReadyForSign(any(AccessEvaluationParameters.class));
    final var actualResult = draftAccessServiceHelper.isAllowedToReadyForSign(draft);
    assertFalse(actualResult);
  }

  @Test
  void shallAllowIfAllowAccessToReadyForSignCertificatePassingAccessEvaluationParameters() {
    doReturn(createAccessResult())
        .when(draftAccessService)
        .allowToReadyForSign(any(AccessEvaluationParameters.class));
    final var actualResult =
        draftAccessServiceHelper.isAllowedToReadyForSign(mock(AccessEvaluationParameters.class));
    assertTrue(actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToReadyForSignCertificatePassingAccessEvaluationParameters() {
    doReturn(createNoAccessResult())
        .when(draftAccessService)
        .allowToReadyForSign(any(AccessEvaluationParameters.class));
    final var actualResult =
        draftAccessServiceHelper.isAllowedToReadyForSign(mock(AccessEvaluationParameters.class));
    assertFalse(actualResult);
  }

  @Test
  void shallThrowExceptionIfNoAccessToCopyFromCandidate() {
    try {
      doReturn(createNoAccessResult())
          .when(draftAccessService)
          .allowToCopyFromCandidate(any(AccessEvaluationParameters.class));
      draftAccessServiceHelper.validateAllowToCopyFromCandidate(draft);
      fail();
    } catch (AuthoritiesException ex) {
      assertTrue(true);
    }
  }

  @Test
  void shallNotThrowExeptionIfAllowAccessToCopyFromCandidate() {
    doReturn(createAccessResult())
        .when(draftAccessService)
        .allowToCopyFromCandidate(any(AccessEvaluationParameters.class));
    draftAccessServiceHelper.validateAllowToCopyFromCandidate(draft);
    assertTrue(true);
  }

  @Test
  void shallAllowIfAllowAccessToCopyFromCandidate() {
    doReturn(createAccessResult())
        .when(draftAccessService)
        .allowToCopyFromCandidate(any(AccessEvaluationParameters.class));
    final var actualResult = draftAccessServiceHelper.isAllowedToCopyFromCandidate(draft);
    assertTrue(actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToCopyFromCandidate() {
    doReturn(createNoAccessResult())
        .when(draftAccessService)
        .allowToCopyFromCandidate(any(AccessEvaluationParameters.class));
    final var actualResult = draftAccessServiceHelper.isAllowedToCopyFromCandidate(draft);
    assertFalse(actualResult);
  }

  @Test
  void shallAllowIfAllowAccessToCopyFromCandidatePassingAccessEvaluationParameters() {
    doReturn(createAccessResult())
        .when(draftAccessService)
        .allowToCopyFromCandidate(any(AccessEvaluationParameters.class));
    final var actualResult =
        draftAccessServiceHelper.isAllowedToCopyFromCandidate(
            mock(AccessEvaluationParameters.class));
    assertTrue(actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToCopyFromCandidatePassingAccessEvaluationParameters() {
    doReturn(createNoAccessResult())
        .when(draftAccessService)
        .allowToCopyFromCandidate(any(AccessEvaluationParameters.class));
    final var actualResult =
        draftAccessServiceHelper.isAllowedToCopyFromCandidate(
            mock(AccessEvaluationParameters.class));
    assertFalse(actualResult);
  }

  @Test
  void shallAllowToSignWithConfirmation() {
    final var accessEvaluationParameters = mock(AccessEvaluationParameters.class);
    doReturn(createAccessResult())
        .when(draftAccessService)
        .allowToSignWithConfirmation(accessEvaluationParameters);
    final var actualResult =
        draftAccessServiceHelper.isAllowToSignWithConfirmation(accessEvaluationParameters);
    assertTrue(actualResult);
  }

  private AccessResult createAccessResult() {
    return AccessResult.noProblem();
  }

  private AccessResult createNoAccessResult() {
    return AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION, "No Access");
  }
}
