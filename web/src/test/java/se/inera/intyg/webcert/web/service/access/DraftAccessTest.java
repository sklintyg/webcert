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
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.web.service.access.data.AccessServiceTestData;
import se.inera.intyg.webcert.web.service.access.util.AccessServiceTestToolkit;
import se.inera.intyg.webcert.web.service.subscription.dto.SubscriptionInfo;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

public abstract class DraftAccessTest extends AccessTest {

  @InjectMocks private DraftAccessServiceImpl accessService;

  DraftAccessTest(String intygsTyp, AccessServiceTestData accessServiceTestData) {
    super(intygsTyp, accessServiceTestData);
  }

  @BeforeEach
  void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void isAllowedToCreateUtkastNoConditions() {
    setupMocksForNoConditions();

    assertAllowedToCreateUtkastNoConditions(
        accessService.allowToCreateDraft(
            AccessEvaluationParameters.create(intygsTyp, PERSONNUMMER)));
  }

  protected abstract void assertAllowedToCreateUtkastNoConditions(AccessResult actualValue);

  @Test
  void isAllowedToCreateUtkastWhenMissingSubscription() {
    setupMocksForMissingSubscription();

    assertAllowedToCreateUtkastWhenMissingSubscription(
        accessService.allowToCreateDraft(
            AccessEvaluationParameters.create(intygsTyp, PERSONNUMMER)));
  }

  protected abstract void assertAllowedToCreateUtkastWhenMissingSubscription(
      AccessResult actualValue);

