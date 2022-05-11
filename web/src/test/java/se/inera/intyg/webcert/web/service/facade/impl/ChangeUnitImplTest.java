/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Mottagning;
import se.inera.intyg.infra.integration.hsatk.model.legacy.SelectableVardenhet;
import se.inera.intyg.infra.security.authorities.CommonAuthoritiesResolver;
import se.inera.intyg.webcert.web.service.facade.UserService;
import se.inera.intyg.webcert.web.service.underskrift.dss.DssSignatureService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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

    private WebCertUser user;

    private static final String UNIT_ID = "UNIT_ID";
    private static final String NEW_UNIT_ID = "NEW_UNIT_ID";

    void setUpUserWithSuccessfulChange() {
        user = mock(WebCertUser.class);

       doReturn(user)
            .when(webCertUserService)
            .getUser();

       doReturn(getUnit())
               .when(user)
               .getValdVardenhet();

       doReturn(true)
               .when(user)
               .changeValdVardenhet(anyString());

        /*doReturn(true)
                .when(dssSignatureService)
                .isUnitInIeWhitelist(anyString());*/
    }

    void setUpUserWithFailedChange() {
        user = mock(WebCertUser.class);

        doReturn(user)
                .when(webCertUserService)
                .getUser();

        doReturn(getUnit())
                .when(user)
                .getValdVardenhet();

        doReturn(false)
                .when(user)
                .changeValdVardenhet(anyString());
    }


    @Nested
    class ChangeUnit {
        @Test
        void shouldUpdateUnitForUser() throws ChangeUnitException {
            setUpUserWithSuccessfulChange();

            changeUnitService.change(NEW_UNIT_ID);

            verify(user).changeValdVardenhet(NEW_UNIT_ID);
        }

        @Test
        void shouldThrowExceptionIfChangeFails() {
            setUpUserWithFailedChange();

            final var exception = assertThrows(ChangeUnitException.class, () -> changeUnitService.change(NEW_UNIT_ID));

            assertEquals(ChangeUnitException.class, exception.getClass());        }
        }

    private SelectableVardenhet getUnit() {
        final var unit = new Mottagning();
        unit.setId(UNIT_ID);
        return unit;
    }
}
