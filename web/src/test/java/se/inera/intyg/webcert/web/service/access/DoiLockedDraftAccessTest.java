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

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import java.util.Arrays;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;
import se.inera.intyg.webcert.web.service.access.data.AccessServiceTestData;
import se.inera.intyg.webcert.web.service.access.data.DoiAccessServiceTestData;

@RunWith(Parameterized.class)
public class DoiLockedDraftAccessTest extends LockedDraftAccessTest {

    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { DoiModuleEntryPoint.MODULE_ID, new DoiAccessServiceTestData() }
        });
    }

    public DoiLockedDraftAccessTest(String intygsTyp, AccessServiceTestData accessServiceTestData) {
        super(intygsTyp, accessServiceTestData);
    }

    @Override
    protected void assertAllowToReadUtkastNoConditions(boolean actualValue) {
        assertTrue(actualValue);
    }

    @Override
    protected void assertAllowToReadUtkastOnDeceasedPatient(boolean actualValue) {
        assertTrue(actualValue);
    }

    @Override
    protected void assertAllowToReadUtkastOnInactiveUnit(boolean actualValue) {
        assertTrue(actualValue);
    }

    @Override
    protected void assertAllowToReadUtkastOnRenewFalse(boolean actualValue) {
        assertTrue(actualValue);
    }

    @Override
    protected void assertAllowToReadOnSekretessPatientOnSameUnit(boolean actualValue) {
        assertTrue(actualValue);
    }

    @Override
    protected void assertAllowToReadUtkastOnSekretessPatientOnDifferentUnit(boolean actualValue) {
        assertFalse(actualValue);
    }

    @Override
    protected void assertAllowToReadUtkastNoConditionsOnDifferentUnit(boolean actualValue) {
        assertTrue(actualValue);
    }

    @Override
    protected void assertAllowToReadUtkastOnDeceasedPatientOnDifferentUnit(boolean actualValue) {
        assertTrue(actualValue);
    }

    @Override
    protected void assertAllowToReadUtkastOnInactiveUnitOnDifferentUnit(boolean actualValue) {
        assertTrue(actualValue);
    }

    @Override
    protected void assertAllowToReadUtkastOnRenewFalseOnDifferentUnit(boolean actualValue) {
        assertTrue(actualValue);
    }

    @Override
    protected void assertAllowToCopyNoConditions(boolean actualValue) {
        assertTrue(actualValue);
    }

    @Override
    protected void assertAllowToCopyOnDeceasedPatient(boolean actualValue) {
        assertTrue(actualValue);
    }

    @Override
    protected void assertAllowToCopyOnInactiveCareUnit(boolean actualValue) {
        assertFalse(actualValue);
    }

    @Override
    protected void assertAllowToCopyOnRenewFalse(boolean actualValue) {
        assertTrue(actualValue);
    }

    @Override
    protected void assertAllowToCopyOnSameCareProviderWhenUtkastSameVGExists(boolean actualValue) {
        assertFalse(actualValue);
    }

    @Override
    protected void assertAllowToCopyOnDifferentCareProviderWhenIntygSameVGExists(boolean actualValue) {
        assertFalse(actualValue);
    }

    @Override
    protected void assertAllowToCopyOnSameCareProviderWhenIntygExists(boolean actualValue) {
        assertTrue(actualValue);
    }

    @Override
    protected void assertAllowToCopyNoConditionsDifferentUnit(boolean actualValue) {
        assertTrue(actualValue);
    }

    @Override
    protected void assertAllowToCopyOnDeceasedPatientDifferentUnit(boolean actualValue) {
        assertTrue(actualValue);
    }

    @Override
    protected void assertAllowToCopyOnInactiveCareUnitDifferentUnit(boolean actualValue) {
        assertFalse(actualValue);
    }

    @Override
    protected void assertAllowToCopyOnRenewFalseDifferentUnit(boolean actualValue) {
        assertTrue(actualValue);
    }

    @Override
    protected void assertAllowToDeleteUtkastNoConditions(boolean actualValue) {
        assertTrue(actualValue);
    }

    @Override
    protected void assertAllowToDeleteUtkastOnDeceasedPatient(boolean actualValue) {
        assertTrue(actualValue);
    }

    @Override
    protected void assertAllowToDeleteUtkastOnInactiveUnit(boolean actualValue) {
        assertTrue(actualValue);
    }

    @Override
    protected void assertAllowToDeleteUtkastOnRenewFalse(boolean actualValue) {
        assertTrue(actualValue);
    }

    @Override
    protected void assertAllowToDeleteUtkastOnSekretessPatientOnSameUnit(boolean actualValue) {
        assertTrue(actualValue);
    }

    @Override
    protected void assertAllowToDeleteUtkastOnSekretessPatientOnDifferentUnit(boolean actualValue) {
        assertFalse(actualValue);
    }

    @Override
    protected void assertAllowToDeleteUtkastOnDeceasedPatientOnDifferentUnit(boolean actualValue) {
        assertFalse(actualValue);
    }

    @Override
    protected void assertAllowToDeleteUtkastOnInactiveUnitOnDifferentUnit(boolean actualValue) {
        assertFalse(actualValue);
    }

    @Override
    protected void assertAllowToDeleteUtkastOnRenewFalseOnDifferentUnit(boolean actualValue) {
        assertFalse(actualValue);
    }

    @Override
    protected void assertAllowToDeleteUtkastNoConditionsDifferentUnit(boolean actualValue) {
        assertFalse(actualValue);
    }

    @Override
    protected void assertAllowToPrintUtkastNoConditions(boolean actualValue) {
        assertTrue(actualValue);
    }

    @Override
    protected void assertAllowToPrintUtkastOnDeceasedPatient(boolean actualValue) {
        assertTrue(actualValue);
    }

    @Override
    protected void assertAllowToPrintUtkastOnInactiveUnit(boolean actualValue) {
        assertTrue(actualValue);
    }

    @Override
    protected void assertAllowToPrintUtkastOnRenewFalse(boolean actualValue) {
        assertTrue(actualValue);
    }

    @Override
    protected void assertAllowToPrintUtkastOnSekretessPatientOnSameUnit(boolean actualValue) {
        assertTrue(actualValue);
    }

    @Override
    protected void assertAllowToPrintUtkastOnSekretessPatientOnDifferentUnit(boolean actualValue) {
        assertFalse(actualValue);
    }

    @Override
    protected void assertAllowToPrintUtkastOnDeceasedPatientOnDifferentUnit(boolean actualValue) {
        assertFalse(actualValue);
    }

    @Override
    protected void assertAllowToPrintUtkastOnInactiveUnitOnDifferentUnit(boolean actualValue) {
        assertFalse(actualValue);
    }

    @Override
    protected void assertAllowToPrintUtkastOnRenewFalseOnDifferentUnit(boolean actualValue) {
        assertFalse(actualValue);
    }

    @Override
    protected void assertAllowToPrintUtkastNoConditionsDifferentUnit(boolean actualValue) {
        assertFalse(actualValue);
    }
}
