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
package se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static se.inera.intyg.webcert.web.service.utkast.UtkastServiceImpl.INTYG_INDICATOR;

import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.PreviousIntyg;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@ExtendWith(MockitoExtension.class)
class MissingRelatedCertificateConfirmationImplTest {

    @Mock
    private UtkastService utkastService;

    @Mock
    private WebCertUserService webCertUserService;

    @InjectMocks
    private MissingRelatedCertificateConfirmationImpl missingRelatedCertificateConfirmation;

    private WebCertUser webCertUser;

    private static final String PERSON_ID = "19121212-1212";
    private static final Personnummer PERSONNUMMER = Personnummer.createPersonnummer(PERSON_ID).get();

    @BeforeEach
    void setUp() {
        webCertUser = mock(WebCertUser.class);
    }

    @Test
    void shallNotReturnResourceLinkIfItsNotOfCorrectType() {
        final var actualResourcelink = missingRelatedCertificateConfirmation.get(DbModuleEntryPoint.MODULE_ID, PERSONNUMMER);
        assertTrue(actualResourcelink.isEmpty(), "Expect no resource link!");
    }

    @Test
    void shallReturnResourceLinkIfItsDoiAndNoDbExists() {
        final var actualResourcelink = missingRelatedCertificateConfirmation.get(DoiModuleEntryPoint.MODULE_ID, PERSONNUMMER);
        assertTrue(actualResourcelink.isPresent(), "Expect resource link!");
    }

    @Test
    void shallReturnResourceLinkIfItsDoiAndNoDbExistsWithCorrectType() {
        final var actualResourcelink = missingRelatedCertificateConfirmation.get(DoiModuleEntryPoint.MODULE_ID, PERSONNUMMER);
        assertEquals(ResourceLinkTypeDTO.MISSING_RELATED_CERTIFICATE_CONFIRMATION, actualResourcelink.get().getType());
    }

    @Test
    void shallReturnResourceLinkIfItsDoiAndNoDbExistsWithCorrectName() {
        final var actualResourcelink = missingRelatedCertificateConfirmation.get(DoiModuleEntryPoint.MODULE_ID, PERSONNUMMER);
        assertEquals("Dödsbevis saknas", actualResourcelink.get().getName());
    }

    @Test
    void shallReturnResourceLinkIfItsDoiAndNoDbExistsWithCorrectBody() {
        final var actualResourcelink = missingRelatedCertificateConfirmation.get(DoiModuleEntryPoint.MODULE_ID, PERSONNUMMER);
        assertEquals("Är du säker att du vill skapa ett dödsorsaksintyg? Det finns inget dödsbevis i nuläget inom vårdgivaren.\n"
            + "\n"
            + "Dödsorsaksintyget bör alltid skapas efter dödsbeviset.", actualResourcelink.get().getBody());
    }

    @Test
    void shallNotReturnResourceLinkIfItsDoiAndDbExistsWithinCareProvider() {
        final var dbWithinCareProvider = Map.of(
            INTYG_INDICATOR,
            Map.of(
                DbModuleEntryPoint.MODULE_ID,
                PreviousIntyg.of(true, false, false, "ENHET", "123", LocalDateTime.now())
            )
        );

        doReturn(webCertUser)
            .when(webCertUserService).getUser();

        doReturn(dbWithinCareProvider)
            .when(utkastService).checkIfPersonHasExistingIntyg(PERSONNUMMER, webCertUser, null);

        final var actualResourcelink = missingRelatedCertificateConfirmation.get(DoiModuleEntryPoint.MODULE_ID, PERSONNUMMER);
        assertTrue(actualResourcelink.isEmpty(), "Expect no resource link!");
    }

    @Test
    void shallReturnResourceLinkIfItsDoiAndDbExistsOutsideCareProvider() {
        final var dbWithinCareProvider = Map.of(
            INTYG_INDICATOR,
            Map.of(
                DbModuleEntryPoint.MODULE_ID,
                PreviousIntyg.of(false, false, false, "ENHET", "123", LocalDateTime.now())
            )
        );

        doReturn(webCertUser)
            .when(webCertUserService).getUser();

        doReturn(dbWithinCareProvider)
            .when(utkastService).checkIfPersonHasExistingIntyg(PERSONNUMMER, webCertUser, null);

        final var actualResourcelink = missingRelatedCertificateConfirmation.get(DoiModuleEntryPoint.MODULE_ID, PERSONNUMMER);
        assertTrue(actualResourcelink.isPresent(), "Expect resource link!");
    }
}
