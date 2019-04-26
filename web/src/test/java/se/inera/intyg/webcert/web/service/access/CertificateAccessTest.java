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
}
