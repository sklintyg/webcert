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
import static org.mockito.Mockito.mock;

import org.mockito.Mock;

import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.web.service.access.data.AccessServiceTestData;
import se.inera.intyg.webcert.web.service.access.util.AccessServiceTestToolkit;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;

abstract public class AccessTest {
    @Mock
    protected PatientDetailsResolver patientDetailsResolver;

    @Mock
    protected WebCertUserService webCertUserService;

    @Mock
    protected UtkastService utkastService;

    protected final String intygsTyp;
    protected final AccessServiceTestData accessServiceTestData;

    public static final Vardenhet vardenhet = mock(Vardenhet.class);
    public static final Vardgivare vardgivare = mock(Vardgivare.class);
    public static final String enhetsId = "EnhetsId";
    public static final boolean isEmployer = false;
    public static final String vardgivarId = "VardgivarId";
    public static final String PERSONNUMMER_TOLVAN = "191212121212";
    public static final Personnummer PERSONNUMMER = Personnummer.createPersonnummer(PERSONNUMMER_TOLVAN).get();

    public AccessTest(String intygsTyp, AccessServiceTestData accessServiceTestData) {
        this.intygsTyp = intygsTyp;
        this.accessServiceTestData = accessServiceTestData;
    }

    protected void setupMocksForNoConditions() {
        final WebCertUser user = AccessServiceTestToolkit.createUser(intygsTyp, accessServiceTestData);
        doReturn(user)
                .when(webCertUserService).getUser();
        doReturn(false)
                .when(patientDetailsResolver).isAvliden(PERSONNUMMER);
        doReturn(SekretessStatus.FALSE)
                .when(patientDetailsResolver).getSekretessStatus(PERSONNUMMER);
        doReturn(true)
                .when(webCertUserService).userIsLoggedInOnEnhetOrUnderenhet(enhetsId);
        doReturn(AccessServiceTestToolkit.createEmptyPreviousForUtkast(intygsTyp))
                .when(utkastService).checkIfPersonHasExistingIntyg(PERSONNUMMER, user);
        doReturn(enhetsId).when(vardenhet).getEnhetsid();
        doReturn(vardgivare).when(vardenhet).getVardgivare();
        doReturn(vardgivarId).when(vardgivare).getVardgivarid();
        doReturn(true).when(webCertUserService).isAuthorizedForUnit(vardgivarId, enhetsId, true);
    }

    protected void setupMocksForNoConditionsDifferentUnit() {
        final WebCertUser user = AccessServiceTestToolkit.createUserDifferentUnit(intygsTyp, accessServiceTestData);
        doReturn(user)
                .when(webCertUserService).getUser();
        doReturn(false)
                .when(patientDetailsResolver).isAvliden(PERSONNUMMER);
        doReturn(SekretessStatus.FALSE)
                .when(patientDetailsResolver).getSekretessStatus(PERSONNUMMER);
        doReturn(false)
                .when(webCertUserService).userIsLoggedInOnEnhetOrUnderenhet(enhetsId);
        doReturn(AccessServiceTestToolkit.createEmptyPreviousForUtkast(intygsTyp))
                .when(utkastService).checkIfPersonHasExistingIntyg(PERSONNUMMER, user);
        doReturn(enhetsId).when(vardenhet).getEnhetsid();
        doReturn(vardgivare).when(vardenhet).getVardgivare();
        doReturn(vardgivarId).when(vardgivare).getVardgivarid();
        doReturn(true).when(webCertUserService).isAuthorizedForUnit(vardgivarId, enhetsId, true);
    }

    protected void setupMocksForDeceasedPatient() {
        final WebCertUser user = AccessServiceTestToolkit.createUser(intygsTyp, accessServiceTestData);
        doReturn(user)
                .when(webCertUserService).getUser();
        doReturn(true)
                .when(patientDetailsResolver).isAvliden(PERSONNUMMER);
        doReturn(SekretessStatus.FALSE)
                .when(patientDetailsResolver).getSekretessStatus(PERSONNUMMER);
        doReturn(true)
                .when(webCertUserService).userIsLoggedInOnEnhetOrUnderenhet(enhetsId);
        doReturn(AccessServiceTestToolkit.createEmptyPreviousForUtkast(intygsTyp))
                .when(utkastService).checkIfPersonHasExistingIntyg(PERSONNUMMER, user);
        doReturn(enhetsId).when(vardenhet).getEnhetsid();
        doReturn(vardgivare).when(vardenhet).getVardgivare();
        doReturn(vardgivarId).when(vardgivare).getVardgivarid();
        doReturn(true).when(webCertUserService).isAuthorizedForUnit(vardgivarId, enhetsId, true);
    }

