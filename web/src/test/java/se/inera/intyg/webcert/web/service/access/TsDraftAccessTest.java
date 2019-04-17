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

import se.inera.intyg.common.ts_bas.support.TsBasEntryPoint;
import se.inera.intyg.common.ts_diabetes.support.TsDiabetesEntryPoint;
import se.inera.intyg.common.tstrk1009.support.Tstrk1009EntryPoint;
import se.inera.intyg.common.tstrk1062.support.TsTrk1062EntryPoint;
import se.inera.intyg.webcert.web.service.access.data.AccessServiceTestData;
import se.inera.intyg.webcert.web.service.access.data.TsAccessServiceTestData;

@RunWith(Parameterized.class)
public class TsDraftAccessTest extends DraftAccessTest {

    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { TsTrk1062EntryPoint.MODULE_ID, new TsAccessServiceTestData() },
                { TsBasEntryPoint.MODULE_ID, new TsAccessServiceTestData() },
                { TsDiabetesEntryPoint.MODULE_ID, new TsAccessServiceTestData() },
                { Tstrk1009EntryPoint.MODULE_ID, new TsAccessServiceTestData() }
        });
    }

    public TsDraftAccessTest(String intygsTyp, AccessServiceTestData accessServiceTestData) {
        super(intygsTyp, accessServiceTestData);
    }

    @Override
    protected void assertAllowedToCreateUtkastNoConditions(boolean actualValue) {
        assertTrue(actualValue);
    }

    @Override
    protected void assertAllowedToCreateUtkastOnDeceasedPatient(boolean actualValue) {
        assertFalse(actualValue);
    }

    @Override
    protected void assertAllowedToCreateUtkastOnInactiveCareUnit(boolean actualValue) {
        assertFalse(actualValue);
    }

    @Override
    protected void assertAllowedToCreateUtkastOnRenewFalse(boolean actualValue) {
        assertFalse(actualValue);
    }

    @Override
    protected void assertAllowToCreateUtkastOnSameCareProviderWhenUtkastSameVGExists(boolean actualValue) {
        assertTrue(actualValue);
    }

    @Override
    protected void assertAllowToCreateUtkastOnDifferentCareProviderWhenIntygSameVGExists(boolean actualValue) {
        assertTrue(actualValue);
    }

    @Override
    protected void assertAllowToCreateUtkastOnSameCareProviderWhenIntygExists(boolean actualValue) {
        assertTrue(actualValue);
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
    protected void assertAllowToEditUtkastNoConditions(boolean actualValue) {
        assertTrue(actualValue);
    }

    @Override
    protected void assertAllowToEditUtkastOnDeceasedPatient(boolean actualValue) {
        assertTrue(actualValue);
    }

    @Override
    protected void assertAllowToEditUtkastOnInactiveUnit(boolean actualValue) {
        assertTrue(actualValue);
    }

    @Override
    protected void assertAllowToEditUtkastOnRenewFalse(boolean actualValue) {
        assertTrue(actualValue);
    }

    @Override
    protected void assertAllowToEditUtkastOnSekretessPatientOnSameUnit(boolean actualValue) {
        assertTrue(actualValue);
    }

    @Override
    protected void assertAllowToEditUtkastOnSekretessPatientOnDifferentUnit(boolean actualValue) {
        assertFalse(actualValue);
    }

    @Override
    protected void assertAllowToEditUtkastOnDeceasedPatientOnDifferentUnit(boolean actualValue) {
        assertFalse(actualValue);
    }

    @Override
    protected void assertAllowToEditUtkastOnInactiveUnitOnDifferentUnit(boolean actualValue) {
        assertFalse(actualValue);
    }

    @Override
    protected void assertAllowToEditUtkastOnRenewFalseOnDifferentUnit(boolean actualValue) {
        assertFalse(actualValue);
    }

    @Override
    protected void assertAllowToEditUtkastNoConditionsDifferentUnit(boolean actualValue) {
        assertFalse(actualValue);
    }

    @Override
    protected void assertAllowToSignUtkastNoConditions(boolean actualValue) {
        assertTrue(actualValue);
    }

    @Override
    protected void assertAllowToSignUtkastOnDeceasedPatient(boolean actualValue) {
        assertTrue(actualValue);
    }

    @Override
    protected void assertAllowToSignUtkastOnInactiveUnit(boolean actualValue) {
        assertTrue(actualValue);
    }

    @Override
    protected void assertAllowToSignUtkastOnRenewFalse(boolean actualValue) {
        assertTrue(actualValue);
    }

    @Override
    protected void assertAllowToSignUtkastOnSekretessPatientOnSameUnit(boolean actualValue) {
        assertTrue(actualValue);
    }

    @Override
    protected void assertAllowToSignUtkastOnSekretessPatientOnDifferentUnit(boolean actualValue) {
        assertFalse(actualValue);
    }

    @Override
    protected void assertAllowToSignUtkastOnDeceasedPatientOnDifferentUnit(boolean actualValue) {
        assertFalse(actualValue);
    }

    @Override
    protected void assertAllowToSignUtkastOnInactiveUnitOnDifferentUnit(boolean actualValue) {
        assertFalse(actualValue);
    }

    @Override
    protected void assertAllowToSignUtkastOnRenewFalseOnDifferentUnit(boolean actualValue) {
        assertFalse(actualValue);
    }

    @Override
    protected void assertAllowToSignUtkastNoConditionsDifferentUnit(boolean actualValue) {
        assertFalse(actualValue);
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

    @Override
    protected void assertAllowToForwardUtkastNoConditions(boolean actualValue) {
        assertTrue(actualValue);
    }

    @Override
    protected void assertAllowToForwardUtkastOnDeceasedPatient(boolean actualValue) {
        assertTrue(actualValue);
    }

    @Override
    protected void assertAllowToForwardUtkastOnInactiveUnit(boolean actualValue) {
        assertTrue(actualValue);
    }

    @Override
    protected void assertAllowToForwardUtkastOnRenewFalse(boolean actualValue) {
        assertTrue(actualValue);
    }

    @Override
    protected void assertAllowToForwardUtkastOnSekretessPatientOnSameUnit(boolean actualValue) {
        assertTrue(actualValue);
    }

    @Override
    protected void assertAllowToForwardUtkastOnSekretessPatientOnDifferentUnit(boolean actualValue) {
        assertFalse(actualValue);
    }

    @Override
    protected void assertAllowToForwardUtkastOnDeceasedPatientOnDifferentUnit(boolean actualValue) {
        assertFalse(actualValue);
    }

    @Override
    protected void assertAllowToForwardUtkastOnInactiveUnitOnDifferentUnit(boolean actualValue) {
        assertFalse(actualValue);
    }

    @Override
    protected void assertAllowToForwardUtkastOnRenewFalseOnDifferentUnit(boolean actualValue) {
        assertFalse(actualValue);
    }

    @Override
    protected void assertAllowToForwardUtkastNoConditionsDifferentUnit(boolean actualValue) {
        assertFalse(actualValue);
    }
}
