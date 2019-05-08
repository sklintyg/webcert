/*
 * Replaceright (C) 2019 Inera AB (http://www.inera.se)
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
 * You should have received a Replace of the GNU General Public License
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

    CertificateAccessTest(String intygsTyp, AccessServiceTestData accessServiceTestData) {
        super(intygsTyp, accessServiceTestData);
    }

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void isAllowToReadNoConditions() {
        setupMocksForNoConditions();

        assertAllowToReadNoConditions(accessService.allowToRead(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToReadNoConditions(AccessResult actualValue);

    @Test
    public void isAllowToReadOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToReadOnDeceasedPatient(accessService.allowToRead(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToReadOnDeceasedPatient(AccessResult actualValue);

    @Test
    public void isAllowToReadOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToReadOnInactiveUnit(accessService.allowToRead(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToReadOnInactiveUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToReadOnRenewFalse(accessService.allowToRead(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToReadOnRenewFalse(AccessResult actualValue);

    @Test
    public void isAllowToReadOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToReadOnSekretessPatientOnSameUnit(accessService.allowToRead(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToReadOnSekretessPatientOnSameUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToReadOnSekretessPatientOnDifferentUnit(accessService.allowToRead(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToReadOnSekretessPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadNoConditionsOnDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToReadNoConditionsOnDifferentUnit(accessService.allowToRead(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToReadNoConditionsOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadOnDeceasedPatientOnDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToReadOnDeceasedPatientOnDifferentUnit(accessService.allowToRead(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToReadOnDeceasedPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadOnInactiveUnitOnDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToReadOnInactiveUnitOnDifferentUnit(accessService.allowToRead(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToReadOnInactiveUnitOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadOnRenewFalseOnDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToReadOnRenewFalseOnDifferentUnit(accessService.allowToRead(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToReadOnRenewFalseOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReplaceNoConditions() {
        setupMocksForNoConditions();

        assertAllowToReplaceNoConditions(accessService.allowToReplace(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToReplaceNoConditions(AccessResult actualValue);

    @Test
    public void isAllowToReplaceOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToReplaceOnDeceasedPatient(accessService.allowToReplace(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToReplaceOnDeceasedPatient(AccessResult actualValue);

    @Test
    public void isAllowToReplaceOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToReplaceOnInactiveCareUnit(accessService.allowToReplace(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToReplaceOnInactiveCareUnit(AccessResult actualValue);

    @Test
    public void isAllowToReplaceOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToReplaceOnRenewFalse(accessService.allowToReplace(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToReplaceOnRenewFalse(AccessResult actualValue);

    @Test
    public void isAllowToReplaceNoConditionsDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToReplaceNoConditionsDifferentUnit(accessService.allowToReplace(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToReplaceNoConditionsDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReplaceOnDeceasedPatientDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToReplaceOnDeceasedPatientDifferentUnit(accessService.allowToReplace(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToReplaceOnDeceasedPatientDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReplaceOnInactiveUnitDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToReplaceOnInactiveCareUnitDifferentUnit(accessService.allowToReplace(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToReplaceOnInactiveCareUnitDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReplaceOnRenewFalseDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToReplaceOnRenewFalseDifferentUnit(accessService.allowToReplace(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToReplaceOnRenewFalseDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReplaceOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToReplaceOnSekretessPatientOnSameUnit(accessService.allowToReplace(intygsTyp, vardenhet, PERSONNUMMER));
    }

    protected abstract void assertAllowToReplaceOnSekretessPatientOnSameUnit(AccessResult actualValue);

    @Test
    public void isAllowToReplaceOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToReplaceOnSekretessPatientOnDifferentUnit(accessService.allowToReplace(intygsTyp, vardenhet, PERSONNUMMER));
    }

    protected abstract void assertAllowToReplaceOnSekretessPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToRenewNoConditions() {
        setupMocksForNoConditions();

        assertAllowToRenewNoConditions(accessService.allowToRenew(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToRenewNoConditions(AccessResult actualValue);

    @Test
    public void isAllowToRenewOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToRenewOnDeceasedPatient(accessService.allowToRenew(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToRenewOnDeceasedPatient(AccessResult actualValue);

    @Test
    public void isAllowToRenewOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToRenewOnInactiveCareUnit(accessService.allowToRenew(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToRenewOnInactiveCareUnit(AccessResult actualValue);

    @Test
    public void isAllowToRenewOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToRenewOnRenewFalse(accessService.allowToRenew(intygsTyp, vardenhet, PERSONNUMMER));
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
                .when(utkastService).checkIfPersonHasExistingIntyg(PERSONNUMMER, user);

        assertAllowToRenewOnSameCareProviderWhenSameVGExists(
                accessService.allowToRenew(intygsTyp, vardenhet, PERSONNUMMER));
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
                .when(utkastService).checkIfPersonHasExistingIntyg(PERSONNUMMER, user);

        assertAllowToRenewOnDifferentCareProviderWhenIntygSameVGExists(
                accessService.allowToRenew(intygsTyp, vardenhet, PERSONNUMMER));
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
                .when(utkastService).checkIfPersonHasExistingIntyg(PERSONNUMMER, user);

        assertAllowToRenewOnSameCareProviderWhenIntygExists(
                accessService.allowToRenew(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToRenewOnSameCareProviderWhenIntygExists(AccessResult actualValue);

    @Test
    public void isAllowToRenewNoConditionsDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToRenewNoConditionsDifferentUnit(accessService.allowToRenew(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToRenewNoConditionsDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToRenewOnDeceasedPatientDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToRenewOnDeceasedPatientDifferentUnit(accessService.allowToRenew(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToRenewOnDeceasedPatientDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToRenewOnInactiveUnitDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToRenewOnInactiveCareUnitDifferentUnit(accessService.allowToRenew(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToRenewOnInactiveCareUnitDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToRenewOnRenewFalseDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToRenewOnRenewFalseDifferentUnit(accessService.allowToRenew(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToRenewOnRenewFalseDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToRenewOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToRenewOnSekretessPatientOnSameUnit(accessService.allowToRenew(intygsTyp, vardenhet, PERSONNUMMER));
    }

    protected abstract void assertAllowToRenewOnSekretessPatientOnSameUnit(AccessResult actualValue);

    @Test
    public void isAllowToRenewOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToRenewOnSekretessPatientOnDifferentUnit(accessService.allowToRenew(intygsTyp, vardenhet, PERSONNUMMER));
    }

    protected abstract void assertAllowToRenewOnSekretessPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToDeleteNoConditions() {
        setupMocksForNoConditions();

        assertAllowToDeleteNoConditions(accessService.allowToInvalidate(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToDeleteNoConditions(AccessResult actualValue);

    @Test
    public void isAllowToDeleteOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToDeleteOnDeceasedPatient(accessService.allowToInvalidate(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToDeleteOnDeceasedPatient(AccessResult actualValue);

    @Test
    public void isAllowToDeleteOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToDeleteOnInactiveUnit(accessService.allowToInvalidate(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToDeleteOnInactiveUnit(AccessResult actualValue);

    @Test
    public void isAllowToDeleteOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToDeleteOnRenewFalse(accessService.allowToInvalidate(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToDeleteOnRenewFalse(AccessResult actualValue);

    @Test
    public void isAllowToDeleteOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToDeleteOnSekretessPatientOnSameUnit(
                accessService.allowToInvalidate(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToDeleteOnSekretessPatientOnSameUnit(AccessResult actualValue);

    @Test
    public void isAllowToDeleteOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToDeleteOnSekretessPatientOnDifferentUnit(
                accessService.allowToInvalidate(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToDeleteOnSekretessPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToDeleteOnDeceasedPatientOnDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToDeleteOnDeceasedPatientOnDifferentUnit(
                accessService.allowToInvalidate(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToDeleteOnDeceasedPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToDeleteOnInactiveUnitOnDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToDeleteOnInactiveUnitOnDifferentUnit(
                accessService.allowToInvalidate(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToDeleteOnInactiveUnitOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToDeleteOnRenewFalseOnDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToDeleteOnRenewFalseOnDifferentUnit(
                accessService.allowToInvalidate(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToDeleteOnRenewFalseOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToDeleteNoConditionsDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToDeleteNoConditionsDifferentUnit(
                accessService.allowToInvalidate(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToDeleteNoConditionsDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToPrintNoConditions() {
        setupMocksForNoConditions();

        assertAllowToPrintNoConditions(accessService.allowToPrint(intygsTyp, vardenhet, PERSONNUMMER, isEmployer));
    }

    abstract protected void assertAllowToPrintNoConditions(AccessResult actualValue);

    @Test
    public void isAllowToPrintOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToPrintOnDeceasedPatient(accessService.allowToPrint(intygsTyp, vardenhet, PERSONNUMMER, isEmployer));
    }

    abstract protected void assertAllowToPrintOnDeceasedPatient(AccessResult actualValue);

    @Test
    public void isAllowToPrintOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToPrintOnInactiveUnit(accessService.allowToPrint(intygsTyp, vardenhet, PERSONNUMMER, isEmployer));
    }

    abstract protected void assertAllowToPrintOnInactiveUnit(AccessResult actualValue);

    @Test
    public void isAllowToPrintOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToPrintOnRenewFalse(accessService.allowToPrint(intygsTyp, vardenhet, PERSONNUMMER, isEmployer));
    }

    abstract protected void assertAllowToPrintOnRenewFalse(AccessResult actualValue);

    @Test
    public void isAllowToPrintOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToPrintOnSekretessPatientOnSameUnit(accessService.allowToPrint(intygsTyp, vardenhet, PERSONNUMMER, isEmployer));
    }

    abstract protected void assertAllowToPrintOnSekretessPatientOnSameUnit(AccessResult actualValue);

    @Test
    public void isAllowToPrintOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToPrintOnSekretessPatientOnDifferentUnit(accessService.allowToPrint(intygsTyp, vardenhet, PERSONNUMMER, isEmployer));
    }

    abstract protected void assertAllowToPrintOnSekretessPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToPrintOnDeceasedPatientOnDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToPrintOnDeceasedPatientOnDifferentUnit(accessService.allowToPrint(intygsTyp, vardenhet, PERSONNUMMER, isEmployer));
    }

    abstract protected void assertAllowToPrintOnDeceasedPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToPrintOnInactiveUnitOnDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToPrintOnInactiveUnitOnDifferentUnit(accessService.allowToPrint(intygsTyp, vardenhet, PERSONNUMMER, isEmployer));
    }

    abstract protected void assertAllowToPrintOnInactiveUnitOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToPrintOnRenewFalseOnDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToPrintOnRenewFalseOnDifferentUnit(accessService.allowToPrint(intygsTyp, vardenhet, PERSONNUMMER, isEmployer));
    }

    abstract protected void assertAllowToPrintOnRenewFalseOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToPrintNoConditionsDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToPrintNoConditionsDifferentUnit(accessService.allowToPrint(intygsTyp, vardenhet, PERSONNUMMER, isEmployer));
    }

    abstract protected void assertAllowToPrintNoConditionsDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToSendNoConditions() {
        setupMocksForNoConditions();

        assertAllowToSendNoConditions(accessService.allowToSend(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToSendNoConditions(AccessResult actualValue);

    @Test
    public void isAllowToSendOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToSendOnDeceasedPatient(accessService.allowToSend(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToSendOnDeceasedPatient(AccessResult actualValue);

    @Test
    public void isAllowToSendOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToSendOnInactiveUnit(accessService.allowToSend(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToSendOnInactiveUnit(AccessResult actualValue);

    @Test
    public void isAllowToSendOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToSendOnRenewFalse(accessService.allowToSend(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToSendOnRenewFalse(AccessResult actualValue);

    @Test
    public void isAllowToSendOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToSendOnSekretessPatientOnSameUnit(accessService.allowToSend(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToSendOnSekretessPatientOnSameUnit(AccessResult actualValue);

    @Test
    public void isAllowToSendOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToSendOnSekretessPatientOnDifferentUnit(accessService.allowToSend(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToSendOnSekretessPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToSendOnDeceasedPatientOnDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToSendOnDeceasedPatientOnDifferentUnit(accessService.allowToSend(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToSendOnDeceasedPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToSendOnInactiveUnitOnDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToSendOnInactiveUnitOnDifferentUnit(accessService.allowToSend(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToSendOnInactiveUnitOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToSendOnRenewFalseOnDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToSendOnRenewFalseOnDifferentUnit(accessService.allowToSend(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToSendOnRenewFalseOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToSendNoConditionsDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToSendNoConditionsDifferentUnit(accessService.allowToSend(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToSendNoConditionsDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToAnswerComplementNoConditions() {
        setupMocksForNoConditions();

        assertAllowToAnswerComplementNoConditions(accessService.allowToAnswerComplementQuestion(intygsTyp, vardenhet, PERSONNUMMER, true));
    }

    abstract protected void assertAllowToAnswerComplementNoConditions(AccessResult actualValue);

    @Test
    public void isAllowToAnswerComplementOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToAnswerComplementOnDeceasedPatient(
                accessService.allowToAnswerComplementQuestion(intygsTyp, vardenhet, PERSONNUMMER, true));
    }

    abstract protected void assertAllowToAnswerComplementOnDeceasedPatient(AccessResult actualValue);

    @Test
    public void isAllowToAnswerComplementOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToAnswerComplementOnInactiveUnit(
                accessService.allowToAnswerComplementQuestion(intygsTyp, vardenhet, PERSONNUMMER, true));
    }

    abstract protected void assertAllowToAnswerComplementOnInactiveUnit(AccessResult actualValue);

    @Test
    public void isAllowToAnswerComplementOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToAnswerComplementOnRenewFalse(accessService.allowToAnswerComplementQuestion(intygsTyp, vardenhet, PERSONNUMMER, true));
    }

    abstract protected void assertAllowToAnswerComplementOnRenewFalse(AccessResult actualValue);

    @Test
    public void isAllowToAnswerComplementOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToAnswerComplementOnSekretessPatientOnSameUnit(
                accessService.allowToAnswerComplementQuestion(intygsTyp, vardenhet, PERSONNUMMER, true));
    }

    abstract protected void assertAllowToAnswerComplementOnSekretessPatientOnSameUnit(AccessResult actualValue);

    @Test
    public void isAllowToAnswerComplementOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToAnswerComplementOnSekretessPatientOnDifferentUnit(
                accessService.allowToAnswerComplementQuestion(intygsTyp, vardenhet, PERSONNUMMER, true));
    }

    abstract protected void assertAllowToAnswerComplementOnSekretessPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToAnswerComplementOnDeceasedPatientOnDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToAnswerComplementOnDeceasedPatientOnDifferentUnit(
                accessService.allowToAnswerComplementQuestion(intygsTyp, vardenhet, PERSONNUMMER, true));
    }

    abstract protected void assertAllowToAnswerComplementOnDeceasedPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToAnswerComplementOnInactiveUnitOnDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToAnswerComplementOnInactiveUnitOnDifferentUnit(
                accessService.allowToAnswerComplementQuestion(intygsTyp, vardenhet, PERSONNUMMER, true));
    }

    abstract protected void assertAllowToAnswerComplementOnInactiveUnitOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToAnswerComplementOnRenewFalseOnDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToAnswerComplementOnRenewFalseOnDifferentUnit(
                accessService.allowToAnswerComplementQuestion(intygsTyp, vardenhet, PERSONNUMMER, true));
    }

    abstract protected void assertAllowToAnswerComplementOnRenewFalseOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToAnswerComplementNoConditionsDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToAnswerComplementNoConditionsDifferentUnit(
                accessService.allowToAnswerComplementQuestion(intygsTyp, vardenhet, PERSONNUMMER, true));
    }

    abstract protected void assertAllowToAnswerComplementNoConditionsDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToAnswerQuestionNoConditions() {
        setupMocksForNoConditions();

        assertAllowToAnswerQuestionNoConditions(accessService.allowToAnswerComplementQuestion(intygsTyp, vardenhet, PERSONNUMMER, false));
    }

    abstract protected void assertAllowToAnswerQuestionNoConditions(AccessResult actualValue);

    @Test
    public void isAllowToAnswerQuestionOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToAnswerQuestionOnDeceasedPatient(
                accessService.allowToAnswerComplementQuestion(intygsTyp, vardenhet, PERSONNUMMER, false));
    }

    abstract protected void assertAllowToAnswerQuestionOnDeceasedPatient(AccessResult actualValue);

    @Test
    public void isAllowToAnswerQuestionOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToAnswerQuestionOnInactiveUnit(accessService.allowToAnswerComplementQuestion(intygsTyp, vardenhet, PERSONNUMMER, false));
    }

    abstract protected void assertAllowToAnswerQuestionOnInactiveUnit(AccessResult actualValue);

    @Test
    public void isAllowToAnswerQuestionOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToAnswerQuestionOnRenewFalse(accessService.allowToAnswerComplementQuestion(intygsTyp, vardenhet, PERSONNUMMER, false));
    }

    abstract protected void assertAllowToAnswerQuestionOnRenewFalse(AccessResult actualValue);

    @Test
    public void isAllowToAnswerQuestionOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToAnswerQuestionOnSekretessPatientOnSameUnit(
                accessService.allowToAnswerComplementQuestion(intygsTyp, vardenhet, PERSONNUMMER, false));
    }

    abstract protected void assertAllowToAnswerQuestionOnSekretessPatientOnSameUnit(AccessResult actualValue);

    @Test
    public void isAllowToAnswerQuestionOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToAnswerQuestionOnSekretessPatientOnDifferentUnit(
                accessService.allowToAnswerComplementQuestion(intygsTyp, vardenhet, PERSONNUMMER, false));
    }

    abstract protected void assertAllowToAnswerQuestionOnSekretessPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToAnswerQuestionOnDeceasedPatientOnDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToAnswerQuestionOnDeceasedPatientOnDifferentUnit(
                accessService.allowToAnswerComplementQuestion(intygsTyp, vardenhet, PERSONNUMMER, false));
    }

    abstract protected void assertAllowToAnswerQuestionOnDeceasedPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToAnswerQuestionOnInactiveUnitOnDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToAnswerQuestionOnInactiveUnitOnDifferentUnit(
                accessService.allowToAnswerComplementQuestion(intygsTyp, vardenhet, PERSONNUMMER, false));
    }

    abstract protected void assertAllowToAnswerQuestionOnInactiveUnitOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToAnswerQuestionOnRenewFalseOnDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToAnswerQuestionOnRenewFalseOnDifferentUnit(
                accessService.allowToAnswerComplementQuestion(intygsTyp, vardenhet, PERSONNUMMER, false));
    }

    abstract protected void assertAllowToAnswerQuestionOnRenewFalseOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToAnswerQuestionNoConditionsDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToAnswerQuestionNoConditionsDifferentUnit(
                accessService.allowToAnswerComplementQuestion(intygsTyp, vardenhet, PERSONNUMMER, false));
    }

    abstract protected void assertAllowToAnswerQuestionNoConditionsDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadQuestionsNoConditions() {
        setupMocksForNoConditions();

        assertAllowToReadQuestionsNoConditions(accessService.allowToReadQuestions(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToReadQuestionsNoConditions(AccessResult actualValue);

    @Test
    public void isAllowToReadQuestionsOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToReadQuestionsOnDeceasedPatient(accessService.allowToReadQuestions(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToReadQuestionsOnDeceasedPatient(AccessResult actualValue);

    @Test
    public void isAllowToReadQuestionsOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToReadQuestionsOnInactiveUnit(accessService.allowToReadQuestions(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToReadQuestionsOnInactiveUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadQuestionsOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToReadQuestionsOnRenewFalse(accessService.allowToReadQuestions(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToReadQuestionsOnRenewFalse(AccessResult actualValue);

    @Test
    public void isAllowToReadQuestionsOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToReadQuestionsOnSekretessPatientOnSameUnit(accessService.allowToReadQuestions(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToReadQuestionsOnSekretessPatientOnSameUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadQuestionsOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToReadQuestionsOnSekretessPatientOnDifferentUnit(accessService.allowToReadQuestions(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToReadQuestionsOnSekretessPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadQuestionsOnDeceasedPatientOnDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToReadQuestionsOnDeceasedPatientOnDifferentUnit(accessService.allowToReadQuestions(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToReadQuestionsOnDeceasedPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadQuestionsOnInactiveUnitOnDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToReadQuestionsOnInactiveUnitOnDifferentUnit(accessService.allowToReadQuestions(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToReadQuestionsOnInactiveUnitOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadQuestionsOnRenewFalseOnDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToReadQuestionsOnRenewFalseOnDifferentUnit(accessService.allowToReadQuestions(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToReadQuestionsOnRenewFalseOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadQuestionsNoConditionsDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToReadQuestionsNoConditionsDifferentUnit(accessService.allowToReadQuestions(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToReadQuestionsNoConditionsDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToForwardQuestionsNoConditions() {
        setupMocksForNoConditions();

        assertAllowToForwardQuestionsNoConditions(accessService.allowToForwardQuestions(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToForwardQuestionsNoConditions(AccessResult actualValue);

    @Test
    public void isAllowToForwardQuestionsOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToForwardQuestionsOnDeceasedPatient(accessService.allowToForwardQuestions(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToForwardQuestionsOnDeceasedPatient(AccessResult actualValue);

    @Test
    public void isAllowToForwardQuestionsOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToForwardQuestionsOnInactiveUnit(accessService.allowToForwardQuestions(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToForwardQuestionsOnInactiveUnit(AccessResult actualValue);

    @Test
    public void isAllowToForwardQuestionsOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToForwardQuestionsOnRenewFalse(accessService.allowToForwardQuestions(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToForwardQuestionsOnRenewFalse(AccessResult actualValue);

    @Test
    public void isAllowToForwardQuestionsOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToForwardQuestionsOnSekretessPatientOnSameUnit(
                accessService.allowToForwardQuestions(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToForwardQuestionsOnSekretessPatientOnSameUnit(AccessResult actualValue);

    @Test
    public void isAllowToForwardQuestionsOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToForwardQuestionsOnSekretessPatientOnDifferentUnit(
                accessService.allowToForwardQuestions(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToForwardQuestionsOnSekretessPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToForwardQuestionsOnDeceasedPatientOnDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToForwardQuestionsOnDeceasedPatientOnDifferentUnit(
                accessService.allowToForwardQuestions(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToForwardQuestionsOnDeceasedPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToForwardQuestionsOnInactiveUnitOnDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToForwardQuestionsOnInactiveUnitOnDifferentUnit(
                accessService.allowToForwardQuestions(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToForwardQuestionsOnInactiveUnitOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToForwardQuestionsOnRenewFalseOnDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToForwardQuestionsOnRenewFalseOnDifferentUnit(accessService.allowToForwardQuestions(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToForwardQuestionsOnRenewFalseOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToForwardQuestionsNoConditionsDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToForwardQuestionsNoConditionsDifferentUnit(accessService.allowToForwardQuestions(intygsTyp, vardenhet, PERSONNUMMER));
    }

    abstract protected void assertAllowToForwardQuestionsNoConditionsDifferentUnit(AccessResult actualValue);
}
