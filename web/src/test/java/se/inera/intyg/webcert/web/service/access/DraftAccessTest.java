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

abstract public class DraftAccessTest extends AccessTest {
    @InjectMocks
    private DraftAccessServiceImpl accessService;

    DraftAccessTest(String intygsTyp, AccessServiceTestData accessServiceTestData) {
        super(intygsTyp, accessServiceTestData);
    }

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void isAllowedToCreateUtkastNoConditions() {
        setupMocksForNoConditions();

        assertAllowedToCreateUtkastNoConditions(accessService.allowToCreateDraft(intygsTyp, PERSONNUMMER));
    }

    abstract protected void assertAllowedToCreateUtkastNoConditions(AccessResult actualValue);

    @Test
    public void isAllowedToCreateUtkastOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowedToCreateUtkastOnDeceasedPatient(accessService.allowToCreateDraft(intygsTyp, PERSONNUMMER));
    }

    abstract protected void assertAllowedToCreateUtkastOnDeceasedPatient(AccessResult actualValue);

    @Test
    public void isAllowedToCreateUtkastOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowedToCreateUtkastOnInactiveCareUnit(accessService.allowToCreateDraft(intygsTyp, PERSONNUMMER));
    }

    abstract protected void assertAllowedToCreateUtkastOnInactiveCareUnit(AccessResult actualValue);

    @Test
    public void isAllowedToCreateUtkastOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowedToCreateUtkastOnRenewFalse(accessService.allowToCreateDraft(intygsTyp, PERSONNUMMER));
    }

    abstract protected void assertAllowedToCreateUtkastOnRenewFalse(AccessResult actualValue);

    @Test
    public void isAllowToCreateUtkastOnSameCareProviderWhenUtkastSameVGExists() {
        final WebCertUser user = AccessServiceTestToolkit.createUser(intygsTyp, accessServiceTestData);
        doReturn(user)
                .when(webCertUserService).getUser();
        doReturn(false)
                .when(patientDetailsResolver).isAvliden(PERSONNUMMER);
        doReturn(SekretessStatus.FALSE)
                .when(patientDetailsResolver).getSekretessStatus(PERSONNUMMER);
        doReturn(true)
                .when(webCertUserService).userIsLoggedInOnEnhetOrUnderenhet(enhetsId);
        doReturn(AccessServiceTestToolkit.createPreviousUtkastForUtkast(intygsTyp))
                .when(utkastService).checkIfPersonHasExistingIntyg(PERSONNUMMER, user);

        assertAllowToCreateUtkastOnSameCareProviderWhenUtkastSameVGExists(accessService.allowToCreateDraft(intygsTyp, PERSONNUMMER));
    }

    abstract protected void assertAllowToCreateUtkastOnSameCareProviderWhenUtkastSameVGExists(AccessResult actualValue);

    @Test
    public void isAllowToCreateUtkastOnDifferentCareProviderWhenIntygSameVGExists() {
        final WebCertUser user = AccessServiceTestToolkit.createUser(intygsTyp, accessServiceTestData);
        doReturn(user)
                .when(webCertUserService).getUser();
        doReturn(false)
                .when(patientDetailsResolver).isAvliden(PERSONNUMMER);
        doReturn(SekretessStatus.FALSE)
                .when(patientDetailsResolver).getSekretessStatus(PERSONNUMMER);
        doReturn(true)
                .when(webCertUserService).userIsLoggedInOnEnhetOrUnderenhet(enhetsId);
        doReturn(AccessServiceTestToolkit.createPreviousIntygForUtkast(intygsTyp, true))
                .when(utkastService).checkIfPersonHasExistingIntyg(PERSONNUMMER, user);

        assertAllowToCreateUtkastOnDifferentCareProviderWhenIntygSameVGExists(accessService.allowToCreateDraft(intygsTyp, PERSONNUMMER));
    }

    abstract protected void assertAllowToCreateUtkastOnDifferentCareProviderWhenIntygSameVGExists(AccessResult actualValue);

    @Test
    public void isAllowToCreateUtkastOnSameCareProviderWhenIntygExists() {
        final WebCertUser user = AccessServiceTestToolkit.createUser(intygsTyp, accessServiceTestData);
        doReturn(user)
                .when(webCertUserService).getUser();
        doReturn(false)
                .when(patientDetailsResolver).isAvliden(PERSONNUMMER);
        doReturn(SekretessStatus.FALSE)
                .when(patientDetailsResolver).getSekretessStatus(PERSONNUMMER);
        doReturn(true)
                .when(webCertUserService).userIsLoggedInOnEnhetOrUnderenhet(enhetsId);
        doReturn(AccessServiceTestToolkit.createPreviousIntygForUtkast(intygsTyp, false))
                .when(utkastService).checkIfPersonHasExistingIntyg(PERSONNUMMER, user);

        assertAllowToCreateUtkastOnSameCareProviderWhenIntygExists(accessService.allowToCreateDraft(intygsTyp, PERSONNUMMER));
    }

    abstract protected void assertAllowToCreateUtkastOnSameCareProviderWhenIntygExists(AccessResult actualValue);

    @Test
    public void isAllowToReadUtkastNoConditions() {
        setupMocksForNoConditions();

        assertAllowToReadUtkastNoConditions(accessService.allowToReadDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToReadUtkastNoConditions(AccessResult actualValue);

    @Test
    public void isAllowToReadUtkastOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToReadUtkastOnDeceasedPatient(accessService.allowToReadDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToReadUtkastOnDeceasedPatient(AccessResult actualValue);

    @Test
    public void isAllowToReadUtkastOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToReadUtkastOnInactiveUnit(accessService.allowToReadDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToReadUtkastOnInactiveUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadUtkastOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToReadUtkastOnRenewFalse(accessService.allowToReadDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToReadUtkastOnRenewFalse(AccessResult actualValue);

    @Test
    public void isAllowToReadOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToReadOnSekretessPatientOnSameUnit(accessService.allowToReadDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToReadOnSekretessPatientOnSameUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadUtkastOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToReadUtkastOnSekretessPatientOnDifferentUnit(accessService.allowToReadDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToReadUtkastOnSekretessPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadUtkastNoConditionsOnDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToReadUtkastNoConditionsOnDifferentUnit(accessService.allowToReadDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToReadUtkastNoConditionsOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadUtkastOnDeceasedPatientOnDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToReadUtkastOnDeceasedPatientOnDifferentUnit(accessService.allowToReadDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToReadUtkastOnDeceasedPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadUtkastOnInactiveUnitOnDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToReadUtkastOnInactiveUnitOnDifferentUnit(accessService.allowToReadDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToReadUtkastOnInactiveUnitOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToReadUtkastOnRenewFalseOnDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToReadUtkastOnRenewFalseOnDifferentUnit(accessService.allowToReadDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToReadUtkastOnRenewFalseOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToEditUtkastNoConditions() {
        setupMocksForNoConditions();

        assertAllowToEditUtkastNoConditions(accessService.allowToEditDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToEditUtkastNoConditions(AccessResult actualValue);

    @Test
    public void isAllowToEditUtkastOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToEditUtkastOnDeceasedPatient(accessService.allowToEditDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToEditUtkastOnDeceasedPatient(AccessResult actualValue);

    @Test
    public void isAllowToEditUtkastOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToEditUtkastOnInactiveUnit(accessService.allowToEditDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToEditUtkastOnInactiveUnit(AccessResult actualValue);

    @Test
    public void isAllowToEditUtkastOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToEditUtkastOnRenewFalse(accessService.allowToEditDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToEditUtkastOnRenewFalse(AccessResult actualValue);

    @Test
    public void isAllowToEditUtkastOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToEditUtkastOnSekretessPatientOnSameUnit(accessService.allowToEditDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToEditUtkastOnSekretessPatientOnSameUnit(AccessResult actualValue);

    @Test
    public void isAllowToEditUtkastOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToEditUtkastOnSekretessPatientOnDifferentUnit(accessService.allowToEditDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToEditUtkastOnSekretessPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToEditUtkastOnDeceasedPatientOnDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToEditUtkastOnDeceasedPatientOnDifferentUnit(accessService.allowToEditDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToEditUtkastOnDeceasedPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToEditUtkastOnInactiveUnitOnDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToEditUtkastOnInactiveUnitOnDifferentUnit(accessService.allowToEditDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToEditUtkastOnInactiveUnitOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToEditUtkastOnRenewFalseOnDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToEditUtkastOnRenewFalseOnDifferentUnit(accessService.allowToEditDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToEditUtkastOnRenewFalseOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToEditUtkastNoConditionsDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToEditUtkastNoConditionsDifferentUnit(accessService.allowToEditDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToEditUtkastNoConditionsDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToSignUtkastNoConditions() {
        setupMocksForNoConditions();

        assertAllowToSignUtkastNoConditions(accessService.allowToSignDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToSignUtkastNoConditions(AccessResult actualValue);

    @Test
    public void isAllowToSignUtkastOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToSignUtkastOnDeceasedPatient(accessService.allowToSignDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToSignUtkastOnDeceasedPatient(AccessResult actualValue);

    @Test
    public void isAllowToSignUtkastOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToSignUtkastOnInactiveUnit(accessService.allowToSignDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToSignUtkastOnInactiveUnit(AccessResult actualValue);

    @Test
    public void isAllowToSignUtkastOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToSignUtkastOnRenewFalse(accessService.allowToSignDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToSignUtkastOnRenewFalse(AccessResult actualValue);

    @Test
    public void isAllowToSignUtkastOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToSignUtkastOnSekretessPatientOnSameUnit(accessService.allowToSignDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToSignUtkastOnSekretessPatientOnSameUnit(AccessResult actualValue);

    @Test
    public void isAllowToSignUtkastOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToSignUtkastOnSekretessPatientOnDifferentUnit(accessService.allowToSignDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToSignUtkastOnSekretessPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToSignUtkastOnDeceasedPatientOnDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToSignUtkastOnDeceasedPatientOnDifferentUnit(accessService.allowToSignDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToSignUtkastOnDeceasedPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToSignUtkastOnInactiveUnitOnDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToSignUtkastOnInactiveUnitOnDifferentUnit(accessService.allowToSignDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToSignUtkastOnInactiveUnitOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToSignUtkastOnRenewFalseOnDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToSignUtkastOnRenewFalseOnDifferentUnit(accessService.allowToSignDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToSignUtkastOnRenewFalseOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToSignUtkastNoConditionsDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToSignUtkastNoConditionsDifferentUnit(accessService.allowToSignDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToSignUtkastNoConditionsDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToDeleteUtkastNoConditions() {
        setupMocksForNoConditions();

        assertAllowToDeleteUtkastNoConditions(accessService.allowToDeleteDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToDeleteUtkastNoConditions(AccessResult actualValue);

    @Test
    public void isAllowToDeleteUtkastOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToDeleteUtkastOnDeceasedPatient(accessService.allowToDeleteDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToDeleteUtkastOnDeceasedPatient(AccessResult actualValue);

    @Test
    public void isAllowToDeleteUtkastOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToDeleteUtkastOnInactiveUnit(accessService.allowToDeleteDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToDeleteUtkastOnInactiveUnit(AccessResult actualValue);

    @Test
    public void isAllowToDeleteUtkastOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToDeleteUtkastOnRenewFalse(accessService.allowToDeleteDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToDeleteUtkastOnRenewFalse(AccessResult actualValue);

    @Test
    public void isAllowToDeleteUtkastOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToDeleteUtkastOnSekretessPatientOnSameUnit(accessService.allowToDeleteDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToDeleteUtkastOnSekretessPatientOnSameUnit(AccessResult actualValue);

    @Test
    public void isAllowToDeleteUtkastOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToDeleteUtkastOnSekretessPatientOnDifferentUnit(accessService.allowToDeleteDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToDeleteUtkastOnSekretessPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToDeleteUtkastOnDeceasedPatientOnDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToDeleteUtkastOnDeceasedPatientOnDifferentUnit(accessService.allowToDeleteDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToDeleteUtkastOnDeceasedPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToDeleteUtkastOnInactiveUnitOnDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToDeleteUtkastOnInactiveUnitOnDifferentUnit(accessService.allowToDeleteDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToDeleteUtkastOnInactiveUnitOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToDeleteUtkastOnRenewFalseOnDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToDeleteUtkastOnRenewFalseOnDifferentUnit(accessService.allowToDeleteDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToDeleteUtkastOnRenewFalseOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToDeleteUtkastNoConditionsDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToDeleteUtkastNoConditionsDifferentUnit(accessService.allowToDeleteDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToDeleteUtkastNoConditionsDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToPrintUtkastNoConditions() {
        setupMocksForNoConditions();

        assertAllowToPrintUtkastNoConditions(accessService.allowToPrintDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToPrintUtkastNoConditions(AccessResult actualValue);

    @Test
    public void isAllowToPrintUtkastOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToPrintUtkastOnDeceasedPatient(accessService.allowToPrintDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToPrintUtkastOnDeceasedPatient(AccessResult actualValue);

    @Test
    public void isAllowToPrintUtkastOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToPrintUtkastOnInactiveUnit(accessService.allowToPrintDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToPrintUtkastOnInactiveUnit(AccessResult actualValue);

    @Test
    public void isAllowToPrintUtkastOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToPrintUtkastOnRenewFalse(accessService.allowToPrintDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToPrintUtkastOnRenewFalse(AccessResult actualValue);

    @Test
    public void isAllowToPrintUtkastOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToPrintUtkastOnSekretessPatientOnSameUnit(accessService.allowToPrintDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToPrintUtkastOnSekretessPatientOnSameUnit(AccessResult actualValue);

    @Test
    public void isAllowToPrintUtkastOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToPrintUtkastOnSekretessPatientOnDifferentUnit(accessService.allowToPrintDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToPrintUtkastOnSekretessPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToPrintUtkastOnDeceasedPatientOnDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToPrintUtkastOnDeceasedPatientOnDifferentUnit(accessService.allowToPrintDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToPrintUtkastOnDeceasedPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToPrintUtkastOnInactiveUnitOnDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToPrintUtkastOnInactiveUnitOnDifferentUnit(accessService.allowToPrintDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToPrintUtkastOnInactiveUnitOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToPrintUtkastOnRenewFalseOnDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToPrintUtkastOnRenewFalseOnDifferentUnit(accessService.allowToPrintDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToPrintUtkastOnRenewFalseOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToPrintUtkastNoConditionsDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToPrintUtkastNoConditionsDifferentUnit(accessService.allowToPrintDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToPrintUtkastNoConditionsDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToForwardUtkastNoConditions() {
        setupMocksForNoConditions();

        assertAllowToForwardUtkastNoConditions(accessService.allowToForwardDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToForwardUtkastNoConditions(AccessResult actualValue);

    @Test
    public void isAllowToForwardUtkastOnDeceasedPatient() {
        setupMocksForDeceasedPatient();

        assertAllowToForwardUtkastOnDeceasedPatient(accessService.allowToForwardDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToForwardUtkastOnDeceasedPatient(AccessResult actualValue);

    @Test
    public void isAllowToForwardUtkastOnInactiveUnit() {
        setupMocksForInactiveUnit();

        assertAllowToForwardUtkastOnInactiveUnit(accessService.allowToForwardDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToForwardUtkastOnInactiveUnit(AccessResult actualValue);

    @Test
    public void isAllowToForwardUtkastOnRenewFalse() {
        setupMocksForOnRenewFalse();

        assertAllowToForwardUtkastOnRenewFalse(accessService.allowToForwardDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToForwardUtkastOnRenewFalse(AccessResult actualValue);

    @Test
    public void isAllowToForwardUtkastOnSekretessPatientOnSameUnit() {
        setupMocksForOnSekretessPatient();

        assertAllowToForwardUtkastOnSekretessPatientOnSameUnit(accessService.allowToForwardDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToForwardUtkastOnSekretessPatientOnSameUnit(AccessResult actualValue);

    @Test
    public void isAllowToForwardUtkastOnSekretessPatientOnDifferentUnit() {
        setupMocksForOnSekretessPatientDifferentUnit();

        assertAllowToForwardUtkastOnSekretessPatientOnDifferentUnit(accessService.allowToForwardDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToForwardUtkastOnSekretessPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToForwardUtkastOnDeceasedPatientOnDifferentUnit() {
        setupMocksForDeceasedPatientDifferentUnit();

        assertAllowToForwardUtkastOnDeceasedPatientOnDifferentUnit(accessService.allowToForwardDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToForwardUtkastOnDeceasedPatientOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToForwardUtkastOnInactiveUnitOnDifferentUnit() {
        setupMocksForInactiveUnitDifferentUnit();

        assertAllowToForwardUtkastOnInactiveUnitOnDifferentUnit(accessService.allowToForwardDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToForwardUtkastOnInactiveUnitOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToForwardUtkastOnRenewFalseOnDifferentUnit() {
        setupMocksForOnRenewFalseDifferentUnit();

        assertAllowToForwardUtkastOnRenewFalseOnDifferentUnit(accessService.allowToForwardDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToForwardUtkastOnRenewFalseOnDifferentUnit(AccessResult actualValue);

    @Test
    public void isAllowToForwardUtkastNoConditionsDifferentUnit() {
        setupMocksForNoConditionsDifferentUnit();

        assertAllowToForwardUtkastNoConditionsDifferentUnit(accessService.allowToForwardDraft(intygsTyp, enhetsId, PERSONNUMMER));
    }

    abstract protected void assertAllowToForwardUtkastNoConditionsDifferentUnit(AccessResult actualValue);
}
