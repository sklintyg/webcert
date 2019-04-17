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

import static org.mockito.Mockito.doReturn;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.web.service.access.data.AccessServiceTestData;
import se.inera.intyg.webcert.web.service.access.util.AccessServiceTestToolkit;
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

        assertAllowToReadUtkastNoConditions(accessService.allowToRead(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToReadUtkastNoConditions(boolean actualValue);

    @Test
    public void isAllowToReadUtkastOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToReadUtkastOnDeceasedPatient(accessService.allowToRead(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToReadUtkastOnDeceasedPatient(boolean actualValue);

    @Test
    public void isAllowToReadUtkastOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToReadUtkastOnInactiveUnit(accessService.allowToRead(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToReadUtkastOnInactiveUnit(boolean actualValue);

    @Test
    public void isAllowToReadUtkastOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToReadUtkastOnRenewFalse(accessService.allowToRead(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToReadUtkastOnRenewFalse(boolean actualValue);

    @Test
    public void isAllowToReadOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToReadOnSekretessPatientOnSameUnit(accessService.allowToRead(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToReadOnSekretessPatientOnSameUnit(boolean actualValue);

    @Test
    public void isAllowToReadUtkastOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToReadUtkastOnSekretessPatientOnDifferentUnit(accessService.allowToRead(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToReadUtkastOnSekretessPatientOnDifferentUnit(boolean actualValue);

    @Test
    public void isAllowToReadUtkastNoConditionsOnDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToReadUtkastNoConditionsOnDifferentUnit(accessService.allowToRead(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToReadUtkastNoConditionsOnDifferentUnit(boolean actualValue);

    @Test
    public void isAllowToReadUtkastOnDeceasedPatientOnDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToReadUtkastOnDeceasedPatientOnDifferentUnit(accessService.allowToRead(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToReadUtkastOnDeceasedPatientOnDifferentUnit(boolean actualValue);

    @Test
    public void isAllowToReadUtkastOnInactiveUnitOnDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToReadUtkastOnInactiveUnitOnDifferentUnit(accessService.allowToRead(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToReadUtkastOnInactiveUnitOnDifferentUnit(boolean actualValue);

    @Test
    public void isAllowToReadUtkastOnRenewFalseOnDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToReadUtkastOnRenewFalseOnDifferentUnit(accessService.allowToRead(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToReadUtkastOnRenewFalseOnDifferentUnit(boolean actualValue);

    @Test
    public void isAllowToCopyNoConditions() {
        setupMocksForNoConditions();

        assertAllowToCopyNoConditions(accessService.allowedToCopyLockedUtkast(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToCopyNoConditions(boolean actualValue);

    @Test
    public void isAllowToCopyOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToCopyOnDeceasedPatient(accessService.allowedToCopyLockedUtkast(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToCopyOnDeceasedPatient(boolean actualValue);

    @Test
    public void isAllowToCopyOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToCopyOnInactiveCareUnit(accessService.allowedToCopyLockedUtkast(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToCopyOnInactiveCareUnit(boolean actualValue);

    @Test
    public void isAllowToCopyOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToCopyOnRenewFalse(accessService.allowedToCopyLockedUtkast(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToCopyOnRenewFalse(boolean actualValue);

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
                .when(utkastService).checkIfPersonHasExistingIntyg(PERSONNUMMER, user);

        assertAllowToCopyOnSameCareProviderWhenUtkastSameVGExists(
                accessService.allowedToCopyLockedUtkast(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToCopyOnSameCareProviderWhenUtkastSameVGExists(boolean actualValue);

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
                .when(utkastService).checkIfPersonHasExistingIntyg(PERSONNUMMER, user);

        assertAllowToCopyOnDifferentCareProviderWhenIntygSameVGExists(
                accessService.allowedToCopyLockedUtkast(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToCopyOnDifferentCareProviderWhenIntygSameVGExists(boolean actualValue);

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
                .when(utkastService).checkIfPersonHasExistingIntyg(PERSONNUMMER, user);

        assertAllowToCopyOnSameCareProviderWhenIntygExists(
                accessService.allowedToCopyLockedUtkast(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToCopyOnSameCareProviderWhenIntygExists(boolean actualValue);

    @Test
    public void isAllowToCopyNoConditionsDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToCopyNoConditionsDifferentUnit(accessService.allowedToCopyLockedUtkast(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToCopyNoConditionsDifferentUnit(boolean actualValue);

    @Test
    public void isAllowToCopyOnDeceasedPatientDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToCopyOnDeceasedPatientDifferentUnit(accessService.allowedToCopyLockedUtkast(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToCopyOnDeceasedPatientDifferentUnit(boolean actualValue);

    @Test
    public void isAllowToCopyOnInactiveUnitDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToCopyOnInactiveCareUnitDifferentUnit(accessService.allowedToCopyLockedUtkast(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToCopyOnInactiveCareUnitDifferentUnit(boolean actualValue);

    @Test
    public void isAllowToCopyOnRenewFalseDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToCopyOnRenewFalseDifferentUnit(accessService.allowedToCopyLockedUtkast(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToCopyOnRenewFalseDifferentUnit(boolean actualValue);

    @Test
    public void isAllowToDeleteUtkastNoConditions() {
        setupMocksForNoConditions();

        assertAllowToDeleteUtkastNoConditions(accessService.allowedToInvalidateLockedUtkast(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToDeleteUtkastNoConditions(boolean actualValue);

    @Test
    public void isAllowToDeleteUtkastOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToDeleteUtkastOnDeceasedPatient(accessService.allowedToInvalidateLockedUtkast(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToDeleteUtkastOnDeceasedPatient(boolean actualValue);

    @Test
    public void isAllowToDeleteUtkastOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToDeleteUtkastOnInactiveUnit(accessService.allowedToInvalidateLockedUtkast(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToDeleteUtkastOnInactiveUnit(boolean actualValue);

    @Test
    public void isAllowToDeleteUtkastOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToDeleteUtkastOnRenewFalse(accessService.allowedToInvalidateLockedUtkast(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToDeleteUtkastOnRenewFalse(boolean actualValue);

    @Test
    public void isAllowToDeleteUtkastOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToDeleteUtkastOnSekretessPatientOnSameUnit(
                accessService.allowedToInvalidateLockedUtkast(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToDeleteUtkastOnSekretessPatientOnSameUnit(boolean actualValue);

    @Test
    public void isAllowToDeleteUtkastOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToDeleteUtkastOnSekretessPatientOnDifferentUnit(
                accessService.allowedToInvalidateLockedUtkast(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToDeleteUtkastOnSekretessPatientOnDifferentUnit(boolean actualValue);

    @Test
    public void isAllowToDeleteUtkastOnDeceasedPatientOnDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToDeleteUtkastOnDeceasedPatientOnDifferentUnit(
                accessService.allowedToInvalidateLockedUtkast(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToDeleteUtkastOnDeceasedPatientOnDifferentUnit(boolean actualValue);

    @Test
    public void isAllowToDeleteUtkastOnInactiveUnitOnDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToDeleteUtkastOnInactiveUnitOnDifferentUnit(
                accessService.allowedToInvalidateLockedUtkast(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToDeleteUtkastOnInactiveUnitOnDifferentUnit(boolean actualValue);

    @Test
    public void isAllowToDeleteUtkastOnRenewFalseOnDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToDeleteUtkastOnRenewFalseOnDifferentUnit(
                accessService.allowedToInvalidateLockedUtkast(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToDeleteUtkastOnRenewFalseOnDifferentUnit(boolean actualValue);

    @Test
    public void isAllowToDeleteUtkastNoConditionsDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToDeleteUtkastNoConditionsDifferentUnit(
                accessService.allowedToInvalidateLockedUtkast(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToDeleteUtkastNoConditionsDifferentUnit(boolean actualValue);

    @Test
    public void isAllowToPrintUtkastNoConditions() {
        setupMocksForNoConditions();

        assertAllowToPrintUtkastNoConditions(accessService.allowToPrint(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToPrintUtkastNoConditions(boolean actualValue);

    @Test
    public void isAllowToPrintUtkastOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToPrintUtkastOnDeceasedPatient(accessService.allowToPrint(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToPrintUtkastOnDeceasedPatient(boolean actualValue);

    @Test
    public void isAllowToPrintUtkastOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToPrintUtkastOnInactiveUnit(accessService.allowToPrint(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToPrintUtkastOnInactiveUnit(boolean actualValue);

    @Test
    public void isAllowToPrintUtkastOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToPrintUtkastOnRenewFalse(accessService.allowToPrint(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToPrintUtkastOnRenewFalse(boolean actualValue);

    @Test
    public void isAllowToPrintUtkastOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToPrintUtkastOnSekretessPatientOnSameUnit(accessService.allowToPrint(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToPrintUtkastOnSekretessPatientOnSameUnit(boolean actualValue);

    @Test
    public void isAllowToPrintUtkastOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToPrintUtkastOnSekretessPatientOnDifferentUnit(accessService.allowToPrint(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToPrintUtkastOnSekretessPatientOnDifferentUnit(boolean actualValue);

    @Test
    public void isAllowToPrintUtkastOnDeceasedPatientOnDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToPrintUtkastOnDeceasedPatientOnDifferentUnit(accessService.allowToPrint(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToPrintUtkastOnDeceasedPatientOnDifferentUnit(boolean actualValue);

    @Test
    public void isAllowToPrintUtkastOnInactiveUnitOnDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToPrintUtkastOnInactiveUnitOnDifferentUnit(accessService.allowToPrint(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToPrintUtkastOnInactiveUnitOnDifferentUnit(boolean actualValue);

    @Test
    public void isAllowToPrintUtkastOnRenewFalseOnDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToPrintUtkastOnRenewFalseOnDifferentUnit(accessService.allowToPrint(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToPrintUtkastOnRenewFalseOnDifferentUnit(boolean actualValue);

    @Test
    public void isAllowToPrintUtkastNoConditionsDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToPrintUtkastNoConditionsDifferentUnit(accessService.allowToPrint(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToPrintUtkastNoConditionsDifferentUnit(boolean actualValue);
}
