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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static se.inera.intyg.webcert.web.service.utkast.UtkastServiceImpl.INTYG_INDICATOR;
import static se.inera.intyg.webcert.web.service.utkast.UtkastServiceImpl.UTKAST_INDICATOR;

import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.PreviousIntyg;

@ExtendWith(MockitoExtension.class)
class CertificateTypeMessageServiceImplTest {

    private static final String CERTIFICATE_TYPE = "db";
    private static final Personnummer PERSON_ID = Personnummer.createPersonnummer("19121212-1212").get();

    @Mock
    private WebCertUserService webCertUserService;

    @Mock
    private UtkastService utkastService;

    @InjectMocks
    private CertificateTypeMessageServiceImpl certificateTypeMessageService;

    private WebCertUser mockedUser;

    @BeforeEach
    void setUp() {
        mockedUser = mock(WebCertUser.class);
        doReturn(mockedUser)
            .when(webCertUserService).getUser();
    }

    @Test
    void shallReturnMessageForExistingDraftWithinSameCareProviderSameCareUnit() {
        final var expectedMessage = "Det finns ett utkast på dödsbevis för detta personnummer. Du kan inte skapa ett nytt utkast men kan däremot välja att fortsätta med det befintliga utkastet.";

        doReturn(Map.of(UTKAST_INDICATOR, Map.of(CERTIFICATE_TYPE, PreviousIntyg.of(true, true, false, "", "123", LocalDateTime.now()))))
            .when(utkastService).checkIfPersonHasExistingIntyg(PERSON_ID, mockedUser, null);

        final var actualMessage = certificateTypeMessageService.get(CERTIFICATE_TYPE, PERSON_ID);

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void shallReturnMessageForExistingDraftWithinSameCareProviderButDifferentCareUnit() {
        final var expectedMessage = "Det finns ett utkast på dödsbevis för detta personnummer på annan vårdenhet. Du kan inte skapa ett nytt utkast men kan däremot välja att fortsätta med det befintliga utkastet.";

        doReturn(Map.of(UTKAST_INDICATOR, Map.of(CERTIFICATE_TYPE, PreviousIntyg.of(true, false, false, "", "123", LocalDateTime.now()))))
            .when(utkastService).checkIfPersonHasExistingIntyg(PERSON_ID, mockedUser, null);

        final var actualMessage = certificateTypeMessageService.get(CERTIFICATE_TYPE, PERSON_ID);

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void shallReturnMessageForExistingDraftDifferentCareProvider() {
        final var expectedMessage = "Det finns ett utkast på dödsbevis för detta personnummer hos annan vårdgivare. Senast skapade dödsbevis är det som gäller. Om du fortsätter och lämnar in dödsbeviset så blir det därför detta dödsbevis som gäller.";

        doReturn(Map.of(UTKAST_INDICATOR, Map.of(CERTIFICATE_TYPE, PreviousIntyg.of(false, false, false, "", "123", LocalDateTime.now()))))
            .when(utkastService).checkIfPersonHasExistingIntyg(PERSON_ID, mockedUser, null);

        final var actualMessage = certificateTypeMessageService.get(CERTIFICATE_TYPE, PERSON_ID);

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void shallReturnMessageForExistingCertificateWithinSameCareProviderSameCareUnit() {
        final var expectedMessage = "Det finns ett signerat dödsbevis för detta personnummer. Du kan inte skapa ett nytt dödsbevis men kan däremot välja att ersätta det befintliga dödsbeviset.";

        doReturn(Map.of(INTYG_INDICATOR, Map.of(CERTIFICATE_TYPE, PreviousIntyg.of(true, true, false, "", "123", LocalDateTime.now()))))
            .when(utkastService).checkIfPersonHasExistingIntyg(PERSON_ID, mockedUser, null);

        final var actualMessage = certificateTypeMessageService.get(CERTIFICATE_TYPE, PERSON_ID);

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void shallReturnMessageForExistingCertificateWithinSameCareProviderDifferentCareUnit() {
        final var expectedMessage = "Det finns ett signerat dödsbevis för detta personnummer på annan vårdenhet. Du kan inte skapa ett nytt dödsbevis men kan däremot välja att ersätta det befintliga dödsbeviset.";

        doReturn(Map.of(INTYG_INDICATOR, Map.of(CERTIFICATE_TYPE, PreviousIntyg.of(true, false, false, "", "123", LocalDateTime.now()))))
            .when(utkastService).checkIfPersonHasExistingIntyg(PERSON_ID, mockedUser, null);

        final var actualMessage = certificateTypeMessageService.get(CERTIFICATE_TYPE, PERSON_ID);

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void shallReturnMessageForExistingCertificateDifferentCareProvider() {
        final var expectedMessage = "Det finns ett signerat dödsbevis för detta personnummer hos annan vårdgivare. Det är inte möjligt att skapa ett nytt dödsbevis.";

        doReturn(Map.of(INTYG_INDICATOR, Map.of(CERTIFICATE_TYPE, PreviousIntyg.of(false, false, false, "", "123", LocalDateTime.now()))))
            .when(utkastService).checkIfPersonHasExistingIntyg(PERSON_ID, mockedUser, null);

        final var actualMessage = certificateTypeMessageService.get(CERTIFICATE_TYPE, PERSON_ID);

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void shallReturnCertificateMessageWhenBothCertificateAndDraftMessageIsPresent() {
        final var expectedMessage = "Det finns ett signerat dödsbevis för detta personnummer. Du kan inte skapa ett nytt dödsbevis men kan däremot välja att ersätta det befintliga dödsbeviset.";

        doReturn(Map.of(INTYG_INDICATOR, Map.of(CERTIFICATE_TYPE, PreviousIntyg.of(true, true, false, "", "123", LocalDateTime.now())),
            UTKAST_INDICATOR, Map.of(CERTIFICATE_TYPE, PreviousIntyg.of(true, true, false, "", "123", LocalDateTime.now()))))
            .when(utkastService).checkIfPersonHasExistingIntyg(PERSON_ID, mockedUser, null);

        final var actualMessage = certificateTypeMessageService.get(CERTIFICATE_TYPE, PERSON_ID);

        assertEquals(expectedMessage, actualMessage);
    }
}