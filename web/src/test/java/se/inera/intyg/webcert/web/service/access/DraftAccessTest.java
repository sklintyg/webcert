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
package se.inera.intyg.webcert.web.service.access;

import static org.mockito.Mockito.doReturn;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.web.service.access.data.AccessServiceTestData;
import se.inera.intyg.webcert.web.service.access.util.AccessServiceTestToolkit;
import se.inera.intyg.webcert.web.service.subscription.dto.SubscriptionInfo;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

abstract public class DraftAccessTest extends AccessTest {

    @InjectMocks
    private DraftAccessServiceImpl accessService;

    DraftAccessTest(String intygsTyp, AccessServiceTestData accessServiceTestData) {
        super(intygsTyp, accessServiceTestData);
    }

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void isAllowedToCreateUtkastNoConditions() {
        setupMocksForNoConditions();

        assertAllowedToCreateUtkastNoConditions(accessService.allowToCreateDraft(
            AccessEvaluationParameters.create(intygsTyp, PERSONNUMMER)
        ));
    }

    abstract protected void assertAllowedToCreateUtkastNoConditions(AccessResult actualValue);

    @Test
    public void isAllowedToCreateUtkastWhenMissingSubscription() {
        setupMocksForMissingSubscription();

        assertAllowedToCreateUtkastWhenMissingSubscription(accessService.allowToCreateDraft(
            AccessEvaluationParameters.create(intygsTyp, PERSONNUMMER)
        ));
    }

    abstract protected void assertAllowedToCreateUtkastWhenMissingSubscription(AccessResult actualValue);

    @Test
    public void isAllowToCreateUtkastNotLatestMajorVersion() {
        setupMocksForNotLatestMajorVersion();

        assertAllowToCreateUtkastNotLatestMajorVersion(accessService.allowToCreateDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToCreateUtkastNotLatestMajorVersion(AccessResult actualValue);

    @Test
    public void isAllowedToCreateUtkastOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowedToCreateUtkastOnDeceasedPatient(accessService.allowToCreateDraft(
            AccessEvaluationParameters.create(intygsTyp, PERSONNUMMER)
        ));
    }

    abstract protected void assertAllowedToCreateUtkastOnDeceasedPatient(AccessResult actualValue);

    @Test
    public void isAllowedToCreateUtkastOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowedToCreateUtkastOnInactiveCareUnit(accessService.allowToCreateDraft(
            AccessEvaluationParameters.create(intygsTyp, PERSONNUMMER)
        ));
    }

    abstract protected void assertAllowedToCreateUtkastOnInactiveCareUnit(AccessResult actualValue);