    protected void setupMocksForDeceasedPatientDifferentUnit() {
        final WebCertUser user = AccessServiceTestToolkit.createUser(intygsTyp, accessServiceTestData);
        doReturn(user)
                .when(webCertUserService).getUser();
        doReturn(true)
                .when(patientDetailsResolver).isAvliden(PERSONNUMMER);
        doReturn(SekretessStatus.FALSE)
                .when(patientDetailsResolver).getSekretessStatus(PERSONNUMMER);
        doReturn(false)
                .when(webCertUserService).userIsLoggedInOnEnhetOrUnderenhet(enhetsId);
        doReturn(AccessServiceTestToolkit.createEmptyPreviousForUtkast(intygsTyp))
                .when(utkastService).checkIfPersonHasExistingIntyg(PERSONNUMMER, user);
        doReturn(enhetsId).when(vardenhet).getEnhetsid();
        doReturn(vardgivare).when(vardenhet).getVardgivare();
        doReturn(vardgivarId).when(vardgivare).getVardgivarid();
        doReturn(true).when(webCertUserService).isAuthorizedForUnit(vardgivarId, enhetsId, true);
    }

    protected void setupMocksForInactiveUnit() {
        final WebCertUser user = AccessServiceTestToolkit.createUser(intygsTyp, accessServiceTestData, true);
        doReturn(user)
                .when(webCertUserService).getUser();
        doReturn(false)
                .when(patientDetailsResolver).isAvliden(PERSONNUMMER);
        doReturn(SekretessStatus.FALSE)
                .when(patientDetailsResolver).getSekretessStatus(PERSONNUMMER);
        doReturn(true)
                .when(webCertUserService).userIsLoggedInOnEnhetOrUnderenhet(enhetsId);
        doReturn(AccessServiceTestToolkit.createEmptyPreviousForUtkast(intygsTyp))
                .when(utkastService).checkIfPersonHasExistingIntyg(PERSONNUMMER, user);
        doReturn(enhetsId).when(vardenhet).getEnhetsid();
        doReturn(vardgivare).when(vardenhet).getVardgivare();
        doReturn(vardgivarId).when(vardgivare).getVardgivarid();
        doReturn(true).when(webCertUserService).isAuthorizedForUnit(vardgivarId, enhetsId, true);
    }

    protected void setupMocksForInactiveUnitDifferentUnit() {
        final WebCertUser user = AccessServiceTestToolkit.createUser(intygsTyp, accessServiceTestData, true);
        doReturn(user)
                .when(webCertUserService).getUser();
        doReturn(false)
                .when(patientDetailsResolver).isAvliden(PERSONNUMMER);
        doReturn(SekretessStatus.FALSE)
                .when(patientDetailsResolver).getSekretessStatus(PERSONNUMMER);
        doReturn(false)
                .when(webCertUserService).userIsLoggedInOnEnhetOrUnderenhet(enhetsId);
        doReturn(AccessServiceTestToolkit.createEmptyPreviousForUtkast(intygsTyp))
                .when(utkastService).checkIfPersonHasExistingIntyg(PERSONNUMMER, user);
        doReturn(enhetsId).when(vardenhet).getEnhetsid();
        doReturn(vardgivare).when(vardenhet).getVardgivare();
        doReturn(vardgivarId).when(vardgivare).getVardgivarid();
        doReturn(true).when(webCertUserService).isAuthorizedForUnit(vardgivarId, enhetsId, true);
    }

