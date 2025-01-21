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
        return Arrays.asList(new Object[][]{
            {LisjpEntryPoint.MODULE_ID, new LisjpAccessServiceTestData()}
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
    protected void assertAllowToReadWhenMissingSubscription(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReadNotLatestMajorVersion(AccessResult actualValue) {
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
    protected void assertAllowToReplaceWhenMissingSubscription(AccessResult actualValue) {
        assertEquals(AccessResultCode.MISSING_SUBSCRIPTION, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReplaceNotLatestMajorVersion(AccessResult actualValue) {
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
    protected void assertAllowToReplaceNotLatestMajorVersionDifferentUnit(AccessResult actualValue) {
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
    protected void assertAllowToRenewWhenMissingSubscription(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_VALIDATION, actualValue.getCode());
    }

    @Override
    protected void assertAllowToRenewNotLatestMajorVersion(AccessResult actualValue) {
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
    protected void assertAllowToDeleteWhenNoSubscription(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToDeleteNotLatestMajorVersion(AccessResult actualValue) {
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
    protected void assertAllowToPrintWhenMissingSubscription(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToPrintNotLatestMajorVersion(AccessResult actualValue) {
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
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToPrintOnInactiveUnitOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToPrintOnRenewFalseOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToPrintNoConditionsDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSendNoConditions(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSendWhenMissingSubscription(AccessResult actualValue) {
        assertEquals(AccessResultCode.MISSING_SUBSCRIPTION, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSendNotLatestMajorVersion(AccessResult actualValue) {
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
    protected void assertAllowToCreateQuestionNoConditions(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToCreateQuestionWhenMissingSubscription(AccessResult actualValue) {
        assertEquals(AccessResultCode.MISSING_SUBSCRIPTION, actualValue.getCode());
    }

    @Override
    protected void assertAllowToCreateQuestionNotLatestMajorVersion(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToCreateQuestionOnDeceasedPatient(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToCreateQuestionOnInactiveUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToCreateQuestionOnRenewFalse(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToCreateQuestionOnSekretessPatientOnSameUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToCreateQuestionOnSekretessPatientOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_SEKRETESS_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToCreateQuestionOnDeceasedPatientOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.DECEASED_PATIENT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToCreateQuestionOnInactiveUnitOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.INACTIVE_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToCreateQuestionOnRenewFalseOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.RENEW_FALSE, actualValue.getCode());
    }

    @Override
    protected void assertAllowToCreateQuestionNoConditionsDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_DIFFERENT_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToAnswerComplementNoConditions(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToAnswerComplementWhenMissingSubscription(AccessResult actualValue) {
        assertEquals(AccessResultCode.MISSING_SUBSCRIPTION, actualValue.getCode());
    }

    @Override
    protected void assertAllowToAnswerComplementNotLatestMajorVersion(AccessResult actualValue) {
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
    protected void assertAllowToAnswerQuestionWhenMissingSubscription(AccessResult actualValue) {
        assertEquals(AccessResultCode.MISSING_SUBSCRIPTION, actualValue.getCode());
    }

    @Override
    protected void assertAllowToAnswerQuestionNotLatestMajorVersion(AccessResult actualValue) {
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
    protected void assertAllowToAnswerAdminQuestionNoConditions(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToAnswerAdminQuestionWhenMissingSubscription(AccessResult actualValue) {
        assertEquals(AccessResultCode.MISSING_SUBSCRIPTION, actualValue.getCode());
    }

    @Override
    protected void assertAllowToAnswerAdminQuestionNotLatestMajorVersion(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToAnswerAdminQuestionOnDeceasedPatient(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToAnswerAdminQuestionOnInactiveUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToAnswerAdminQuestionOnRenewFalse(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToAnswerAdminQuestionOnSekretessPatientOnSameUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToAnswerAdminQuestionOnSekretessPatientOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_SEKRETESS_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToAnswerAdminQuestionOnDeceasedPatientOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.DECEASED_PATIENT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToAnswerAdminQuestionOnInactiveUnitOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.INACTIVE_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToAnswerAdminQuestionOnRenewFalseOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.RENEW_FALSE, actualValue.getCode());
    }

    @Override
    protected void assertAllowToAnswerAdminQuestionNoConditionsDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_DIFFERENT_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSetComplementAsHandledNoConditions(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSetComplementAsHandledWhenMissingSubscription(AccessResult actualValue) {
        assertEquals(AccessResultCode.MISSING_SUBSCRIPTION, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSetComplementAsHandledNotLatestMajorVersion(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSetComplementAsHandledOnDeceasedPatient(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSetComplementAsHandledOnInactiveUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSetComplementAsHandledOnRenewFalse(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSetComplementAsHandledOnSekretessPatientOnSameUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSetComplementAsHandledOnSekretessPatientOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_SEKRETESS_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSetComplementAsHandledOnDeceasedPatientOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.DECEASED_PATIENT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSetComplementAsHandledOnInactiveUnitOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.INACTIVE_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSetComplementAsHandledOnRenewFalseOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.RENEW_FALSE, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSetComplementAsHandledNoConditionsDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_DIFFERENT_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSetQuestionAsHandledNoConditions(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSetQuestionAsHandledWhenMissingSubscription(AccessResult actualValue) {
        assertEquals(AccessResultCode.MISSING_SUBSCRIPTION, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSetQuestionAsHandledNotLatestMajorVersion(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSetQuestionAsHandledOnDeceasedPatient(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSetQuestionAsHandledOnInactiveUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSetQuestionAsHandledOnRenewFalse(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSetQuestionAsHandledOnSekretessPatientOnSameUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSetQuestionAsHandledOnSekretessPatientOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_SEKRETESS_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSetQuestionAsHandledOnDeceasedPatientOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.DECEASED_PATIENT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSetQuestionAsHandledOnInactiveUnitOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.INACTIVE_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSetQuestionAsHandledOnRenewFalseOnDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.RENEW_FALSE, actualValue.getCode());
    }

    @Override
    protected void assertAllowToSetQuestionAsHandledNoConditionsDifferentUnit(AccessResult actualValue) {
        assertEquals(AccessResultCode.AUTHORIZATION_DIFFERENT_UNIT, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReadQuestionsNoConditions(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReadQuestionsWhenMissingSubscription(AccessResult actualValue) {
        assertEquals(AccessResultCode.NO_PROBLEM, actualValue.getCode());
    }

    @Override
    protected void assertAllowToReadQuestionsNotLatestMajorVersion(AccessResult actualValue) {
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
    protected void assertAllowToForwardQuestionsWhenMissingSubscription(AccessResult actualValue) {
        assertEquals(AccessResultCode.MISSING_SUBSCRIPTION, actualValue.getCode());
    }

    @Override
    protected void assertAllowToForwardQuestionsNotLatestMajorVersion(AccessResult actualValue) {
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