    @Test
    public void isAllowedToCreateUtkastOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowedToCreateUtkastOnRenewFalse(accessService.allowToCreateDraft(
            AccessEvaluationParameters.create(intygsTyp, PERSONNUMMER)
        ));
    }

    abstract protected void assertAllowedToCreateUtkastOnRenewFalse(AccessResult actualValue);

    @Test
    public void isAllowToCreateUtkastOnSameCareProviderWhenUtkastSameVGExists() {
        final WebCertUser user = AccessServiceTestToolkit.createUser(intygsTyp, accessServiceTestData);
        doReturn(user)
            .when(webCertUserService).getUser();
        doReturn(false)
            .when(patientDetailsResolver).isAvliden(PERSONNUMMER);
        doReturn(SekretessStatus.FALSE)
            .when(patientDetailsResolver).getSekretessStatus(PERSONNUMMER);
        doReturn(true)
            .when(webCertUserService).isUserAllowedAccessToUnit(enhetsId);
        doReturn(AccessServiceTestToolkit.createPreviousUtkastForUtkast(intygsTyp))
            .when(utkastService).checkIfPersonHasExistingIntyg(PERSONNUMMER, user, null);
        doReturn(selectedVardgivare).when(user).getValdVardgivare();
        doReturn(vardgivarId).when(selectedVardgivare).getId();
        doReturn(selectedVardenhet).when(user).getValdVardenhet();
        doReturn(enhetsId).when(selectedVardenhet).getId();
        doReturn(new SubscriptionInfo()).when(user).getSubscriptionInfo();

        assertAllowToCreateUtkastOnSameCareProviderWhenUtkastSameVGExists(accessService.allowToCreateDraft(
            AccessEvaluationParameters.create(intygsTyp, PERSONNUMMER)
        ));
    }

    abstract protected void assertAllowToCreateUtkastOnSameCareProviderWhenUtkastSameVGExists(AccessResult actualValue);

    @Test
    public void isAllowToCreateUtkastOnDifferentCareProviderWhenIntygSameVGExists() {
        final WebCertUser user = AccessServiceTestToolkit.createUser(intygsTyp, accessServiceTestData);
        doReturn(user)
            .when(webCertUserService).getUser();
        doReturn(false)
            .when(patientDetailsResolver).isAvliden(PERSONNUMMER);
        doReturn(SekretessStatus.FALSE)
            .when(patientDetailsResolver).getSekretessStatus(PERSONNUMMER);
        doReturn(true)
            .when(webCertUserService).isUserAllowedAccessToUnit(enhetsId);
        doReturn(AccessServiceTestToolkit.createPreviousIntygForUtkast(intygsTyp, true))
            .when(utkastService).checkIfPersonHasExistingIntyg(PERSONNUMMER, user, null);
        doReturn(selectedVardgivare).when(user).getValdVardgivare();
        doReturn(vardgivarId).when(selectedVardgivare).getId();
        doReturn(selectedVardenhet).when(user).getValdVardenhet();
        doReturn(enhetsId).when(selectedVardenhet).getId();
        doReturn(new SubscriptionInfo()).when(user).getSubscriptionInfo();

        assertAllowToCreateUtkastOnDifferentCareProviderWhenIntygSameVGExists(accessService.allowToCreateDraft(
            AccessEvaluationParameters.create(intygsTyp, PERSONNUMMER)
        ));
    }

    abstract protected void assertAllowToCreateUtkastOnDifferentCareProviderWhenIntygSameVGExists(AccessResult actualValue);

    @Test
    public void isAllowToCreateUtkastOnSameCareProviderWhenIntygExists() {
        final WebCertUser user = AccessServiceTestToolkit.createUser(intygsTyp, accessServiceTestData);
        doReturn(user)
            .when(webCertUserService).getUser();
        doReturn(false)
            .when(patientDetailsResolver).isAvliden(PERSONNUMMER);
        doReturn(SekretessStatus.FALSE)
            .when(patientDetailsResolver).getSekretessStatus(PERSONNUMMER);
        doReturn(true)
            .when(webCertUserService).isUserAllowedAccessToUnit(enhetsId);
        doReturn(AccessServiceTestToolkit.createPreviousIntygForUtkast(intygsTyp, false))
            .when(utkastService).checkIfPersonHasExistingIntyg(PERSONNUMMER, user, null);
        doReturn(selectedVardgivare).when(user).getValdVardgivare();
        doReturn(vardgivarId).when(selectedVardgivare).getId();
        doReturn(selectedVardenhet).when(user).getValdVardenhet();
        doReturn(enhetsId).when(selectedVardenhet).getId();
        doReturn(new SubscriptionInfo()).when(user).getSubscriptionInfo();

        assertAllowToCreateUtkastOnSameCareProviderWhenIntygExists(accessService.allowToCreateDraft(
            AccessEvaluationParameters.create(intygsTyp, PERSONNUMMER)
        ));
    }

    abstract protected void assertAllowToCreateUtkastOnSameCareProviderWhenIntygExists(AccessResult actualValue);

    @Test
    public void isAllowedToCreateUtkastOnSekretessPatient() {
        setupMocksForOnSekretessPatient();

        assertAllowedToCreateUtkastOnSekretessPatient(accessService.allowToCreateDraft(
            AccessEvaluationParameters.create(intygsTyp, PERSONNUMMER)
        ));
    }

    abstract protected void assertAllowedToCreateUtkastOnSekretessPatient(AccessResult actualValue);

    @Test
    public void isAllowToReadUtkastNoConditions() {
        setupMocksForNoConditions();

        assertAllowToReadUtkastNoConditions(accessService.allowToReadDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToReadUtkastNoConditions(AccessResult actualValue);

    @Test
    public void isAllowToReadUtkastWhenMissingSubscription() {
        setupMocksForMissingSubscription();

        assertAllowToReadUtkastWhenMissingSubscription(accessService.allowToReadDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToReadUtkastWhenMissingSubscription(AccessResult actualValue);

    @Test
    public void isAllowToReadUtkastNotLatestMajorVersion() {
        setupMocksForNotLatestMajorVersion();

        assertAllowToReadUtkastNotLatestMajorVersion(accessService.allowToReadDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToReadUtkastNotLatestMajorVersion(AccessResult actualValue);

    @Test
    public void isAllowToReadUtkastOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToReadUtkastOnDeceasedPatient(accessService.allowToReadDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToReadUtkastOnDeceasedPatient(AccessResult actualValue);


    @Test
    public void isAllowToReadUtkastOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToReadUtkastOnInactiveUnit(accessService.allowToReadDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToReadUtkastOnInactiveUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadUtkastOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToReadUtkastOnRenewFalse(accessService.allowToReadDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToReadUtkastOnRenewFalse(AccessResult actualValue);

    @Test
    public void isAllowToReadOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToReadOnSekretessPatientOnSameUnit(accessService.allowToReadDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToReadOnSekretessPatientOnSameUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadUtkastOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToReadUtkastOnSekretessPatientOnDifferentUnit(accessService.allowToReadDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToReadUtkastOnSekretessPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadUtkastNoConditionsOnDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToReadUtkastNoConditionsOnDifferentUnit(accessService.allowToReadDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToReadUtkastNoConditionsOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadUtkastOnDeceasedPatientOnDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToReadUtkastOnDeceasedPatientOnDifferentUnit(accessService.allowToReadDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToReadUtkastOnDeceasedPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadUtkastOnInactiveUnitOnDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToReadUtkastOnInactiveUnitOnDifferentUnit(accessService.allowToReadDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToReadUtkastOnInactiveUnitOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadUtkastOnRenewFalseOnDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToReadUtkastOnRenewFalseOnDifferentUnit(accessService.allowToReadDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToReadUtkastOnRenewFalseOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToEditUtkastNoConditions() {
        setupMocksForNoConditions();

        assertAllowToEditUtkastNoConditions(accessService.allowToEditDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToEditUtkastNoConditions(AccessResult actualValue);

    @Test
    public void isAllowToEditUtkastWhenMissingSubscription() {
        setupMocksForMissingSubscription();

        assertAllowToEditUtkastWhenMissingSubscription(accessService.allowToEditDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToEditUtkastWhenMissingSubscription(AccessResult actualValue);

    @Test
    public void isAllowToEditUtkastNotLatestMajorVersion() {
        setupMocksForNotLatestMajorVersion();

        assertAllowToEditUtkastNotLatestMajorVersion(accessService.allowToEditDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToEditUtkastNotLatestMajorVersion(AccessResult actualValue);

    @Test
    public void isAllowToEditUtkastOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToEditUtkastOnDeceasedPatient(accessService.allowToEditDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToEditUtkastOnDeceasedPatient(AccessResult actualValue);

    @Test
    public void isAllowToEditUtkastOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToEditUtkastOnInactiveUnit(accessService.allowToEditDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToEditUtkastOnInactiveUnit(AccessResult actualValue);

    @Test
    public void isAllowToEditUtkastOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToEditUtkastOnRenewFalse(accessService.allowToEditDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToEditUtkastOnRenewFalse(AccessResult actualValue);

    @Test
    public void isAllowToEditUtkastOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToEditUtkastOnSekretessPatientOnSameUnit(accessService.allowToEditDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToEditUtkastOnSekretessPatientOnSameUnit(AccessResult actualValue);

    @Test
    public void isAllowToEditUtkastOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToEditUtkastOnSekretessPatientOnDifferentUnit(accessService.allowToEditDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToEditUtkastOnSekretessPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToEditUtkastOnDeceasedPatientOnDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToEditUtkastOnDeceasedPatientOnDifferentUnit(accessService.allowToEditDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToEditUtkastOnDeceasedPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToEditUtkastOnInactiveUnitOnDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToEditUtkastOnInactiveUnitOnDifferentUnit(accessService.allowToEditDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToEditUtkastOnInactiveUnitOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToEditUtkastOnRenewFalseOnDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToEditUtkastOnRenewFalseOnDifferentUnit(accessService.allowToEditDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToEditUtkastOnRenewFalseOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToEditUtkastNoConditionsDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToEditUtkastNoConditionsDifferentUnit(accessService.allowToEditDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToEditUtkastNoConditionsDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToSignUtkastNoConditions() {
        setupMocksForNoConditions();

        assertAllowToSignUtkastNoConditions(accessService.allowToSignDraft(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, null));
    }

    abstract protected void assertAllowToSignUtkastNoConditions(AccessResult actualValue);

    @Test
    public void isAllowToSignUtkastWhenMissingSubscription() {
        setupMocksForMissingSubscription();

        assertAllowToSignUtkastWhenMissingSubscription(
            accessService.allowToSignDraft(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, null));
    }

    abstract protected void assertAllowToSignUtkastWhenMissingSubscription(AccessResult actualValue);

    @Test
    public void isAllowToSignUtkastNotLatestMajorVersion() {
        setupMocksForNotLatestMajorVersion();

        assertAllowToSignUtkastNotLatestMajorVersion(
            accessService.allowToSignDraft(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, null));
    }

    abstract protected void assertAllowToSignUtkastNotLatestMajorVersion(AccessResult actualValue);

    @Test
    public void isAllowToSignUtkastOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToSignUtkastOnDeceasedPatient(
            accessService.allowToSignDraft(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, null));
    }

    abstract protected void assertAllowToSignUtkastOnDeceasedPatient(AccessResult actualValue);

    @Test
    public void isAllowToSignUtkastOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToSignUtkastOnInactiveUnit(accessService.allowToSignDraft(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, null));
    }

    abstract protected void assertAllowToSignUtkastOnInactiveUnit(AccessResult actualValue);

    @Test
    public void isAllowToSignUtkastOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToSignUtkastOnRenewFalse(accessService.allowToSignDraft(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, null));
    }

    abstract protected void assertAllowToSignUtkastOnRenewFalse(AccessResult actualValue);

    @Test
    public void isAllowToSignUtkastOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToSignUtkastOnSekretessPatientOnSameUnit(
            accessService.allowToSignDraft(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, null));
    }

    abstract protected void assertAllowToSignUtkastOnSekretessPatientOnSameUnit(AccessResult actualValue);

    @Test
    public void isAllowToSignUtkastOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToSignUtkastOnSekretessPatientOnDifferentUnit(
            accessService.allowToSignDraft(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, null));
    }

    abstract protected void assertAllowToSignUtkastOnSekretessPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToSignUtkastOnDeceasedPatientOnDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToSignUtkastOnDeceasedPatientOnDifferentUnit(
            accessService.allowToSignDraft(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, null));
    }

    abstract protected void assertAllowToSignUtkastOnDeceasedPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToSignUtkastOnInactiveUnitOnDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToSignUtkastOnInactiveUnitOnDifferentUnit(
            accessService.allowToSignDraft(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, null));
    }

    abstract protected void assertAllowToSignUtkastOnInactiveUnitOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToSignUtkastOnRenewFalseOnDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToSignUtkastOnRenewFalseOnDifferentUnit(
            accessService.allowToSignDraft(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, null));
    }

    abstract protected void assertAllowToSignUtkastOnRenewFalseOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToSignUtkastNoConditionsDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToSignUtkastNoConditionsDifferentUnit(
            accessService.allowToSignDraft(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, null));
    }

    abstract protected void assertAllowToSignUtkastNoConditionsDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToSignWithConfirmationNoConditions() {
        setupMocksForNoConditions();

        assertAllowToSignWithConfirmationNoConditions(accessService.allowToSignWithConfirmation(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
    }

    abstract void assertAllowToSignWithConfirmationNoConditions(AccessResult actualValue);

    @Test
    public void isAllowToSignWithConfirmationWhenMissingSubscription() {
        setupMocksForMissingSubscription();

        assertAllowToSignWithConfirmationWhenMissingSubscription(
            accessService.allowToSignWithConfirmation(
                AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
    }

    abstract void assertAllowToSignWithConfirmationWhenMissingSubscription(AccessResult actualValue);

    @Test
    public void isAllowToSignWithConfirmationNotLatestMajorVersion() {
        setupMocksForNotLatestMajorVersion();

        assertAllowToSignWithConfirmationNotLatestMajorVersion(
            accessService.allowToSignDraft(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, null));
    }

    abstract void assertAllowToSignWithConfirmationNotLatestMajorVersion(AccessResult actualValue);

    @Test
    public void isAllowToSignWithConfirmationOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToSignWithConfirmationOnDeceasedPatient(
            accessService.allowToSignWithConfirmation(
                AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
    }

    abstract void assertAllowToSignWithConfirmationOnDeceasedPatient(AccessResult actualValue);

    @Test
    public void isAllowToSignWithConfirmationOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToSignWithConfirmationOnInactiveUnit(
            accessService.allowToSignWithConfirmation(
                AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
    }

    abstract void assertAllowToSignWithConfirmationOnInactiveUnit(AccessResult actualValue);

    @Test
    public void isAllowToSignWithConfirmationOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToSignWithConfirmationOnRenewFalse(
            accessService.allowToSignWithConfirmation(
                AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
    }

    abstract void assertAllowToSignWithConfirmationOnRenewFalse(AccessResult actualValue);

    @Test
    public void isAllowToSignWithConfirmationOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToSignWithConfirmationOnSekretessPatientOnSameUnit(
            accessService.allowToSignWithConfirmation(
                AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
    }

    abstract void assertAllowToSignWithConfirmationOnSekretessPatientOnSameUnit(AccessResult actualValue);

    @Test
    public void isAllowToSignWithConfirmationOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToSignWithConfirmationOnSekretessPatientOnDifferentUnit(
            accessService.allowToSignWithConfirmation(
                AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
    }

    abstract void assertAllowToSignWithConfirmationOnSekretessPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToSignWithConfirmationOnDeceasedPatientOnDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToSignWithConfirmationOnDeceasedPatientOnDifferentUnit(
            accessService.allowToSignWithConfirmation(
                AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
    }

    abstract void assertAllowToSignWithConfirmationOnDeceasedPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToSignWithConfirmationOnInactiveUnitOnDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToSignWithConfirmationOnInactiveUnitOnDifferentUnit(
            accessService.allowToSignWithConfirmation(
                AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
    }

    abstract void assertAllowToSignWithConfirmationOnInactiveUnitOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToSignWithConfirmationOnRenewFalseOnDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToSignWithConfirmationOnRenewFalseOnDifferentUnit(
            accessService.allowToSignWithConfirmation(
                AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
    }

    abstract void assertAllowToSignWithConfirmationOnRenewFalseOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToSignWithConfirmationNoConditionsDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToSignWithConfirmationNoConditionsDifferentUnit(
            accessService.allowToSignWithConfirmation(
                AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)));
    }

    abstract void assertAllowToSignWithConfirmationNoConditionsDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToDeleteUtkastNoConditions() {
        setupMocksForNoConditions();

        assertAllowToDeleteUtkastNoConditions(accessService.allowToDeleteDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToDeleteUtkastNoConditions(AccessResult actualValue);

    @Test
    public void isAllowToDeleteUtkastWhenMissingSubscription() {
        setupMocksForMissingSubscription();

        assertAllowToDeleteUtkastWhenMissingSubscription(accessService.allowToDeleteDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToDeleteUtkastWhenMissingSubscription(AccessResult actualValue);

    @Test
    public void isAllowToDeleteUtkastNotLatestMajorVersion() {
        setupMocksForNotLatestMajorVersion();

        assertAllowToDeleteUtkastNotLatestMajorVersion(accessService.allowToDeleteDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToDeleteUtkastNotLatestMajorVersion(AccessResult actualValue);

    @Test
    public void isAllowToDeleteUtkastOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToDeleteUtkastOnDeceasedPatient(accessService.allowToDeleteDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToDeleteUtkastOnDeceasedPatient(AccessResult actualValue);

    @Test
    public void isAllowToDeleteUtkastOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToDeleteUtkastOnInactiveUnit(accessService.allowToDeleteDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToDeleteUtkastOnInactiveUnit(AccessResult actualValue);

    @Test
    public void isAllowToDeleteUtkastOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToDeleteUtkastOnRenewFalse(accessService.allowToDeleteDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToDeleteUtkastOnRenewFalse(AccessResult actualValue);

    @Test
    public void isAllowToDeleteUtkastOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToDeleteUtkastOnSekretessPatientOnSameUnit(accessService.allowToDeleteDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToDeleteUtkastOnSekretessPatientOnSameUnit(AccessResult actualValue);

    @Test
    public void isAllowToDeleteUtkastOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToDeleteUtkastOnSekretessPatientOnDifferentUnit(accessService.allowToDeleteDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToDeleteUtkastOnSekretessPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToDeleteUtkastOnDeceasedPatientOnDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToDeleteUtkastOnDeceasedPatientOnDifferentUnit(accessService.allowToDeleteDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToDeleteUtkastOnDeceasedPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToDeleteUtkastOnInactiveUnitOnDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToDeleteUtkastOnInactiveUnitOnDifferentUnit(accessService.allowToDeleteDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToDeleteUtkastOnInactiveUnitOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToDeleteUtkastOnRenewFalseOnDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToDeleteUtkastOnRenewFalseOnDifferentUnit(accessService.allowToDeleteDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToDeleteUtkastOnRenewFalseOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToDeleteUtkastNoConditionsDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToDeleteUtkastNoConditionsDifferentUnit(accessService.allowToDeleteDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToDeleteUtkastNoConditionsDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToPrintUtkastNoConditions() {
        setupMocksForNoConditions();

        assertAllowToPrintUtkastNoConditions(accessService.allowToPrintDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToPrintUtkastNoConditions(AccessResult actualValue);

    @Test
    public void isAllowToPrintUtkastWhenMissingSubscription() {
        setupMocksForMissingSubscription();

        assertAllowToPrintUtkastWhenMissingSubscription(accessService.allowToPrintDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToPrintUtkastWhenMissingSubscription(AccessResult actualValue);

    @Test
    public void isAllowToPrintUtkastNotLatestMajorVersion() {
        setupMocksForNotLatestMajorVersion();

        assertAllowToPrintUtkastNotLatestMajorVersion(accessService.allowToPrintDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToPrintUtkastNotLatestMajorVersion(AccessResult actualValue);

    @Test
    public void isAllowToPrintUtkastOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToPrintUtkastOnDeceasedPatient(accessService.allowToPrintDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToPrintUtkastOnDeceasedPatient(AccessResult actualValue);

    @Test
    public void isAllowToPrintUtkastOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToPrintUtkastOnInactiveUnit(accessService.allowToPrintDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToPrintUtkastOnInactiveUnit(AccessResult actualValue);

    @Test
    public void isAllowToPrintUtkastOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToPrintUtkastOnRenewFalse(accessService.allowToPrintDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToPrintUtkastOnRenewFalse(AccessResult actualValue);

    @Test
    public void isAllowToPrintUtkastOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToPrintUtkastOnSekretessPatientOnSameUnit(accessService.allowToPrintDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToPrintUtkastOnSekretessPatientOnSameUnit(AccessResult actualValue);

    @Test
    public void isAllowToPrintUtkastOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToPrintUtkastOnSekretessPatientOnDifferentUnit(accessService.allowToPrintDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToPrintUtkastOnSekretessPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToPrintUtkastOnDeceasedPatientOnDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToPrintUtkastOnDeceasedPatientOnDifferentUnit(accessService.allowToPrintDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToPrintUtkastOnDeceasedPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToPrintUtkastOnInactiveUnitOnDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToPrintUtkastOnInactiveUnitOnDifferentUnit(accessService.allowToPrintDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToPrintUtkastOnInactiveUnitOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToPrintUtkastOnRenewFalseOnDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToPrintUtkastOnRenewFalseOnDifferentUnit(accessService.allowToPrintDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToPrintUtkastOnRenewFalseOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToPrintUtkastNoConditionsDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToPrintUtkastNoConditionsDifferentUnit(accessService.allowToPrintDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToPrintUtkastNoConditionsDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToForwardUtkastNoConditions() {
        setupMocksForNoConditions();

        assertAllowToForwardUtkastNoConditions(accessService.allowToForwardDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToForwardUtkastNoConditions(AccessResult actualValue);

    @Test
    public void isAllowToForwardUtkastWhenMissingSubscription() {
        setupMocksForMissingSubscription();

        assertAllowToForwardUtkastWhenMissingSubscription(accessService.allowToForwardDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToForwardUtkastWhenMissingSubscription(AccessResult actualValue);

    @Test
    public void isAllowToForwardUtkastNotLatestMajorVersion() {
        setupMocksForNotLatestMajorVersion();

        assertAllowToForwardUtkastNotLatestMajorVersion(accessService.allowToForwardDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToForwardUtkastNotLatestMajorVersion(AccessResult actualValue);

    @Test
    public void isAllowToForwardUtkastOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToForwardUtkastOnDeceasedPatient(accessService.allowToForwardDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToForwardUtkastOnDeceasedPatient(AccessResult actualValue);

    @Test
    public void isAllowToForwardUtkastOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToForwardUtkastOnInactiveUnit(accessService.allowToForwardDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToForwardUtkastOnInactiveUnit(AccessResult actualValue);

    @Test
    public void isAllowToForwardUtkastOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToForwardUtkastOnRenewFalse(accessService.allowToForwardDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToForwardUtkastOnRenewFalse(AccessResult actualValue);

    @Test
    public void isAllowToForwardUtkastOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToForwardUtkastOnSekretessPatientOnSameUnit(accessService.allowToForwardDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToForwardUtkastOnSekretessPatientOnSameUnit(AccessResult actualValue);

    @Test
    public void isAllowToForwardUtkastOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToForwardUtkastOnSekretessPatientOnDifferentUnit(accessService.allowToForwardDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToForwardUtkastOnSekretessPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToForwardUtkastOnDeceasedPatientOnDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToForwardUtkastOnDeceasedPatientOnDifferentUnit(accessService.allowToForwardDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToForwardUtkastOnDeceasedPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToForwardUtkastOnInactiveUnitOnDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToForwardUtkastOnInactiveUnitOnDifferentUnit(accessService.allowToForwardDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToForwardUtkastOnInactiveUnitOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToForwardUtkastOnRenewFalseOnDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToForwardUtkastOnRenewFalseOnDifferentUnit(accessService.allowToForwardDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToForwardUtkastOnRenewFalseOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToForwardUtkastNoConditionsDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToForwardUtkastNoConditionsDifferentUnit(accessService.allowToForwardDraft(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToForwardUtkastNoConditionsDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadyForSignNoConditions() {
        setupMocksForNoConditions();

        assertAllowToReadyForSignNoConditions(accessService.allowToReadyForSign(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToReadyForSignNoConditions(AccessResult actualValue);

    @Test
    public void isAllowToReadyForSignWhenMissingSubscription() {
        setupMocksForMissingSubscription();

        assertAllowToReadyForSignWhenMissingSubscription(accessService.allowToReadyForSign(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToReadyForSignWhenMissingSubscription(AccessResult actualValue);

    @Test
    public void isAllowToReadyForSignNotLatestMajorVersion() {
        setupMocksForNotLatestMajorVersion();

        assertAllowToReadyForSignNotLatestMajorVersion(accessService.allowToReadyForSign(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToReadyForSignNotLatestMajorVersion(AccessResult actualValue);

    @Test
    public void isAllowToReadyForSignOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToReadyForSignOnDeceasedPatient(accessService.allowToReadyForSign(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToReadyForSignOnDeceasedPatient(AccessResult actualValue);

    @Test
    public void isAllowToReadyForSignOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToReadyForSignOnInactiveUnit(accessService.allowToReadyForSign(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToReadyForSignOnInactiveUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadyForSignOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToReadyForSignOnRenewFalse(accessService.allowToReadyForSign(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToReadyForSignOnRenewFalse(AccessResult actualValue);

    @Test
    public void isAllowToReadyForSignOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToReadyForSignOnSekretessPatientOnSameUnit(accessService.allowToReadyForSign(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToReadyForSignOnSekretessPatientOnSameUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadyForSignOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToReadyForSignOnSekretessPatientOnDifferentUnit(accessService.allowToReadyForSign(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToReadyForSignOnSekretessPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadyForSignOnDeceasedPatientOnDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToReadyForSignOnDeceasedPatientOnDifferentUnit(accessService.allowToReadyForSign(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToReadyForSignOnDeceasedPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadyForSignOnInactiveUnitOnDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToReadyForSignOnInactiveUnitOnDifferentUnit(accessService.allowToReadyForSign(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToReadyForSignOnInactiveUnitOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadyForSignOnRenewFalseOnDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToReadyForSignOnRenewFalseOnDifferentUnit(accessService.allowToReadyForSign(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToReadyForSignOnRenewFalseOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadyForSignNoConditionsDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToReadyForSignNoConditionsDifferentUnit(accessService.allowToReadyForSign(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToReadyForSignNoConditionsDifferentUnit(AccessResult actualValue);
}