    protected void setupMocksForOnRenewFalse() {
        final WebCertUser user = AccessServiceTestToolkit.createUser(intygsTyp, accessServiceTestData, false, false);
        doReturn(user)
                .when(webCertUserService).getUser();
        doReturn(false)
                .when(patientDetailsResolver).isAvliden(PERSONNUMMER);
        doReturn(SekretessStatus.FALSE)
                .when(patientDetailsResolver).getSekretessStatus(PERSONNUMMER);
        doReturn(true)
                .when(webCertUserService).userIsLoggedInOnEnhetOrUnderenhet(enhetsId);
        doReturn(AccessServiceTestToolkit.createEmptyPreviousForUtkast(intygsTyp))
                .when(utkastService).checkIfPersonHasExistingIntyg(PERSONNUMMER, user);
        doReturn(enhetsId).when(vardenhet).getEnhetsid();
        doReturn(vardgivare).when(vardenhet).getVardgivare();
        doReturn(vardgivarId).when(vardgivare).getVardgivarid();
        doReturn(true).when(webCertUserService).isAuthorizedForUnit(vardgivarId, enhetsId, true);
    }

    protected void setupMocksForOnRenewFalseDifferentUnit() {
        final WebCertUser user = AccessServiceTestToolkit.createUser(intygsTyp, accessServiceTestData, false, false);
        doReturn(user)
                .when(webCertUserService).getUser();
        doReturn(false)
                .when(patientDetailsResolver).isAvliden(PERSONNUMMER);
        doReturn(SekretessStatus.FALSE)
                .when(patientDetailsResolver).getSekretessStatus(PERSONNUMMER);
        doReturn(false)
                .when(webCertUserService).userIsLoggedInOnEnhetOrUnderenhet(enhetsId);
        doReturn(AccessServiceTestToolkit.createEmptyPreviousForUtkast(intygsTyp))
                .when(utkastService).checkIfPersonHasExistingIntyg(PERSONNUMMER, user);
        doReturn(enhetsId).when(vardenhet).getEnhetsid();
        doReturn(vardgivare).when(vardenhet).getVardgivare();
        doReturn(vardgivarId).when(vardgivare).getVardgivarid();
        doReturn(true).when(webCertUserService).isAuthorizedForUnit(vardgivarId, enhetsId, true);
    }

    protected void setupMocksForOnSekretessPatient() {
        final WebCertUser user = AccessServiceTestToolkit.createUser(intygsTyp, accessServiceTestData);
        doReturn(user)
                .when(webCertUserService).getUser();
        doReturn(false)
                .when(patientDetailsResolver).isAvliden(PERSONNUMMER);
        doReturn(SekretessStatus.TRUE)
                .when(patientDetailsResolver).getSekretessStatus(PERSONNUMMER);
        doReturn(true)
                .when(webCertUserService).userIsLoggedInOnEnhetOrUnderenhet(enhetsId);
        doReturn(AccessServiceTestToolkit.createEmptyPreviousForUtkast(intygsTyp))
                .when(utkastService).checkIfPersonHasExistingIntyg(PERSONNUMMER, user);
        doReturn(enhetsId).when(vardenhet).getEnhetsid();
        doReturn(vardgivare).when(vardenhet).getVardgivare();
        doReturn(vardgivarId).when(vardgivare).getVardgivarid();
        doReturn(true).when(webCertUserService).isAuthorizedForUnit(vardgivarId, enhetsId, true);
    }

    protected void setupMocksForOnSekretessPatientDifferentUnit() {
        final WebCertUser user = AccessServiceTestToolkit.createUser(intygsTyp, accessServiceTestData);
        doReturn(user)
                .when(webCertUserService).getUser();
        doReturn(false)
                .when(patientDetailsResolver).isAvliden(PERSONNUMMER);
        doReturn(SekretessStatus.TRUE)
                .when(patientDetailsResolver).getSekretessStatus(PERSONNUMMER);
        doReturn(false)
                .when(webCertUserService).userIsLoggedInOnEnhetOrUnderenhet(enhetsId);
        doReturn(AccessServiceTestToolkit.createEmptyPreviousForUtkast(intygsTyp))
                .when(utkastService).checkIfPersonHasExistingIntyg(PERSONNUMMER, user);
        doReturn(enhetsId).when(vardenhet).getEnhetsid();
        doReturn(vardgivare).when(vardenhet).getVardgivare();
        doReturn(vardgivarId).when(vardgivare).getVardgivarid();
        doReturn(true).when(webCertUserService).isAuthorizedForUnit(vardgivarId, enhetsId, true);
    }
}