  @Test
  void isAllowToCreateUtkastNotLatestMajorVersion() {
    setupMocksForNotLatestMajorVersion();

    assertAllowToCreateUtkastNotLatestMajorVersion(
        accessService.allowToCreateDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToCreateUtkastNotLatestMajorVersion(AccessResult actualValue);

  @Test
  void isAllowedToCreateUtkastOnDeceasedPatient() {
    setupMocksForDeceasedPatient();

    assertAllowedToCreateUtkastOnDeceasedPatient(
        accessService.allowToCreateDraft(
            AccessEvaluationParameters.create(intygsTyp, PERSONNUMMER)));
  }

  protected abstract void assertAllowedToCreateUtkastOnDeceasedPatient(AccessResult actualValue);

  @Test
  void isAllowedToCreateUtkastOnInactiveUnit() {
    setupMocksForInactiveUnit();

    assertAllowedToCreateUtkastOnInactiveCareUnit(
        accessService.allowToCreateDraft(
            AccessEvaluationParameters.create(intygsTyp, PERSONNUMMER)));
  }

  protected abstract void assertAllowedToCreateUtkastOnInactiveCareUnit(AccessResult actualValue);

  @Test
  void isAllowedToCreateUtkastOnRenewFalse() {
    setupMocksForOnRenewFalse();

    assertAllowedToCreateUtkastOnRenewFalse(
        accessService.allowToCreateDraft(
            AccessEvaluationParameters.create(intygsTyp, PERSONNUMMER)));
  }

  protected abstract void assertAllowedToCreateUtkastOnRenewFalse(AccessResult actualValue);

  @Test
  void isAllowToCreateUtkastOnSameCareProviderWhenUtkastSameVGExists() {
    final WebCertUser user = AccessServiceTestToolkit.createUser(intygsTyp, accessServiceTestData);
    doReturn(user).when(webCertUserService).getUser();
    doReturn(false).when(patientDetailsResolver).isAvliden(PERSONNUMMER);
    doReturn(SekretessStatus.FALSE).when(patientDetailsResolver).getSekretessStatus(PERSONNUMMER);
    doReturn(true).when(webCertUserService).isUserAllowedAccessToUnit(enhetsId);
    doReturn(AccessServiceTestToolkit.createPreviousUtkastForUtkast(intygsTyp))
        .when(utkastService)
        .checkIfPersonHasExistingIntyg(PERSONNUMMER, user, null);
    doReturn(selectedVardgivare).when(user).getValdVardgivare();
    doReturn(vardgivarId).when(selectedVardgivare).getId();
    doReturn(selectedVardenhet).when(user).getValdVardenhet();
    doReturn(enhetsId).when(selectedVardenhet).getId();
    doReturn(new SubscriptionInfo()).when(user).getSubscriptionInfo();

    assertAllowToCreateUtkastOnSameCareProviderWhenUtkastSameVGExists(
        accessService.allowToCreateDraft(
            AccessEvaluationParameters.create(intygsTyp, PERSONNUMMER)));
  }

  protected abstract void assertAllowToCreateUtkastOnSameCareProviderWhenUtkastSameVGExists(
      AccessResult actualValue);

  @Test
  void isAllowToCreateUtkastOnDifferentCareProviderWhenIntygSameVGExists() {
    final WebCertUser user = AccessServiceTestToolkit.createUser(intygsTyp, accessServiceTestData);
    doReturn(user).when(webCertUserService).getUser();
    doReturn(false).when(patientDetailsResolver).isAvliden(PERSONNUMMER);
    doReturn(SekretessStatus.FALSE).when(patientDetailsResolver).getSekretessStatus(PERSONNUMMER);
    doReturn(true).when(webCertUserService).isUserAllowedAccessToUnit(enhetsId);
    doReturn(AccessServiceTestToolkit.createPreviousIntygForUtkast(intygsTyp, true))
        .when(utkastService)
        .checkIfPersonHasExistingIntyg(PERSONNUMMER, user, null);
    doReturn(selectedVardgivare).when(user).getValdVardgivare();
    doReturn(vardgivarId).when(selectedVardgivare).getId();
    doReturn(selectedVardenhet).when(user).getValdVardenhet();
    doReturn(enhetsId).when(selectedVardenhet).getId();
    doReturn(new SubscriptionInfo()).when(user).getSubscriptionInfo();

    assertAllowToCreateUtkastOnDifferentCareProviderWhenIntygSameVGExists(
        accessService.allowToCreateDraft(
            AccessEvaluationParameters.create(intygsTyp, PERSONNUMMER)));
  }

  protected abstract void assertAllowToCreateUtkastOnDifferentCareProviderWhenIntygSameVGExists(
      AccessResult actualValue);

  @Test
  void isAllowToCreateUtkastOnSameCareProviderWhenIntygExists() {
    final WebCertUser user = AccessServiceTestToolkit.createUser(intygsTyp, accessServiceTestData);
    doReturn(user).when(webCertUserService).getUser();
    doReturn(false).when(patientDetailsResolver).isAvliden(PERSONNUMMER);
    doReturn(SekretessStatus.FALSE).when(patientDetailsResolver).getSekretessStatus(PERSONNUMMER);
    doReturn(true).when(webCertUserService).isUserAllowedAccessToUnit(enhetsId);
    doReturn(AccessServiceTestToolkit.createPreviousIntygForUtkast(intygsTyp, false))
        .when(utkastService)
        .checkIfPersonHasExistingIntyg(PERSONNUMMER, user, null);
    doReturn(selectedVardgivare).when(user).getValdVardgivare();
    doReturn(vardgivarId).when(selectedVardgivare).getId();
    doReturn(selectedVardenhet).when(user).getValdVardenhet();
    doReturn(enhetsId).when(selectedVardenhet).getId();
    doReturn(new SubscriptionInfo()).when(user).getSubscriptionInfo();

    assertAllowToCreateUtkastOnSameCareProviderWhenIntygExists(
        accessService.allowToCreateDraft(
            AccessEvaluationParameters.create(intygsTyp, PERSONNUMMER)));
  }

  protected abstract void assertAllowToCreateUtkastOnSameCareProviderWhenIntygExists(
      AccessResult actualValue);

  @Test
  void isAllowedToCreateUtkastOnSekretessPatient() {
    setupMocksForOnSekretessPatient();

    assertAllowedToCreateUtkastOnSekretessPatient(
        accessService.allowToCreateDraft(
            AccessEvaluationParameters.create(intygsTyp, PERSONNUMMER)));
  }

  protected abstract void assertAllowedToCreateUtkastOnSekretessPatient(AccessResult actualValue);

  @Test
  void isAllowToReadUtkastNoConditions() {
    setupMocksForNoConditions();

    assertAllowToReadUtkastNoConditions(
        accessService.allowToReadDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToReadUtkastNoConditions(AccessResult actualValue);

  @Test
  void isAllowToReadUtkastWhenMissingSubscription() {
    setupMocksForMissingSubscription();

    assertAllowToReadUtkastWhenMissingSubscription(
        accessService.allowToReadDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToReadUtkastWhenMissingSubscription(AccessResult actualValue);

  @Test
  void isAllowToReadUtkastNotLatestMajorVersion() {
    setupMocksForNotLatestMajorVersion();

    assertAllowToReadUtkastNotLatestMajorVersion(
        accessService.allowToReadDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToReadUtkastNotLatestMajorVersion(AccessResult actualValue);

  @Test
  void isAllowToReadUtkastOnDeceasedPatient() {
    setupMocksForDeceasedPatient();

    assertAllowToReadUtkastOnDeceasedPatient(
        accessService.allowToReadDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToReadUtkastOnDeceasedPatient(AccessResult actualValue);

  @Test
  void isAllowToReadUtkastOnInactiveUnit() {
    setupMocksForInactiveUnit();

    assertAllowToReadUtkastOnInactiveUnit(
        accessService.allowToReadDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToReadUtkastOnInactiveUnit(AccessResult actualValue);

  @Test
  void isAllowToReadUtkastOnRenewFalse() {
    setupMocksForOnRenewFalse();

    assertAllowToReadUtkastOnRenewFalse(
        accessService.allowToReadDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToReadUtkastOnRenewFalse(AccessResult actualValue);

  @Test
  void isAllowToReadOnSekretessPatientOnSameUnit() {
    setupMocksForOnSekretessPatient();

    assertAllowToReadOnSekretessPatientOnSameUnit(
        accessService.allowToReadDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToReadOnSekretessPatientOnSameUnit(AccessResult actualValue);

  @Test
  void isAllowToReadUtkastOnSekretessPatientOnDifferentUnit() {
    setupMocksForOnSekretessPatientDifferentUnit();

    assertAllowToReadUtkastOnSekretessPatientOnDifferentUnit(
        accessService.allowToReadDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToReadUtkastOnSekretessPatientOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToReadUtkastNoConditionsOnDifferentUnit() {
    setupMocksForNoConditionsDifferentUnit();

    assertAllowToReadUtkastNoConditionsOnDifferentUnit(
        accessService.allowToReadDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToReadUtkastNoConditionsOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToReadUtkastOnDeceasedPatientOnDifferentUnit() {
    setupMocksForDeceasedPatientDifferentUnit();

    assertAllowToReadUtkastOnDeceasedPatientOnDifferentUnit(
        accessService.allowToReadDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToReadUtkastOnDeceasedPatientOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToReadUtkastOnInactiveUnitOnDifferentUnit() {
    setupMocksForInactiveUnitDifferentUnit();

    assertAllowToReadUtkastOnInactiveUnitOnDifferentUnit(
        accessService.allowToReadDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToReadUtkastOnInactiveUnitOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToReadUtkastOnRenewFalseOnDifferentUnit() {
    setupMocksForOnRenewFalseDifferentUnit();

    assertAllowToReadUtkastOnRenewFalseOnDifferentUnit(
        accessService.allowToReadDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToReadUtkastOnRenewFalseOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToEditUtkastNoConditions() {
    setupMocksForNoConditions();

    assertAllowToEditUtkastNoConditions(
        accessService.allowToEditDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToEditUtkastNoConditions(AccessResult actualValue);

  @Test
  void isAllowToEditUtkastWhenMissingSubscription() {
    setupMocksForMissingSubscription();

    assertAllowToEditUtkastWhenMissingSubscription(
        accessService.allowToEditDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToEditUtkastWhenMissingSubscription(AccessResult actualValue);

  @Test
  void isAllowToEditUtkastNotLatestMajorVersion() {
    setupMocksForNotLatestMajorVersion();

    assertAllowToEditUtkastNotLatestMajorVersion(
        accessService.allowToEditDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToEditUtkastNotLatestMajorVersion(AccessResult actualValue);

  @Test
  void isAllowToEditUtkastOnDeceasedPatient() {
    setupMocksForDeceasedPatient();

    assertAllowToEditUtkastOnDeceasedPatient(
        accessService.allowToEditDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToEditUtkastOnDeceasedPatient(AccessResult actualValue);

  @Test
  void isAllowToEditUtkastOnInactiveUnit() {
    setupMocksForInactiveUnit();

    assertAllowToEditUtkastOnInactiveUnit(
        accessService.allowToEditDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToEditUtkastOnInactiveUnit(AccessResult actualValue);

  @Test
  void isAllowToEditUtkastOnRenewFalse() {
    setupMocksForOnRenewFalse();

    assertAllowToEditUtkastOnRenewFalse(
        accessService.allowToEditDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToEditUtkastOnRenewFalse(AccessResult actualValue);

  @Test
  void isAllowToEditUtkastOnSekretessPatientOnSameUnit() {
    setupMocksForOnSekretessPatient();

    assertAllowToEditUtkastOnSekretessPatientOnSameUnit(
        accessService.allowToEditDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToEditUtkastOnSekretessPatientOnSameUnit(
      AccessResult actualValue);

  @Test
  void isAllowToEditUtkastOnSekretessPatientOnDifferentUnit() {
    setupMocksForOnSekretessPatientDifferentUnit();

    assertAllowToEditUtkastOnSekretessPatientOnDifferentUnit(
        accessService.allowToEditDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToEditUtkastOnSekretessPatientOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToEditUtkastOnDeceasedPatientOnDifferentUnit() {
    setupMocksForDeceasedPatientDifferentUnit();

    assertAllowToEditUtkastOnDeceasedPatientOnDifferentUnit(
        accessService.allowToEditDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToEditUtkastOnDeceasedPatientOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToEditUtkastOnInactiveUnitOnDifferentUnit() {
    setupMocksForInactiveUnitDifferentUnit();

    assertAllowToEditUtkastOnInactiveUnitOnDifferentUnit(
        accessService.allowToEditDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToEditUtkastOnInactiveUnitOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToEditUtkastOnRenewFalseOnDifferentUnit() {
    setupMocksForOnRenewFalseDifferentUnit();

    assertAllowToEditUtkastOnRenewFalseOnDifferentUnit(
        accessService.allowToEditDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToEditUtkastOnRenewFalseOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToEditUtkastNoConditionsDifferentUnit() {
    setupMocksForNoConditionsDifferentUnit();

    assertAllowToEditUtkastNoConditionsDifferentUnit(
        accessService.allowToEditDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToEditUtkastNoConditionsDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToSignUtkastNoConditions() {
    setupMocksForNoConditions();

    assertAllowToSignUtkastNoConditions(
        accessService.allowToSignDraft(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, null));
  }

  protected abstract void assertAllowToSignUtkastNoConditions(AccessResult actualValue);

  @Test
  void isAllowToSignUtkastWhenMissingSubscription() {
    setupMocksForMissingSubscription();

    assertAllowToSignUtkastWhenMissingSubscription(
        accessService.allowToSignDraft(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, null));
  }

  protected abstract void assertAllowToSignUtkastWhenMissingSubscription(AccessResult actualValue);

  @Test
  void isAllowToSignUtkastNotLatestMajorVersion() {
    setupMocksForNotLatestMajorVersion();

    assertAllowToSignUtkastNotLatestMajorVersion(
        accessService.allowToSignDraft(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, null));
  }

  protected abstract void assertAllowToSignUtkastNotLatestMajorVersion(AccessResult actualValue);

  @Test
  void isAllowToSignUtkastOnDeceasedPatient() {
    setupMocksForDeceasedPatient();

    assertAllowToSignUtkastOnDeceasedPatient(
        accessService.allowToSignDraft(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, null));
  }

  protected abstract void assertAllowToSignUtkastOnDeceasedPatient(AccessResult actualValue);

  @Test
  void isAllowToSignUtkastOnInactiveUnit() {
    setupMocksForInactiveUnit();

    assertAllowToSignUtkastOnInactiveUnit(
        accessService.allowToSignDraft(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, null));
  }

  protected abstract void assertAllowToSignUtkastOnInactiveUnit(AccessResult actualValue);

  @Test
  void isAllowToSignUtkastOnRenewFalse() {
    setupMocksForOnRenewFalse();

    assertAllowToSignUtkastOnRenewFalse(
        accessService.allowToSignDraft(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, null));
  }

  protected abstract void assertAllowToSignUtkastOnRenewFalse(AccessResult actualValue);

  @Test
  void isAllowToSignUtkastOnSekretessPatientOnSameUnit() {
    setupMocksForOnSekretessPatient();

    assertAllowToSignUtkastOnSekretessPatientOnSameUnit(
        accessService.allowToSignDraft(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, null));
  }

  protected abstract void assertAllowToSignUtkastOnSekretessPatientOnSameUnit(
      AccessResult actualValue);

  @Test
  void isAllowToSignUtkastOnSekretessPatientOnDifferentUnit() {
    setupMocksForOnSekretessPatientDifferentUnit();

    assertAllowToSignUtkastOnSekretessPatientOnDifferentUnit(
        accessService.allowToSignDraft(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, null));
  }

  protected abstract void assertAllowToSignUtkastOnSekretessPatientOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToSignUtkastOnDeceasedPatientOnDifferentUnit() {
    setupMocksForDeceasedPatientDifferentUnit();

    assertAllowToSignUtkastOnDeceasedPatientOnDifferentUnit(
        accessService.allowToSignDraft(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, null));
  }

  protected abstract void assertAllowToSignUtkastOnDeceasedPatientOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToSignUtkastOnInactiveUnitOnDifferentUnit() {
    setupMocksForInactiveUnitDifferentUnit();

    assertAllowToSignUtkastOnInactiveUnitOnDifferentUnit(
        accessService.allowToSignDraft(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, null));
  }

  protected abstract void assertAllowToSignUtkastOnInactiveUnitOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToSignUtkastOnRenewFalseOnDifferentUnit() {
    setupMocksForOnRenewFalseDifferentUnit();

    assertAllowToSignUtkastOnRenewFalseOnDifferentUnit(
        accessService.allowToSignDraft(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, null));
  }

  protected abstract void assertAllowToSignUtkastOnRenewFalseOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToSignUtkastNoConditionsDifferentUnit() {
    setupMocksForNoConditionsDifferentUnit();

    assertAllowToSignUtkastNoConditionsDifferentUnit(
        accessService.allowToSignDraft(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, null));
  }

  protected abstract void assertAllowToSignUtkastNoConditionsDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToSignWithConfirmationNoConditions() {
    setupMocksForNoConditions();

    assertAllowToSignWithConfirmationNoConditions(
        accessService.allowToSignWithConfirmation(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  abstract void assertAllowToSignWithConfirmationNoConditions(AccessResult actualValue);

  @Test
  void isAllowToSignWithConfirmationWhenMissingSubscription() {
    setupMocksForMissingSubscription();

    assertAllowToSignWithConfirmationWhenMissingSubscription(
        accessService.allowToSignWithConfirmation(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  abstract void assertAllowToSignWithConfirmationWhenMissingSubscription(AccessResult actualValue);

  @Test
  void isAllowToSignWithConfirmationNotLatestMajorVersion() {
    setupMocksForNotLatestMajorVersion();

    assertAllowToSignWithConfirmationNotLatestMajorVersion(
        accessService.allowToSignDraft(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, null));
  }

  abstract void assertAllowToSignWithConfirmationNotLatestMajorVersion(AccessResult actualValue);

  @Test
  void isAllowToSignWithConfirmationOnDeceasedPatient() {
    setupMocksForDeceasedPatient();

    assertAllowToSignWithConfirmationOnDeceasedPatient(
        accessService.allowToSignWithConfirmation(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  abstract void assertAllowToSignWithConfirmationOnDeceasedPatient(AccessResult actualValue);

  @Test
  void isAllowToSignWithConfirmationOnInactiveUnit() {
    setupMocksForInactiveUnit();

    assertAllowToSignWithConfirmationOnInactiveUnit(
        accessService.allowToSignWithConfirmation(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  abstract void assertAllowToSignWithConfirmationOnInactiveUnit(AccessResult actualValue);

  @Test
  void isAllowToSignWithConfirmationOnRenewFalse() {
    setupMocksForOnRenewFalse();

    assertAllowToSignWithConfirmationOnRenewFalse(
        accessService.allowToSignWithConfirmation(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  abstract void assertAllowToSignWithConfirmationOnRenewFalse(AccessResult actualValue);

  @Test
  void isAllowToSignWithConfirmationOnSekretessPatientOnSameUnit() {
    setupMocksForOnSekretessPatient();

    assertAllowToSignWithConfirmationOnSekretessPatientOnSameUnit(
        accessService.allowToSignWithConfirmation(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  abstract void assertAllowToSignWithConfirmationOnSekretessPatientOnSameUnit(
      AccessResult actualValue);

  @Test
  void isAllowToSignWithConfirmationOnSekretessPatientOnDifferentUnit() {
    setupMocksForOnSekretessPatientDifferentUnit();

    assertAllowToSignWithConfirmationOnSekretessPatientOnDifferentUnit(
        accessService.allowToSignWithConfirmation(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  abstract void assertAllowToSignWithConfirmationOnSekretessPatientOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToSignWithConfirmationOnDeceasedPatientOnDifferentUnit() {
    setupMocksForDeceasedPatientDifferentUnit();

    assertAllowToSignWithConfirmationOnDeceasedPatientOnDifferentUnit(
        accessService.allowToSignWithConfirmation(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  abstract void assertAllowToSignWithConfirmationOnDeceasedPatientOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToSignWithConfirmationOnInactiveUnitOnDifferentUnit() {
    setupMocksForInactiveUnitDifferentUnit();

    assertAllowToSignWithConfirmationOnInactiveUnitOnDifferentUnit(
        accessService.allowToSignWithConfirmation(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  abstract void assertAllowToSignWithConfirmationOnInactiveUnitOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToSignWithConfirmationOnRenewFalseOnDifferentUnit() {
    setupMocksForOnRenewFalseDifferentUnit();

    assertAllowToSignWithConfirmationOnRenewFalseOnDifferentUnit(
        accessService.allowToSignWithConfirmation(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  abstract void assertAllowToSignWithConfirmationOnRenewFalseOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToSignWithConfirmationNoConditionsDifferentUnit() {
    setupMocksForNoConditionsDifferentUnit();

    assertAllowToSignWithConfirmationNoConditionsDifferentUnit(
        accessService.allowToSignWithConfirmation(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  abstract void assertAllowToSignWithConfirmationNoConditionsDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToDeleteUtkastNoConditions() {
    setupMocksForNoConditions();

    assertAllowToDeleteUtkastNoConditions(
        accessService.allowToDeleteDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToDeleteUtkastNoConditions(AccessResult actualValue);

  @Test
  void isAllowToDeleteUtkastWhenMissingSubscription() {
    setupMocksForMissingSubscription();

    assertAllowToDeleteUtkastWhenMissingSubscription(
        accessService.allowToDeleteDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToDeleteUtkastWhenMissingSubscription(
      AccessResult actualValue);

  @Test
  void isAllowToDeleteUtkastNotLatestMajorVersion() {
    setupMocksForNotLatestMajorVersion();

    assertAllowToDeleteUtkastNotLatestMajorVersion(
        accessService.allowToDeleteDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToDeleteUtkastNotLatestMajorVersion(AccessResult actualValue);

  @Test
  void isAllowToDeleteUtkastOnDeceasedPatient() {
    setupMocksForDeceasedPatient();

    assertAllowToDeleteUtkastOnDeceasedPatient(
        accessService.allowToDeleteDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToDeleteUtkastOnDeceasedPatient(AccessResult actualValue);

  @Test
  void isAllowToDeleteUtkastOnInactiveUnit() {
    setupMocksForInactiveUnit();

    assertAllowToDeleteUtkastOnInactiveUnit(
        accessService.allowToDeleteDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToDeleteUtkastOnInactiveUnit(AccessResult actualValue);

  @Test
  void isAllowToDeleteUtkastOnRenewFalse() {
    setupMocksForOnRenewFalse();

    assertAllowToDeleteUtkastOnRenewFalse(
        accessService.allowToDeleteDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToDeleteUtkastOnRenewFalse(AccessResult actualValue);

  @Test
  void isAllowToDeleteUtkastOnSekretessPatientOnSameUnit() {
    setupMocksForOnSekretessPatient();

    assertAllowToDeleteUtkastOnSekretessPatientOnSameUnit(
        accessService.allowToDeleteDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToDeleteUtkastOnSekretessPatientOnSameUnit(
      AccessResult actualValue);

  @Test
  void isAllowToDeleteUtkastOnSekretessPatientOnDifferentUnit() {
    setupMocksForOnSekretessPatientDifferentUnit();

    assertAllowToDeleteUtkastOnSekretessPatientOnDifferentUnit(
        accessService.allowToDeleteDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToDeleteUtkastOnSekretessPatientOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToDeleteUtkastOnDeceasedPatientOnDifferentUnit() {
    setupMocksForDeceasedPatientDifferentUnit();

    assertAllowToDeleteUtkastOnDeceasedPatientOnDifferentUnit(
        accessService.allowToDeleteDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToDeleteUtkastOnDeceasedPatientOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToDeleteUtkastOnInactiveUnitOnDifferentUnit() {
    setupMocksForInactiveUnitDifferentUnit();

    assertAllowToDeleteUtkastOnInactiveUnitOnDifferentUnit(
        accessService.allowToDeleteDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToDeleteUtkastOnInactiveUnitOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToDeleteUtkastOnRenewFalseOnDifferentUnit() {
    setupMocksForOnRenewFalseDifferentUnit();

    assertAllowToDeleteUtkastOnRenewFalseOnDifferentUnit(
        accessService.allowToDeleteDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToDeleteUtkastOnRenewFalseOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToDeleteUtkastNoConditionsDifferentUnit() {
    setupMocksForNoConditionsDifferentUnit();

    assertAllowToDeleteUtkastNoConditionsDifferentUnit(
        accessService.allowToDeleteDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToDeleteUtkastNoConditionsDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToPrintUtkastNoConditions() {
    setupMocksForNoConditions();

    assertAllowToPrintUtkastNoConditions(
        accessService.allowToPrintDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToPrintUtkastNoConditions(AccessResult actualValue);

  @Test
  void isAllowToPrintUtkastWhenMissingSubscription() {
    setupMocksForMissingSubscription();

    assertAllowToPrintUtkastWhenMissingSubscription(
        accessService.allowToPrintDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToPrintUtkastWhenMissingSubscription(AccessResult actualValue);

  @Test
  void isAllowToPrintUtkastNotLatestMajorVersion() {
    setupMocksForNotLatestMajorVersion();

    assertAllowToPrintUtkastNotLatestMajorVersion(
        accessService.allowToPrintDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToPrintUtkastNotLatestMajorVersion(AccessResult actualValue);

  @Test
  void isAllowToPrintUtkastOnDeceasedPatient() {
    setupMocksForDeceasedPatient();

    assertAllowToPrintUtkastOnDeceasedPatient(
        accessService.allowToPrintDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToPrintUtkastOnDeceasedPatient(AccessResult actualValue);

  @Test
  void isAllowToPrintUtkastOnInactiveUnit() {
    setupMocksForInactiveUnit();

    assertAllowToPrintUtkastOnInactiveUnit(
        accessService.allowToPrintDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToPrintUtkastOnInactiveUnit(AccessResult actualValue);

  @Test
  void isAllowToPrintUtkastOnRenewFalse() {
    setupMocksForOnRenewFalse();

    assertAllowToPrintUtkastOnRenewFalse(
        accessService.allowToPrintDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToPrintUtkastOnRenewFalse(AccessResult actualValue);

  @Test
  void isAllowToPrintUtkastOnSekretessPatientOnSameUnit() {
    setupMocksForOnSekretessPatient();

    assertAllowToPrintUtkastOnSekretessPatientOnSameUnit(
        accessService.allowToPrintDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToPrintUtkastOnSekretessPatientOnSameUnit(
      AccessResult actualValue);

  @Test
  void isAllowToPrintUtkastOnSekretessPatientOnDifferentUnit() {
    setupMocksForOnSekretessPatientDifferentUnit();

    assertAllowToPrintUtkastOnSekretessPatientOnDifferentUnit(
        accessService.allowToPrintDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToPrintUtkastOnSekretessPatientOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToPrintUtkastOnDeceasedPatientOnDifferentUnit() {
    setupMocksForDeceasedPatientDifferentUnit();

    assertAllowToPrintUtkastOnDeceasedPatientOnDifferentUnit(
        accessService.allowToPrintDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToPrintUtkastOnDeceasedPatientOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToPrintUtkastOnInactiveUnitOnDifferentUnit() {
    setupMocksForInactiveUnitDifferentUnit();

    assertAllowToPrintUtkastOnInactiveUnitOnDifferentUnit(
        accessService.allowToPrintDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToPrintUtkastOnInactiveUnitOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToPrintUtkastOnRenewFalseOnDifferentUnit() {
    setupMocksForOnRenewFalseDifferentUnit();

    assertAllowToPrintUtkastOnRenewFalseOnDifferentUnit(
        accessService.allowToPrintDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToPrintUtkastOnRenewFalseOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToPrintUtkastNoConditionsDifferentUnit() {
    setupMocksForNoConditionsDifferentUnit();

    assertAllowToPrintUtkastNoConditionsDifferentUnit(
        accessService.allowToPrintDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToPrintUtkastNoConditionsDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToForwardUtkastNoConditions() {
    setupMocksForNoConditions();

    assertAllowToForwardUtkastNoConditions(
        accessService.allowToForwardDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToForwardUtkastNoConditions(AccessResult actualValue);

  @Test
  void isAllowToForwardUtkastWhenMissingSubscription() {
    setupMocksForMissingSubscription();

    assertAllowToForwardUtkastWhenMissingSubscription(
        accessService.allowToForwardDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToForwardUtkastWhenMissingSubscription(
      AccessResult actualValue);

  @Test
  void isAllowToForwardUtkastNotLatestMajorVersion() {
    setupMocksForNotLatestMajorVersion();

    assertAllowToForwardUtkastNotLatestMajorVersion(
        accessService.allowToForwardDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToForwardUtkastNotLatestMajorVersion(AccessResult actualValue);

  @Test
  void isAllowToForwardUtkastOnDeceasedPatient() {
    setupMocksForDeceasedPatient();

    assertAllowToForwardUtkastOnDeceasedPatient(
        accessService.allowToForwardDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToForwardUtkastOnDeceasedPatient(AccessResult actualValue);

  @Test
  void isAllowToForwardUtkastOnInactiveUnit() {
    setupMocksForInactiveUnit();

    assertAllowToForwardUtkastOnInactiveUnit(
        accessService.allowToForwardDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToForwardUtkastOnInactiveUnit(AccessResult actualValue);

  @Test
  void isAllowToForwardUtkastOnRenewFalse() {
    setupMocksForOnRenewFalse();

    assertAllowToForwardUtkastOnRenewFalse(
        accessService.allowToForwardDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToForwardUtkastOnRenewFalse(AccessResult actualValue);

  @Test
  void isAllowToForwardUtkastOnSekretessPatientOnSameUnit() {
    setupMocksForOnSekretessPatient();

    assertAllowToForwardUtkastOnSekretessPatientOnSameUnit(
        accessService.allowToForwardDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToForwardUtkastOnSekretessPatientOnSameUnit(
      AccessResult actualValue);

  @Test
  void isAllowToForwardUtkastOnSekretessPatientOnDifferentUnit() {
    setupMocksForOnSekretessPatientDifferentUnit();

    assertAllowToForwardUtkastOnSekretessPatientOnDifferentUnit(
        accessService.allowToForwardDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToForwardUtkastOnSekretessPatientOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToForwardUtkastOnDeceasedPatientOnDifferentUnit() {
    setupMocksForDeceasedPatientDifferentUnit();

    assertAllowToForwardUtkastOnDeceasedPatientOnDifferentUnit(
        accessService.allowToForwardDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToForwardUtkastOnDeceasedPatientOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToForwardUtkastOnInactiveUnitOnDifferentUnit() {
    setupMocksForInactiveUnitDifferentUnit();

    assertAllowToForwardUtkastOnInactiveUnitOnDifferentUnit(
        accessService.allowToForwardDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToForwardUtkastOnInactiveUnitOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToForwardUtkastOnRenewFalseOnDifferentUnit() {
    setupMocksForOnRenewFalseDifferentUnit();

    assertAllowToForwardUtkastOnRenewFalseOnDifferentUnit(
        accessService.allowToForwardDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToForwardUtkastOnRenewFalseOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToForwardUtkastNoConditionsDifferentUnit() {
    setupMocksForNoConditionsDifferentUnit();

    assertAllowToForwardUtkastNoConditionsDifferentUnit(
        accessService.allowToForwardDraft(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToForwardUtkastNoConditionsDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToReadyForSignNoConditions() {
    setupMocksForNoConditions();

    assertAllowToReadyForSignNoConditions(
        accessService.allowToReadyForSign(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToReadyForSignNoConditions(AccessResult actualValue);

  @Test
  void isAllowToReadyForSignWhenMissingSubscription() {
    setupMocksForMissingSubscription();

    assertAllowToReadyForSignWhenMissingSubscription(
        accessService.allowToReadyForSign(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToReadyForSignWhenMissingSubscription(
      AccessResult actualValue);

  @Test
  void isAllowToReadyForSignNotLatestMajorVersion() {
    setupMocksForNotLatestMajorVersion();

    assertAllowToReadyForSignNotLatestMajorVersion(
        accessService.allowToReadyForSign(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToReadyForSignNotLatestMajorVersion(AccessResult actualValue);

  @Test
  void isAllowToReadyForSignOnDeceasedPatient() {
    setupMocksForDeceasedPatient();

    assertAllowToReadyForSignOnDeceasedPatient(
        accessService.allowToReadyForSign(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToReadyForSignOnDeceasedPatient(AccessResult actualValue);

  @Test
  void isAllowToReadyForSignOnInactiveUnit() {
    setupMocksForInactiveUnit();

    assertAllowToReadyForSignOnInactiveUnit(
        accessService.allowToReadyForSign(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToReadyForSignOnInactiveUnit(AccessResult actualValue);

  @Test
  void isAllowToReadyForSignOnRenewFalse() {
    setupMocksForOnRenewFalse();

    assertAllowToReadyForSignOnRenewFalse(
        accessService.allowToReadyForSign(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToReadyForSignOnRenewFalse(AccessResult actualValue);

  @Test
  void isAllowToReadyForSignOnSekretessPatientOnSameUnit() {
    setupMocksForOnSekretessPatient();

    assertAllowToReadyForSignOnSekretessPatientOnSameUnit(
        accessService.allowToReadyForSign(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToReadyForSignOnSekretessPatientOnSameUnit(
      AccessResult actualValue);

  @Test
  void isAllowToReadyForSignOnSekretessPatientOnDifferentUnit() {
    setupMocksForOnSekretessPatientDifferentUnit();

    assertAllowToReadyForSignOnSekretessPatientOnDifferentUnit(
        accessService.allowToReadyForSign(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToReadyForSignOnSekretessPatientOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToReadyForSignOnDeceasedPatientOnDifferentUnit() {
    setupMocksForDeceasedPatientDifferentUnit();

    assertAllowToReadyForSignOnDeceasedPatientOnDifferentUnit(
        accessService.allowToReadyForSign(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToReadyForSignOnDeceasedPatientOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToReadyForSignOnInactiveUnitOnDifferentUnit() {
    setupMocksForInactiveUnitDifferentUnit();

    assertAllowToReadyForSignOnInactiveUnitOnDifferentUnit(
        accessService.allowToReadyForSign(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToReadyForSignOnInactiveUnitOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToReadyForSignOnRenewFalseOnDifferentUnit() {
    setupMocksForOnRenewFalseDifferentUnit();

    assertAllowToReadyForSignOnRenewFalseOnDifferentUnit(
        accessService.allowToReadyForSign(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToReadyForSignOnRenewFalseOnDifferentUnit(
      AccessResult actualValue);

  @Test
  void isAllowToReadyForSignNoConditionsDifferentUnit() {
    setupMocksForNoConditionsDifferentUnit();

    assertAllowToReadyForSignNoConditionsDifferentUnit(
        accessService.allowToReadyForSign(
            AccessEvaluationParameters.create(
                intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
  }

  protected abstract void assertAllowToReadyForSignNoConditionsDifferentUnit(
      AccessResult actualValue);
}
