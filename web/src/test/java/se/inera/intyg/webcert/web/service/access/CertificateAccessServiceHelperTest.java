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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.infra.security.authorities.AuthoritiesException;
import se.inera.intyg.webcert.web.web.util.access.AccessResultExceptionHelper;
import se.inera.intyg.webcert.web.web.util.access.AccessResultExceptionHelperImpl;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class CertificateAccessServiceHelperTest {

  @Mock private CertificateAccessService certificateAccessService;

  @Spy
  private final AccessResultExceptionHelper accessResultExceptionHelper =
      new AccessResultExceptionHelperImpl();

  @InjectMocks private CertificateAccessServiceHelper certificateAccessServiceHelper;

  private Utlatande certificate;

  @BeforeEach
  void setup() {
    certificate = mock(Utlatande.class);
    doReturn("certificateType").when(certificate).getTyp();
    doReturn("certificateTypeVersion").when(certificate).getTextVersion();
    doReturn(createGrundData()).when(certificate).getGrundData();
  }

  @Test
  void shallThrowExceptionIfNoAccessToRenew() {
    try {
      doReturn(createNoAccessResult())
          .when(certificateAccessService)
          .allowToRenew(any(AccessEvaluationParameters.class));
      certificateAccessServiceHelper.validateAccessToRenew(certificate);
      fail();
    } catch (AuthoritiesException ex) {
      assertTrue(true);
    }
  }

  @Test
  void shallNotThrowExeptionIfAllowAccessToRenew() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToRenew(any(AccessEvaluationParameters.class));
    certificateAccessServiceHelper.validateAccessToRenew(certificate);
    assertTrue(true);
  }

  @Test
  void shallAllowIfAllowAccessToRenew() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToRenew(any(AccessEvaluationParameters.class));
    final var actualResult = certificateAccessServiceHelper.isAllowToRenew(certificate);
    assertTrue(actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToRenew() {
    doReturn(createNoAccessResult())
        .when(certificateAccessService)
        .allowToRenew(any(AccessEvaluationParameters.class));
    final var actualResult = certificateAccessServiceHelper.isAllowToRenew(certificate);
    assertFalse(actualResult);
  }

  @Test
  void shallAllowIfAllowAccessToRenewPassingAccessEvaluationParameters() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToRenew(any(AccessEvaluationParameters.class));
    final var actualResult =
        certificateAccessServiceHelper.isAllowToRenew(mock(AccessEvaluationParameters.class));
    assertTrue(actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToRenewPassingAccessEvaluationParameters() {
    doReturn(createNoAccessResult())
        .when(certificateAccessService)
        .allowToRenew(any(AccessEvaluationParameters.class));
    final var actualResult =
        certificateAccessServiceHelper.isAllowToRenew(mock(AccessEvaluationParameters.class));
    assertFalse(actualResult);
  }

  @Test
  void shallThrowExceptionIfNoAccessToAnswerComplementQuestion() {
    try {
      doReturn(createNoAccessResult())
          .when(certificateAccessService)
          .allowToAnswerComplementQuestion(any(AccessEvaluationParameters.class), eq(true));
      certificateAccessServiceHelper.validateAccessToAnswerComplementQuestion(certificate, true);
      fail();
    } catch (AuthoritiesException ex) {
      assertTrue(true);
    }
  }

  @Test
  void shallNotThrowExeptionIfAllowAccessToAnswerComplementQuestion() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToAnswerComplementQuestion(any(AccessEvaluationParameters.class), eq(true));
    certificateAccessServiceHelper.validateAccessToAnswerComplementQuestion(certificate, true);
    assertTrue(true);
  }

  @Test
  void shallAllowIfAllowAccessToAnswerComplementQuestion() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToAnswerComplementQuestion(any(AccessEvaluationParameters.class), anyBoolean());
    final var actualResult =
        certificateAccessServiceHelper.isAllowToAnswerComplementQuestion(certificate, true);
    assertTrue(actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToAnswerComplementQuestion() {
    doReturn(createNoAccessResult())
        .when(certificateAccessService)
        .allowToAnswerComplementQuestion(any(AccessEvaluationParameters.class), anyBoolean());
    final var actualResult =
        certificateAccessServiceHelper.isAllowToAnswerComplementQuestion(certificate, true);
    assertFalse(actualResult);
  }

  @Test
  void shallAllowIfAllowAccessToAnswerComplementQuestionPassingAccessEvaluationParameters() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToAnswerComplementQuestion(any(AccessEvaluationParameters.class), anyBoolean());
    final var actualResult =
        certificateAccessServiceHelper.isAllowToAnswerComplementQuestion(
            mock(AccessEvaluationParameters.class), true);
    assertTrue(actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToAnswerComplementQuestionPassingAccessEvaluationParameters() {
    doReturn(createNoAccessResult())
        .when(certificateAccessService)
        .allowToAnswerComplementQuestion(any(AccessEvaluationParameters.class), anyBoolean());
    final var actualResult =
        certificateAccessServiceHelper.isAllowToAnswerComplementQuestion(
            mock(AccessEvaluationParameters.class), true);
    assertFalse(actualResult);
  }

  @Test
  void shallThrowExceptionIfNoAccessToReplace() {
    try {
      doReturn(createNoAccessResult())
          .when(certificateAccessService)
          .allowToReplace(any(AccessEvaluationParameters.class));
      certificateAccessServiceHelper.validateAccessToReplace(certificate);
      fail();
    } catch (AuthoritiesException ex) {
      assertTrue(true);
    }
  }

  @Test
  void shallNotThrowExeptionIfAllowAccessToReplace() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToReplace(any(AccessEvaluationParameters.class));
    certificateAccessServiceHelper.validateAccessToReplace(certificate);
    assertTrue(true);
  }

  @Test
  void shallAllowIfAllowAccessToReplace() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToReplace(any(AccessEvaluationParameters.class));
    final var actualResult = certificateAccessServiceHelper.isAllowToReplace(certificate);
    assertTrue(actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToReplace() {
    doReturn(createNoAccessResult())
        .when(certificateAccessService)
        .allowToReplace(any(AccessEvaluationParameters.class));
    final var actualResult = certificateAccessServiceHelper.isAllowToReplace(certificate);
    assertFalse(actualResult);
  }

  @Test
  void shallAllowIfAllowAccessToReplacePassingAccessEvaluationParameters() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToReplace(any(AccessEvaluationParameters.class));
    final var actualResult =
        certificateAccessServiceHelper.isAllowToReplace(mock(AccessEvaluationParameters.class));
    assertTrue(actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToReplacePassingAccessEvaluationParameters() {
    doReturn(createNoAccessResult())
        .when(certificateAccessService)
        .allowToReplace(any(AccessEvaluationParameters.class));
    final var actualResult =
        certificateAccessServiceHelper.isAllowToReplace(mock(AccessEvaluationParameters.class));
    assertFalse(actualResult);
  }

  @Test
  void shallThrowExceptionIfNoAccessToCreateDraftFromSignedTemplate() {
    try {
      doReturn(createNoAccessResult())
          .when(certificateAccessService)
          .allowToCreateDraftFromSignedTemplate(any(AccessEvaluationParameters.class));
      certificateAccessServiceHelper.validateAllowCreateDraftFromSignedTemplate(certificate);
      fail();
    } catch (AuthoritiesException ex) {
      assertTrue(true);
    }
  }

  @Test
  void shallNotThrowExeptionIfAllowAccessToCreateDraftFromSignedTemplate() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToCreateDraftFromSignedTemplate(any(AccessEvaluationParameters.class));
    certificateAccessServiceHelper.validateAllowCreateDraftFromSignedTemplate(certificate);
    assertTrue(true);
  }

  @Test
  void shallAllowIfAllowAccessToCreateDraftFromSignedTemplate() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToCreateDraftFromSignedTemplate(any(AccessEvaluationParameters.class));
    final var actualResult =
        certificateAccessServiceHelper.isAllowToCreateDraftFromSignedTemplate(certificate);
    assertTrue(actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToCreateDraftFromSignedTemplate() {
    doReturn(createNoAccessResult())
        .when(certificateAccessService)
        .allowToCreateDraftFromSignedTemplate(any(AccessEvaluationParameters.class));
    final var actualResult =
        certificateAccessServiceHelper.isAllowToCreateDraftFromSignedTemplate(certificate);
    assertFalse(actualResult);
  }

  @Test
  void shallAllowIfAllowAccessToCreateDraftFromSignedTemplatePassingAccessEvaluationParameters() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToCreateDraftFromSignedTemplate(any(AccessEvaluationParameters.class));
    final var actualResult =
        certificateAccessServiceHelper.isAllowToCreateDraftFromSignedTemplate(
            mock(AccessEvaluationParameters.class));
    assertTrue(actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToCreateDraftFromSignedTemplatePassingAccessEvaluationParameters() {
    doReturn(createNoAccessResult())
        .when(certificateAccessService)
        .allowToCreateDraftFromSignedTemplate(any(AccessEvaluationParameters.class));
    final var actualResult =
        certificateAccessServiceHelper.isAllowToCreateDraftFromSignedTemplate(
            mock(AccessEvaluationParameters.class));
    assertFalse(actualResult);
  }

  @Test
  void shallThrowExceptionIfNoAccessToInvalidate() {
    try {
      doReturn(createNoAccessResult())
          .when(certificateAccessService)
          .allowToInvalidate(any(AccessEvaluationParameters.class));
      certificateAccessServiceHelper.validateAccessToInvalidate(certificate);
      fail();
    } catch (AuthoritiesException ex) {
      assertTrue(true);
    }
  }

  @Test
  void shallNotThrowExeptionIfAllowAccessToInvalidate() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToInvalidate(any(AccessEvaluationParameters.class));
    certificateAccessServiceHelper.validateAccessToInvalidate(certificate);
    assertTrue(true);
  }

  @Test
  void shallAllowIfAllowAccessToInvalidate() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToInvalidate(any(AccessEvaluationParameters.class));
    final var actualResult = certificateAccessServiceHelper.isAllowToInvalidate(certificate);
    assertTrue(actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToInvalidate() {
    doReturn(createNoAccessResult())
        .when(certificateAccessService)
        .allowToInvalidate(any(AccessEvaluationParameters.class));
    final var actualResult = certificateAccessServiceHelper.isAllowToInvalidate(certificate);
    assertFalse(actualResult);
  }

  @Test
  void shallAllowIfAllowAccessToInvalidatePassingAccessEvaluationParameters() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToInvalidate(any(AccessEvaluationParameters.class));
    final var actualResult =
        certificateAccessServiceHelper.isAllowToInvalidate(mock(AccessEvaluationParameters.class));
    assertTrue(actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToInvalidatePassingAccessEvaluationParameters() {
    doReturn(createNoAccessResult())
        .when(certificateAccessService)
        .allowToInvalidate(any(AccessEvaluationParameters.class));
    final var actualResult =
        certificateAccessServiceHelper.isAllowToInvalidate(mock(AccessEvaluationParameters.class));
    assertFalse(actualResult);
  }

  @Test
  void shallThrowExceptionIfNoAccessToSend() {
    try {
      doReturn(createNoAccessResult())
          .when(certificateAccessService)
          .allowToSend(any(AccessEvaluationParameters.class));
      certificateAccessServiceHelper.validateAccessToSend(certificate);
      fail();
    } catch (AuthoritiesException ex) {
      assertTrue(true);
    }
  }

  @Test
  void shallNotThrowExeptionIfAllowAccessToSend() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToSend(any(AccessEvaluationParameters.class));
    certificateAccessServiceHelper.validateAccessToSend(certificate);
    assertTrue(true);
  }

  @Test
  void shallAllowIfAllowAccessToSend() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToSend(any(AccessEvaluationParameters.class));
    final var actualResult = certificateAccessServiceHelper.isAllowToSend(certificate);
    assertTrue(actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToSend() {
    doReturn(createNoAccessResult())
        .when(certificateAccessService)
        .allowToSend(any(AccessEvaluationParameters.class));
    final var actualResult = certificateAccessServiceHelper.isAllowToSend(certificate);
    assertFalse(actualResult);
  }

  @Test
  void shallAllowIfAllowAccessToSendPassingAccessEvaluationParameters() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToSend(any(AccessEvaluationParameters.class));
    final var actualResult =
        certificateAccessServiceHelper.isAllowToSend(mock(AccessEvaluationParameters.class));
    assertTrue(actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToSendPassingAccessEvaluationParameters() {
    doReturn(createNoAccessResult())
        .when(certificateAccessService)
        .allowToSend(any(AccessEvaluationParameters.class));
    final var actualResult =
        certificateAccessServiceHelper.isAllowToSend(mock(AccessEvaluationParameters.class));
    assertFalse(actualResult);
  }

  @Test
  void shallThrowExceptionIfNoAccessToApproveReceivers() {
    try {
      doReturn(createNoAccessResult())
          .when(certificateAccessService)
          .allowToApproveReceivers(any(AccessEvaluationParameters.class));
      certificateAccessServiceHelper.validateAccessToApproveReceivers(certificate);
      fail();
    } catch (AuthoritiesException ex) {
      assertTrue(true);
    }
  }

  @Test
  void shallNotThrowExeptionIfAllowAccessToApproveReceivers() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToApproveReceivers(any(AccessEvaluationParameters.class));
    certificateAccessServiceHelper.validateAccessToApproveReceivers(certificate);
    assertTrue(true);
  }

  @Test
  void shallAllowIfAllowAccessToApproveReceivers() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToApproveReceivers(any(AccessEvaluationParameters.class));
    final var actualResult = certificateAccessServiceHelper.isAllowToApproveReceivers(certificate);
    assertTrue(actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToApproveReceivers() {
    doReturn(createNoAccessResult())
        .when(certificateAccessService)
        .allowToApproveReceivers(any(AccessEvaluationParameters.class));
    final var actualResult = certificateAccessServiceHelper.isAllowToApproveReceivers(certificate);
    assertFalse(actualResult);
  }

  @Test
  void shallAllowIfAllowAccessToApproveReceiversPassingAccessEvaluationParameters() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToApproveReceivers(any(AccessEvaluationParameters.class));
    final var actualResult =
        certificateAccessServiceHelper.isAllowToApproveReceivers(
            mock(AccessEvaluationParameters.class));
    assertTrue(actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToApproveReceiversPassingAccessEvaluationParameters() {
    doReturn(createNoAccessResult())
        .when(certificateAccessService)
        .allowToApproveReceivers(any(AccessEvaluationParameters.class));
    final var actualResult =
        certificateAccessServiceHelper.isAllowToApproveReceivers(
            mock(AccessEvaluationParameters.class));
    assertFalse(actualResult);
  }

  @Test
  void shallThrowExceptionIfNoAccessToPrint() {
    try {
      doReturn(createNoAccessResult())
          .when(certificateAccessService)
          .allowToPrint(any(AccessEvaluationParameters.class), anyBoolean());
      certificateAccessServiceHelper.validateAccessToPrint(certificate, false);
      fail();
    } catch (AuthoritiesException ex) {
      assertTrue(true);
    }
  }

  @Test
  void shallNotThrowExeptionIfAllowAccessToPrint() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToPrint(any(AccessEvaluationParameters.class), anyBoolean());
    certificateAccessServiceHelper.validateAccessToPrint(certificate, false);
    assertTrue(true);
  }

  @Test
  void shallAllowIfAllowAccessToPrint() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToPrint(any(AccessEvaluationParameters.class), anyBoolean());
    final var actualResult = certificateAccessServiceHelper.isAllowToPrint(certificate, false);
    assertTrue(actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToPrint() {
    doReturn(createNoAccessResult())
        .when(certificateAccessService)
        .allowToPrint(any(AccessEvaluationParameters.class), anyBoolean());
    final var actualResult = certificateAccessServiceHelper.isAllowToPrint(certificate, false);
    assertFalse(actualResult);
  }

  @Test
  void shallAllowIfAllowAccessToPrintPassingAccessEvaluationParameters() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToPrint(any(AccessEvaluationParameters.class), anyBoolean());
    final var actualResult =
        certificateAccessServiceHelper.isAllowToPrint(
            mock(AccessEvaluationParameters.class), false);
    assertTrue(actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToPrintPassingAccessEvaluationParameters() {
    doReturn(createNoAccessResult())
        .when(certificateAccessService)
        .allowToPrint(any(AccessEvaluationParameters.class), anyBoolean());
    final var actualResult =
        certificateAccessServiceHelper.isAllowToPrint(
            mock(AccessEvaluationParameters.class), false);
    assertFalse(actualResult);
  }

  @Test
  void shallThrowExceptionIfNoAccessToRead() {
    try {
      doReturn(createNoAccessResult())
          .when(certificateAccessService)
          .allowToRead(any(AccessEvaluationParameters.class));
      certificateAccessServiceHelper.validateAccessToRead(certificate);
      fail();
    } catch (AuthoritiesException ex) {
      assertTrue(true);
    }
  }

  @Test
  void shallNotThrowExeptionIfAllowAccessToRead() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToRead(any(AccessEvaluationParameters.class));
    certificateAccessServiceHelper.validateAccessToRead(certificate);
    assertTrue(true);
  }

  @Test
  void shallAllowIfAllowAccessToRead() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToRead(any(AccessEvaluationParameters.class));
    final var actualResult = certificateAccessServiceHelper.isAllowToRead(certificate);
    assertTrue(actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToRead() {
    doReturn(createNoAccessResult())
        .when(certificateAccessService)
        .allowToRead(any(AccessEvaluationParameters.class));
    final var actualResult = certificateAccessServiceHelper.isAllowToRead(certificate);
    assertFalse(actualResult);
  }

  @Test
  void shallAllowIfAllowAccessToReadPassingAccessEvaluationParameters() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToRead(any(AccessEvaluationParameters.class));
    final var actualResult =
        certificateAccessServiceHelper.isAllowToRead(mock(AccessEvaluationParameters.class));
    assertTrue(actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToReadPassingAccessEvaluationParameters() {
    doReturn(createNoAccessResult())
        .when(certificateAccessService)
        .allowToRead(any(AccessEvaluationParameters.class));
    final var actualResult =
        certificateAccessServiceHelper.isAllowToRead(mock(AccessEvaluationParameters.class));
    assertFalse(actualResult);
  }

  @Test
  void shallThrowExceptionIfNoAccessToForwardQuestions() {
    try {
      doReturn(createNoAccessResult())
          .when(certificateAccessService)
          .allowToForwardQuestions(any(AccessEvaluationParameters.class));
      certificateAccessServiceHelper.validateAccessToForwardQuestions(certificate);
      fail();
    } catch (AuthoritiesException ex) {
      assertTrue(true);
    }
  }

  @Test
  void shallNotThrowExeptionIfAllowAccessToForwardQuestions() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToForwardQuestions(any(AccessEvaluationParameters.class));
    certificateAccessServiceHelper.validateAccessToForwardQuestions(certificate);
    assertTrue(true);
  }

  @Test
  void shallAllowIfAllowAccessToForwardQuestions() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToForwardQuestions(any(AccessEvaluationParameters.class));
    final var actualResult = certificateAccessServiceHelper.isAllowToForwardQuestions(certificate);
    assertTrue(actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToForwardQuestions() {
    doReturn(createNoAccessResult())
        .when(certificateAccessService)
        .allowToForwardQuestions(any(AccessEvaluationParameters.class));
    final var actualResult = certificateAccessServiceHelper.isAllowToForwardQuestions(certificate);
    assertFalse(actualResult);
  }

  @Test
  void shallAllowIfAllowAccessToForwardQuestionsPassingAccessEvaluationParameters() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToForwardQuestions(any(AccessEvaluationParameters.class));
    final var actualResult =
        certificateAccessServiceHelper.isAllowToForwardQuestions(
            mock(AccessEvaluationParameters.class));
    assertTrue(actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToForwardQuestionsPassingAccessEvaluationParameters() {
    doReturn(createNoAccessResult())
        .when(certificateAccessService)
        .allowToForwardQuestions(any(AccessEvaluationParameters.class));
    final var actualResult =
        certificateAccessServiceHelper.isAllowToForwardQuestions(
            mock(AccessEvaluationParameters.class));
    assertFalse(actualResult);
  }

  @Test
  void shallThrowExceptionIfNoAccessToCreateQuestion() {
    try {
      doReturn(createNoAccessResult())
          .when(certificateAccessService)
          .allowToCreateQuestion(any(AccessEvaluationParameters.class));
      certificateAccessServiceHelper.validateAccessToCreateQuestion(certificate);
      fail();
    } catch (AuthoritiesException ex) {
      assertTrue(true);
    }
  }

  @Test
  void shallNotThrowExeptionIfAllowAccessToCreateQuestion() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToCreateQuestion(any(AccessEvaluationParameters.class));
    certificateAccessServiceHelper.validateAccessToCreateQuestion(certificate);
    assertTrue(true);
  }

  @Test
  void shallAllowIfAllowAccessToCreateQuestion() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToCreateQuestion(any(AccessEvaluationParameters.class));
    final var actualResult = certificateAccessServiceHelper.isAllowToCreateQuestion(certificate);
    assertTrue(actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToCreateQuestion() {
    doReturn(createNoAccessResult())
        .when(certificateAccessService)
        .allowToCreateQuestion(any(AccessEvaluationParameters.class));
    final var actualResult = certificateAccessServiceHelper.isAllowToCreateQuestion(certificate);
    assertFalse(actualResult);
  }

  @Test
  void shallAllowIfAllowAccessToCreateQuestionPassingAccessEvaluationParameters() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToCreateQuestion(any(AccessEvaluationParameters.class));
    final var actualResult =
        certificateAccessServiceHelper.isAllowToCreateQuestion(
            mock(AccessEvaluationParameters.class));
    assertTrue(actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToCreateQuestionPassingAccessEvaluationParameters() {
    doReturn(createNoAccessResult())
        .when(certificateAccessService)
        .allowToCreateQuestion(any(AccessEvaluationParameters.class));
    final var actualResult =
        certificateAccessServiceHelper.isAllowToCreateQuestion(
            mock(AccessEvaluationParameters.class));
    assertFalse(actualResult);
  }

  @Test
  void shallThrowExceptionIfNoAccessToAnswerAdminQuestion() {
    try {
      doReturn(createNoAccessResult())
          .when(certificateAccessService)
          .allowToAnswerAdminQuestion(any(AccessEvaluationParameters.class));
      certificateAccessServiceHelper.validateAccessToAnswerAdminQuestion(certificate);
      fail();
    } catch (AuthoritiesException ex) {
      assertTrue(true);
    }
  }

  @Test
  void shallNotThrowExeptionIfAllowAccessToAnswerAdminQuestion() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToAnswerAdminQuestion(any(AccessEvaluationParameters.class));
    certificateAccessServiceHelper.validateAccessToAnswerAdminQuestion(certificate);
    assertTrue(true);
  }

  @Test
  void shallAllowIfAllowAccessToAnswerAdminQuestion() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToAnswerAdminQuestion(any(AccessEvaluationParameters.class));
    final var actualResult =
        certificateAccessServiceHelper.isAllowToAnswerAdminQuestion(certificate);
    assertTrue(actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToAnswerAdminQuestion() {
    doReturn(createNoAccessResult())
        .when(certificateAccessService)
        .allowToAnswerAdminQuestion(any(AccessEvaluationParameters.class));
    final var actualResult =
        certificateAccessServiceHelper.isAllowToAnswerAdminQuestion(certificate);
    assertFalse(actualResult);
  }

  @Test
  void shallAllowIfAllowAccessToAnswerAdminQuestionPassingAccessEvaluationParameters() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToAnswerAdminQuestion(any(AccessEvaluationParameters.class));
    final var actualResult =
        certificateAccessServiceHelper.isAllowToAnswerAdminQuestion(
            mock(AccessEvaluationParameters.class));
    assertTrue(actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToAnswerAdminQuestionPassingAccessEvaluationParameters() {
    doReturn(createNoAccessResult())
        .when(certificateAccessService)
        .allowToAnswerAdminQuestion(any(AccessEvaluationParameters.class));
    final var actualResult =
        certificateAccessServiceHelper.isAllowToAnswerAdminQuestion(
            mock(AccessEvaluationParameters.class));
    assertFalse(actualResult);
  }

  @Test
  void shallThrowExceptionIfNoAccessToReadQuestions() {
    try {
      doReturn(createNoAccessResult())
          .when(certificateAccessService)
          .allowToReadQuestions(any(AccessEvaluationParameters.class));
      certificateAccessServiceHelper.validateAccessToReadQuestions(certificate);
      fail();
    } catch (AuthoritiesException ex) {
      assertTrue(true);
    }
  }

  @Test
  void shallNotThrowExeptionIfAllowAccessToReadQuestions() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToReadQuestions(any(AccessEvaluationParameters.class));
    certificateAccessServiceHelper.validateAccessToReadQuestions(certificate);
    assertTrue(true);
  }

  @Test
  void shallAllowIfAllowAccessToReadQuestions() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToReadQuestions(any(AccessEvaluationParameters.class));
    final var actualResult = certificateAccessServiceHelper.isAllowToReadQuestions(certificate);
    assertTrue(actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToReadQuestions() {
    doReturn(createNoAccessResult())
        .when(certificateAccessService)
        .allowToReadQuestions(any(AccessEvaluationParameters.class));
    final var actualResult = certificateAccessServiceHelper.isAllowToReadQuestions(certificate);
    assertFalse(actualResult);
  }

  @Test
  void shallAllowIfAllowAccessToReadQuestionsPassingAccessEvaluationParameters() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToReadQuestions(any(AccessEvaluationParameters.class));
    final var actualResult =
        certificateAccessServiceHelper.isAllowToReadQuestions(
            mock(AccessEvaluationParameters.class));
    assertTrue(actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToReadQuestionsPassingAccessEvaluationParameters() {
    doReturn(createNoAccessResult())
        .when(certificateAccessService)
        .allowToReadQuestions(any(AccessEvaluationParameters.class));
    final var actualResult =
        certificateAccessServiceHelper.isAllowToReadQuestions(
            mock(AccessEvaluationParameters.class));
    assertFalse(actualResult);
  }

  @Test
  void shallThrowExceptionIfNoAccessToSetComplementAsHandled() {
    try {
      doReturn(createNoAccessResult())
          .when(certificateAccessService)
          .allowToSetComplementAsHandled(any(AccessEvaluationParameters.class));
      certificateAccessServiceHelper.validateAccessToSetComplementAsHandled(certificate);
      fail();
    } catch (AuthoritiesException ex) {
      assertTrue(true);
    }
  }

  @Test
  void shallNotThrowExeptionIfAllowAccessToSetComplementAsHandled() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToSetComplementAsHandled(any(AccessEvaluationParameters.class));
    certificateAccessServiceHelper.validateAccessToSetComplementAsHandled(certificate);
    assertTrue(true);
  }

  @Test
  void shallAllowIfAllowAccessToSetComplementAsHandled() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToSetComplementAsHandled(any(AccessEvaluationParameters.class));
    final var actualResult =
        certificateAccessServiceHelper.isAllowToSetComplementAsHandled(certificate);
    assertTrue(actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToSetComplementAsHandled() {
    doReturn(createNoAccessResult())
        .when(certificateAccessService)
        .allowToSetComplementAsHandled(any(AccessEvaluationParameters.class));
    final var actualResult =
        certificateAccessServiceHelper.isAllowToSetComplementAsHandled(certificate);
    assertFalse(actualResult);
  }

  @Test
  void shallAllowIfAllowAccessToSetComplementAsHandledPassingAccessEvaluationParameters() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToSetComplementAsHandled(any(AccessEvaluationParameters.class));
    final var actualResult =
        certificateAccessServiceHelper.isAllowToSetComplementAsHandled(
            mock(AccessEvaluationParameters.class));
    assertTrue(actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToSetComplementAsHandledPassingAccessEvaluationParameters() {
    doReturn(createNoAccessResult())
        .when(certificateAccessService)
        .allowToSetComplementAsHandled(any(AccessEvaluationParameters.class));
    final var actualResult =
        certificateAccessServiceHelper.isAllowToSetComplementAsHandled(
            mock(AccessEvaluationParameters.class));
    assertFalse(actualResult);
  }

  @Test
  void shallThrowExceptionIfNoAccessToSetQuestionAsHandled() {
    try {
      doReturn(createNoAccessResult())
          .when(certificateAccessService)
          .allowToSetQuestionAsHandled(any(AccessEvaluationParameters.class));
      certificateAccessServiceHelper.validateAccessToSetQuestionAsHandled(certificate);
      fail();
    } catch (AuthoritiesException ex) {
      assertTrue(true);
    }
  }

  @Test
  void shallNotThrowExeptionIfAllowAccessToSetQuestionAsHandled() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToSetQuestionAsHandled(any(AccessEvaluationParameters.class));
    certificateAccessServiceHelper.validateAccessToSetQuestionAsHandled(certificate);
    assertTrue(true);
  }

  @Test
  void shallAllowIfAllowAccessToSetQuestionAsHandled() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToSetQuestionAsHandled(any(AccessEvaluationParameters.class));
    final var actualResult =
        certificateAccessServiceHelper.isAllowToSetQuestionAsHandled(certificate);
    assertTrue(actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToSetQuestionAsHandled() {
    doReturn(createNoAccessResult())
        .when(certificateAccessService)
        .allowToSetQuestionAsHandled(any(AccessEvaluationParameters.class));
    final var actualResult =
        certificateAccessServiceHelper.isAllowToSetQuestionAsHandled(certificate);
    assertFalse(actualResult);
  }

  @Test
  void shallAllowIfAllowAccessToSetQuestionAsHandledPassingAccessEvaluationParameters() {
    doReturn(createAccessResult())
        .when(certificateAccessService)
        .allowToSetQuestionAsHandled(any(AccessEvaluationParameters.class));
    final var actualResult =
        certificateAccessServiceHelper.isAllowToSetQuestionAsHandled(
            mock(AccessEvaluationParameters.class));
    assertTrue(actualResult);
  }

  @Test
  void shallNotAllowIfNoAccessToSetQuestionAsHandledPassingAccessEvaluationParameters() {
    doReturn(createNoAccessResult())
        .when(certificateAccessService)
        .allowToSetQuestionAsHandled(any(AccessEvaluationParameters.class));
    final var actualResult =
        certificateAccessServiceHelper.isAllowToSetQuestionAsHandled(
            mock(AccessEvaluationParameters.class));
    assertFalse(actualResult);
  }

  private AccessResult createAccessResult() {
    return AccessResult.noProblem();
  }

  private AccessResult createNoAccessResult() {
    return AccessResult.create(AccessResultCode.AUTHORIZATION_VALIDATION, "No Access");
  }

  private GrundData createGrundData() {
    final var patient = new Patient();
    patient.setPersonId(Personnummer.createPersonnummer("191212121212").get());
    final var vardenhet = new Vardenhet();
    final var skapadAv = new HoSPersonal();
    skapadAv.setVardenhet(vardenhet);
    final var grundData = new GrundData();
    grundData.setPatient(patient);
    grundData.setSkapadAv(skapadAv);
    return grundData;
  }
}
