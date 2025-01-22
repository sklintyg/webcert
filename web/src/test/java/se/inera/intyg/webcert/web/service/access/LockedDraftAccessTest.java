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

abstract public class LockedDraftAccessTest extends AccessTest {

    @InjectMocks
    private LockedDraftAccessServiceImpl accessService;

    LockedDraftAccessTest(String intygsTyp, AccessServiceTestData accessServiceTestData) {
        super(intygsTyp, accessServiceTestData);
    }

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void isAllowToReadUtkastNoConditions() {
        setupMocksForNoConditions();

        assertAllowToReadUtkastNoConditions(accessService.allowToRead(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToReadUtkastNoConditions(AccessResult actualValue);

    @Test
    public void isAllowToReadUtkastWhenMissingSubscription() {
        setupMocksForMissingSubscription();

        assertAllowToReadUtkastWhenMissingSubscription(accessService.allowToRead(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToReadUtkastWhenMissingSubscription(AccessResult actualValue);

    @Test
    public void isAllowToReadNotLatestMajorVersion() {
        setupMocksForNotLatestMajorVersion();

        assertAllowToReadNotLatestMajorVersion(accessService.allowToRead(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToReadNotLatestMajorVersion(AccessResult actualValue);

    @Test
    public void isAllowToReadUtkastOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToReadUtkastOnDeceasedPatient(accessService.allowToRead(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToReadUtkastOnDeceasedPatient(AccessResult actualValue);

    @Test
    public void isAllowToReadUtkastOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToReadUtkastOnInactiveUnit(accessService.allowToRead(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToReadUtkastOnInactiveUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadUtkastOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToReadUtkastOnRenewFalse(accessService.allowToRead(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToReadUtkastOnRenewFalse(AccessResult actualValue);

    @Test
    public void isAllowToReadOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToReadOnSekretessPatientOnSameUnit(accessService.allowToRead(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToReadOnSekretessPatientOnSameUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadUtkastOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToReadUtkastOnSekretessPatientOnDifferentUnit(accessService.allowToRead(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToReadUtkastOnSekretessPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadUtkastNoConditionsOnDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToReadUtkastNoConditionsOnDifferentUnit(accessService.allowToRead(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToReadUtkastNoConditionsOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadUtkastOnDeceasedPatientOnDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToReadUtkastOnDeceasedPatientOnDifferentUnit(accessService.allowToRead(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToReadUtkastOnDeceasedPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadUtkastOnInactiveUnitOnDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToReadUtkastOnInactiveUnitOnDifferentUnit(accessService.allowToRead(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToReadUtkastOnInactiveUnitOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadUtkastOnRenewFalseOnDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToReadUtkastOnRenewFalseOnDifferentUnit(accessService.allowToRead(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToReadUtkastOnRenewFalseOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToCopyNoConditions() {
        setupMocksForNoConditions();

        assertAllowToCopyNoConditions(accessService.allowToCopy(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToCopyNoConditions(AccessResult actualValue);

    @Test
    public void isAllowToCopyWhenMissingSubscription() {
        setupMocksForMissingSubscription();

        assertAllowToCopyWhenMissingSubscriptions(accessService.allowToCopy(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToCopyWhenMissingSubscriptions(AccessResult actualValue);

    @Test
    public void isAllowToCopyNotLatestMajorVersion() {
        setupMocksForNotLatestMajorVersion();

        assertAllowToCopyNotLatestMajorVersion(accessService.allowToCopy(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToCopyNotLatestMajorVersion(AccessResult actualValue);

    @Test
    public void isAllowToCopyOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToCopyOnDeceasedPatient(accessService.allowToCopy(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToCopyOnDeceasedPatient(AccessResult actualValue);

    @Test
    public void isAllowToCopyOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToCopyOnInactiveCareUnit(accessService.allowToCopy(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToCopyOnInactiveCareUnit(AccessResult actualValue);

    @Test
    public void isAllowToCopyOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToCopyOnRenewFalse(accessService.allowToCopy(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToCopyOnRenewFalse(AccessResult actualValue);

    @Test
    public void isAllowToCopyOnSameCareProviderWhenUtkastSameVGExists() {
        final WebCertUser user = AccessServiceTestToolkit.createUser(intygsTyp, accessServiceTestData);
        doReturn(user)
            .when(webCertUserService).getUser();
        // TODO Manage this
        // doReturn(true)
        // .when(patientDetailsResolver).isAvliden(PERSONNUMMER);
        doReturn(false)
            .when(patientDetailsResolver).isAvliden(PERSONNUMMER);
        doReturn(SekretessStatus.FALSE)
            .when(patientDetailsResolver).getSekretessStatus(PERSONNUMMER);
        doReturn(AccessServiceTestToolkit.createPreviousUtkastForUtkast(intygsTyp))
            .when(utkastService).checkIfPersonHasExistingIntyg(PERSONNUMMER, user, null);
        doReturn(enhetsId).when(vardenhet).getEnhetsid();
        doReturn(vardgivare).when(vardenhet).getVardgivare();
        doReturn(vardgivarId).when(vardgivare).getVardgivarid();
        doReturn(true).when(webCertUserService).isAuthorizedForUnit(vardgivarId, enhetsId, true);
        doReturn(true).when(webCertUserService).isAuthorizedForUnit(vardgivarId, enhetsId, false);
        doReturn(true).when(intygTextsService).isLatestMajorVersion(intygsTyp, intygsTypVersion);
        doReturn(new SubscriptionInfo()).when(user).getSubscriptionInfo();

        assertAllowToCopyOnSameCareProviderWhenUtkastSameVGExists(
            accessService.allowToCopy(
                AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
            ));
    }

    abstract protected void assertAllowToCopyOnSameCareProviderWhenUtkastSameVGExists(AccessResult actualValue);

    @Test
    public void isAllowToCopyOnDifferentCareProviderWhenIntygSameVGExists() {
        final WebCertUser user = AccessServiceTestToolkit.createUser(intygsTyp, accessServiceTestData);
        doReturn(user)
            .when(webCertUserService).getUser();
        // TODO Manage this
        // doReturn(true)
        // .when(patientDetailsResolver).isAvliden(PERSONNUMMER);
        doReturn(false)
            .when(patientDetailsResolver).isAvliden(PERSONNUMMER);
        doReturn(SekretessStatus.FALSE)
            .when(patientDetailsResolver).getSekretessStatus(PERSONNUMMER);
        doReturn(AccessServiceTestToolkit.createPreviousIntygForUtkast(intygsTyp, true))
            .when(utkastService).checkIfPersonHasExistingIntyg(PERSONNUMMER, user, null);
        doReturn(enhetsId).when(vardenhet).getEnhetsid();
        doReturn(vardgivare).when(vardenhet).getVardgivare();
        doReturn(vardgivarId).when(vardgivare).getVardgivarid();
        doReturn(true).when(webCertUserService).isAuthorizedForUnit(vardgivarId, enhetsId, true);
        doReturn(true).when(webCertUserService).isAuthorizedForUnit(vardgivarId, enhetsId, false);
        doReturn(true).when(intygTextsService).isLatestMajorVersion(intygsTyp, intygsTypVersion);
        doReturn(new SubscriptionInfo()).when(user).getSubscriptionInfo();

        assertAllowToCopyOnDifferentCareProviderWhenIntygSameVGExists(
            accessService.allowToCopy(
                AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
            ));
    }

    abstract protected void assertAllowToCopyOnDifferentCareProviderWhenIntygSameVGExists(AccessResult actualValue);

    @Test
    public void isAllowToCopyOnSameCareProviderWhenIntygExists() {
        final WebCertUser user = AccessServiceTestToolkit.createUser(intygsTyp, accessServiceTestData);
        doReturn(user)
            .when(webCertUserService).getUser();
        // TODO Manage this
        // doReturn(true)
        // .when(patientDetailsResolver).isAvliden(PERSONNUMMER);
        doReturn(false)
            .when(patientDetailsResolver).isAvliden(PERSONNUMMER);
        doReturn(SekretessStatus.FALSE)
            .when(patientDetailsResolver).getSekretessStatus(PERSONNUMMER);
        doReturn(AccessServiceTestToolkit.createPreviousIntygForUtkast(intygsTyp, false))
            .when(utkastService).checkIfPersonHasExistingIntyg(PERSONNUMMER, user, null);
        doReturn(enhetsId).when(vardenhet).getEnhetsid();
        doReturn(vardgivare).when(vardenhet).getVardgivare();
        doReturn(vardgivarId).when(vardgivare).getVardgivarid();
        doReturn(true).when(webCertUserService).isAuthorizedForUnit(vardgivarId, enhetsId, true);
        doReturn(true).when(webCertUserService).isAuthorizedForUnit(vardgivarId, enhetsId, false);
        doReturn(true).when(intygTextsService).isLatestMajorVersion(intygsTyp, intygsTypVersion);
        doReturn(new SubscriptionInfo()).when(user).getSubscriptionInfo();

        assertAllowToCopyOnSameCareProviderWhenIntygExists(
            accessService.allowToCopy(
                AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
            ));
    }

    abstract protected void assertAllowToCopyOnSameCareProviderWhenIntygExists(AccessResult actualValue);

    @Test
    public void isAllowToCopyNoConditionsDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToCopyNoConditionsDifferentUnit(accessService.allowToCopy(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToCopyNoConditionsDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToCopyNotLatestMajorVersionDifferentUnit() {
        setupMocksForNotLatestMajorVersionDifferentUnit();

        assertAllowToCopyNotLatestMajorVersionDIfferentUnit(accessService.allowToCopy(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToCopyNotLatestMajorVersionDIfferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToCopyOnDeceasedPatientDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToCopyOnDeceasedPatientDifferentUnit(accessService.allowToCopy(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToCopyOnDeceasedPatientDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToCopyOnInactiveUnitDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToCopyOnInactiveCareUnitDifferentUnit(accessService.allowToCopy(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToCopyOnInactiveCareUnitDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToCopyOnRenewFalseDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToCopyOnRenewFalseDifferentUnit(accessService.allowToCopy(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToCopyOnRenewFalseDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToCopyOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToCopyOnSekretessPatientOnSameUnit(accessService.allowToCopy(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    protected abstract void assertAllowToCopyOnSekretessPatientOnSameUnit(AccessResult actualValue);

    @Test
    public void isAllowToCopyOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToCopyOnSekretessPatientOnDifferentUnit(accessService.allowToCopy(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    protected abstract void assertAllowToCopyOnSekretessPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToDeleteUtkastNoConditions() {
        setupMocksForNoConditions();

        assertAllowToDeleteUtkastNoConditions(accessService.allowToInvalidate(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToDeleteUtkastNoConditions(AccessResult actualValue);

    @Test
    public void isAllowToDeleteUtkastWhenMissingSubscription() {
        setupMocksForMissingSubscription();

        assertAllowToDeleteUtkastWhenMissingSubscription(accessService.allowToInvalidate(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToDeleteUtkastWhenMissingSubscription(AccessResult actualValue);

    @Test
    public void isAllowToDeleteNotLatestMajorVersion() {
        setupMocksForNotLatestMajorVersion();

        assertAllowToDeleteNotLatestMajorVersion(accessService.allowToInvalidate(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToDeleteNotLatestMajorVersion(AccessResult actualValue);

    @Test
    public void isAllowToDeleteUtkastOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToDeleteUtkastOnDeceasedPatient(accessService.allowToInvalidate(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToDeleteUtkastOnDeceasedPatient(AccessResult actualValue);

    @Test
    public void isAllowToDeleteUtkastOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToDeleteUtkastOnInactiveUnit(accessService.allowToInvalidate(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToDeleteUtkastOnInactiveUnit(AccessResult actualValue);

    @Test
    public void isAllowToDeleteUtkastOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToDeleteUtkastOnRenewFalse(accessService.allowToInvalidate(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToDeleteUtkastOnRenewFalse(AccessResult actualValue);

    @Test
    public void isAllowToDeleteUtkastOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToDeleteUtkastOnSekretessPatientOnSameUnit(
            accessService.allowToInvalidate(
                AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
            ));
    }

    abstract protected void assertAllowToDeleteUtkastOnSekretessPatientOnSameUnit(AccessResult actualValue);

    @Test
    public void isAllowToDeleteUtkastOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToDeleteUtkastOnSekretessPatientOnDifferentUnit(
            accessService.allowToInvalidate(
                AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
            ));
    }

    abstract protected void assertAllowToDeleteUtkastOnSekretessPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToDeleteUtkastOnDeceasedPatientOnDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToDeleteUtkastOnDeceasedPatientOnDifferentUnit(
            accessService.allowToInvalidate(
                AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
            ));
    }

    abstract protected void assertAllowToDeleteUtkastOnDeceasedPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToDeleteUtkastOnInactiveUnitOnDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToDeleteUtkastOnInactiveUnitOnDifferentUnit(
            accessService.allowToInvalidate(
                AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
            ));
    }

    abstract protected void assertAllowToDeleteUtkastOnInactiveUnitOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToDeleteUtkastOnRenewFalseOnDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToDeleteUtkastOnRenewFalseOnDifferentUnit(
            accessService.allowToInvalidate(
                AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
            ));
    }

    abstract protected void assertAllowToDeleteUtkastOnRenewFalseOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToDeleteUtkastNoConditionsDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToDeleteUtkastNoConditionsDifferentUnit(
            accessService.allowToInvalidate(
                AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
            ));
    }

    abstract protected void assertAllowToDeleteUtkastNoConditionsDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToPrintUtkastNoConditions() {
        setupMocksForNoConditions();

        assertAllowToPrintUtkastNoConditions(accessService.allowToPrint(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToPrintUtkastNoConditions(AccessResult actualValue);

    @Test
    public void isAllowToPrintUtkastWhenMissingSubscription() {
        setupMocksForMissingSubscription();

        assertAllowToPrintUtkastWhenMissingSubscription(accessService.allowToPrint(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToPrintUtkastWhenMissingSubscription(AccessResult actualValue);

    @Test
    public void isAllowToPrintNotLatestMajorVersion() {
        setupMocksForNotLatestMajorVersion();

        assertAllowToPrintNotLatestMajorVersion(accessService.allowToPrint(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToPrintNotLatestMajorVersion(AccessResult actualValue);

    @Test
    public void isAllowToPrintUtkastOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToPrintUtkastOnDeceasedPatient(accessService.allowToPrint(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToPrintUtkastOnDeceasedPatient(AccessResult actualValue);

    @Test
    public void isAllowToPrintUtkastOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToPrintUtkastOnInactiveUnit(accessService.allowToPrint(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToPrintUtkastOnInactiveUnit(AccessResult actualValue);

    @Test
    public void isAllowToPrintUtkastOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToPrintUtkastOnRenewFalse(accessService.allowToPrint(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToPrintUtkastOnRenewFalse(AccessResult actualValue);

    @Test
    public void isAllowToPrintUtkastOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToPrintUtkastOnSekretessPatientOnSameUnit(accessService.allowToPrint(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToPrintUtkastOnSekretessPatientOnSameUnit(AccessResult actualValue);

    @Test
    public void isAllowToPrintUtkastOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToPrintUtkastOnSekretessPatientOnDifferentUnit(accessService.allowToPrint(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToPrintUtkastOnSekretessPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToPrintUtkastOnDeceasedPatientOnDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToPrintUtkastOnDeceasedPatientOnDifferentUnit(accessService.allowToPrint(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToPrintUtkastOnDeceasedPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToPrintUtkastOnInactiveUnitOnDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToPrintUtkastOnInactiveUnitOnDifferentUnit(accessService.allowToPrint(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToPrintUtkastOnInactiveUnitOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToPrintUtkastOnRenewFalseOnDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToPrintUtkastOnRenewFalseOnDifferentUnit(accessService.allowToPrint(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToPrintUtkastOnRenewFalseOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToPrintUtkastNoConditionsDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToPrintUtkastNoConditionsDifferentUnit(accessService.allowToPrint(
            AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false)
        ));
    }

    abstract protected void assertAllowToPrintUtkastNoConditionsDifferentUnit(AccessResult actualValue);
}
