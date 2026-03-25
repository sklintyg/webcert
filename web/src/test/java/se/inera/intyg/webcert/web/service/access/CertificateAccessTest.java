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

import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.web.service.access.data.AccessServiceTestData;
import se.inera.intyg.webcert.web.service.access.util.AccessServiceTestToolkit;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public abstract class CertificateAccessTest extends AccessTest {

  @InjectMocks private CertificateAccessServiceImpl accessService;

  private AccessEvaluationParameters accessEvaluationParameters;

  CertificateAccessTest(String intygsTyp, AccessServiceTestData accessServiceTestData) {
    super(intygsTyp, accessServiceTestData);
  }

  @BeforeEach
  void initMocks() {
    accessEvaluationParameters =
        AccessEvaluationParameters.create(
            intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false);
  }

  @Test
  void isAllowToReadNoConditions() {
    setupMocksForNoConditions();

    assertAllowToReadNoConditions(accessService.allowToRead(accessEvaluationParameters));
  }

  protected abstract void assertAllowToReadNoConditions(AccessResult actualValue);

  @Test
  void isAllowToReadNotLatestMajorVersion() {
    setupMocksForNotLatestMajorVersion();

    assertAllowToReadNotLatestMajorVersion(accessService.allowToRead(accessEvaluationParameters));
  }

  protected abstract void assertAllowToReadNotLatestMajorVersion(AccessResult actualValue);

  @Test
  void isAllowToReadWhenMissingSubscription() {
    setupMocksForMissingSubscription();

    assertAllowToReadWhenMissingSubscription(accessService.allowToRead(accessEvaluationParameters));
  }

  protected abstract void assertAllowToReadWhenMissingSubscription(AccessResult actualValue);

  @Test
  void isAllowToReadOnDeceasedPatient() {
    setupMocksForDeceasedPatient();

    assertAllowToReadOnDeceasedPatient(accessService.allowToRead(accessEvaluationParameters));
  }

  protected abstract void assertAllowToReadOnDeceasedPatient(AccessResult actualValue);

  @Test
  void isAllowToReadOnInactiveUnit() {
    setupMocksForInactiveUnit();

    assertAllowToReadOnInactiveUnit(accessService.allowToRead(accessEvaluationParameters));
  }

  protected abstract void assertAllowToReadOnInactiveUnit(AccessResult actualValue);

  @Test
  void isAllowToReadOnRenewFalse() {
    setupMocksForOnRenewFalse();

    assertAllowToReadOnRenewFalse(accessService.allowToRead(accessEvaluationParameters));
  }

  protected abstract void assertAllowToReadOnRenewFalse(AccessResult actualValue);

  @Test
  void isAllowToReadOnSekretessPatientOnSameUnit() {
    setupMocksForOnSekretessPatient();

    assertAllowToReadOnSekretessPatientOnSameUnit(
        accessService.allowToRead(accessEvaluationParameters));
  }

  protected abstract void assertAllowToReadOnSekretessPatientOnSameUnit(AccessResult actualValue);

  @Test
  void isAllowToReadOnSekretessPatientOnDifferentUnit() {
    setupMocksForOnSekretessPatientDifferentUnit();

    assertAllowToReadOnSekretessPatientOnDifferentUnit(
        accessService.allowToRead(accessEvaluationParameters));
  }

  protected abstract void assertAllowToReadOnSekretessPatientOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToReadNoConditionsOnDifferentUnit() {
    setupMocksForNoConditionsDifferentUnit();

    assertAllowToReadNoConditionsOnDifferentUnit(
        accessService.allowToRead(accessEvaluationParameters));
  }

  protected abstract void assertAllowToReadNoConditionsOnDifferentUnit(AccessResult actualValue);

  @Test
  void isAllowToReadOnDeceasedPatientOnDifferentUnit() {
    setupMocksForDeceasedPatientDifferentUnit();

    assertAllowToReadOnDeceasedPatientOnDifferentUnit(
        accessService.allowToRead(accessEvaluationParameters));
  }

  protected abstract void assertAllowToReadOnDeceasedPatientOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToReadOnInactiveUnitOnDifferentUnit() {
    setupMocksForInactiveUnitDifferentUnit();

    assertAllowToReadOnInactiveUnitOnDifferentUnit(
        accessService.allowToRead(accessEvaluationParameters));
  }

  protected abstract void assertAllowToReadOnInactiveUnitOnDifferentUnit(AccessResult actualValue);

  @Test
  void isAllowToReadOnRenewFalseOnDifferentUnit() {
    setupMocksForOnRenewFalseDifferentUnit();

    assertAllowToReadOnRenewFalseOnDifferentUnit(
        accessService.allowToRead(accessEvaluationParameters));
  }

  protected abstract void assertAllowToReadOnRenewFalseOnDifferentUnit(AccessResult actualValue);

  @Test
  void isAllowToReplaceNoConditions() {
    setupMocksForNoConditions();

    assertAllowToReplaceNoConditions(accessService.allowToReplace(accessEvaluationParameters));
  }

  protected abstract void assertAllowToReplaceNoConditions(AccessResult actualValue);

  @Test
  void isAllowToReplaceWhenMissingSubscription() {
    setupMocksForMissingSubscription();

    assertAllowToReplaceWhenMissingSubscription(
        accessService.allowToReplace(accessEvaluationParameters));
  }

  protected abstract void assertAllowToReplaceWhenMissingSubscription(AccessResult actualValue);

  @Test
  void isAllowToReplaceNotLatestMajorVersion() {
    setupMocksForNotLatestMajorVersion();

    assertAllowToReplaceNotLatestMajorVersion(
        accessService.allowToReplace(accessEvaluationParameters));
  }

  protected abstract void assertAllowToReplaceNotLatestMajorVersion(AccessResult actualValue);

  @Test
  void isAllowToReplaceOnDeceasedPatient() {
    setupMocksForDeceasedPatient();

    assertAllowToReplaceOnDeceasedPatient(accessService.allowToReplace(accessEvaluationParameters));
  }

  protected abstract void assertAllowToReplaceOnDeceasedPatient(AccessResult actualValue);

  @Test
  void isAllowToReplaceOnInactiveUnit() {
    setupMocksForInactiveUnit();

    assertAllowToReplaceOnInactiveCareUnit(
        accessService.allowToReplace(accessEvaluationParameters));
  }

  protected abstract void assertAllowToReplaceOnInactiveCareUnit(AccessResult actualValue);

  @Test
  void isAllowToReplaceOnRenewFalse() {
    setupMocksForOnRenewFalse();

    assertAllowToReplaceOnRenewFalse(accessService.allowToReplace(accessEvaluationParameters));
  }

  protected abstract void assertAllowToReplaceOnRenewFalse(AccessResult actualValue);

  @Test
  void isAllowToReplaceNoConditionsDifferentUnit() {
    setupMocksForNoConditionsDifferentUnit();

    assertAllowToReplaceNoConditionsDifferentUnit(
        accessService.allowToReplace(accessEvaluationParameters));
  }

  protected abstract void assertAllowToReplaceNoConditionsDifferentUnit(AccessResult actualValue);

  @Test
  void isAllowToReplaceNotLatestMajorVersionDifferentUnit() {
    setupMocksForNotLatestMajorVersionDifferentUnit();

    assertAllowToReplaceNotLatestMajorVersionDifferentUnit(
        accessService.allowToReplace(accessEvaluationParameters));
  }

  protected abstract void assertAllowToReplaceNotLatestMajorVersionDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToReplaceOnDeceasedPatientDifferentUnit() {
    setupMocksForDeceasedPatientDifferentUnit();

    assertAllowToReplaceOnDeceasedPatientDifferentUnit(
        accessService.allowToReplace(accessEvaluationParameters));
  }

  protected abstract void assertAllowToReplaceOnDeceasedPatientDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToReplaceOnInactiveUnitDifferentUnit() {
    setupMocksForInactiveUnitDifferentUnit();

    assertAllowToReplaceOnInactiveCareUnitDifferentUnit(
        accessService.allowToReplace(accessEvaluationParameters));
  }

  protected abstract void assertAllowToReplaceOnInactiveCareUnitDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToReplaceOnRenewFalseDifferentUnit() {
    setupMocksForOnRenewFalseDifferentUnit();

    assertAllowToReplaceOnRenewFalseDifferentUnit(
        accessService.allowToReplace(accessEvaluationParameters));
  }

  protected abstract void assertAllowToReplaceOnRenewFalseDifferentUnit(AccessResult actualValue);

  @Test
  void isAllowToReplaceOnSekretessPatientOnSameUnit() {
    setupMocksForOnSekretessPatient();

    assertAllowToReplaceOnSekretessPatientOnSameUnit(
        accessService.allowToReplace(accessEvaluationParameters));
  }

  protected abstract void assertAllowToReplaceOnSekretessPatientOnSameUnit(
      AccessResult actualValue);

  @Test
  void isAllowToReplaceOnSekretessPatientOnDifferentUnit() {
    setupMocksForOnSekretessPatientDifferentUnit();

    assertAllowToReplaceOnSekretessPatientOnDifferentUnit(
        accessService.allowToReplace(accessEvaluationParameters));
  }

  protected abstract void assertAllowToReplaceOnSekretessPatientOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToRenewNoConditions() {
    setupMocksForNoConditions();

    assertAllowToRenewNoConditions(accessService.allowToRenew(accessEvaluationParameters));
  }

  protected abstract void assertAllowToRenewNoConditions(AccessResult actualValue);

  @Test
  void isAllowToRenewWhenMissingSubscription() {
    setupMocksForMissingSubscription();

    assertAllowToRenewWhenMissingSubscription(
        accessService.allowToRenew(accessEvaluationParameters));
  }

  protected abstract void assertAllowToRenewWhenMissingSubscription(AccessResult actualValue);

  @Test
  void isAllowToRenewNotLatestMajorVersion() {
    setupMocksForNotLatestMajorVersion();

    assertAllowToRenewNotLatestMajorVersion(accessService.allowToRenew(accessEvaluationParameters));
  }

  protected abstract void assertAllowToRenewNotLatestMajorVersion(AccessResult actualValue);

  @Test
  void isAllowToRenewOnDeceasedPatient() {
    setupMocksForDeceasedPatient();

    assertAllowToRenewOnDeceasedPatient(accessService.allowToRenew(accessEvaluationParameters));
  }

  protected abstract void assertAllowToRenewOnDeceasedPatient(AccessResult actualValue);

  @Test
  void isAllowToRenewOnInactiveUnit() {
    setupMocksForInactiveUnit();

    assertAllowToRenewOnInactiveCareUnit(accessService.allowToRenew(accessEvaluationParameters));
  }

  protected abstract void assertAllowToRenewOnInactiveCareUnit(AccessResult actualValue);

  @Test
  void isAllowToRenewOnRenewFalse() {
    setupMocksForOnRenewFalse();

    assertAllowToRenewOnRenewFalse(accessService.allowToRenew(accessEvaluationParameters));
  }

  protected abstract void assertAllowToRenewOnRenewFalse(AccessResult actualValue);

  @Test
  void isAllowToRenewOnSameCareProviderWhenSameVGExists() {
    final WebCertUser user = AccessServiceTestToolkit.createUser(intygsTyp, accessServiceTestData);
    doReturn(user).when(webCertUserService).getUser();
    // TODO Manage this
    // doReturn(true)
    // .when(patientDetailsResolver).isAvliden(PERSONNUMMER);
    doReturn(false).when(patientDetailsResolver).isAvliden(PERSONNUMMER);
    doReturn(SekretessStatus.FALSE).when(patientDetailsResolver).getSekretessStatus(PERSONNUMMER);
    doReturn(AccessServiceTestToolkit.createPreviousUtkastForUtkast(intygsTyp))
        .when(utkastService)
        .checkIfPersonHasExistingIntyg(PERSONNUMMER, user, null);

    assertAllowToRenewOnSameCareProviderWhenSameVGExists(
        accessService.allowToRenew(accessEvaluationParameters));
  }

  protected abstract void assertAllowToRenewOnSameCareProviderWhenSameVGExists(
      AccessResult actualValue);

  @Test
  void isAllowToRenewOnDifferentCareProviderWhenIntygSameVGExists() {
    final WebCertUser user = AccessServiceTestToolkit.createUser(intygsTyp, accessServiceTestData);
    doReturn(user).when(webCertUserService).getUser();
    // TODO Manage this
    // doReturn(true)
    // .when(patientDetailsResolver).isAvliden(PERSONNUMMER);
    doReturn(false).when(patientDetailsResolver).isAvliden(PERSONNUMMER);
    doReturn(SekretessStatus.FALSE).when(patientDetailsResolver).getSekretessStatus(PERSONNUMMER);
    doReturn(AccessServiceTestToolkit.createPreviousIntygForUtkast(intygsTyp, true))
        .when(utkastService)
        .checkIfPersonHasExistingIntyg(PERSONNUMMER, user, null);

    assertAllowToRenewOnDifferentCareProviderWhenIntygSameVGExists(
        accessService.allowToRenew(accessEvaluationParameters));
  }

  protected abstract void assertAllowToRenewOnDifferentCareProviderWhenIntygSameVGExists(
      AccessResult actualValue);

  @Test
  void isAllowToRenewOnSameCareProviderWhenIntygExists() {
    final WebCertUser user = AccessServiceTestToolkit.createUser(intygsTyp, accessServiceTestData);
    doReturn(user).when(webCertUserService).getUser();
    // TODO Manage this
    // doReturn(true)
    // .when(patientDetailsResolver).isAvliden(PERSONNUMMER);
    doReturn(false).when(patientDetailsResolver).isAvliden(PERSONNUMMER);
    doReturn(SekretessStatus.FALSE).when(patientDetailsResolver).getSekretessStatus(PERSONNUMMER);
    doReturn(AccessServiceTestToolkit.createPreviousIntygForUtkast(intygsTyp, false))
        .when(utkastService)
        .checkIfPersonHasExistingIntyg(PERSONNUMMER, user, null);

    assertAllowToRenewOnSameCareProviderWhenIntygExists(
        accessService.allowToRenew(accessEvaluationParameters));
  }

  protected abstract void assertAllowToRenewOnSameCareProviderWhenIntygExists(
      AccessResult actualValue);

  @Test
  void isAllowToRenewNoConditionsDifferentUnit() {
    setupMocksForNoConditionsDifferentUnit();

    assertAllowToRenewNoConditionsDifferentUnit(
        accessService.allowToRenew(accessEvaluationParameters));
  }

  protected abstract void assertAllowToRenewNoConditionsDifferentUnit(AccessResult actualValue);

  @Test
  void isAllowToRenewOnDeceasedPatientDifferentUnit() {
    setupMocksForDeceasedPatientDifferentUnit();

    assertAllowToRenewOnDeceasedPatientDifferentUnit(
        accessService.allowToRenew(accessEvaluationParameters));
  }

  protected abstract void assertAllowToRenewOnDeceasedPatientDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToRenewOnInactiveUnitDifferentUnit() {
    setupMocksForInactiveUnitDifferentUnit();

    assertAllowToRenewOnInactiveCareUnitDifferentUnit(
        accessService.allowToRenew(accessEvaluationParameters));
  }

  protected abstract void assertAllowToRenewOnInactiveCareUnitDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToRenewOnRenewFalseDifferentUnit() {
    setupMocksForOnRenewFalseDifferentUnit();

    assertAllowToRenewOnRenewFalseDifferentUnit(
        accessService.allowToRenew(accessEvaluationParameters));
  }

  protected abstract void assertAllowToRenewOnRenewFalseDifferentUnit(AccessResult actualValue);

  @Test
  void isAllowToRenewOnSekretessPatientOnSameUnit() {
    setupMocksForOnSekretessPatient();

    assertAllowToRenewOnSekretessPatientOnSameUnit(
        accessService.allowToRenew(accessEvaluationParameters));
  }

  protected abstract void assertAllowToRenewOnSekretessPatientOnSameUnit(AccessResult actualValue);

  @Test
  void isAllowToRenewOnSekretessPatientOnDifferentUnit() {
    setupMocksForOnSekretessPatientDifferentUnit();

    assertAllowToRenewOnSekretessPatientOnDifferentUnit(
        accessService.allowToRenew(accessEvaluationParameters));
  }

  protected abstract void assertAllowToRenewOnSekretessPatientOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToDeleteNoConditions() {
    setupMocksForNoConditions();

    assertAllowToDeleteNoConditions(accessService.allowToInvalidate(accessEvaluationParameters));
  }

  protected abstract void assertAllowToDeleteNoConditions(AccessResult actualValue);

  @Test
  void isAllowToDeleteWhenNoSubscription() {
    setupMocksForMissingSubscription();

    assertAllowToDeleteWhenNoSubscription(
        accessService.allowToInvalidate(accessEvaluationParameters));
  }

  protected abstract void assertAllowToDeleteWhenNoSubscription(AccessResult actualValue);

  @Test
  void isAllowToDeleteNotLatestMajorVersion() {
    setupMocksForNotLatestMajorVersion();

    assertAllowToDeleteNotLatestMajorVersion(
        accessService.allowToInvalidate(accessEvaluationParameters));
  }

  protected abstract void assertAllowToDeleteNotLatestMajorVersion(AccessResult actualValue);

  @Test
  void isAllowToDeleteOnDeceasedPatient() {
    setupMocksForDeceasedPatient();

    assertAllowToDeleteOnDeceasedPatient(
        accessService.allowToInvalidate(accessEvaluationParameters));
  }

  protected abstract void assertAllowToDeleteOnDeceasedPatient(AccessResult actualValue);

  @Test
  void isAllowToDeleteOnInactiveUnit() {
    setupMocksForInactiveUnit();

    assertAllowToDeleteOnInactiveUnit(accessService.allowToInvalidate(accessEvaluationParameters));
  }

  protected abstract void assertAllowToDeleteOnInactiveUnit(AccessResult actualValue);

  @Test
  void isAllowToDeleteOnRenewFalse() {
    setupMocksForOnRenewFalse();

    assertAllowToDeleteOnRenewFalse(accessService.allowToInvalidate(accessEvaluationParameters));
  }

  protected abstract void assertAllowToDeleteOnRenewFalse(AccessResult actualValue);

  @Test
  void isAllowToDeleteOnSekretessPatientOnSameUnit() {
    setupMocksForOnSekretessPatient();

    assertAllowToDeleteOnSekretessPatientOnSameUnit(
        accessService.allowToInvalidate(accessEvaluationParameters));
  }

  protected abstract void assertAllowToDeleteOnSekretessPatientOnSameUnit(AccessResult actualValue);

  @Test
  void isAllowToDeleteOnSekretessPatientOnDifferentUnit() {
    setupMocksForOnSekretessPatientDifferentUnit();

    assertAllowToDeleteOnSekretessPatientOnDifferentUnit(
        accessService.allowToInvalidate(accessEvaluationParameters));
  }

  protected abstract void assertAllowToDeleteOnSekretessPatientOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToDeleteOnDeceasedPatientOnDifferentUnit() {
    setupMocksForDeceasedPatientDifferentUnit();

    assertAllowToDeleteOnDeceasedPatientOnDifferentUnit(
        accessService.allowToInvalidate(accessEvaluationParameters));
  }

  protected abstract void assertAllowToDeleteOnDeceasedPatientOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToDeleteOnInactiveUnitOnDifferentUnit() {
    setupMocksForInactiveUnitDifferentUnit();

    assertAllowToDeleteOnInactiveUnitOnDifferentUnit(
        accessService.allowToInvalidate(accessEvaluationParameters));
  }

  protected abstract void assertAllowToDeleteOnInactiveUnitOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToDeleteOnRenewFalseOnDifferentUnit() {
    setupMocksForOnRenewFalseDifferentUnit();

    assertAllowToDeleteOnRenewFalseOnDifferentUnit(
        accessService.allowToInvalidate(accessEvaluationParameters));
  }

  protected abstract void assertAllowToDeleteOnRenewFalseOnDifferentUnit(AccessResult actualValue);

  @Test
  void isAllowToDeleteNoConditionsDifferentUnit() {
    setupMocksForNoConditionsDifferentUnit();

    assertAllowToDeleteNoConditionsDifferentUnit(
        accessService.allowToInvalidate(accessEvaluationParameters));
  }

  protected abstract void assertAllowToDeleteNoConditionsDifferentUnit(AccessResult actualValue);

  @Test
  void isAllowToPrintNoConditions() {
    setupMocksForNoConditions();

    assertAllowToPrintNoConditions(
        accessService.allowToPrint(accessEvaluationParameters, isEmployer));
  }

  protected abstract void assertAllowToPrintNoConditions(AccessResult actualValue);

  @Test
  void isAllowToPrintWhenMissingSubscription() {
    setupMocksForNoConditions();

    assertAllowToPrintWhenMissingSubscription(
        accessService.allowToPrint(accessEvaluationParameters, isEmployer));
  }

  protected abstract void assertAllowToPrintWhenMissingSubscription(AccessResult actualValue);

  @Test
  void isAllowToPrintNotLatestMajorVersion() {
    setupMocksForNotLatestMajorVersion();

    assertAllowToPrintNotLatestMajorVersion(
        accessService.allowToPrint(accessEvaluationParameters, isEmployer));
  }

  protected abstract void assertAllowToPrintNotLatestMajorVersion(AccessResult actualValue);

  @Test
  void isAllowToPrintOnDeceasedPatient() {
    setupMocksForDeceasedPatient();

    assertAllowToPrintOnDeceasedPatient(
        accessService.allowToPrint(accessEvaluationParameters, isEmployer));
  }

  protected abstract void assertAllowToPrintOnDeceasedPatient(AccessResult actualValue);

  @Test
  void isAllowToPrintOnInactiveUnit() {
    setupMocksForInactiveUnit();

    assertAllowToPrintOnInactiveUnit(
        accessService.allowToPrint(accessEvaluationParameters, isEmployer));
  }

  protected abstract void assertAllowToPrintOnInactiveUnit(AccessResult actualValue);

  @Test
  void isAllowToPrintOnRenewFalse() {
    setupMocksForOnRenewFalse();

    assertAllowToPrintOnRenewFalse(
        accessService.allowToPrint(accessEvaluationParameters, isEmployer));
  }

  protected abstract void assertAllowToPrintOnRenewFalse(AccessResult actualValue);

  @Test
  void isAllowToPrintOnSekretessPatientOnSameUnit() {
    setupMocksForOnSekretessPatient();

    assertAllowToPrintOnSekretessPatientOnSameUnit(
        accessService.allowToPrint(accessEvaluationParameters, isEmployer));
  }

  protected abstract void assertAllowToPrintOnSekretessPatientOnSameUnit(AccessResult actualValue);

  @Test
  void isAllowToPrintOnSekretessPatientOnDifferentUnit() {
    setupMocksForOnSekretessPatientDifferentUnit();

    assertAllowToPrintOnSekretessPatientOnDifferentUnit(
        accessService.allowToPrint(accessEvaluationParameters, isEmployer));
  }

  protected abstract void assertAllowToPrintOnSekretessPatientOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToPrintOnDeceasedPatientOnDifferentUnit() {
    setupMocksForDeceasedPatientDifferentUnit();

    assertAllowToPrintOnDeceasedPatientOnDifferentUnit(
        accessService.allowToPrint(accessEvaluationParameters, isEmployer));
  }

  protected abstract void assertAllowToPrintOnDeceasedPatientOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToPrintOnInactiveUnitOnDifferentUnit() {
    setupMocksForInactiveUnitDifferentUnit();

    assertAllowToPrintOnInactiveUnitOnDifferentUnit(
        accessService.allowToPrint(accessEvaluationParameters, isEmployer));
  }

  protected abstract void assertAllowToPrintOnInactiveUnitOnDifferentUnit(AccessResult actualValue);

  @Test
  void isAllowToPrintOnRenewFalseOnDifferentUnit() {
    setupMocksForOnRenewFalseDifferentUnit();

    assertAllowToPrintOnRenewFalseOnDifferentUnit(
        accessService.allowToPrint(accessEvaluationParameters, isEmployer));
  }

  protected abstract void assertAllowToPrintOnRenewFalseOnDifferentUnit(AccessResult actualValue);

  @Test
  void isAllowToPrintNoConditionsDifferentUnit() {
    setupMocksForNoConditionsDifferentUnit();

    assertAllowToPrintNoConditionsDifferentUnit(
        accessService.allowToPrint(accessEvaluationParameters, isEmployer));
  }

  protected abstract void assertAllowToPrintNoConditionsDifferentUnit(AccessResult actualValue);

  @Test
  void isAllowToSendNoConditions() {
    setupMocksForNoConditions();

    assertAllowToSendNoConditions(accessService.allowToSend(accessEvaluationParameters));
  }

  protected abstract void assertAllowToSendNoConditions(AccessResult actualValue);

  @Test
  void isAllowToSendWhenMissingSubscription() {
    setupMocksForMissingSubscription();

    assertAllowToSendWhenMissingSubscription(accessService.allowToSend(accessEvaluationParameters));
  }

  protected abstract void assertAllowToSendWhenMissingSubscription(AccessResult actualValue);

  @Test
  void isAllowToSendNotLatestMajorVersion() {
    setupMocksForNotLatestMajorVersion();

    assertAllowToSendNotLatestMajorVersion(accessService.allowToSend(accessEvaluationParameters));
  }

  protected abstract void assertAllowToSendNotLatestMajorVersion(AccessResult actualValue);

  @Test
  void isAllowToSendOnDeceasedPatient() {
    setupMocksForDeceasedPatient();

    assertAllowToSendOnDeceasedPatient(accessService.allowToSend(accessEvaluationParameters));
  }

  protected abstract void assertAllowToSendOnDeceasedPatient(AccessResult actualValue);

  @Test
  void isAllowToSendOnInactiveUnit() {
    setupMocksForInactiveUnit();

    assertAllowToSendOnInactiveUnit(accessService.allowToSend(accessEvaluationParameters));
  }

  protected abstract void assertAllowToSendOnInactiveUnit(AccessResult actualValue);

  @Test
  void isAllowToSendOnRenewFalse() {
    setupMocksForOnRenewFalse();

    assertAllowToSendOnRenewFalse(accessService.allowToSend(accessEvaluationParameters));
  }

  protected abstract void assertAllowToSendOnRenewFalse(AccessResult actualValue);

  @Test
  void isAllowToSendOnSekretessPatientOnSameUnit() {
    setupMocksForOnSekretessPatient();

    assertAllowToSendOnSekretessPatientOnSameUnit(
        accessService.allowToSend(accessEvaluationParameters));
  }

  protected abstract void assertAllowToSendOnSekretessPatientOnSameUnit(AccessResult actualValue);

  @Test
  void isAllowToSendOnSekretessPatientOnDifferentUnit() {
    setupMocksForOnSekretessPatientDifferentUnit();

    assertAllowToSendOnSekretessPatientOnDifferentUnit(
        accessService.allowToSend(accessEvaluationParameters));
  }

  protected abstract void assertAllowToSendOnSekretessPatientOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToSendOnDeceasedPatientOnDifferentUnit() {
    setupMocksForDeceasedPatientDifferentUnit();

    assertAllowToSendOnDeceasedPatientOnDifferentUnit(
        accessService.allowToSend(accessEvaluationParameters));
  }

  protected abstract void assertAllowToSendOnDeceasedPatientOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToSendOnInactiveUnitOnDifferentUnit() {
    setupMocksForInactiveUnitDifferentUnit();

    assertAllowToSendOnInactiveUnitOnDifferentUnit(
        accessService.allowToSend(accessEvaluationParameters));
  }

  protected abstract void assertAllowToSendOnInactiveUnitOnDifferentUnit(AccessResult actualValue);

  @Test
  void isAllowToSendOnRenewFalseOnDifferentUnit() {
    setupMocksForOnRenewFalseDifferentUnit();

    assertAllowToSendOnRenewFalseOnDifferentUnit(
        accessService.allowToSend(accessEvaluationParameters));
  }

  protected abstract void assertAllowToSendOnRenewFalseOnDifferentUnit(AccessResult actualValue);

  @Test
  void isAllowToSendNoConditionsDifferentUnit() {
    setupMocksForNoConditionsDifferentUnit();

    assertAllowToSendNoConditionsDifferentUnit(
        accessService.allowToSend(accessEvaluationParameters));
  }

  protected abstract void assertAllowToSendNoConditionsDifferentUnit(AccessResult actualValue);

  @Test
  void isallowToCreateQuestionNoConditions() {
    setupMocksForNoConditions();

    assertAllowToCreateQuestionNoConditions(
        accessService.allowToCreateQuestion(accessEvaluationParameters));
  }

  protected abstract void assertAllowToCreateQuestionNoConditions(AccessResult actualValue);

  @Test
  void isallowToCreateQuestionWhenMissingSubscription() {
    setupMocksForMissingSubscription();

    assertAllowToCreateQuestionWhenMissingSubscription(
        accessService.allowToCreateQuestion(accessEvaluationParameters));
  }

  protected abstract void assertAllowToCreateQuestionWhenMissingSubscription(
      AccessResult actualValue);

  @Test
  void isAllowToCreateQuestionNotLatestMajorVersion() {
    setupMocksForNotLatestMajorVersion();

    assertAllowToCreateQuestionNotLatestMajorVersion(
        accessService.allowToCreateQuestion(accessEvaluationParameters));
  }

  protected abstract void assertAllowToCreateQuestionNotLatestMajorVersion(
      AccessResult actualValue);

  @Test
  void isallowToCreateQuestionOnDeceasedPatient() {
    setupMocksForDeceasedPatient();

    assertAllowToCreateQuestionOnDeceasedPatient(
        accessService.allowToCreateQuestion(accessEvaluationParameters));
  }

  protected abstract void assertAllowToCreateQuestionOnDeceasedPatient(AccessResult actualValue);

  @Test
  void isallowToCreateQuestionOnInactiveUnit() {
    setupMocksForInactiveUnit();

    assertAllowToCreateQuestionOnInactiveUnit(
        accessService.allowToCreateQuestion(accessEvaluationParameters));
  }

  protected abstract void assertAllowToCreateQuestionOnInactiveUnit(AccessResult actualValue);

  @Test
  void isallowToCreateQuestionOnRenewFalse() {
    setupMocksForOnRenewFalse();

    assertAllowToCreateQuestionOnRenewFalse(
        accessService.allowToCreateQuestion(accessEvaluationParameters));
  }

  protected abstract void assertAllowToCreateQuestionOnRenewFalse(AccessResult actualValue);

  @Test
  void isallowToCreateQuestionOnSekretessPatientOnSameUnit() {
    setupMocksForOnSekretessPatient();

    assertAllowToCreateQuestionOnSekretessPatientOnSameUnit(
        accessService.allowToCreateQuestion(accessEvaluationParameters));
  }

  protected abstract void assertAllowToCreateQuestionOnSekretessPatientOnSameUnit(
      AccessResult actualValue);

  @Test
  void isallowToCreateQuestionOnSekretessPatientOnDifferentUnit() {
    setupMocksForOnSekretessPatientDifferentUnit();

    assertAllowToCreateQuestionOnSekretessPatientOnDifferentUnit(
        accessService.allowToCreateQuestion(accessEvaluationParameters));
  }

  protected abstract void assertAllowToCreateQuestionOnSekretessPatientOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isallowToCreateQuestionOnDeceasedPatientOnDifferentUnit() {
    setupMocksForDeceasedPatientDifferentUnit();

    assertAllowToCreateQuestionOnDeceasedPatientOnDifferentUnit(
        accessService.allowToCreateQuestion(accessEvaluationParameters));
  }

  protected abstract void assertAllowToCreateQuestionOnDeceasedPatientOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isallowToCreateQuestionOnInactiveUnitOnDifferentUnit() {
    setupMocksForInactiveUnitDifferentUnit();

    assertAllowToCreateQuestionOnInactiveUnitOnDifferentUnit(
        accessService.allowToCreateQuestion(accessEvaluationParameters));
  }

  protected abstract void assertAllowToCreateQuestionOnInactiveUnitOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isallowToCreateQuestionOnRenewFalseOnDifferentUnit() {
    setupMocksForOnRenewFalseDifferentUnit();

    assertAllowToCreateQuestionOnRenewFalseOnDifferentUnit(
        accessService.allowToCreateQuestion(accessEvaluationParameters));
  }

  protected abstract void assertAllowToCreateQuestionOnRenewFalseOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isallowToCreateQuestionNoConditionsDifferentUnit() {
    setupMocksForNoConditionsDifferentUnit();

    assertAllowToCreateQuestionNoConditionsDifferentUnit(
        accessService.allowToCreateQuestion(accessEvaluationParameters));
  }

  protected abstract void assertAllowToCreateQuestionNoConditionsDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToAnswerComplementNoConditions() {
    setupMocksForNoConditions();

    assertAllowToAnswerComplementNoConditions(
        accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, true));
  }

  protected abstract void assertAllowToAnswerComplementNoConditions(AccessResult actualValue);

  @Test
  void isAllowToAnswerComplementWhenMissingSubscription() {
    setupMocksForMissingSubscription();

    assertAllowToAnswerComplementWhenMissingSubscription(
        accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, true));
  }

  protected abstract void assertAllowToAnswerComplementWhenMissingSubscription(
      AccessResult actualValue);

  @Test
  void isAllowToAnswerComplementNotLatestMajorVersion() {
    setupMocksForNotLatestMajorVersion();

    assertAllowToAnswerComplementNotLatestMajorVersion(
        accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, true));
  }

  protected abstract void assertAllowToAnswerComplementNotLatestMajorVersion(
      AccessResult actualValue);

  @Test
  void isAllowToAnswerComplementOnDeceasedPatient() {
    setupMocksForDeceasedPatient();

    assertAllowToAnswerComplementOnDeceasedPatient(
        accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, true));
  }

  protected abstract void assertAllowToAnswerComplementOnDeceasedPatient(AccessResult actualValue);

  @Test
  void isAllowToAnswerComplementOnInactiveUnit() {
    setupMocksForInactiveUnit();

    assertAllowToAnswerComplementOnInactiveUnit(
        accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, true));
  }

  protected abstract void assertAllowToAnswerComplementOnInactiveUnit(AccessResult actualValue);

  @Test
  void isAllowToAnswerComplementOnRenewFalse() {
    setupMocksForOnRenewFalse();

    assertAllowToAnswerComplementOnRenewFalse(
        accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, true));
  }

  protected abstract void assertAllowToAnswerComplementOnRenewFalse(AccessResult actualValue);

  @Test
  void isAllowToAnswerComplementOnSekretessPatientOnSameUnit() {
    setupMocksForOnSekretessPatient();

    assertAllowToAnswerComplementOnSekretessPatientOnSameUnit(
        accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, true));
  }

  protected abstract void assertAllowToAnswerComplementOnSekretessPatientOnSameUnit(
      AccessResult actualValue);

  @Test
  void isAllowToAnswerComplementOnSekretessPatientOnDifferentUnit() {
    setupMocksForOnSekretessPatientDifferentUnit();

    assertAllowToAnswerComplementOnSekretessPatientOnDifferentUnit(
        accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, true));
  }

  protected abstract void assertAllowToAnswerComplementOnSekretessPatientOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToAnswerComplementOnDeceasedPatientOnDifferentUnit() {
    setupMocksForDeceasedPatientDifferentUnit();

    assertAllowToAnswerComplementOnDeceasedPatientOnDifferentUnit(
        accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, true));
  }

  protected abstract void assertAllowToAnswerComplementOnDeceasedPatientOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToAnswerComplementOnInactiveUnitOnDifferentUnit() {
    setupMocksForInactiveUnitDifferentUnit();

    assertAllowToAnswerComplementOnInactiveUnitOnDifferentUnit(
        accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, true));
  }

  protected abstract void assertAllowToAnswerComplementOnInactiveUnitOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToAnswerComplementOnRenewFalseOnDifferentUnit() {
    setupMocksForOnRenewFalseDifferentUnit();

    assertAllowToAnswerComplementOnRenewFalseOnDifferentUnit(
        accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, true));
  }

  protected abstract void assertAllowToAnswerComplementOnRenewFalseOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToAnswerComplementNoConditionsDifferentUnit() {
    setupMocksForNoConditionsDifferentUnit();

    assertAllowToAnswerComplementNoConditionsDifferentUnit(
        accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, true));
  }

  protected abstract void assertAllowToAnswerComplementNoConditionsDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToAnswerQuestionNoConditions() {
    setupMocksForNoConditions();

    assertAllowToAnswerQuestionNoConditions(
        accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, false));
  }

  protected abstract void assertAllowToAnswerQuestionNoConditions(AccessResult actualValue);

  @Test
  void isAllowToAnswerQuestionWhenMissingSubscription() {
    setupMocksForMissingSubscription();

    assertAllowToAnswerQuestionWhenMissingSubscription(
        accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, false));
  }

  protected abstract void assertAllowToAnswerQuestionWhenMissingSubscription(
      AccessResult actualValue);

  @Test
  void isAllowToAnswerQuestionNotLatestMajorVersion() {
    setupMocksForNotLatestMajorVersion();

    assertAllowToAnswerQuestionNotLatestMajorVersion(
        accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, false));
  }

  protected abstract void assertAllowToAnswerQuestionNotLatestMajorVersion(
      AccessResult actualValue);

  @Test
  void isAllowToAnswerQuestionOnDeceasedPatient() {
    setupMocksForDeceasedPatient();

    assertAllowToAnswerQuestionOnDeceasedPatient(
        accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, false));
  }

  protected abstract void assertAllowToAnswerQuestionOnDeceasedPatient(AccessResult actualValue);

  @Test
  void isAllowToAnswerQuestionOnInactiveUnit() {
    setupMocksForInactiveUnit();

    assertAllowToAnswerQuestionOnInactiveUnit(
        accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, false));
  }

  protected abstract void assertAllowToAnswerQuestionOnInactiveUnit(AccessResult actualValue);

  @Test
  void isAllowToAnswerQuestionOnRenewFalse() {
    setupMocksForOnRenewFalse();

    assertAllowToAnswerQuestionOnRenewFalse(
        accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, false));
  }

  protected abstract void assertAllowToAnswerQuestionOnRenewFalse(AccessResult actualValue);

  @Test
  void isAllowToAnswerQuestionOnSekretessPatientOnSameUnit() {
    setupMocksForOnSekretessPatient();

    assertAllowToAnswerQuestionOnSekretessPatientOnSameUnit(
        accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, false));
  }

  protected abstract void assertAllowToAnswerQuestionOnSekretessPatientOnSameUnit(
      AccessResult actualValue);

  @Test
  void isAllowToAnswerQuestionOnSekretessPatientOnDifferentUnit() {
    setupMocksForOnSekretessPatientDifferentUnit();

    assertAllowToAnswerQuestionOnSekretessPatientOnDifferentUnit(
        accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, false));
  }

  protected abstract void assertAllowToAnswerQuestionOnSekretessPatientOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToAnswerQuestionOnDeceasedPatientOnDifferentUnit() {
    setupMocksForDeceasedPatientDifferentUnit();

    assertAllowToAnswerQuestionOnDeceasedPatientOnDifferentUnit(
        accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, false));
  }

  protected abstract void assertAllowToAnswerQuestionOnDeceasedPatientOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToAnswerQuestionOnInactiveUnitOnDifferentUnit() {
    setupMocksForInactiveUnitDifferentUnit();

    assertAllowToAnswerQuestionOnInactiveUnitOnDifferentUnit(
        accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, false));
  }

  protected abstract void assertAllowToAnswerQuestionOnInactiveUnitOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToAnswerQuestionOnRenewFalseOnDifferentUnit() {
    setupMocksForOnRenewFalseDifferentUnit();

    assertAllowToAnswerQuestionOnRenewFalseOnDifferentUnit(
        accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, false));
  }

  protected abstract void assertAllowToAnswerQuestionOnRenewFalseOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToAnswerQuestionNoConditionsDifferentUnit() {
    setupMocksForNoConditionsDifferentUnit();

    assertAllowToAnswerQuestionNoConditionsDifferentUnit(
        accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, false));
  }

  protected abstract void assertAllowToAnswerQuestionNoConditionsDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToAnswerAdminQuestionNoConditions() {
    setupMocksForNoConditions();

    assertAllowToAnswerAdminQuestionNoConditions(
        accessService.allowToAnswerAdminQuestion(accessEvaluationParameters));
  }

  protected abstract void assertAllowToAnswerAdminQuestionNoConditions(AccessResult actualValue);

  @Test
  void isAllowToAnswerAdminQuestionWhenMissingSubscription() {
    setupMocksForMissingSubscription();

    assertAllowToAnswerAdminQuestionWhenMissingSubscription(
        accessService.allowToAnswerAdminQuestion(accessEvaluationParameters));
  }

  protected abstract void assertAllowToAnswerAdminQuestionWhenMissingSubscription(
      AccessResult actualValue);

  @Test
  void isAllowToAnswerAdminQuestionNotLatestMajorVersion() {
    setupMocksForNotLatestMajorVersion();

    assertAllowToAnswerAdminQuestionNotLatestMajorVersion(
        accessService.allowToAnswerAdminQuestion(accessEvaluationParameters));
  }

  protected abstract void assertAllowToAnswerAdminQuestionNotLatestMajorVersion(
      AccessResult actualValue);

  @Test
  void isAllowToAnswerAdminQuestionOnDeceasedPatient() {
    setupMocksForDeceasedPatient();

    assertAllowToAnswerAdminQuestionOnDeceasedPatient(
        accessService.allowToAnswerAdminQuestion(accessEvaluationParameters));
  }

  protected abstract void assertAllowToAnswerAdminQuestionOnDeceasedPatient(
      AccessResult actualValue);

  @Test
  void isAllowToAnswerAdminQuestionOnInactiveUnit() {
    setupMocksForInactiveUnit();

    assertAllowToAnswerAdminQuestionOnInactiveUnit(
        accessService.allowToAnswerAdminQuestion(accessEvaluationParameters));
  }

  protected abstract void assertAllowToAnswerAdminQuestionOnInactiveUnit(AccessResult actualValue);

  @Test
  void isAllowToAnswerAdminQuestionOnRenewFalse() {
    setupMocksForOnRenewFalse();

    assertAllowToAnswerAdminQuestionOnRenewFalse(
        accessService.allowToAnswerAdminQuestion(accessEvaluationParameters));
  }

  protected abstract void assertAllowToAnswerAdminQuestionOnRenewFalse(AccessResult actualValue);

  @Test
  void isAllowToAnswerAdminQuestionOnSekretessPatientOnSameUnit() {
    setupMocksForOnSekretessPatient();

    assertAllowToAnswerAdminQuestionOnSekretessPatientOnSameUnit(
        accessService.allowToAnswerAdminQuestion(accessEvaluationParameters));
  }

  protected abstract void assertAllowToAnswerAdminQuestionOnSekretessPatientOnSameUnit(
      AccessResult actualValue);

  @Test
  void isAllowToAnswerAdminQuestionOnSekretessPatientOnDifferentUnit() {
    setupMocksForOnSekretessPatientDifferentUnit();

    assertAllowToAnswerAdminQuestionOnSekretessPatientOnDifferentUnit(
        accessService.allowToAnswerAdminQuestion(accessEvaluationParameters));
  }

  protected abstract void assertAllowToAnswerAdminQuestionOnSekretessPatientOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToAnswerAdminQuestionOnDeceasedPatientOnDifferentUnit() {
    setupMocksForDeceasedPatientDifferentUnit();

    assertAllowToAnswerAdminQuestionOnDeceasedPatientOnDifferentUnit(
        accessService.allowToAnswerAdminQuestion(accessEvaluationParameters));
  }

  protected abstract void assertAllowToAnswerAdminQuestionOnDeceasedPatientOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToAnswerAdminQuestionOnInactiveUnitOnDifferentUnit() {
    setupMocksForInactiveUnitDifferentUnit();

    assertAllowToAnswerAdminQuestionOnInactiveUnitOnDifferentUnit(
        accessService.allowToAnswerAdminQuestion(accessEvaluationParameters));
  }

  protected abstract void assertAllowToAnswerAdminQuestionOnInactiveUnitOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToAnswerAdminQuestionOnRenewFalseOnDifferentUnit() {
    setupMocksForOnRenewFalseDifferentUnit();

    assertAllowToAnswerAdminQuestionOnRenewFalseOnDifferentUnit(
        accessService.allowToAnswerAdminQuestion(accessEvaluationParameters));
  }

  protected abstract void assertAllowToAnswerAdminQuestionOnRenewFalseOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToAnswerAdminQuestionNoConditionsDifferentUnit() {
    setupMocksForNoConditionsDifferentUnit();

    assertAllowToAnswerAdminQuestionNoConditionsDifferentUnit(
        accessService.allowToAnswerAdminQuestion(accessEvaluationParameters));
  }

  protected abstract void assertAllowToAnswerAdminQuestionNoConditionsDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToSetComplementAsHandledNoConditions() {
    setupMocksForNoConditions();

    assertAllowToSetComplementAsHandledNoConditions(
        accessService.allowToSetComplementAsHandled(accessEvaluationParameters));
  }

  protected abstract void assertAllowToSetComplementAsHandledNoConditions(AccessResult actualValue);

  @Test
  void isAllowToSetComplementAsHandledWhenMissingSubscription() {
    setupMocksForMissingSubscription();

    assertAllowToSetComplementAsHandledWhenMissingSubscription(
        accessService.allowToSetComplementAsHandled(accessEvaluationParameters));
  }

  protected abstract void assertAllowToSetComplementAsHandledWhenMissingSubscription(
      AccessResult actualValue);

  @Test
  void isAllowToSetComplementAsHandledNotLatestMajorVersion() {
    setupMocksForNotLatestMajorVersion();

    assertAllowToSetComplementAsHandledNotLatestMajorVersion(
        accessService.allowToSetComplementAsHandled(accessEvaluationParameters));
  }

  protected abstract void assertAllowToSetComplementAsHandledNotLatestMajorVersion(
      AccessResult actualValue);

  @Test
  void isAllowToSetComplementAsHandledOnDeceasedPatient() {
    setupMocksForDeceasedPatient();

    assertAllowToSetComplementAsHandledOnDeceasedPatient(
        accessService.allowToSetComplementAsHandled(accessEvaluationParameters));
  }

  protected abstract void assertAllowToSetComplementAsHandledOnDeceasedPatient(
      AccessResult actualValue);

  @Test
  void isAllowToSetComplementAsHandledOnInactiveUnit() {
    setupMocksForInactiveUnit();

    assertAllowToSetComplementAsHandledOnInactiveUnit(
        accessService.allowToSetComplementAsHandled(accessEvaluationParameters));
  }

  protected abstract void assertAllowToSetComplementAsHandledOnInactiveUnit(
      AccessResult actualValue);

  @Test
  void isAllowToSetComplementAsHandledOnRenewFalse() {
    setupMocksForOnRenewFalse();

    assertAllowToSetComplementAsHandledOnRenewFalse(
        accessService.allowToSetComplementAsHandled(accessEvaluationParameters));
  }

  protected abstract void assertAllowToSetComplementAsHandledOnRenewFalse(AccessResult actualValue);

  @Test
  void isAllowToSetComplementAsHandledOnSekretessPatientOnSameUnit() {
    setupMocksForOnSekretessPatient();

    assertAllowToSetComplementAsHandledOnSekretessPatientOnSameUnit(
        accessService.allowToSetComplementAsHandled(accessEvaluationParameters));
  }

  protected abstract void assertAllowToSetComplementAsHandledOnSekretessPatientOnSameUnit(
      AccessResult actualValue);

  @Test
  void isAllowToSetComplementAsHandledOnSekretessPatientOnDifferentUnit() {
    setupMocksForOnSekretessPatientDifferentUnit();

    assertAllowToSetComplementAsHandledOnSekretessPatientOnDifferentUnit(
        accessService.allowToSetComplementAsHandled(accessEvaluationParameters));
  }

  protected abstract void assertAllowToSetComplementAsHandledOnSekretessPatientOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToSetComplementAsHandledOnDeceasedPatientOnDifferentUnit() {
    setupMocksForDeceasedPatientDifferentUnit();

    assertAllowToSetComplementAsHandledOnDeceasedPatientOnDifferentUnit(
        accessService.allowToSetComplementAsHandled(accessEvaluationParameters));
  }

  protected abstract void assertAllowToSetComplementAsHandledOnDeceasedPatientOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToSetComplementAsHandledOnInactiveUnitOnDifferentUnit() {
    setupMocksForInactiveUnitDifferentUnit();

    assertAllowToSetComplementAsHandledOnInactiveUnitOnDifferentUnit(
        accessService.allowToSetComplementAsHandled(accessEvaluationParameters));
  }

  protected abstract void assertAllowToSetComplementAsHandledOnInactiveUnitOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToSetComplementAsHandledOnRenewFalseOnDifferentUnit() {
    setupMocksForOnRenewFalseDifferentUnit();

    assertAllowToSetComplementAsHandledOnRenewFalseOnDifferentUnit(
        accessService.allowToSetComplementAsHandled(accessEvaluationParameters));
  }

  protected abstract void assertAllowToSetComplementAsHandledOnRenewFalseOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToSetComplementAsHandledNoConditionsDifferentUnit() {
    setupMocksForNoConditionsDifferentUnit();

    assertAllowToSetComplementAsHandledNoConditionsDifferentUnit(
        accessService.allowToSetComplementAsHandled(accessEvaluationParameters));
  }

  protected abstract void assertAllowToSetComplementAsHandledNoConditionsDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToSetQuestionAsHandledNoConditions() {
    setupMocksForNoConditions();

    assertAllowToSetQuestionAsHandledNoConditions(
        accessService.allowToSetQuestionAsHandled(accessEvaluationParameters));
  }

  protected abstract void assertAllowToSetQuestionAsHandledNoConditions(AccessResult actualValue);

  @Test
  void isAllowToSetQuestionAsHandledWhenMissingSubscription() {
    setupMocksForMissingSubscription();

    assertAllowToSetQuestionAsHandledWhenMissingSubscription(
        accessService.allowToSetQuestionAsHandled(accessEvaluationParameters));
  }

  protected abstract void assertAllowToSetQuestionAsHandledWhenMissingSubscription(
      AccessResult actualValue);

  @Test
  void isAllowToSetQuestionAsHandledNotLatestMajorVersion() {
    setupMocksForNotLatestMajorVersion();

    assertAllowToSetQuestionAsHandledNotLatestMajorVersion(
        accessService.allowToSetQuestionAsHandled(accessEvaluationParameters));
  }

  protected abstract void assertAllowToSetQuestionAsHandledNotLatestMajorVersion(
      AccessResult actualValue);

  @Test
  void isAllowToSetQuestionAsHandledOnDeceasedPatient() {
    setupMocksForDeceasedPatient();

    assertAllowToSetQuestionAsHandledOnDeceasedPatient(
        accessService.allowToSetQuestionAsHandled(accessEvaluationParameters));
  }

  protected abstract void assertAllowToSetQuestionAsHandledOnDeceasedPatient(
      AccessResult actualValue);

  @Test
  void isAllowToSetQuestionAsHandledOnInactiveUnit() {
    setupMocksForInactiveUnit();

    assertAllowToSetQuestionAsHandledOnInactiveUnit(
        accessService.allowToSetQuestionAsHandled(accessEvaluationParameters));
  }

  protected abstract void assertAllowToSetQuestionAsHandledOnInactiveUnit(AccessResult actualValue);

  @Test
  void isAllowToSetQuestionAsHandledOnRenewFalse() {
    setupMocksForOnRenewFalse();

    assertAllowToSetQuestionAsHandledOnRenewFalse(
        accessService.allowToSetQuestionAsHandled(accessEvaluationParameters));
  }

  protected abstract void assertAllowToSetQuestionAsHandledOnRenewFalse(AccessResult actualValue);

  @Test
  void isAllowToSetQuestionAsHandledOnSekretessPatientOnSameUnit() {
    setupMocksForOnSekretessPatient();

    assertAllowToSetQuestionAsHandledOnSekretessPatientOnSameUnit(
        accessService.allowToSetQuestionAsHandled(accessEvaluationParameters));
  }

  protected abstract void assertAllowToSetQuestionAsHandledOnSekretessPatientOnSameUnit(
      AccessResult actualValue);

  @Test
  void isAllowToSetQuestionAsHandledOnSekretessPatientOnDifferentUnit() {
    setupMocksForOnSekretessPatientDifferentUnit();

    assertAllowToSetQuestionAsHandledOnSekretessPatientOnDifferentUnit(
        accessService.allowToSetQuestionAsHandled(accessEvaluationParameters));
  }

  protected abstract void assertAllowToSetQuestionAsHandledOnSekretessPatientOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToSetQuestionAsHandledOnDeceasedPatientOnDifferentUnit() {
    setupMocksForDeceasedPatientDifferentUnit();

    assertAllowToSetQuestionAsHandledOnDeceasedPatientOnDifferentUnit(
        accessService.allowToSetQuestionAsHandled(accessEvaluationParameters));
  }

  protected abstract void assertAllowToSetQuestionAsHandledOnDeceasedPatientOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToSetQuestionAsHandledOnInactiveUnitOnDifferentUnit() {
    setupMocksForInactiveUnitDifferentUnit();

    assertAllowToSetQuestionAsHandledOnInactiveUnitOnDifferentUnit(
        accessService.allowToSetQuestionAsHandled(accessEvaluationParameters));
  }

  protected abstract void assertAllowToSetQuestionAsHandledOnInactiveUnitOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToSetQuestionAsHandledOnRenewFalseOnDifferentUnit() {
    setupMocksForOnRenewFalseDifferentUnit();

    assertAllowToSetQuestionAsHandledOnRenewFalseOnDifferentUnit(
        accessService.allowToSetQuestionAsHandled(accessEvaluationParameters));
  }

  protected abstract void assertAllowToSetQuestionAsHandledOnRenewFalseOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToSetQuestionAsHandledNoConditionsDifferentUnit() {
    setupMocksForNoConditionsDifferentUnit();

    assertAllowToSetQuestionAsHandledNoConditionsDifferentUnit(
        accessService.allowToSetQuestionAsHandled(accessEvaluationParameters));
  }

  protected abstract void assertAllowToSetQuestionAsHandledNoConditionsDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToReadQuestionsNoConditions() {
    setupMocksForNoConditions();

    assertAllowToReadQuestionsNoConditions(
        accessService.allowToReadQuestions(accessEvaluationParameters));
  }

  protected abstract void assertAllowToReadQuestionsNoConditions(AccessResult actualValue);

  @Test
  void isAllowToReadQuestionsWhenMissingSubscription() {
    setupMocksForMissingSubscription();

    assertAllowToReadQuestionsWhenMissingSubscription(
        accessService.allowToReadQuestions(accessEvaluationParameters));
  }

  protected abstract void assertAllowToReadQuestionsWhenMissingSubscription(
      AccessResult actualValue);

  @Test
  void isAllowToReadQuestionsNotLatestMajorVersion() {
    setupMocksForNotLatestMajorVersion();

    assertAllowToReadQuestionsNotLatestMajorVersion(
        accessService.allowToReadQuestions(accessEvaluationParameters));
  }

  protected abstract void assertAllowToReadQuestionsNotLatestMajorVersion(AccessResult actualValue);

  @Test
  void isAllowToReadQuestionsOnDeceasedPatient() {
    setupMocksForDeceasedPatient();

    assertAllowToReadQuestionsOnDeceasedPatient(
        accessService.allowToReadQuestions(accessEvaluationParameters));
  }

  protected abstract void assertAllowToReadQuestionsOnDeceasedPatient(AccessResult actualValue);

  @Test
  void isAllowToReadQuestionsOnInactiveUnit() {
    setupMocksForInactiveUnit();

    assertAllowToReadQuestionsOnInactiveUnit(
        accessService.allowToReadQuestions(accessEvaluationParameters));
  }

  protected abstract void assertAllowToReadQuestionsOnInactiveUnit(AccessResult actualValue);

  @Test
  void isAllowToReadQuestionsOnRenewFalse() {
    setupMocksForOnRenewFalse();

    assertAllowToReadQuestionsOnRenewFalse(
        accessService.allowToReadQuestions(accessEvaluationParameters));
  }

  protected abstract void assertAllowToReadQuestionsOnRenewFalse(AccessResult actualValue);

  @Test
  void isAllowToReadQuestionsOnSekretessPatientOnSameUnit() {
    setupMocksForOnSekretessPatient();

    assertAllowToReadQuestionsOnSekretessPatientOnSameUnit(
        accessService.allowToReadQuestions(accessEvaluationParameters));
  }

  protected abstract void assertAllowToReadQuestionsOnSekretessPatientOnSameUnit(
      AccessResult actualValue);

  @Test
  void isAllowToReadQuestionsOnSekretessPatientOnDifferentUnit() {
    setupMocksForOnSekretessPatientDifferentUnit();

    assertAllowToReadQuestionsOnSekretessPatientOnDifferentUnit(
        accessService.allowToReadQuestions(accessEvaluationParameters));
  }

  protected abstract void assertAllowToReadQuestionsOnSekretessPatientOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToReadQuestionsOnDeceasedPatientOnDifferentUnit() {
    setupMocksForDeceasedPatientDifferentUnit();

    assertAllowToReadQuestionsOnDeceasedPatientOnDifferentUnit(
        accessService.allowToReadQuestions(accessEvaluationParameters));
  }

  protected abstract void assertAllowToReadQuestionsOnDeceasedPatientOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToReadQuestionsOnInactiveUnitOnDifferentUnit() {
    setupMocksForInactiveUnitDifferentUnit();

    assertAllowToReadQuestionsOnInactiveUnitOnDifferentUnit(
        accessService.allowToReadQuestions(accessEvaluationParameters));
  }

  protected abstract void assertAllowToReadQuestionsOnInactiveUnitOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToReadQuestionsOnRenewFalseOnDifferentUnit() {
    setupMocksForOnRenewFalseDifferentUnit();

    assertAllowToReadQuestionsOnRenewFalseOnDifferentUnit(
        accessService.allowToReadQuestions(accessEvaluationParameters));
  }

  protected abstract void assertAllowToReadQuestionsOnRenewFalseOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToReadQuestionsNoConditionsDifferentUnit() {
    setupMocksForNoConditionsDifferentUnit();

    assertAllowToReadQuestionsNoConditionsDifferentUnit(
        accessService.allowToReadQuestions(accessEvaluationParameters));
  }

  protected abstract void assertAllowToReadQuestionsNoConditionsDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToForwardQuestionsNoConditions() {
    setupMocksForNoConditions();

    assertAllowToForwardQuestionsNoConditions(
        accessService.allowToForwardQuestions(accessEvaluationParameters));
  }

  protected abstract void assertAllowToForwardQuestionsNoConditions(AccessResult actualValue);

  @Test
  void isAllowToForwardQuestionsWhenMissingSubscription() {
    setupMocksForMissingSubscription();

    assertAllowToForwardQuestionsWhenMissingSubscription(
        accessService.allowToForwardQuestions(accessEvaluationParameters));
  }

  protected abstract void assertAllowToForwardQuestionsWhenMissingSubscription(
      AccessResult actualValue);

  @Test
  void isAllowToForwardQuestionsNotLatestMajorVersion() {
    setupMocksForNotLatestMajorVersion();

    assertAllowToForwardQuestionsNotLatestMajorVersion(
        accessService.allowToForwardQuestions(accessEvaluationParameters));
  }

  protected abstract void assertAllowToForwardQuestionsNotLatestMajorVersion(
      AccessResult actualValue);

  @Test
  void isAllowToForwardQuestionsOnDeceasedPatient() {
    setupMocksForDeceasedPatient();

    assertAllowToForwardQuestionsOnDeceasedPatient(
        accessService.allowToForwardQuestions(accessEvaluationParameters));
  }

  protected abstract void assertAllowToForwardQuestionsOnDeceasedPatient(AccessResult actualValue);

  @Test
  void isAllowToForwardQuestionsOnInactiveUnit() {
    setupMocksForInactiveUnit();

    assertAllowToForwardQuestionsOnInactiveUnit(
        accessService.allowToForwardQuestions(accessEvaluationParameters));
  }

  protected abstract void assertAllowToForwardQuestionsOnInactiveUnit(AccessResult actualValue);

  @Test
  void isAllowToForwardQuestionsOnRenewFalse() {
    setupMocksForOnRenewFalse();

    assertAllowToForwardQuestionsOnRenewFalse(
        accessService.allowToForwardQuestions(accessEvaluationParameters));
  }

  protected abstract void assertAllowToForwardQuestionsOnRenewFalse(AccessResult actualValue);

  @Test
  void isAllowToForwardQuestionsOnSekretessPatientOnSameUnit() {
    setupMocksForOnSekretessPatient();

    assertAllowToForwardQuestionsOnSekretessPatientOnSameUnit(
        accessService.allowToForwardQuestions(accessEvaluationParameters));
  }

  protected abstract void assertAllowToForwardQuestionsOnSekretessPatientOnSameUnit(
      AccessResult actualValue);

  @Test
  void isAllowToForwardQuestionsOnSekretessPatientOnDifferentUnit() {
    setupMocksForOnSekretessPatientDifferentUnit();

    assertAllowToForwardQuestionsOnSekretessPatientOnDifferentUnit(
        accessService.allowToForwardQuestions(accessEvaluationParameters));
  }

  protected abstract void assertAllowToForwardQuestionsOnSekretessPatientOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToForwardQuestionsOnDeceasedPatientOnDifferentUnit() {
    setupMocksForDeceasedPatientDifferentUnit();

    assertAllowToForwardQuestionsOnDeceasedPatientOnDifferentUnit(
        accessService.allowToForwardQuestions(accessEvaluationParameters));
  }

  protected abstract void assertAllowToForwardQuestionsOnDeceasedPatientOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToForwardQuestionsOnInactiveUnitOnDifferentUnit() {
    setupMocksForInactiveUnitDifferentUnit();

    assertAllowToForwardQuestionsOnInactiveUnitOnDifferentUnit(
        accessService.allowToForwardQuestions(accessEvaluationParameters));
  }

  protected abstract void assertAllowToForwardQuestionsOnInactiveUnitOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToForwardQuestionsOnRenewFalseOnDifferentUnit() {
    setupMocksForOnRenewFalseDifferentUnit();

    assertAllowToForwardQuestionsOnRenewFalseOnDifferentUnit(
        accessService.allowToForwardQuestions(accessEvaluationParameters));
  }

  protected abstract void assertAllowToForwardQuestionsOnRenewFalseOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToForwardQuestionsNoConditionsDifferentUnit() {
    setupMocksForNoConditionsDifferentUnit();

    assertAllowToForwardQuestionsNoConditionsDifferentUnit(
        accessService.allowToForwardQuestions(accessEvaluationParameters));
  }

  protected abstract void assertAllowToForwardQuestionsNoConditionsDifferentUnit(
      AccessResult actualValue);
}
