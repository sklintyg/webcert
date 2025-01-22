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
package se.inera.intyg.webcert.web.service.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.metadata.Unit;
import se.inera.intyg.common.support.facade.model.user.User;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Mottagning;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.infra.security.authorities.CommonAuthoritiesResolver;
import se.inera.intyg.infra.security.common.model.Feature;
import se.inera.intyg.webcert.web.service.facade.user.UserService;
import se.inera.intyg.webcert.web.service.underskrift.dss.DssSignatureService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@ExtendWith(MockitoExtension.class)
class ChangeUnitImplTest {

    @Mock
    private WebCertUserService webCertUserService;
    @Mock
    private UserService userService;
    @Mock
    private DssSignatureService dssSignatureService;
    @Mock
    private CommonAuthoritiesResolver commonAuthoritiesResolver;

    @InjectMocks
    private ChangeUnitServiceImpl changeUnitService;

    private WebCertUser webcertUser;

    private static final String UNIT_ID = "UNIT_ID";
    private static final String CARE_PROVIDER_ID = "CARE_PROVIDER_ID";
    private static final String NEW_UNIT_ID = "NEW_UNIT_ID";
    public static final String TEST_FEATURE = "testFeature";

    void setUpUserForSuccessfulChange() {
        webcertUser = createWebCertUser();

        doReturn(getFeatures())
            .when(commonAuthoritiesResolver)
            .getFeatures(any());

        doReturn(webcertUser)
            .when(webCertUserService)
            .getUser();

        doReturn(getUser())
            .when(userService)
            .getLoggedInUser();

        doReturn(true)
            .when(dssSignatureService)
            .shouldUseSigningService(NEW_UNIT_ID);
    }

    void setUpUserForFailedChange() {
        webcertUser = createWebCertUser();
        webcertUser.setVardgivare(null);

        doReturn(webcertUser)
            .when(webCertUserService)
            .getUser();
    }

    @Nested
    class ChangeUnit {

        @Test
        void shouldUpdateUnitForUser() throws ChangeUnitException {
            setUpUserForSuccessfulChange();

            final var user = changeUnitService.change(NEW_UNIT_ID);

            assertEquals(user.getLoggedInCareUnit().getUnitId(), webcertUser.getValdVardenhet().getId());
        }

        @Test
        void shouldUpdateSigningService() throws ChangeUnitException {
            setUpUserForSuccessfulChange();

            changeUnitService.change(NEW_UNIT_ID);

            verify(dssSignatureService, times(1)).shouldUseSigningService(NEW_UNIT_ID);
            assertTrue(webcertUser.isUseSigningService());
        }

        @Test
        void shouldSetFeaturesForUser() throws ChangeUnitException {
            setUpUserForSuccessfulChange();

            changeUnitService.change(NEW_UNIT_ID);

            verify(commonAuthoritiesResolver, times(1)).getFeatures(any());
            assertEquals(TEST_FEATURE, webcertUser.getFeatures().get(TEST_FEATURE).getName());
        }

        @Test
        void shouldThrowExceptionIfChangeFails() {
            setUpUserForFailedChange();

            final var exception = assertThrows(ChangeUnitException.class, () -> changeUnitService.change(NEW_UNIT_ID));

            assertEquals(ChangeUnitException.class, exception.getClass());
        }
    }

    private Vardenhet getUnit(String unitId) {
        final var unit = new Vardenhet();
        unit.setId(unitId);
        unit.setMottagningar(List.of(new Mottagning()));
        return unit;
    }

    private Vardgivare getCareProvider() {
        final var carerovider = new Vardgivare();
        carerovider.setId(CARE_PROVIDER_ID);
        carerovider.setVardenheter(List.of(getUnit(UNIT_ID), getUnit(NEW_UNIT_ID)));
        return carerovider;
    }

    private WebCertUser createWebCertUser() {
        final var user = new WebCertUser();
        user.setValdVardenhet(getUnit(UNIT_ID));
        user.setValdVardgivare(getCareProvider());
        user.setVardgivare(List.of(getCareProvider()));
        return user;
    }

    private User getUser() {
        final var unit = Unit.builder()
            .unitId(NEW_UNIT_ID).build();
        return User.builder()
            .loggedInCareUnit(unit).build();
    }

    private Map<String, Feature> getFeatures() {
        final var features = new HashMap<String, Feature>();
        final var feature = new Feature();
        feature.setName(TEST_FEATURE);
        features.put(TEST_FEATURE, feature);
        return features;
    }
}
