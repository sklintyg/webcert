/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
public class DbDraftAccessTest extends DraftAccessTest {

    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { DbModuleEntryPoint.MODULE_ID, new DbAccessServiceTestData() }
        });
    }

    public DbDraftAccessTest(String intygsTyp, AccessServiceTestData accessServiceTestData) {
        super(intygsTyp, accessServiceTestData);
    }

    @Override
    protected void assertAllowedToCreateUtkastNoConditions(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowedToCreateUtkastOnDeceasedPatient(AccessResult actualValue) {
        assertEquals(AccessResultCode.DECEASED_PATIENT, actualValue.getCode());
    }

    @Override
    protected void assertAllowedToCreateUtkastOnInactiveCareUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.INACTIVE_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowedToCreateUtkastOnRenewFalse(AccessResult actualValue) {
        assertEquals(AccessResultCode.RENEW_FALSE, actualValue.getCode());
    }

    @Override
    protected void assertAllowToCreateUtkastOnSameCareProviderWhenUtkastSameVGExists(AccessResult actualValue) {
        assertEquals(AccessResultCode.UNIQUE_DRAFT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToCreateUtkastOnDifferentCareProviderWhenIntygSameVGExists(AccessResult actualValue) {
        assertEquals(AccessResultCode.UNIQUE_CERTIFICATE, actualValue.getCode());
    }

    @Override
    protected void assertAllowToCreateUtkastOnSameCareProviderWhenIntygExists(AccessResult actualValue) {
        assertEquals(AccessResultCode.UNIQUE_CERTIFICATE, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReadUtkastNoConditions(AccessResult actualValue) {
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
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReadUtkastOnSekretessPatientOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_SEKRETESS_UNIT, actualValue.getCode());
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
    protected void assertAllowToEditUtkastNoConditions(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToEditUtkastOnDeceasedPatient(AccessResult actualValue) {
        assertEquals(AccessResultCode.DECEASED_PATIENT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToEditUtkastOnInactiveUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToEditUtkastOnRenewFalse(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToEditUtkastOnSekretessPatientOnSameUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToEditUtkastOnSekretessPatientOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_SEKRETESS_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToEditUtkastOnDeceasedPatientOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.DECEASED_PATIENT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToEditUtkastOnInactiveUnitOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.INACTIVE_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToEditUtkastOnRenewFalseOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.RENEW_FALSE, actualValue.getCode());
    }

    @Override
    protected void assertAllowToEditUtkastNoConditionsDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_DIFFERENT_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSignUtkastNoConditions(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSignUtkastOnDeceasedPatient(AccessResult actualValue) {
        assertEquals(AccessResultCode.DECEASED_PATIENT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSignUtkastOnInactiveUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSignUtkastOnRenewFalse(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSignUtkastOnSekretessPatientOnSameUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSignUtkastOnSekretessPatientOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_SEKRETESS_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSignUtkastOnDeceasedPatientOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.DECEASED_PATIENT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSignUtkastOnInactiveUnitOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.INACTIVE_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSignUtkastOnRenewFalseOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.RENEW_FALSE, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSignUtkastNoConditionsDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_DIFFERENT_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToDeleteUtkastNoConditions(AccessResult actualValue) {
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
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToDeleteUtkastOnSekretessPatientOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_SEKRETESS_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToDeleteUtkastOnDeceasedPatientOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToDeleteUtkastOnInactiveUnitOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.INACTIVE_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToDeleteUtkastOnRenewFalseOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToDeleteUtkastNoConditionsDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToPrintUtkastNoConditions(AccessResult actualValue) {
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
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToPrintUtkastOnSekretessPatientOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_SEKRETESS_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToPrintUtkastOnDeceasedPatientOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.DECEASED_PATIENT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToPrintUtkastOnInactiveUnitOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.INACTIVE_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToPrintUtkastOnRenewFalseOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.RENEW_FALSE, actualValue.getCode());
    }

    @Override
    protected void assertAllowToPrintUtkastNoConditionsDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_DIFFERENT_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToForwardUtkastNoConditions(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToForwardUtkastOnDeceasedPatient(AccessResult actualValue) {
        assertEquals(AccessResultCode.DECEASED_PATIENT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToForwardUtkastOnInactiveUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToForwardUtkastOnRenewFalse(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToForwardUtkastOnSekretessPatientOnSameUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToForwardUtkastOnSekretessPatientOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_SEKRETESS_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToForwardUtkastOnDeceasedPatientOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.DECEASED_PATIENT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToForwardUtkastOnInactiveUnitOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.INACTIVE_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToForwardUtkastOnRenewFalseOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.RENEW_FALSE, actualValue.getCode());
    }

    @Override
    protected void assertAllowToForwardUtkastNoConditionsDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_DIFFERENT_UNIT, actualValue.getCode());
    }
}
