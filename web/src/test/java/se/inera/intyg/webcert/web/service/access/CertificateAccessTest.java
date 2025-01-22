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
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

public abstract class CertificateAccessTest extends AccessTest {

    @InjectMocks
    private CertificateAccessServiceImpl accessService;

    private AccessEvaluationParameters accessEvaluationParameters;

    CertificateAccessTest(String intygsTyp, AccessServiceTestData accessServiceTestData) {
        super(intygsTyp, accessServiceTestData);
    }

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        accessEvaluationParameters = AccessEvaluationParameters.create(intygsTyp, intygsTypVersion, vardenhet, PERSONNUMMER, false);
    }

    @Test
    public void isAllowToReadNoConditions() {
        setupMocksForNoConditions();

        assertAllowToReadNoConditions(accessService.allowToRead(accessEvaluationParameters));
    }

    abstract protected void assertAllowToReadNoConditions(AccessResult actualValue);

    @Test
    public void isAllowToReadNotLatestMajorVersion() {
        setupMocksForNotLatestMajorVersion();

        assertAllowToReadNotLatestMajorVersion(accessService.allowToRead(accessEvaluationParameters));
    }

    abstract protected void assertAllowToReadNotLatestMajorVersion(AccessResult actualValue);

    @Test
    public void isAllowToReadWhenMissingSubscription() {
        setupMocksForMissingSubscription();

        assertAllowToReadWhenMissingSubscription(accessService.allowToRead(accessEvaluationParameters));
    }

    abstract protected void assertAllowToReadWhenMissingSubscription(AccessResult actualValue);

    @Test
    public void isAllowToReadOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToReadOnDeceasedPatient(accessService.allowToRead(accessEvaluationParameters));
    }

    abstract protected void assertAllowToReadOnDeceasedPatient(AccessResult actualValue);

    @Test
    public void isAllowToReadOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToReadOnInactiveUnit(accessService.allowToRead(accessEvaluationParameters));
    }

    abstract protected void assertAllowToReadOnInactiveUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToReadOnRenewFalse(accessService.allowToRead(accessEvaluationParameters));
    }

    abstract protected void assertAllowToReadOnRenewFalse(AccessResult actualValue);

    @Test
    public void isAllowToReadOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToReadOnSekretessPatientOnSameUnit(accessService.allowToRead(accessEvaluationParameters));
    }

    abstract protected void assertAllowToReadOnSekretessPatientOnSameUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToReadOnSekretessPatientOnDifferentUnit(accessService.allowToRead(accessEvaluationParameters));
    }

    abstract protected void assertAllowToReadOnSekretessPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadNoConditionsOnDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToReadNoConditionsOnDifferentUnit(accessService.allowToRead(accessEvaluationParameters));
    }

    abstract protected void assertAllowToReadNoConditionsOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadOnDeceasedPatientOnDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToReadOnDeceasedPatientOnDifferentUnit(accessService.allowToRead(accessEvaluationParameters));
    }

    abstract protected void assertAllowToReadOnDeceasedPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadOnInactiveUnitOnDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToReadOnInactiveUnitOnDifferentUnit(accessService.allowToRead(accessEvaluationParameters));
    }

    abstract protected void assertAllowToReadOnInactiveUnitOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadOnRenewFalseOnDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToReadOnRenewFalseOnDifferentUnit(accessService.allowToRead(accessEvaluationParameters));
    }

    abstract protected void assertAllowToReadOnRenewFalseOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReplaceNoConditions() {
        setupMocksForNoConditions();

        assertAllowToReplaceNoConditions(accessService.allowToReplace(accessEvaluationParameters));
    }

    abstract protected void assertAllowToReplaceNoConditions(AccessResult actualValue);

    @Test
    public void isAllowToReplaceWhenMissingSubscription() {
        setupMocksForMissingSubscription();

        assertAllowToReplaceWhenMissingSubscription(accessService.allowToReplace(accessEvaluationParameters));
    }

    abstract protected void assertAllowToReplaceWhenMissingSubscription(AccessResult actualValue);

    @Test
    public void isAllowToReplaceNotLatestMajorVersion() {
        setupMocksForNotLatestMajorVersion();

        assertAllowToReplaceNotLatestMajorVersion(accessService.allowToReplace(accessEvaluationParameters));
    }

    abstract protected void assertAllowToReplaceNotLatestMajorVersion(AccessResult actualValue);

    @Test
    public void isAllowToReplaceOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToReplaceOnDeceasedPatient(accessService.allowToReplace(accessEvaluationParameters));
    }

    abstract protected void assertAllowToReplaceOnDeceasedPatient(AccessResult actualValue);

    @Test
    public void isAllowToReplaceOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToReplaceOnInactiveCareUnit(accessService.allowToReplace(accessEvaluationParameters));
    }

    abstract protected void assertAllowToReplaceOnInactiveCareUnit(AccessResult actualValue);

    @Test
    public void isAllowToReplaceOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToReplaceOnRenewFalse(accessService.allowToReplace(accessEvaluationParameters));
    }

    abstract protected void assertAllowToReplaceOnRenewFalse(AccessResult actualValue);

    @Test
    public void isAllowToReplaceNoConditionsDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToReplaceNoConditionsDifferentUnit(accessService.allowToReplace(accessEvaluationParameters));
    }

    abstract protected void assertAllowToReplaceNoConditionsDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReplaceNotLatestMajorVersionDifferentUnit() {
        setupMocksForNotLatestMajorVersionDifferentUnit();

        assertAllowToReplaceNotLatestMajorVersionDifferentUnit(accessService.allowToReplace(accessEvaluationParameters));
    }

    abstract protected void assertAllowToReplaceNotLatestMajorVersionDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReplaceOnDeceasedPatientDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToReplaceOnDeceasedPatientDifferentUnit(accessService.allowToReplace(accessEvaluationParameters));
    }

    abstract protected void assertAllowToReplaceOnDeceasedPatientDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReplaceOnInactiveUnitDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToReplaceOnInactiveCareUnitDifferentUnit(accessService.allowToReplace(accessEvaluationParameters));
    }

    abstract protected void assertAllowToReplaceOnInactiveCareUnitDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReplaceOnRenewFalseDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToReplaceOnRenewFalseDifferentUnit(accessService.allowToReplace(accessEvaluationParameters));
    }

    abstract protected void assertAllowToReplaceOnRenewFalseDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReplaceOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToReplaceOnSekretessPatientOnSameUnit(accessService.allowToReplace(accessEvaluationParameters));
    }

    protected abstract void assertAllowToReplaceOnSekretessPatientOnSameUnit(AccessResult actualValue);

    @Test
    public void isAllowToReplaceOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToReplaceOnSekretessPatientOnDifferentUnit(accessService.allowToReplace(accessEvaluationParameters));
    }

    protected abstract void assertAllowToReplaceOnSekretessPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToRenewNoConditions() {
        setupMocksForNoConditions();

        assertAllowToRenewNoConditions(accessService.allowToRenew(accessEvaluationParameters));
    }

    abstract protected void assertAllowToRenewNoConditions(AccessResult actualValue);

    @Test
    public void isAllowToRenewWhenMissingSubscription() {
        setupMocksForMissingSubscription();

        assertAllowToRenewWhenMissingSubscription(accessService.allowToRenew(accessEvaluationParameters));
    }

    abstract protected void assertAllowToRenewWhenMissingSubscription(AccessResult actualValue);

    @Test
    public void isAllowToRenewNotLatestMajorVersion() {
        setupMocksForNotLatestMajorVersion();

        assertAllowToRenewNotLatestMajorVersion(accessService.allowToRenew(accessEvaluationParameters));
    }

    abstract protected void assertAllowToRenewNotLatestMajorVersion(AccessResult actualValue);

    @Test
    public void isAllowToRenewOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToRenewOnDeceasedPatient(accessService.allowToRenew(accessEvaluationParameters));
    }

    abstract protected void assertAllowToRenewOnDeceasedPatient(AccessResult actualValue);

    @Test
    public void isAllowToRenewOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToRenewOnInactiveCareUnit(accessService.allowToRenew(accessEvaluationParameters));
    }

    abstract protected void assertAllowToRenewOnInactiveCareUnit(AccessResult actualValue);

    @Test
    public void isAllowToRenewOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToRenewOnRenewFalse(accessService.allowToRenew(accessEvaluationParameters));
    }

    abstract protected void assertAllowToRenewOnRenewFalse(AccessResult actualValue);

    @Test
    public void isAllowToRenewOnSameCareProviderWhenSameVGExists() {
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

        assertAllowToRenewOnSameCareProviderWhenSameVGExists(
            accessService.allowToRenew(accessEvaluationParameters));
    }

    abstract protected void assertAllowToRenewOnSameCareProviderWhenSameVGExists(AccessResult actualValue);

    @Test
    public void isAllowToRenewOnDifferentCareProviderWhenIntygSameVGExists() {
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

        assertAllowToRenewOnDifferentCareProviderWhenIntygSameVGExists(
            accessService.allowToRenew(accessEvaluationParameters));
    }

    abstract protected void assertAllowToRenewOnDifferentCareProviderWhenIntygSameVGExists(AccessResult actualValue);

    @Test
    public void isAllowToRenewOnSameCareProviderWhenIntygExists() {
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

        assertAllowToRenewOnSameCareProviderWhenIntygExists(
            accessService.allowToRenew(accessEvaluationParameters));
    }

    abstract protected void assertAllowToRenewOnSameCareProviderWhenIntygExists(AccessResult actualValue);

    @Test
    public void isAllowToRenewNoConditionsDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToRenewNoConditionsDifferentUnit(accessService.allowToRenew(accessEvaluationParameters));
    }

    abstract protected void assertAllowToRenewNoConditionsDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToRenewOnDeceasedPatientDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToRenewOnDeceasedPatientDifferentUnit(accessService.allowToRenew(accessEvaluationParameters));
    }

    abstract protected void assertAllowToRenewOnDeceasedPatientDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToRenewOnInactiveUnitDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToRenewOnInactiveCareUnitDifferentUnit(accessService.allowToRenew(accessEvaluationParameters));
    }

    abstract protected void assertAllowToRenewOnInactiveCareUnitDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToRenewOnRenewFalseDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToRenewOnRenewFalseDifferentUnit(accessService.allowToRenew(accessEvaluationParameters));
    }

    abstract protected void assertAllowToRenewOnRenewFalseDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToRenewOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToRenewOnSekretessPatientOnSameUnit(accessService.allowToRenew(accessEvaluationParameters));
    }

    protected abstract void assertAllowToRenewOnSekretessPatientOnSameUnit(AccessResult actualValue);

    @Test
    public void isAllowToRenewOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToRenewOnSekretessPatientOnDifferentUnit(accessService.allowToRenew(accessEvaluationParameters));
    }

    protected abstract void assertAllowToRenewOnSekretessPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToDeleteNoConditions() {
        setupMocksForNoConditions();

        assertAllowToDeleteNoConditions(accessService.allowToInvalidate(accessEvaluationParameters));
    }

    abstract protected void assertAllowToDeleteNoConditions(AccessResult actualValue);

    @Test
    public void isAllowToDeleteWhenNoSubscription() {
        setupMocksForMissingSubscription();

        assertAllowToDeleteWhenNoSubscription(accessService.allowToInvalidate(accessEvaluationParameters));
    }

    abstract protected void assertAllowToDeleteWhenNoSubscription(AccessResult actualValue);

    @Test
    public void isAllowToDeleteNotLatestMajorVersion() {
        setupMocksForNotLatestMajorVersion();

        assertAllowToDeleteNotLatestMajorVersion(accessService.allowToInvalidate(accessEvaluationParameters));
    }

    abstract protected void assertAllowToDeleteNotLatestMajorVersion(AccessResult actualValue);

    @Test
    public void isAllowToDeleteOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToDeleteOnDeceasedPatient(accessService.allowToInvalidate(accessEvaluationParameters));
    }

    abstract protected void assertAllowToDeleteOnDeceasedPatient(AccessResult actualValue);

    @Test
    public void isAllowToDeleteOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToDeleteOnInactiveUnit(accessService.allowToInvalidate(accessEvaluationParameters));
    }

    abstract protected void assertAllowToDeleteOnInactiveUnit(AccessResult actualValue);

    @Test
    public void isAllowToDeleteOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToDeleteOnRenewFalse(accessService.allowToInvalidate(accessEvaluationParameters));
    }

    abstract protected void assertAllowToDeleteOnRenewFalse(AccessResult actualValue);

    @Test
    public void isAllowToDeleteOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToDeleteOnSekretessPatientOnSameUnit(
            accessService.allowToInvalidate(accessEvaluationParameters));
    }

    abstract protected void assertAllowToDeleteOnSekretessPatientOnSameUnit(AccessResult actualValue);

    @Test
    public void isAllowToDeleteOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToDeleteOnSekretessPatientOnDifferentUnit(
            accessService.allowToInvalidate(accessEvaluationParameters));
    }

    abstract protected void assertAllowToDeleteOnSekretessPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToDeleteOnDeceasedPatientOnDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToDeleteOnDeceasedPatientOnDifferentUnit(
            accessService.allowToInvalidate(accessEvaluationParameters));
    }

    abstract protected void assertAllowToDeleteOnDeceasedPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToDeleteOnInactiveUnitOnDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToDeleteOnInactiveUnitOnDifferentUnit(
            accessService.allowToInvalidate(accessEvaluationParameters));
    }

    abstract protected void assertAllowToDeleteOnInactiveUnitOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToDeleteOnRenewFalseOnDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToDeleteOnRenewFalseOnDifferentUnit(
            accessService.allowToInvalidate(accessEvaluationParameters));
    }

    abstract protected void assertAllowToDeleteOnRenewFalseOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToDeleteNoConditionsDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToDeleteNoConditionsDifferentUnit(
            accessService.allowToInvalidate(accessEvaluationParameters));
    }

    abstract protected void assertAllowToDeleteNoConditionsDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToPrintNoConditions() {
        setupMocksForNoConditions();

        assertAllowToPrintNoConditions(accessService.allowToPrint(accessEvaluationParameters, isEmployer));
    }

    abstract protected void assertAllowToPrintNoConditions(AccessResult actualValue);

    @Test
    public void isAllowToPrintWhenMissingSubscription() {
        setupMocksForNoConditions();

        assertAllowToPrintWhenMissingSubscription(accessService.allowToPrint(accessEvaluationParameters, isEmployer));
    }

    abstract protected void assertAllowToPrintWhenMissingSubscription(AccessResult actualValue);

    @Test
    public void isAllowToPrintNotLatestMajorVersion() {
        setupMocksForNotLatestMajorVersion();

        assertAllowToPrintNotLatestMajorVersion(accessService.allowToPrint(accessEvaluationParameters, isEmployer));
    }

    abstract protected void assertAllowToPrintNotLatestMajorVersion(AccessResult actualValue);

    @Test
    public void isAllowToPrintOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToPrintOnDeceasedPatient(accessService.allowToPrint(accessEvaluationParameters, isEmployer));
    }

    abstract protected void assertAllowToPrintOnDeceasedPatient(AccessResult actualValue);

    @Test
    public void isAllowToPrintOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToPrintOnInactiveUnit(accessService.allowToPrint(accessEvaluationParameters, isEmployer));
    }

    abstract protected void assertAllowToPrintOnInactiveUnit(AccessResult actualValue);

    @Test
    public void isAllowToPrintOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToPrintOnRenewFalse(accessService.allowToPrint(accessEvaluationParameters, isEmployer));
    }

    abstract protected void assertAllowToPrintOnRenewFalse(AccessResult actualValue);

    @Test
    public void isAllowToPrintOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToPrintOnSekretessPatientOnSameUnit(accessService.allowToPrint(accessEvaluationParameters, isEmployer));
    }

    abstract protected void assertAllowToPrintOnSekretessPatientOnSameUnit(AccessResult actualValue);

    @Test
    public void isAllowToPrintOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToPrintOnSekretessPatientOnDifferentUnit(accessService.allowToPrint(accessEvaluationParameters, isEmployer));
    }

    abstract protected void assertAllowToPrintOnSekretessPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToPrintOnDeceasedPatientOnDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToPrintOnDeceasedPatientOnDifferentUnit(accessService.allowToPrint(accessEvaluationParameters, isEmployer));
    }

    abstract protected void assertAllowToPrintOnDeceasedPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToPrintOnInactiveUnitOnDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToPrintOnInactiveUnitOnDifferentUnit(accessService.allowToPrint(accessEvaluationParameters, isEmployer));
    }

    abstract protected void assertAllowToPrintOnInactiveUnitOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToPrintOnRenewFalseOnDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToPrintOnRenewFalseOnDifferentUnit(accessService.allowToPrint(accessEvaluationParameters, isEmployer));
    }

    abstract protected void assertAllowToPrintOnRenewFalseOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToPrintNoConditionsDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToPrintNoConditionsDifferentUnit(accessService.allowToPrint(accessEvaluationParameters, isEmployer));
    }

    abstract protected void assertAllowToPrintNoConditionsDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToSendNoConditions() {
        setupMocksForNoConditions();

        assertAllowToSendNoConditions(accessService.allowToSend(accessEvaluationParameters));
    }

    abstract protected void assertAllowToSendNoConditions(AccessResult actualValue);

    @Test
    public void isAllowToSendWhenMissingSubscription() {
        setupMocksForMissingSubscription();

        assertAllowToSendWhenMissingSubscription(accessService.allowToSend(accessEvaluationParameters));
    }

    abstract protected void assertAllowToSendWhenMissingSubscription(AccessResult actualValue);

    @Test
    public void isAllowToSendNotLatestMajorVersion() {
        setupMocksForNotLatestMajorVersion();

        assertAllowToSendNotLatestMajorVersion(accessService.allowToSend(accessEvaluationParameters));
    }

    abstract protected void assertAllowToSendNotLatestMajorVersion(AccessResult actualValue);

    @Test
    public void isAllowToSendOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToSendOnDeceasedPatient(accessService.allowToSend(accessEvaluationParameters));
    }

    abstract protected void assertAllowToSendOnDeceasedPatient(AccessResult actualValue);

    @Test
    public void isAllowToSendOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToSendOnInactiveUnit(accessService.allowToSend(accessEvaluationParameters));
    }

    abstract protected void assertAllowToSendOnInactiveUnit(AccessResult actualValue);

    @Test
    public void isAllowToSendOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToSendOnRenewFalse(accessService.allowToSend(accessEvaluationParameters));
    }

    abstract protected void assertAllowToSendOnRenewFalse(AccessResult actualValue);

    @Test
    public void isAllowToSendOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToSendOnSekretessPatientOnSameUnit(accessService.allowToSend(accessEvaluationParameters));
    }

    abstract protected void assertAllowToSendOnSekretessPatientOnSameUnit(AccessResult actualValue);

    @Test
    public void isAllowToSendOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToSendOnSekretessPatientOnDifferentUnit(accessService.allowToSend(accessEvaluationParameters));
    }

    abstract protected void assertAllowToSendOnSekretessPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToSendOnDeceasedPatientOnDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToSendOnDeceasedPatientOnDifferentUnit(accessService.allowToSend(accessEvaluationParameters));
    }

    abstract protected void assertAllowToSendOnDeceasedPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToSendOnInactiveUnitOnDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToSendOnInactiveUnitOnDifferentUnit(accessService.allowToSend(accessEvaluationParameters));
    }

    abstract protected void assertAllowToSendOnInactiveUnitOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToSendOnRenewFalseOnDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToSendOnRenewFalseOnDifferentUnit(accessService.allowToSend(accessEvaluationParameters));
    }

    abstract protected void assertAllowToSendOnRenewFalseOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToSendNoConditionsDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToSendNoConditionsDifferentUnit(accessService.allowToSend(accessEvaluationParameters));
    }

    abstract protected void assertAllowToSendNoConditionsDifferentUnit(AccessResult actualValue);

    @Test
    public void isallowToCreateQuestionNoConditions() {
        setupMocksForNoConditions();

        assertAllowToCreateQuestionNoConditions(accessService.allowToCreateQuestion(accessEvaluationParameters));
    }

    abstract protected void assertAllowToCreateQuestionNoConditions(AccessResult actualValue);

    @Test
    public void isallowToCreateQuestionWhenMissingSubscription() {
        setupMocksForMissingSubscription();

        assertAllowToCreateQuestionWhenMissingSubscription(accessService.allowToCreateQuestion(accessEvaluationParameters));
    }

    abstract protected void assertAllowToCreateQuestionWhenMissingSubscription(AccessResult actualValue);

    @Test
    public void isAllowToCreateQuestionNotLatestMajorVersion() {
        setupMocksForNotLatestMajorVersion();

        assertAllowToCreateQuestionNotLatestMajorVersion(accessService.allowToCreateQuestion(accessEvaluationParameters));
    }

    abstract protected void assertAllowToCreateQuestionNotLatestMajorVersion(AccessResult actualValue);

    @Test
    public void isallowToCreateQuestionOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToCreateQuestionOnDeceasedPatient(accessService.allowToCreateQuestion(accessEvaluationParameters));
    }

    abstract protected void assertAllowToCreateQuestionOnDeceasedPatient(AccessResult actualValue);

    @Test
    public void isallowToCreateQuestionOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToCreateQuestionOnInactiveUnit(accessService.allowToCreateQuestion(accessEvaluationParameters));
    }

    abstract protected void assertAllowToCreateQuestionOnInactiveUnit(AccessResult actualValue);

    @Test
    public void isallowToCreateQuestionOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToCreateQuestionOnRenewFalse(accessService.allowToCreateQuestion(accessEvaluationParameters));
    }

    abstract protected void assertAllowToCreateQuestionOnRenewFalse(AccessResult actualValue);

    @Test
    public void isallowToCreateQuestionOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToCreateQuestionOnSekretessPatientOnSameUnit(accessService.allowToCreateQuestion(accessEvaluationParameters));
    }

    abstract protected void assertAllowToCreateQuestionOnSekretessPatientOnSameUnit(AccessResult actualValue);

    @Test
    public void isallowToCreateQuestionOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToCreateQuestionOnSekretessPatientOnDifferentUnit(
            accessService.allowToCreateQuestion(accessEvaluationParameters));
    }

    abstract protected void assertAllowToCreateQuestionOnSekretessPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isallowToCreateQuestionOnDeceasedPatientOnDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToCreateQuestionOnDeceasedPatientOnDifferentUnit(
            accessService.allowToCreateQuestion(accessEvaluationParameters));
    }

    abstract protected void assertAllowToCreateQuestionOnDeceasedPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isallowToCreateQuestionOnInactiveUnitOnDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToCreateQuestionOnInactiveUnitOnDifferentUnit(accessService.allowToCreateQuestion(accessEvaluationParameters));
    }

    abstract protected void assertAllowToCreateQuestionOnInactiveUnitOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isallowToCreateQuestionOnRenewFalseOnDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToCreateQuestionOnRenewFalseOnDifferentUnit(accessService.allowToCreateQuestion(accessEvaluationParameters));
    }

    abstract protected void assertAllowToCreateQuestionOnRenewFalseOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isallowToCreateQuestionNoConditionsDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToCreateQuestionNoConditionsDifferentUnit(accessService.allowToCreateQuestion(accessEvaluationParameters));
    }

    abstract protected void assertAllowToCreateQuestionNoConditionsDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToAnswerComplementNoConditions() {
        setupMocksForNoConditions();

        assertAllowToAnswerComplementNoConditions(accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, true));
    }

    abstract protected void assertAllowToAnswerComplementNoConditions(AccessResult actualValue);

    @Test
    public void isAllowToAnswerComplementWhenMissingSubscription() {
        setupMocksForMissingSubscription();

        assertAllowToAnswerComplementWhenMissingSubscription(
            accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, true));
    }

    abstract protected void assertAllowToAnswerComplementWhenMissingSubscription(AccessResult actualValue);

    @Test
    public void isAllowToAnswerComplementNotLatestMajorVersion() {
        setupMocksForNotLatestMajorVersion();

        assertAllowToAnswerComplementNotLatestMajorVersion(accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, true));
    }

    abstract protected void assertAllowToAnswerComplementNotLatestMajorVersion(AccessResult actualValue);

    @Test
    public void isAllowToAnswerComplementOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToAnswerComplementOnDeceasedPatient(
            accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, true));
    }

    abstract protected void assertAllowToAnswerComplementOnDeceasedPatient(AccessResult actualValue);

    @Test
    public void isAllowToAnswerComplementOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToAnswerComplementOnInactiveUnit(
            accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, true));
    }

    abstract protected void assertAllowToAnswerComplementOnInactiveUnit(AccessResult actualValue);

    @Test
    public void isAllowToAnswerComplementOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToAnswerComplementOnRenewFalse(accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, true));
    }

    abstract protected void assertAllowToAnswerComplementOnRenewFalse(AccessResult actualValue);

    @Test
    public void isAllowToAnswerComplementOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToAnswerComplementOnSekretessPatientOnSameUnit(
            accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, true));
    }

    abstract protected void assertAllowToAnswerComplementOnSekretessPatientOnSameUnit(AccessResult actualValue);

    @Test
    public void isAllowToAnswerComplementOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToAnswerComplementOnSekretessPatientOnDifferentUnit(
            accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, true));
    }

    abstract protected void assertAllowToAnswerComplementOnSekretessPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToAnswerComplementOnDeceasedPatientOnDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToAnswerComplementOnDeceasedPatientOnDifferentUnit(
            accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, true));
    }

    abstract protected void assertAllowToAnswerComplementOnDeceasedPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToAnswerComplementOnInactiveUnitOnDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToAnswerComplementOnInactiveUnitOnDifferentUnit(
            accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, true));
    }

    abstract protected void assertAllowToAnswerComplementOnInactiveUnitOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToAnswerComplementOnRenewFalseOnDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToAnswerComplementOnRenewFalseOnDifferentUnit(
            accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, true));
    }

    abstract protected void assertAllowToAnswerComplementOnRenewFalseOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToAnswerComplementNoConditionsDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToAnswerComplementNoConditionsDifferentUnit(
            accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, true));
    }

    abstract protected void assertAllowToAnswerComplementNoConditionsDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToAnswerQuestionNoConditions() {
        setupMocksForNoConditions();

        assertAllowToAnswerQuestionNoConditions(accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, false));
    }

    abstract protected void assertAllowToAnswerQuestionNoConditions(AccessResult actualValue);

    @Test
    public void isAllowToAnswerQuestionWhenMissingSubscription() {
        setupMocksForMissingSubscription();

        assertAllowToAnswerQuestionWhenMissingSubscription(
            accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, false));
    }

    abstract protected void assertAllowToAnswerQuestionWhenMissingSubscription(AccessResult actualValue);

    @Test
    public void isAllowToAnswerQuestionNotLatestMajorVersion() {
        setupMocksForNotLatestMajorVersion();

        assertAllowToAnswerQuestionNotLatestMajorVersion(accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, false));
    }

    abstract protected void assertAllowToAnswerQuestionNotLatestMajorVersion(AccessResult actualValue);

    @Test
    public void isAllowToAnswerQuestionOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToAnswerQuestionOnDeceasedPatient(
            accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, false));
    }

    abstract protected void assertAllowToAnswerQuestionOnDeceasedPatient(AccessResult actualValue);

    @Test
    public void isAllowToAnswerQuestionOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToAnswerQuestionOnInactiveUnit(accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, false));
    }

    abstract protected void assertAllowToAnswerQuestionOnInactiveUnit(AccessResult actualValue);

    @Test
    public void isAllowToAnswerQuestionOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToAnswerQuestionOnRenewFalse(accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, false));
    }

    abstract protected void assertAllowToAnswerQuestionOnRenewFalse(AccessResult actualValue);

    @Test
    public void isAllowToAnswerQuestionOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToAnswerQuestionOnSekretessPatientOnSameUnit(
            accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, false));
    }

    abstract protected void assertAllowToAnswerQuestionOnSekretessPatientOnSameUnit(AccessResult actualValue);

    @Test
    public void isAllowToAnswerQuestionOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToAnswerQuestionOnSekretessPatientOnDifferentUnit(
            accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, false));
    }

    abstract protected void assertAllowToAnswerQuestionOnSekretessPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToAnswerQuestionOnDeceasedPatientOnDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToAnswerQuestionOnDeceasedPatientOnDifferentUnit(
            accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, false));
    }

    abstract protected void assertAllowToAnswerQuestionOnDeceasedPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToAnswerQuestionOnInactiveUnitOnDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToAnswerQuestionOnInactiveUnitOnDifferentUnit(
            accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, false));
    }

    abstract protected void assertAllowToAnswerQuestionOnInactiveUnitOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToAnswerQuestionOnRenewFalseOnDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToAnswerQuestionOnRenewFalseOnDifferentUnit(
            accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, false));
    }

    abstract protected void assertAllowToAnswerQuestionOnRenewFalseOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToAnswerQuestionNoConditionsDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToAnswerQuestionNoConditionsDifferentUnit(
            accessService.allowToAnswerComplementQuestion(accessEvaluationParameters, false));
    }

    abstract protected void assertAllowToAnswerQuestionNoConditionsDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToAnswerAdminQuestionNoConditions() {
        setupMocksForNoConditions();

        assertAllowToAnswerAdminQuestionNoConditions(
            accessService.allowToAnswerAdminQuestion(accessEvaluationParameters));
    }

    abstract protected void assertAllowToAnswerAdminQuestionNoConditions(AccessResult actualValue);

    @Test
    public void isAllowToAnswerAdminQuestionWhenMissingSubscription() {
        setupMocksForMissingSubscription();

        assertAllowToAnswerAdminQuestionWhenMissingSubscription(accessService.allowToAnswerAdminQuestion(accessEvaluationParameters));
    }

    abstract protected void assertAllowToAnswerAdminQuestionWhenMissingSubscription(AccessResult actualValue);

    @Test
    public void isAllowToAnswerAdminQuestionNotLatestMajorVersion() {
        setupMocksForNotLatestMajorVersion();

        assertAllowToAnswerAdminQuestionNotLatestMajorVersion(accessService.allowToAnswerAdminQuestion(accessEvaluationParameters));
    }

    abstract protected void assertAllowToAnswerAdminQuestionNotLatestMajorVersion(AccessResult actualValue);

    @Test
    public void isAllowToAnswerAdminQuestionOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToAnswerAdminQuestionOnDeceasedPatient(
            accessService.allowToAnswerAdminQuestion(accessEvaluationParameters));
    }

    abstract protected void assertAllowToAnswerAdminQuestionOnDeceasedPatient(AccessResult actualValue);

    @Test
    public void isAllowToAnswerAdminQuestionOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToAnswerAdminQuestionOnInactiveUnit(
            accessService.allowToAnswerAdminQuestion(accessEvaluationParameters));
    }

    abstract protected void assertAllowToAnswerAdminQuestionOnInactiveUnit(AccessResult actualValue);

    @Test
    public void isAllowToAnswerAdminQuestionOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToAnswerAdminQuestionOnRenewFalse(
            accessService.allowToAnswerAdminQuestion(accessEvaluationParameters));
    }

    abstract protected void assertAllowToAnswerAdminQuestionOnRenewFalse(AccessResult actualValue);

    @Test
    public void isAllowToAnswerAdminQuestionOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToAnswerAdminQuestionOnSekretessPatientOnSameUnit(
            accessService.allowToAnswerAdminQuestion(accessEvaluationParameters));
    }

    abstract protected void assertAllowToAnswerAdminQuestionOnSekretessPatientOnSameUnit(AccessResult actualValue);

    @Test
    public void isAllowToAnswerAdminQuestionOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToAnswerAdminQuestionOnSekretessPatientOnDifferentUnit(
            accessService.allowToAnswerAdminQuestion(accessEvaluationParameters));
    }

    abstract protected void assertAllowToAnswerAdminQuestionOnSekretessPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToAnswerAdminQuestionOnDeceasedPatientOnDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToAnswerAdminQuestionOnDeceasedPatientOnDifferentUnit(
            accessService.allowToAnswerAdminQuestion(accessEvaluationParameters));
    }

    abstract protected void assertAllowToAnswerAdminQuestionOnDeceasedPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToAnswerAdminQuestionOnInactiveUnitOnDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToAnswerAdminQuestionOnInactiveUnitOnDifferentUnit(
            accessService.allowToAnswerAdminQuestion(accessEvaluationParameters));
    }

    abstract protected void assertAllowToAnswerAdminQuestionOnInactiveUnitOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToAnswerAdminQuestionOnRenewFalseOnDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToAnswerAdminQuestionOnRenewFalseOnDifferentUnit(
            accessService.allowToAnswerAdminQuestion(accessEvaluationParameters));
    }

    abstract protected void assertAllowToAnswerAdminQuestionOnRenewFalseOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToAnswerAdminQuestionNoConditionsDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToAnswerAdminQuestionNoConditionsDifferentUnit(
            accessService.allowToAnswerAdminQuestion(accessEvaluationParameters));
    }

    abstract protected void assertAllowToAnswerAdminQuestionNoConditionsDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToSetComplementAsHandledNoConditions() {
        setupMocksForNoConditions();

        assertAllowToSetComplementAsHandledNoConditions(
            accessService.allowToSetComplementAsHandled(accessEvaluationParameters));
    }

    abstract protected void assertAllowToSetComplementAsHandledNoConditions(AccessResult actualValue);

    @Test
    public void isAllowToSetComplementAsHandledWhenMissingSubscription() {
        setupMocksForMissingSubscription();

        assertAllowToSetComplementAsHandledWhenMissingSubscription(accessService.allowToSetComplementAsHandled(accessEvaluationParameters));
    }

    abstract protected void assertAllowToSetComplementAsHandledWhenMissingSubscription(AccessResult actualValue);

    @Test
    public void isAllowToSetComplementAsHandledNotLatestMajorVersion() {
        setupMocksForNotLatestMajorVersion();

        assertAllowToSetComplementAsHandledNotLatestMajorVersion(accessService.allowToSetComplementAsHandled(accessEvaluationParameters));
    }

    abstract protected void assertAllowToSetComplementAsHandledNotLatestMajorVersion(AccessResult actualValue);

    @Test
    public void isAllowToSetComplementAsHandledOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToSetComplementAsHandledOnDeceasedPatient(
            accessService.allowToSetComplementAsHandled(accessEvaluationParameters));
    }

    abstract protected void assertAllowToSetComplementAsHandledOnDeceasedPatient(AccessResult actualValue);

    @Test
    public void isAllowToSetComplementAsHandledOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToSetComplementAsHandledOnInactiveUnit(
            accessService.allowToSetComplementAsHandled(accessEvaluationParameters));
    }

    abstract protected void assertAllowToSetComplementAsHandledOnInactiveUnit(AccessResult actualValue);

    @Test
    public void isAllowToSetComplementAsHandledOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToSetComplementAsHandledOnRenewFalse(
            accessService.allowToSetComplementAsHandled(accessEvaluationParameters));
    }

    abstract protected void assertAllowToSetComplementAsHandledOnRenewFalse(AccessResult actualValue);

    @Test
    public void isAllowToSetComplementAsHandledOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToSetComplementAsHandledOnSekretessPatientOnSameUnit(
            accessService.allowToSetComplementAsHandled(accessEvaluationParameters));
    }

    abstract protected void assertAllowToSetComplementAsHandledOnSekretessPatientOnSameUnit(AccessResult actualValue);

    @Test
    public void isAllowToSetComplementAsHandledOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToSetComplementAsHandledOnSekretessPatientOnDifferentUnit(
            accessService.allowToSetComplementAsHandled(accessEvaluationParameters));
    }

    abstract protected void assertAllowToSetComplementAsHandledOnSekretessPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToSetComplementAsHandledOnDeceasedPatientOnDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToSetComplementAsHandledOnDeceasedPatientOnDifferentUnit(
            accessService.allowToSetComplementAsHandled(accessEvaluationParameters));
    }

    abstract protected void assertAllowToSetComplementAsHandledOnDeceasedPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToSetComplementAsHandledOnInactiveUnitOnDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToSetComplementAsHandledOnInactiveUnitOnDifferentUnit(
            accessService.allowToSetComplementAsHandled(accessEvaluationParameters));
    }

    abstract protected void assertAllowToSetComplementAsHandledOnInactiveUnitOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToSetComplementAsHandledOnRenewFalseOnDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToSetComplementAsHandledOnRenewFalseOnDifferentUnit(
            accessService.allowToSetComplementAsHandled(accessEvaluationParameters));
    }

    abstract protected void assertAllowToSetComplementAsHandledOnRenewFalseOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToSetComplementAsHandledNoConditionsDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToSetComplementAsHandledNoConditionsDifferentUnit(
            accessService.allowToSetComplementAsHandled(accessEvaluationParameters));
    }

    abstract protected void assertAllowToSetComplementAsHandledNoConditionsDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToSetQuestionAsHandledNoConditions() {
        setupMocksForNoConditions();

        assertAllowToSetQuestionAsHandledNoConditions(
            accessService.allowToSetQuestionAsHandled(accessEvaluationParameters));
    }

    abstract protected void assertAllowToSetQuestionAsHandledNoConditions(AccessResult actualValue);

    @Test
    public void isAllowToSetQuestionAsHandledWhenMissingSubscription() {
        setupMocksForMissingSubscription();

        assertAllowToSetQuestionAsHandledWhenMissingSubscription(accessService.allowToSetQuestionAsHandled(accessEvaluationParameters));
    }

    abstract protected void assertAllowToSetQuestionAsHandledWhenMissingSubscription(AccessResult actualValue);

    @Test
    public void isAllowToSetQuestionAsHandledNotLatestMajorVersion() {
        setupMocksForNotLatestMajorVersion();

        assertAllowToSetQuestionAsHandledNotLatestMajorVersion(accessService.allowToSetQuestionAsHandled(accessEvaluationParameters));
    }

    abstract protected void assertAllowToSetQuestionAsHandledNotLatestMajorVersion(AccessResult actualValue);

    @Test
    public void isAllowToSetQuestionAsHandledOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToSetQuestionAsHandledOnDeceasedPatient(
            accessService.allowToSetQuestionAsHandled(accessEvaluationParameters));
    }

    abstract protected void assertAllowToSetQuestionAsHandledOnDeceasedPatient(AccessResult actualValue);

    @Test
    public void isAllowToSetQuestionAsHandledOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToSetQuestionAsHandledOnInactiveUnit(
            accessService.allowToSetQuestionAsHandled(accessEvaluationParameters));
    }

    abstract protected void assertAllowToSetQuestionAsHandledOnInactiveUnit(AccessResult actualValue);

    @Test
    public void isAllowToSetQuestionAsHandledOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToSetQuestionAsHandledOnRenewFalse(
            accessService.allowToSetQuestionAsHandled(accessEvaluationParameters));
    }

    abstract protected void assertAllowToSetQuestionAsHandledOnRenewFalse(AccessResult actualValue);

    @Test
    public void isAllowToSetQuestionAsHandledOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToSetQuestionAsHandledOnSekretessPatientOnSameUnit(
            accessService.allowToSetQuestionAsHandled(accessEvaluationParameters));
    }

    abstract protected void assertAllowToSetQuestionAsHandledOnSekretessPatientOnSameUnit(AccessResult actualValue);

    @Test
    public void isAllowToSetQuestionAsHandledOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToSetQuestionAsHandledOnSekretessPatientOnDifferentUnit(
            accessService.allowToSetQuestionAsHandled(accessEvaluationParameters));
    }

    abstract protected void assertAllowToSetQuestionAsHandledOnSekretessPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToSetQuestionAsHandledOnDeceasedPatientOnDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToSetQuestionAsHandledOnDeceasedPatientOnDifferentUnit(
            accessService.allowToSetQuestionAsHandled(accessEvaluationParameters));
    }

    abstract protected void assertAllowToSetQuestionAsHandledOnDeceasedPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToSetQuestionAsHandledOnInactiveUnitOnDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToSetQuestionAsHandledOnInactiveUnitOnDifferentUnit(
            accessService.allowToSetQuestionAsHandled(accessEvaluationParameters));
    }

    abstract protected void assertAllowToSetQuestionAsHandledOnInactiveUnitOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToSetQuestionAsHandledOnRenewFalseOnDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToSetQuestionAsHandledOnRenewFalseOnDifferentUnit(
            accessService.allowToSetQuestionAsHandled(accessEvaluationParameters));
    }

    abstract protected void assertAllowToSetQuestionAsHandledOnRenewFalseOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToSetQuestionAsHandledNoConditionsDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToSetQuestionAsHandledNoConditionsDifferentUnit(
            accessService.allowToSetQuestionAsHandled(accessEvaluationParameters));
    }

    abstract protected void assertAllowToSetQuestionAsHandledNoConditionsDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadQuestionsNoConditions() {
        setupMocksForNoConditions();

        assertAllowToReadQuestionsNoConditions(accessService.allowToReadQuestions(accessEvaluationParameters));
    }

    abstract protected void assertAllowToReadQuestionsNoConditions(AccessResult actualValue);

    @Test
    public void isAllowToReadQuestionsWhenMissingSubscription() {
        setupMocksForMissingSubscription();

        assertAllowToReadQuestionsWhenMissingSubscription(accessService.allowToReadQuestions(accessEvaluationParameters));
    }

    abstract protected void assertAllowToReadQuestionsWhenMissingSubscription(AccessResult actualValue);

    @Test
    public void isAllowToReadQuestionsNotLatestMajorVersion() {
        setupMocksForNotLatestMajorVersion();

        assertAllowToReadQuestionsNotLatestMajorVersion(accessService.allowToReadQuestions(accessEvaluationParameters));
    }

    abstract protected void assertAllowToReadQuestionsNotLatestMajorVersion(AccessResult actualValue);

    @Test
    public void isAllowToReadQuestionsOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToReadQuestionsOnDeceasedPatient(accessService.allowToReadQuestions(accessEvaluationParameters));
    }

    abstract protected void assertAllowToReadQuestionsOnDeceasedPatient(AccessResult actualValue);

    @Test
    public void isAllowToReadQuestionsOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToReadQuestionsOnInactiveUnit(accessService.allowToReadQuestions(accessEvaluationParameters));
    }

    abstract protected void assertAllowToReadQuestionsOnInactiveUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadQuestionsOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToReadQuestionsOnRenewFalse(accessService.allowToReadQuestions(accessEvaluationParameters));
    }

    abstract protected void assertAllowToReadQuestionsOnRenewFalse(AccessResult actualValue);

    @Test
    public void isAllowToReadQuestionsOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToReadQuestionsOnSekretessPatientOnSameUnit(accessService.allowToReadQuestions(accessEvaluationParameters));
    }

    abstract protected void assertAllowToReadQuestionsOnSekretessPatientOnSameUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadQuestionsOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToReadQuestionsOnSekretessPatientOnDifferentUnit(accessService.allowToReadQuestions(accessEvaluationParameters));
    }

    abstract protected void assertAllowToReadQuestionsOnSekretessPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadQuestionsOnDeceasedPatientOnDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToReadQuestionsOnDeceasedPatientOnDifferentUnit(accessService.allowToReadQuestions(accessEvaluationParameters));
    }

    abstract protected void assertAllowToReadQuestionsOnDeceasedPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadQuestionsOnInactiveUnitOnDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToReadQuestionsOnInactiveUnitOnDifferentUnit(accessService.allowToReadQuestions(accessEvaluationParameters));
    }

    abstract protected void assertAllowToReadQuestionsOnInactiveUnitOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadQuestionsOnRenewFalseOnDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToReadQuestionsOnRenewFalseOnDifferentUnit(accessService.allowToReadQuestions(accessEvaluationParameters));
    }

    abstract protected void assertAllowToReadQuestionsOnRenewFalseOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadQuestionsNoConditionsDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToReadQuestionsNoConditionsDifferentUnit(accessService.allowToReadQuestions(accessEvaluationParameters));
    }

    abstract protected void assertAllowToReadQuestionsNoConditionsDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToForwardQuestionsNoConditions() {
        setupMocksForNoConditions();

        assertAllowToForwardQuestionsNoConditions(accessService.allowToForwardQuestions(accessEvaluationParameters));
    }

    abstract protected void assertAllowToForwardQuestionsNoConditions(AccessResult actualValue);

    @Test
    public void isAllowToForwardQuestionsWhenMissingSubscription() {
        setupMocksForMissingSubscription();

        assertAllowToForwardQuestionsWhenMissingSubscription(accessService.allowToForwardQuestions(accessEvaluationParameters));
    }

    abstract protected void assertAllowToForwardQuestionsWhenMissingSubscription(AccessResult actualValue);

    @Test
    public void isAllowToForwardQuestionsNotLatestMajorVersion() {
        setupMocksForNotLatestMajorVersion();

        assertAllowToForwardQuestionsNotLatestMajorVersion(accessService.allowToForwardQuestions(accessEvaluationParameters));
    }

    abstract protected void assertAllowToForwardQuestionsNotLatestMajorVersion(AccessResult actualValue);

    @Test
    public void isAllowToForwardQuestionsOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToForwardQuestionsOnDeceasedPatient(accessService.allowToForwardQuestions(accessEvaluationParameters));
    }

    abstract protected void assertAllowToForwardQuestionsOnDeceasedPatient(AccessResult actualValue);

    @Test
    public void isAllowToForwardQuestionsOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToForwardQuestionsOnInactiveUnit(accessService.allowToForwardQuestions(accessEvaluationParameters));
    }

    abstract protected void assertAllowToForwardQuestionsOnInactiveUnit(AccessResult actualValue);

    @Test
    public void isAllowToForwardQuestionsOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToForwardQuestionsOnRenewFalse(accessService.allowToForwardQuestions(accessEvaluationParameters));
    }

    abstract protected void assertAllowToForwardQuestionsOnRenewFalse(AccessResult actualValue);

    @Test
    public void isAllowToForwardQuestionsOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToForwardQuestionsOnSekretessPatientOnSameUnit(
            accessService.allowToForwardQuestions(accessEvaluationParameters));
    }

    abstract protected void assertAllowToForwardQuestionsOnSekretessPatientOnSameUnit(AccessResult actualValue);

    @Test
    public void isAllowToForwardQuestionsOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToForwardQuestionsOnSekretessPatientOnDifferentUnit(
            accessService.allowToForwardQuestions(accessEvaluationParameters));
    }

    abstract protected void assertAllowToForwardQuestionsOnSekretessPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToForwardQuestionsOnDeceasedPatientOnDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToForwardQuestionsOnDeceasedPatientOnDifferentUnit(
            accessService.allowToForwardQuestions(accessEvaluationParameters));
    }

    abstract protected void assertAllowToForwardQuestionsOnDeceasedPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToForwardQuestionsOnInactiveUnitOnDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToForwardQuestionsOnInactiveUnitOnDifferentUnit(
            accessService.allowToForwardQuestions(accessEvaluationParameters));
    }

    abstract protected void assertAllowToForwardQuestionsOnInactiveUnitOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToForwardQuestionsOnRenewFalseOnDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToForwardQuestionsOnRenewFalseOnDifferentUnit(accessService.allowToForwardQuestions(accessEvaluationParameters));
    }

    abstract protected void assertAllowToForwardQuestionsOnRenewFalseOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToForwardQuestionsNoConditionsDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToForwardQuestionsNoConditionsDifferentUnit(accessService.allowToForwardQuestions(accessEvaluationParameters));
    }

    abstract protected void assertAllowToForwardQuestionsNoConditionsDifferentUnit(AccessResult actualValue);
}
