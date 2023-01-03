/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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

import static junit.framework.TestCase.assertEquals;

import java.util.Arrays;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.webcert.web.service.access.data.AccessServiceTestData;
import se.inera.intyg.webcert.web.service.access.data.DbAccessServiceTestData;

@RunWith(Parameterized.class)
public class DbLockedDraftAccessTest extends LockedDraftAccessTest {

    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {DbModuleEntryPoint.MODULE_ID, new DbAccessServiceTestData()}
        });
    }

    public DbLockedDraftAccessTest(String intygsTyp, AccessServiceTestData accessServiceTestData) {
        super(intygsTyp, accessServiceTestData);
    }

    @Override
    protected void assertAllowToReadUtkastNoConditions(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReadUtkastWhenMissingSubscription(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReadNotLatestMajorVersion(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReadUtkastOnDeceasedPatient(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReadUtkastOnInactiveUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReadUtkastOnRenewFalse(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReadOnSekretessPatientOnSameUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_SEKRETESS, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReadUtkastOnSekretessPatientOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_SEKRETESS, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReadUtkastNoConditionsOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReadUtkastOnDeceasedPatientOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReadUtkastOnInactiveUnitOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReadUtkastOnRenewFalseOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToCopyNoConditions(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToCopyWhenMissingSubscriptions(AccessResult actualValue) {
        assertEquals(AccessResultCode.MISSING_SUBSCRIPTION, actualValue.getCode());
    }

    @Override
    protected void assertAllowToCopyNotLatestMajorVersion(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToCopyOnDeceasedPatient(AccessResult actualValue) {
        assertEquals(AccessResultCode.DECEASED_PATIENT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToCopyOnInactiveCareUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.INACTIVE_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToCopyOnRenewFalse(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToCopyOnSameCareProviderWhenUtkastSameVGExists(AccessResult actualValue) {
        assertEquals(AccessResultCode.UNIQUE_DRAFT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToCopyOnDifferentCareProviderWhenIntygSameVGExists(AccessResult actualValue) {
        assertEquals(AccessResultCode.UNIQUE_CERTIFICATE, actualValue.getCode());
    }

    @Override
    protected void assertAllowToCopyOnSameCareProviderWhenIntygExists(AccessResult actualValue) {
        assertEquals(AccessResultCode.UNIQUE_CERTIFICATE, actualValue.getCode());
    }

    @Override
    protected void assertAllowToCopyNoConditionsDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToCopyNotLatestMajorVersionDIfferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToCopyOnDeceasedPatientDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.DECEASED_PATIENT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToCopyOnInactiveCareUnitDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.INACTIVE_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToCopyOnRenewFalseDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToCopyOnSekretessPatientOnSameUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_SEKRETESS, actualValue.getCode());
    }

    @Override
    protected void assertAllowToCopyOnSekretessPatientOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_SEKRETESS, actualValue.getCode());
    }

    @Override
    protected void assertAllowToDeleteUtkastNoConditions(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToDeleteUtkastWhenMissingSubscription(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToDeleteNotLatestMajorVersion(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToDeleteUtkastOnDeceasedPatient(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToDeleteUtkastOnInactiveUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToDeleteUtkastOnRenewFalse(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToDeleteUtkastOnSekretessPatientOnSameUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_SEKRETESS, actualValue.getCode());
    }

    @Override
    protected void assertAllowToDeleteUtkastOnSekretessPatientOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_SEKRETESS, actualValue.getCode());
    }

    @Override
    protected void assertAllowToDeleteUtkastOnDeceasedPatientOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.DECEASED_PATIENT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToDeleteUtkastOnInactiveUnitOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.INACTIVE_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToDeleteUtkastOnRenewFalseOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.RENEW_FALSE, actualValue.getCode());
    }

    @Override
    protected void assertAllowToDeleteUtkastNoConditionsDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_DIFFERENT_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToPrintUtkastNoConditions(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToPrintUtkastWhenMissingSubscription(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToPrintNotLatestMajorVersion(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToPrintUtkastOnDeceasedPatient(AccessResult actualValue) {
        assertEquals(AccessResultCode.DECEASED_PATIENT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToPrintUtkastOnInactiveUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToPrintUtkastOnRenewFalse(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToPrintUtkastOnSekretessPatientOnSameUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_SEKRETESS, actualValue.getCode());
    }

    @Override
    protected void assertAllowToPrintUtkastOnSekretessPatientOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_SEKRETESS, actualValue.getCode());
    }

    @Override
    protected void assertAllowToPrintUtkastOnDeceasedPatientOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.DECEASED_PATIENT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToPrintUtkastOnInactiveUnitOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToPrintUtkastOnRenewFalseOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToPrintUtkastNoConditionsDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }
}
