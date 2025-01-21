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

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.List;
import org.mockito.Mock;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.infra.integration.hsatk.model.legacy.SelectableVardenhet;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.web.service.access.data.AccessServiceTestData;
import se.inera.intyg.webcert.web.service.access.util.AccessServiceTestToolkit;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.subscription.dto.SubscriptionAction;
import se.inera.intyg.webcert.web.service.subscription.dto.SubscriptionInfo;
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

    @Mock
    protected IntygTextsService intygTextsService;

    protected final String intygsTyp;
    protected final String intygsTypVersion;
    protected final AccessServiceTestData accessServiceTestData;

    public static final Vardenhet vardenhet = mock(Vardenhet.class);
    public static final Vardgivare vardgivare = mock(Vardgivare.class);
    public static final SelectableVardenhet selectedVardgivare = mock(SelectableVardenhet.class);
    public static final SelectableVardenhet selectedVardenhet = mock(SelectableVardenhet.class);
    public static final String enhetsId = "EnhetsId";
    public static final boolean isEmployer = false;
    public static final String vardgivarId = "VardgivarId";
    public static final String PERSONNUMMER_TOLVAN = "191212121212";
    public static final Personnummer PERSONNUMMER = Personnummer.createPersonnummer(PERSONNUMMER_TOLVAN).get();
    public static final SubscriptionInfo SUBSCRIPTION_INFO = new SubscriptionInfo();

    public AccessTest(String intygsTyp, AccessServiceTestData accessServiceTestData) {
        this.intygsTyp = intygsTyp;
        this.accessServiceTestData = accessServiceTestData;
        this.intygsTypVersion = "1.0";
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
            .when(webCertUserService).isUserAllowedAccessToUnit(enhetsId);
        doReturn(AccessServiceTestToolkit.createEmptyPreviousForUtkast(intygsTyp))
            .when(utkastService).checkIfPersonHasExistingIntyg(PERSONNUMMER, user, null);
        doReturn(selectedVardgivare).when(user).getValdVardgivare();
        doReturn(vardgivarId).when(selectedVardgivare).getId();
        doReturn(selectedVardenhet).when(user).getValdVardenhet();
        doReturn(enhetsId).when(selectedVardenhet).getId();
        doReturn(enhetsId).when(vardenhet).getEnhetsid();
        doReturn(vardgivare).when(vardenhet).getVardgivare();
        doReturn(vardgivarId).when(vardgivare).getVardgivarid();
        doReturn(true).when(webCertUserService).isAuthorizedForUnit(vardgivarId, enhetsId, true);
        doReturn(true).when(webCertUserService).isAuthorizedForUnit(vardgivarId, enhetsId, false);
        doReturn(true).when(intygTextsService).isLatestMajorVersion(intygsTyp, intygsTypVersion);
        doReturn(SUBSCRIPTION_INFO).when(user).getSubscriptionInfo();
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
            .when(webCertUserService).isUserAllowedAccessToUnit(enhetsId);
        doReturn(AccessServiceTestToolkit.createEmptyPreviousForUtkast(intygsTyp))
            .when(utkastService).checkIfPersonHasExistingIntyg(PERSONNUMMER, user, null);
        doReturn(selectedVardgivare).when(user).getValdVardgivare();
        doReturn(vardgivarId).when(selectedVardgivare).getId();
        doReturn(selectedVardenhet).when(user).getValdVardenhet();
        doReturn(enhetsId).when(selectedVardenhet).getId();
        doReturn(enhetsId).when(vardenhet).getEnhetsid();
        doReturn(vardgivare).when(vardenhet).getVardgivare();
        doReturn(vardgivarId).when(vardgivare).getVardgivarid();
        doReturn(true).when(webCertUserService).isAuthorizedForUnit(vardgivarId, enhetsId, true);
        doReturn(false).when(webCertUserService).isAuthorizedForUnit(vardgivarId, enhetsId, false);
        doReturn(true).when(intygTextsService).isLatestMajorVersion(intygsTyp, intygsTypVersion);
        doReturn(SUBSCRIPTION_INFO).when(user).getSubscriptionInfo();
    }

    protected void setupMocksForNotLatestMajorVersion() {
        final WebCertUser user = AccessServiceTestToolkit.createUser(intygsTyp, accessServiceTestData);
        doReturn(user)
            .when(webCertUserService).getUser();
        doReturn(false)
            .when(patientDetailsResolver).isAvliden(PERSONNUMMER);
        doReturn(SekretessStatus.FALSE)
            .when(patientDetailsResolver).getSekretessStatus(PERSONNUMMER);
        doReturn(true)
            .when(webCertUserService).isUserAllowedAccessToUnit(enhetsId);
        doReturn(AccessServiceTestToolkit.createEmptyPreviousForUtkast(intygsTyp))
            .when(utkastService).checkIfPersonHasExistingIntyg(PERSONNUMMER, user, null);
        doReturn(selectedVardgivare).when(user).getValdVardgivare();
        doReturn(vardgivarId).when(selectedVardgivare).getId();
        doReturn(selectedVardenhet).when(user).getValdVardenhet();
        doReturn(enhetsId).when(selectedVardenhet).getId();
        doReturn(enhetsId).when(vardenhet).getEnhetsid();
        doReturn(vardgivare).when(vardenhet).getVardgivare();
        doReturn(vardgivarId).when(vardgivare).getVardgivarid();
        doReturn(true).when(webCertUserService).isAuthorizedForUnit(vardgivarId, enhetsId, true);
        doReturn(true).when(webCertUserService).isAuthorizedForUnit(vardgivarId, enhetsId, false);
        doReturn(false).when(intygTextsService).isLatestMajorVersion(intygsTyp, intygsTypVersion);
        doReturn(SUBSCRIPTION_INFO).when(user).getSubscriptionInfo();
    }

    protected void setupMocksForNotLatestMajorVersionDifferentUnit() {
        final WebCertUser user = AccessServiceTestToolkit.createUserDifferentUnit(intygsTyp, accessServiceTestData);
        doReturn(user)
            .when(webCertUserService).getUser();
        doReturn(false)
            .when(patientDetailsResolver).isAvliden(PERSONNUMMER);
        doReturn(SekretessStatus.FALSE)
            .when(patientDetailsResolver).getSekretessStatus(PERSONNUMMER);
        doReturn(false)
            .when(webCertUserService).isUserAllowedAccessToUnit(enhetsId);
        doReturn(AccessServiceTestToolkit.createEmptyPreviousForUtkast(intygsTyp))
            .when(utkastService).checkIfPersonHasExistingIntyg(PERSONNUMMER, user, null);
        doReturn(selectedVardgivare).when(user).getValdVardgivare();
        doReturn(vardgivarId).when(selectedVardgivare).getId();
        doReturn(selectedVardenhet).when(user).getValdVardenhet();
        doReturn(enhetsId).when(selectedVardenhet).getId();
        doReturn(enhetsId).when(vardenhet).getEnhetsid();
        doReturn(vardgivare).when(vardenhet).getVardgivare();
        doReturn(vardgivarId).when(vardgivare).getVardgivarid();
        doReturn(true).when(webCertUserService).isAuthorizedForUnit(vardgivarId, enhetsId, true);
        doReturn(false).when(webCertUserService).isAuthorizedForUnit(vardgivarId, enhetsId, false);
        doReturn(true).when(intygTextsService).isLatestMajorVersion(intygsTyp, intygsTypVersion);
        doReturn(false).when(intygTextsService).isLatestMajorVersion(intygsTyp, intygsTypVersion);
        doReturn(SUBSCRIPTION_INFO).when(user).getSubscriptionInfo();
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
            .when(webCertUserService).isUserAllowedAccessToUnit(enhetsId);
        doReturn(AccessServiceTestToolkit.createEmptyPreviousForUtkast(intygsTyp))
            .when(utkastService).checkIfPersonHasExistingIntyg(PERSONNUMMER, user, null);
        doReturn(selectedVardgivare).when(user).getValdVardgivare();
        doReturn(vardgivarId).when(selectedVardgivare).getId();
        doReturn(selectedVardenhet).when(user).getValdVardenhet();
        doReturn(enhetsId).when(selectedVardenhet).getId();
        doReturn(enhetsId).when(vardenhet).getEnhetsid();
        doReturn(vardgivare).when(vardenhet).getVardgivare();
        doReturn(vardgivarId).when(vardgivare).getVardgivarid();
        doReturn(true).when(webCertUserService).isAuthorizedForUnit(vardgivarId, enhetsId, true);
        doReturn(true).when(webCertUserService).isAuthorizedForUnit(vardgivarId, enhetsId, false);
        doReturn(true).when(intygTextsService).isLatestMajorVersion(intygsTyp, intygsTypVersion);
        doReturn(SUBSCRIPTION_INFO).when(user).getSubscriptionInfo();
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
            .when(webCertUserService).isUserAllowedAccessToUnit(enhetsId);
        doReturn(AccessServiceTestToolkit.createEmptyPreviousForUtkast(intygsTyp))
            .when(utkastService).checkIfPersonHasExistingIntyg(PERSONNUMMER, user, null);
        doReturn(selectedVardgivare).when(user).getValdVardgivare();
        doReturn(vardgivarId).when(selectedVardgivare).getId();
        doReturn(selectedVardenhet).when(user).getValdVardenhet();
        doReturn(enhetsId).when(selectedVardenhet).getId();
        doReturn(enhetsId).when(vardenhet).getEnhetsid();
        doReturn(vardgivare).when(vardenhet).getVardgivare();
        doReturn(vardgivarId).when(vardgivare).getVardgivarid();
        doReturn(true).when(webCertUserService).isAuthorizedForUnit(vardgivarId, enhetsId, true);
        doReturn(false).when(webCertUserService).isAuthorizedForUnit(vardgivarId, enhetsId, false);
        doReturn(true).when(intygTextsService).isLatestMajorVersion(intygsTyp, intygsTypVersion);
        doReturn(SUBSCRIPTION_INFO).when(user).getSubscriptionInfo();
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
            .when(webCertUserService).isUserAllowedAccessToUnit(enhetsId);
        doReturn(AccessServiceTestToolkit.createEmptyPreviousForUtkast(intygsTyp))
            .when(utkastService).checkIfPersonHasExistingIntyg(PERSONNUMMER, user, null);
        doReturn(selectedVardgivare).when(user).getValdVardgivare();
        doReturn(vardgivarId).when(selectedVardgivare).getId();
        doReturn(selectedVardenhet).when(user).getValdVardenhet();
        doReturn(enhetsId).when(selectedVardenhet).getId();
        doReturn(enhetsId).when(vardenhet).getEnhetsid();
        doReturn(vardgivare).when(vardenhet).getVardgivare();
        doReturn(vardgivarId).when(vardgivare).getVardgivarid();
        doReturn(true).when(webCertUserService).isAuthorizedForUnit(vardgivarId, enhetsId, true);
        doReturn(true).when(webCertUserService).isAuthorizedForUnit(vardgivarId, enhetsId, false);
        doReturn(true).when(intygTextsService).isLatestMajorVersion(intygsTyp, intygsTypVersion);
        doReturn(SUBSCRIPTION_INFO).when(user).getSubscriptionInfo();
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
            .when(webCertUserService).isUserAllowedAccessToUnit(enhetsId);
        doReturn(AccessServiceTestToolkit.createEmptyPreviousForUtkast(intygsTyp))
            .when(utkastService).checkIfPersonHasExistingIntyg(PERSONNUMMER, user, null);
        doReturn(selectedVardgivare).when(user).getValdVardgivare();
        doReturn(vardgivarId).when(selectedVardgivare).getId();
        doReturn(selectedVardenhet).when(user).getValdVardenhet();
        doReturn(enhetsId).when(selectedVardenhet).getId();
        doReturn(enhetsId).when(vardenhet).getEnhetsid();
        doReturn(vardgivare).when(vardenhet).getVardgivare();
        doReturn(vardgivarId).when(vardgivare).getVardgivarid();
        doReturn(true).when(webCertUserService).isAuthorizedForUnit(vardgivarId, enhetsId, true);
        doReturn(false).when(webCertUserService).isAuthorizedForUnit(vardgivarId, enhetsId, false);
        doReturn(true).when(intygTextsService).isLatestMajorVersion(intygsTyp, intygsTypVersion);
        doReturn(SUBSCRIPTION_INFO).when(user).getSubscriptionInfo();
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
            .when(webCertUserService).isUserAllowedAccessToUnit(enhetsId);
        doReturn(AccessServiceTestToolkit.createEmptyPreviousForUtkast(intygsTyp))
            .when(utkastService).checkIfPersonHasExistingIntyg(PERSONNUMMER, user, null);
        doReturn(selectedVardgivare).when(user).getValdVardgivare();
        doReturn(vardgivarId).when(selectedVardgivare).getId();
        doReturn(selectedVardenhet).when(user).getValdVardenhet();
        doReturn(enhetsId).when(selectedVardenhet).getId();
        doReturn(enhetsId).when(vardenhet).getEnhetsid();
        doReturn(vardgivare).when(vardenhet).getVardgivare();
        doReturn(vardgivarId).when(vardgivare).getVardgivarid();
        doReturn(true).when(webCertUserService).isAuthorizedForUnit(vardgivarId, enhetsId, true);
        doReturn(true).when(webCertUserService).isAuthorizedForUnit(vardgivarId, enhetsId, false);
        doReturn(true).when(intygTextsService).isLatestMajorVersion(intygsTyp, intygsTypVersion);
        doReturn(SUBSCRIPTION_INFO).when(user).getSubscriptionInfo();
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
            .when(webCertUserService).isUserAllowedAccessToUnit(enhetsId);
        doReturn(AccessServiceTestToolkit.createEmptyPreviousForUtkast(intygsTyp))
            .when(utkastService).checkIfPersonHasExistingIntyg(PERSONNUMMER, user, null);
        doReturn(selectedVardgivare).when(user).getValdVardgivare();
        doReturn(vardgivarId).when(selectedVardgivare).getId();
        doReturn(selectedVardenhet).when(user).getValdVardenhet();
        doReturn(enhetsId).when(selectedVardenhet).getId();
        doReturn(enhetsId).when(vardenhet).getEnhetsid();
        doReturn(vardgivare).when(vardenhet).getVardgivare();
        doReturn(vardgivarId).when(vardgivare).getVardgivarid();
        doReturn(true).when(webCertUserService).isAuthorizedForUnit(vardgivarId, enhetsId, true);
        doReturn(false).when(webCertUserService).isAuthorizedForUnit(vardgivarId, enhetsId, false);
        doReturn(true).when(intygTextsService).isLatestMajorVersion(intygsTyp, intygsTypVersion);
        doReturn(SUBSCRIPTION_INFO).when(user).getSubscriptionInfo();
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
            .when(webCertUserService).isUserAllowedAccessToUnit(enhetsId);
        doReturn(AccessServiceTestToolkit.createEmptyPreviousForUtkast(intygsTyp))
            .when(utkastService).checkIfPersonHasExistingIntyg(PERSONNUMMER, user, null);
        doReturn(selectedVardgivare).when(user).getValdVardgivare();
        doReturn(vardgivarId).when(selectedVardgivare).getId();
        doReturn(selectedVardenhet).when(user).getValdVardenhet();
        doReturn(enhetsId).when(selectedVardenhet).getId();
        doReturn(enhetsId).when(vardenhet).getEnhetsid();
        doReturn(vardgivare).when(vardenhet).getVardgivare();
        doReturn(vardgivarId).when(vardgivare).getVardgivarid();
        doReturn(true).when(webCertUserService).isAuthorizedForUnit(vardgivarId, enhetsId, true);
        doReturn(true).when(webCertUserService).isAuthorizedForUnit(vardgivarId, enhetsId, false);
        doReturn(true).when(intygTextsService).isLatestMajorVersion(intygsTyp, intygsTypVersion);
        doReturn(SUBSCRIPTION_INFO).when(user).getSubscriptionInfo();
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
            .when(webCertUserService).isUserAllowedAccessToUnit(enhetsId);
        doReturn(AccessServiceTestToolkit.createEmptyPreviousForUtkast(intygsTyp))
            .when(utkastService).checkIfPersonHasExistingIntyg(PERSONNUMMER, user, null);
        doReturn(selectedVardgivare).when(user).getValdVardgivare();
        doReturn(vardgivarId).when(selectedVardgivare).getId();
        doReturn(selectedVardenhet).when(user).getValdVardenhet();
        doReturn(enhetsId).when(selectedVardenhet).getId();
        doReturn(enhetsId).when(vardenhet).getEnhetsid();
        doReturn(vardgivare).when(vardenhet).getVardgivare();
        doReturn(vardgivarId).when(vardgivare).getVardgivarid();
        doReturn(true).when(webCertUserService).isAuthorizedForUnit(vardgivarId, enhetsId, true);
        doReturn(false).when(webCertUserService).isAuthorizedForUnit(vardgivarId, enhetsId, false);
        doReturn(true).when(intygTextsService).isLatestMajorVersion(intygsTyp, intygsTypVersion);
        doReturn(SUBSCRIPTION_INFO).when(user).getSubscriptionInfo();
    }

    protected void setupMocksForMissingSubscription() {
        final WebCertUser user = AccessServiceTestToolkit.createUser(intygsTyp, accessServiceTestData);
        final var subscriptionInfo = new SubscriptionInfo();
        subscriptionInfo.setSubscriptionAction(SubscriptionAction.BLOCK);
        subscriptionInfo.setCareProvidersMissingSubscription(List.of(vardgivarId));
        doReturn(user)
            .when(webCertUserService).getUser();
        doReturn(false)
            .when(patientDetailsResolver).isAvliden(PERSONNUMMER);
        doReturn(SekretessStatus.FALSE)
            .when(patientDetailsResolver).getSekretessStatus(PERSONNUMMER);
        doReturn(true)
            .when(webCertUserService).isUserAllowedAccessToUnit(enhetsId);
        doReturn(AccessServiceTestToolkit.createEmptyPreviousForUtkast(intygsTyp))
            .when(utkastService).checkIfPersonHasExistingIntyg(PERSONNUMMER, user, null);
        doReturn(selectedVardgivare).when(user).getValdVardgivare();
        doReturn(vardgivarId).when(selectedVardgivare).getId();
        doReturn(selectedVardenhet).when(user).getValdVardenhet();
        doReturn(enhetsId).when(selectedVardenhet).getId();
        doReturn(enhetsId).when(vardenhet).getEnhetsid();
        doReturn(vardgivare).when(vardenhet).getVardgivare();
        doReturn(vardgivarId).when(vardgivare).getVardgivarid();
        doReturn(true).when(webCertUserService).isAuthorizedForUnit(vardgivarId, enhetsId, true);
        doReturn(true).when(webCertUserService).isAuthorizedForUnit(vardgivarId, enhetsId, false);
        doReturn(true).when(intygTextsService).isLatestMajorVersion(intygsTyp, intygsTypVersion);
        doReturn(subscriptionInfo).when(user).getSubscriptionInfo();
    }
}
