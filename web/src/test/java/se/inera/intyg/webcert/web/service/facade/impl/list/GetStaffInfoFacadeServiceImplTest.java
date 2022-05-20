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
package se.inera.intyg.webcert.web.service.facade.impl.list;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import se.inera.intyg.infra.integration.hsatk.model.legacy.SelectableVardenhet;
import se.inera.intyg.webcert.web.service.dto.Lakare;
import se.inera.intyg.webcert.web.service.facade.list.config.GetStaffInfoFacadeServiceImpl;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GetStaffInfoFacadeServiceImplTest {

    @Mock
    private WebCertUserService webCertUserService;

    @Mock
    private UtkastService utkastService;

    @InjectMocks
    private GetStaffInfoFacadeServiceImpl getStaffInfoFacadeService;

    WebCertUser user = Mockito.mock(WebCertUser.class);
    SelectableVardenhet unit = Mockito.mock(SelectableVardenhet.class);

    @BeforeEach
    public void setup() {
        doReturn(user)
                .when(webCertUserService)
                .getUser();
    }

    @Nested
    class IsLoggedInUserDoctor {
        @Test
        public void shouldReturnTrueIfUserIsDoctor() {
            doReturn(true)
                    .when(user)
                    .isLakare();

            final var result = getStaffInfoFacadeService.isLoggedInUserDoctor();

            assertTrue(result);
        }

        @Test
        public void shouldReturnTrueIfUserIsPrivateDoctor() {
            doReturn(true)
                    .when(user)
                    .isPrivatLakare();

            final var result = getStaffInfoFacadeService.isLoggedInUserDoctor();

            assertTrue(result);
        }

        @Test
        public void shouldReturnFalseIfUserIsNotDoctor() {
            final var result = getStaffInfoFacadeService.isLoggedInUserDoctor();

            assertFalse(result);
        }
    }

    @Nested
    class GetLoggedInStaffHsaId {
        @Test
        public void shouldReturnHsaIdOfLoggedInStaff() {
            doReturn("HSA_ID")
                    .when(user)
                    .getHsaId();

            final var result = getStaffInfoFacadeService.getLoggedInStaffHsaId();

            assertEquals("HSA_ID", result);
        }
    }

    @Nested
    class GetStaffListInfo {
        @BeforeEach
        public void setup() {
            doReturn("UNIT_ID")
                    .when(unit)
                    .getId();

            doReturn(unit)
                    .when(user)
                    .getValdVardenhet();

            doReturn("HSA_ID")
                    .when(user)
                    .getHsaId();
        }

        @Test
        public void shouldAddUserToListIfItDoesNotExists() {
            doReturn(new ArrayList<Lakare>())
                    .when(utkastService)
                    .getLakareWithDraftsByEnhet(any());

            final var result = getStaffInfoFacadeService.get();

            assertEquals(1, result.size());
            assertEquals("HSA_ID", result.get(0).getHsaId());
        }

        @Test
        public void shouldNotAddUserToListIfItExists() {
            doReturn(List.of(new Lakare("HSA_ID", "NAME")))
                    .when(utkastService)
                    .getLakareWithDraftsByEnhet(any());

            final var result = getStaffInfoFacadeService.get();

            assertEquals(1, result.size());
        }

        @Test
        public void shouldConvertLakareHsaId() {
            doReturn(List.of(new Lakare("HSA_ID", "NAME")))
                    .when(utkastService)
                    .getLakareWithDraftsByEnhet(any());

            final var result = getStaffInfoFacadeService.get();

            assertEquals("HSA_ID", result.get(0).getHsaId());
        }

        @Test
        public void shouldConvertLakareName() {
            doReturn(List.of(new Lakare("HSA_ID", "NAME")))
                    .when(utkastService)
                    .getLakareWithDraftsByEnhet(any());

            final var result = getStaffInfoFacadeService.get();

            assertEquals("NAME", result.get(0).getName());
        }
    }

}