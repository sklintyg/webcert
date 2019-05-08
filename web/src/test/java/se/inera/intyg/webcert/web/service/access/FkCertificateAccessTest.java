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

import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.webcert.web.service.access.data.AccessServiceTestData;
import se.inera.intyg.webcert.web.service.access.data.LisjpAccessServiceTestData;

@RunWith(Parameterized.class)
public class FkCertificateAccessTest extends CertificateAccessTest {

    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { LisjpEntryPoint.MODULE_ID, new LisjpAccessServiceTestData() }
        });
    }

    public FkCertificateAccessTest(String intygsTyp, AccessServiceTestData accessServiceTestData) {
        super(intygsTyp, accessServiceTestData);
    }

    @Override
    protected void assertAllowToReadNoConditions(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReadOnDeceasedPatient(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReadOnInactiveUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReadOnRenewFalse(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReadOnSekretessPatientOnSameUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReadOnSekretessPatientOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_SEKRETESS_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReadNoConditionsOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReadOnDeceasedPatientOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReadOnInactiveUnitOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReadOnRenewFalseOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReplaceNoConditions(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReplaceOnDeceasedPatient(AccessResult actualValue) {
        assertEquals(AccessResultCode.DECEASED_PATIENT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReplaceOnInactiveCareUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.INACTIVE_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReplaceOnRenewFalse(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReplaceNoConditionsDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_DIFFERENT_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReplaceOnDeceasedPatientDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.DECEASED_PATIENT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReplaceOnInactiveCareUnitDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.INACTIVE_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReplaceOnRenewFalseDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.RENEW_FALSE, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReplaceOnSekretessPatientOnSameUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReplaceOnSekretessPatientOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_SEKRETESS_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToRenewNoConditions(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_VALIDATION, actualValue.getCode());
    }

    @Override
    protected void assertAllowToRenewOnDeceasedPatient(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_VALIDATION, actualValue.getCode());
    }

    @Override
    protected void assertAllowToRenewOnInactiveCareUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_VALIDATION, actualValue.getCode());
    }

    @Override
    protected void assertAllowToRenewOnRenewFalse(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_VALIDATION, actualValue.getCode());
    }

    @Override
    protected void assertAllowToRenewOnSameCareProviderWhenSameVGExists(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_VALIDATION, actualValue.getCode());
    }

    @Override
    protected void assertAllowToRenewOnDifferentCareProviderWhenIntygSameVGExists(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_VALIDATION, actualValue.getCode());
    }

    @Override
    protected void assertAllowToRenewOnSameCareProviderWhenIntygExists(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_VALIDATION, actualValue.getCode());
    }

    @Override
    protected void assertAllowToRenewNoConditionsDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_VALIDATION, actualValue.getCode());
    }

    @Override
    protected void assertAllowToRenewOnDeceasedPatientDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_VALIDATION, actualValue.getCode());
    }

    @Override
    protected void assertAllowToRenewOnInactiveCareUnitDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_VALIDATION, actualValue.getCode());
    }

    @Override
    protected void assertAllowToRenewOnRenewFalseDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_VALIDATION, actualValue.getCode());
    }

    @Override
    protected void assertAllowToRenewOnSekretessPatientOnSameUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_VALIDATION, actualValue.getCode());
    }

    @Override
    protected void assertAllowToRenewOnSekretessPatientOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_VALIDATION, actualValue.getCode());
    }

    @Override
    protected void assertAllowToDeleteNoConditions(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToDeleteOnDeceasedPatient(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToDeleteOnInactiveUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToDeleteOnRenewFalse(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToDeleteOnSekretessPatientOnSameUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToDeleteOnSekretessPatientOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_SEKRETESS_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToDeleteOnDeceasedPatientOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.DECEASED_PATIENT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToDeleteOnInactiveUnitOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.INACTIVE_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToDeleteOnRenewFalseOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.RENEW_FALSE, actualValue.getCode());
    }

    @Override
    protected void assertAllowToDeleteNoConditionsDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_DIFFERENT_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToPrintNoConditions(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToPrintOnDeceasedPatient(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToPrintOnInactiveUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToPrintOnRenewFalse(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToPrintOnSekretessPatientOnSameUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToPrintOnSekretessPatientOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_SEKRETESS_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToPrintOnDeceasedPatientOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.DECEASED_PATIENT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToPrintOnInactiveUnitOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.INACTIVE_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToPrintOnRenewFalseOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.RENEW_FALSE, actualValue.getCode());
    }

    @Override
    protected void assertAllowToPrintNoConditionsDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_DIFFERENT_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSendNoConditions(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSendOnDeceasedPatient(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSendOnInactiveUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSendOnRenewFalse(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSendOnSekretessPatientOnSameUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSendOnSekretessPatientOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_SEKRETESS_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSendOnDeceasedPatientOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.DECEASED_PATIENT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSendOnInactiveUnitOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.INACTIVE_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSendOnRenewFalseOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.RENEW_FALSE, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSendNoConditionsDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_DIFFERENT_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToAnswerComplementNoConditions(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToAnswerComplementOnDeceasedPatient(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToAnswerComplementOnInactiveUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToAnswerComplementOnRenewFalse(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToAnswerComplementOnSekretessPatientOnSameUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToAnswerComplementOnSekretessPatientOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_SEKRETESS_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToAnswerComplementOnDeceasedPatientOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.DECEASED_PATIENT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToAnswerComplementOnInactiveUnitOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.INACTIVE_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToAnswerComplementOnRenewFalseOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.RENEW_FALSE, actualValue.getCode());
    }

    @Override
    protected void assertAllowToAnswerComplementNoConditionsDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_DIFFERENT_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToAnswerQuestionNoConditions(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToAnswerQuestionOnDeceasedPatient(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToAnswerQuestionOnInactiveUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToAnswerQuestionOnRenewFalse(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToAnswerQuestionOnSekretessPatientOnSameUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToAnswerQuestionOnSekretessPatientOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_SEKRETESS_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToAnswerQuestionOnDeceasedPatientOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.DECEASED_PATIENT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToAnswerQuestionOnInactiveUnitOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.INACTIVE_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToAnswerQuestionOnRenewFalseOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.RENEW_FALSE, actualValue.getCode());
    }

    @Override
    protected void assertAllowToAnswerQuestionNoConditionsDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_DIFFERENT_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReadQuestionsNoConditions(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReadQuestionsOnDeceasedPatient(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReadQuestionsOnInactiveUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReadQuestionsOnRenewFalse(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReadQuestionsOnSekretessPatientOnSameUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReadQuestionsOnSekretessPatientOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_SEKRETESS_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReadQuestionsOnDeceasedPatientOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReadQuestionsOnInactiveUnitOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReadQuestionsOnRenewFalseOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReadQuestionsNoConditionsDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToForwardQuestionsNoConditions(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToForwardQuestionsOnDeceasedPatient(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToForwardQuestionsOnInactiveUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToForwardQuestionsOnRenewFalse(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToForwardQuestionsOnSekretessPatientOnSameUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToForwardQuestionsOnSekretessPatientOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_SEKRETESS_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToForwardQuestionsOnDeceasedPatientOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.DECEASED_PATIENT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToForwardQuestionsOnInactiveUnitOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.INACTIVE_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToForwardQuestionsOnRenewFalseOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.RENEW_FALSE, actualValue.getCode());
    }

    @Override
    protected void assertAllowToForwardQuestionsNoConditionsDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_DIFFERENT_UNIT, actualValue.getCode());
    }
}
