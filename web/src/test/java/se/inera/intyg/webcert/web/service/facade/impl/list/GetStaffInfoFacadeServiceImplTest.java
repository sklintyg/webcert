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
package se.inera.intyg.webcert.web.service.facade.impl.list;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.List;
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
import se.inera.intyg.webcert.web.csintegration.aggregate.ListCertificatesInfoAggregator;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.dto.Lakare;
import se.inera.intyg.webcert.web.service.facade.list.config.GetStaffInfoFacadeServiceImpl;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GetStaffInfoFacadeServiceImplTest {

    @Mock
    private WebCertUserService webCertUserService;

    @Mock
    private UtkastService utkastService;

    @Mock
    private ArendeService arendeService;
    @Mock
    private ListCertificatesInfoAggregator listCertificatesInfoAggregator;
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
        public void shouldAddUserToListIfItDoesNotExistsAndIsDoctorOrDentist() {
            doReturn(new ArrayList<Lakare>()).when(utkastService).getLakareWithDraftsByEnhet(any());
            doReturn(true).when(user).isLakare();

            final var result = getStaffInfoFacadeService.get();

            assertEquals(1, result.size());
            assertEquals("HSA_ID", result.get(0).getHsaId());
        }

        @Test
        public void shouldNotAddUserToListIfNotDoctorOrDentist() {
            doReturn(new ArrayList<Lakare>()).when(utkastService).getLakareWithDraftsByEnhet(any());
            doReturn(false).when(user).isLakare();

            final var result = getStaffInfoFacadeService.get();

            assertEquals(0, result.size());
        }

        @Test
        public void shouldNotAddUserToListIfItExistsAndIsDoctorOrDentist() {
            doReturn(true).when(user).isLakare();
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

    @Nested
    class TestGetStaffInfoForQuestions {

        private final String UNIT_ID = "UNIT_ID";
        private final String LOGGED_IN_STAFF_ID = "HSA_ID";
        private final String STAFF_ID = "STAFF_ID";
        private final String STAFF_NAME = "STAFF_NAME";

        @BeforeEach
        void beforeEach() {
            doReturn(LOGGED_IN_STAFF_ID)
                .when(user)
                .getHsaId();
        }

        void setup(String id, String name) {
            final var list = List.of(new Lakare(id, name));
            doReturn(new ArrayList<>(list))
                .when(arendeService)
                .listSignedByForUnits(any());
        }

        @Test
        public void shouldConvertLakareName() {
            setup(LOGGED_IN_STAFF_ID, STAFF_NAME);

            final var result = getStaffInfoFacadeService.get(UNIT_ID);

            assertEquals(STAFF_NAME, result.get(0).getName());
        }

        @Test
        public void shouldConvertLakareHsaId() {
            setup(LOGGED_IN_STAFF_ID, STAFF_NAME);

            final var result = getStaffInfoFacadeService.get(UNIT_ID);

            assertEquals(LOGGED_IN_STAFF_ID, result.get(0).getHsaId());
        }

        @Test
        public void shouldNotAddUserToListIfItExistsAndIsDoctorOrDentist() {
            doReturn(true).when(user).isLakare();
            setup(LOGGED_IN_STAFF_ID, STAFF_NAME);

            final var result = getStaffInfoFacadeService.get(UNIT_ID);

            assertEquals(1, result.size());
            assertEquals(result.get(0).getHsaId(), LOGGED_IN_STAFF_ID);
        }

        @Test
        public void shouldAddUserToListIfItDoesNotExistsAndIsDoctor() {
            doReturn(true).when(user).isLakare();
            setup(STAFF_ID, STAFF_NAME);

            final var result = getStaffInfoFacadeService.get(UNIT_ID);

            assertEquals(2, result.size());
            assertEquals(LOGGED_IN_STAFF_ID, result.get(1).getHsaId());
        }

        @Test
        public void shouldNotAddUserToListIfNotDoctorOrDentist() {
            doReturn(false).when(user).isLakare();
            setup(STAFF_ID, STAFF_NAME);

            final var result = getStaffInfoFacadeService.get(UNIT_ID);

            assertEquals(1, result.size());
        }
    }

}
