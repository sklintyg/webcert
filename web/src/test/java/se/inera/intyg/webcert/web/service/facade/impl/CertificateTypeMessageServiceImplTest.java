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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static se.inera.intyg.webcert.web.service.utkast.UtkastServiceImpl.INTYG_INDICATOR;
import static se.inera.intyg.webcert.web.service.utkast.UtkastServiceImpl.UTKAST_INDICATOR;

import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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

    private static final String CERTIFICATE_TYPE_DB = "db";
    private static final String CERTIFICATE_TYPE_NOT_DB = "not db";
    private static final String CERTIFICATE_TYPE_DOI = "doi";
    private static final String CERTIFICATE_TYPE_NOT_DOI = "not doi";
    private static final Personnummer PERSON_ID = Personnummer.createPersonnummer("19121212-1212").get();
    @Mock
    private WebCertUserService webCertUserService;

    @Mock
    private UtkastService utkastService;

    @InjectMocks
    private CertificateTypeMessageServiceImpl certificateTypeMessageService;

    private WebCertUser mockedUser;

    @Nested
    class WithTypeDb {

        @BeforeEach
        void setUp() {
            mockedUser = mock(WebCertUser.class);
            doReturn(mockedUser)
                .when(webCertUserService).getUser();
        }

        @Test
        void shallReturnMessageForExistingDraftWithinSameCareProviderSameCareUnit() {
            final var expectedMessage = new CertificateMessage(CertificateMessageType.DRAFT_ON_SAME_CARE_UNIT,
                "Det finns ett utkast på dödsbevis för detta personnummer."
                    + " Du kan inte skapa ett nytt utkast men kan däremot välja att fortsätta med det befintliga utkastet."
            );

            doReturn(
                Map.of(UTKAST_INDICATOR,
                    Map.of(CERTIFICATE_TYPE_DB,
                        PreviousIntyg.of(true, true, false, "", "123", LocalDateTime.now()))))
                .when(utkastService).checkIfPersonHasExistingIntyg(PERSON_ID, mockedUser, null);

            final var actualMessage = certificateTypeMessageService.get(CERTIFICATE_TYPE_DB, PERSON_ID);

            assertEquals(expectedMessage, actualMessage.get());
        }

        @Test
        void shallReturnMessageForExistingDraftWithinSameCareProviderButDifferentCareUnit() {
            final var expectedMessage = new CertificateMessage(CertificateMessageType.DRAFT_ON_DIFFERENT_CARE_UNIT,
                "Det finns ett utkast på dödsbevis för detta personnummer på annan vårdenhet."
                    + " Du kan inte skapa ett nytt utkast men kan däremot välja att fortsätta med det befintliga utkastet."
            );

            doReturn(
                Map.of(UTKAST_INDICATOR,
                    Map.of(CERTIFICATE_TYPE_DB,
                        PreviousIntyg.of(true, false, false, "", "123", LocalDateTime.now()))))
                .when(utkastService).checkIfPersonHasExistingIntyg(PERSON_ID, mockedUser, null);

            final var actualMessage = certificateTypeMessageService.get(CERTIFICATE_TYPE_DB, PERSON_ID);

            assertEquals(expectedMessage, actualMessage.get());
        }

        @Test
        void shallReturnMessageForExistingDraftDifferentCareProvider() {
            final var expectedMessage = new CertificateMessage(CertificateMessageType.DRAFT_ON_DIFFERENT_CARE_PROVIDER,
                "Det finns ett utkast på dödsbevis för detta personnummer hos annan vårdgivare."
                    + " Senast skapade dödsbevis är det som gäller."
                    + " Om du fortsätter och lämnar in dödsbeviset så blir det därför detta dödsbevis som gäller."
            );

            doReturn(
                Map.of(UTKAST_INDICATOR,
                    Map.of(CERTIFICATE_TYPE_DB,
                        PreviousIntyg.of(false, false, false, "", "123", LocalDateTime.now()))))
                .when(utkastService).checkIfPersonHasExistingIntyg(PERSON_ID, mockedUser, null);

            final var actualMessage = certificateTypeMessageService.get(CERTIFICATE_TYPE_DB, PERSON_ID);

            assertEquals(expectedMessage, actualMessage.get());
        }

        @Test
        void shallReturnMessageForExistingCertificateWithinSameCareProviderSameCareUnit() {
            final var expectedMessage = new CertificateMessage(CertificateMessageType.CERTIFICATE_ON_SAME_CARE_UNIT,
                "Det finns ett signerat dödsbevis för detta personnummer."
                    + " Du kan inte skapa ett nytt dödsbevis men kan däremot välja att ersätta det befintliga dödsbeviset."
            );

            doReturn(
                Map.of(INTYG_INDICATOR,
                    Map.of(CERTIFICATE_TYPE_DB,
                        PreviousIntyg.of(true, true, false, "", "123", LocalDateTime.now()))))
                .when(utkastService).checkIfPersonHasExistingIntyg(PERSON_ID, mockedUser, null);

            final var actualMessage = certificateTypeMessageService.get(CERTIFICATE_TYPE_DB, PERSON_ID);

            assertEquals(expectedMessage, actualMessage.get());
        }

        @Test
        void shallReturnMessageForExistingCertificateWithinSameCareProviderDifferentCareUnit() {
            final var expectedMessage = new CertificateMessage(CertificateMessageType.CERTIFICATE_ON_DIFFERENT_CARE_UNIT,
                "Det finns ett signerat dödsbevis för detta personnummer på annan vårdenhet."
                    + " Du kan inte skapa ett nytt dödsbevis men kan däremot välja att ersätta det befintliga dödsbeviset."
            );

            doReturn(
                Map.of(INTYG_INDICATOR,
                    Map.of(CERTIFICATE_TYPE_DB,
                        PreviousIntyg.of(true, false, false, "", "123", LocalDateTime.now()))))
                .when(utkastService).checkIfPersonHasExistingIntyg(PERSON_ID, mockedUser, null);

            final var actualMessage = certificateTypeMessageService.get(CERTIFICATE_TYPE_DB, PERSON_ID);

            assertEquals(expectedMessage, actualMessage.get());
        }

        @Test
        void shallReturnMessageForExistingCertificateDifferentCareProvider() {
            final var expectedMessage = new CertificateMessage(CertificateMessageType.CERTIFICATE_ON_DIFFERENT_CARE_PROVIDER,
                "Det finns ett signerat dödsbevis för detta personnummer hos annan vårdgivare."
                    + " Det är inte möjligt att skapa ett nytt dödsbevis."
            );

            doReturn(
                Map.of(INTYG_INDICATOR,
                    Map.of(CERTIFICATE_TYPE_DB,
                        PreviousIntyg.of(false, false, false, "", "123", LocalDateTime.now()))))
                .when(utkastService).checkIfPersonHasExistingIntyg(PERSON_ID, mockedUser, null);

            final var actualMessage = certificateTypeMessageService.get(CERTIFICATE_TYPE_DB, PERSON_ID);

            assertEquals(expectedMessage, actualMessage.get());
        }

        @Test
        void shallReturnCertificateMessageWhenBothCertificateAndDraftMessageIsPresent() {
            final var expectedMessage = new CertificateMessage(CertificateMessageType.CERTIFICATE_ON_SAME_CARE_UNIT,
                "Det finns ett signerat dödsbevis för detta personnummer."
                    + " Du kan inte skapa ett nytt dödsbevis men kan däremot välja att ersätta det befintliga dödsbeviset."
            );

            doReturn(
                Map.of(INTYG_INDICATOR,
                    Map.of(CERTIFICATE_TYPE_DB,
                        PreviousIntyg.of(true, true, false, "", "123", LocalDateTime.now())),
                    UTKAST_INDICATOR,
                    Map.of(CERTIFICATE_TYPE_DB,
                        PreviousIntyg.of(true, true, false, "", "123", LocalDateTime.now()))))
                .when(utkastService).checkIfPersonHasExistingIntyg(PERSON_ID, mockedUser, null);

            final var actualMessage = certificateTypeMessageService.get(CERTIFICATE_TYPE_DB, PERSON_ID);

            assertEquals(expectedMessage, actualMessage.get());
        }

    }

    @Nested
    class NotTypeDb {

        @Test
        void shallReturnEmptyIfCertificateTypeDoesNotMatch() {

            final var actualMessage = certificateTypeMessageService.get(CERTIFICATE_TYPE_NOT_DB, PERSON_ID);

            assertTrue(actualMessage.isEmpty());
        }
    }

    @Nested
    class NotTypeDoi {

        @Test
        void shallReturnEmptyIfCertificateTypeDoesNotMatch() {

            final var actualMessage = certificateTypeMessageService.get(CERTIFICATE_TYPE_NOT_DOI, PERSON_ID);

            assertTrue(actualMessage.isEmpty());
        }
    }

    @Nested
    class WithTypeDoi {

        @BeforeEach
        void setUp() {
            mockedUser = mock(WebCertUser.class);
            doReturn(mockedUser)
                .when(webCertUserService).getUser();
        }

        @Test
        void shallReturnMessageForExistingDraftWithinSameCareProviderSameCareUnit() {
            final var expectedMessage = new CertificateMessage(CertificateMessageType.DRAFT_ON_SAME_CARE_UNIT,
                "Det finns ett utkast på dödsorsaksintyg för detta personnummer. "
                    + "Du kan inte skapa ett nytt utkast men kan däremot välja att fortsätta med det befintliga utkastet."
            );

            doReturn(
                Map.of(UTKAST_INDICATOR,
                    Map.of(CERTIFICATE_TYPE_DOI,
                        PreviousIntyg.of(true, true, false, "", "123", LocalDateTime.now()))))
                .when(utkastService).checkIfPersonHasExistingIntyg(PERSON_ID, mockedUser, null);

            final var actualMessage = certificateTypeMessageService.get(CERTIFICATE_TYPE_DOI, PERSON_ID);

            assertEquals(expectedMessage, actualMessage.get());
        }

        @Test
        void shallReturnMessageForExistingDraftWithinSameCareProviderNotSameCareUnit() {
            final var expectedMessage = new CertificateMessage(CertificateMessageType.DRAFT_ON_DIFFERENT_CARE_UNIT,
                "Det finns ett utkast på dödsorsaksintyg för detta personnummer på annan vårdenhet. "
                    + "Du kan inte skapa ett nytt utkast men kan däremot välja att fortsätta med det befintliga utkastet."
            );

            doReturn(
                Map.of(UTKAST_INDICATOR,
                    Map.of(CERTIFICATE_TYPE_DOI,
                        PreviousIntyg.of(true, false, false, "", "123", LocalDateTime.now()))))
                .when(utkastService).checkIfPersonHasExistingIntyg(PERSON_ID, mockedUser, null);

            final var actualMessage = certificateTypeMessageService.get(CERTIFICATE_TYPE_DOI, PERSON_ID);

            assertEquals(expectedMessage, actualMessage.get());
        }

        @Test
        void shallReturnMessageForExistingDraftNotWithinSameCareProviderNotSameCareUnit() {
            final var expectedMessage = new CertificateMessage(CertificateMessageType.DRAFT_ON_DIFFERENT_CARE_PROVIDER,
                "Det finns ett utkast på dödsorsaksintyg för detta personnummer hos annan vårdgivare. "
                    + "Senast skapade dödsorsaksintyg är det som gäller. "
                    + "Om du fortsätter och lämnar in dödsorsaksintyget så blir det därför detta dödsorsaksintyg som gäller."
            );

            doReturn(
                Map.of(UTKAST_INDICATOR,
                    Map.of(CERTIFICATE_TYPE_DOI,
                        PreviousIntyg.of(false, false, false, "", "123", LocalDateTime.now()))))
                .when(utkastService).checkIfPersonHasExistingIntyg(PERSON_ID, mockedUser, null);

            final var actualMessage = certificateTypeMessageService.get(CERTIFICATE_TYPE_DOI, PERSON_ID);

            assertEquals(expectedMessage, actualMessage.get());
        }

        @Test
        void shallReturnMessageForExistingCertificateWithinSameCareProviderSameCareUnit() {
            final var expectedMessage = new CertificateMessage(CertificateMessageType.CERTIFICATE_ON_SAME_CARE_UNIT,
                "Det finns ett signerat dödsorsaksintyg för detta personnummer. "
                    + "Du kan inte skapa ett nytt dödsorsaksintyg men kan däremot välja att ersätta det befintliga dödsorsaksintyget."
            );

            doReturn(
                Map.of(INTYG_INDICATOR,
                    Map.of(CERTIFICATE_TYPE_DOI,
                        PreviousIntyg.of(true, true, false, "", "123", LocalDateTime.now()))))
                .when(utkastService).checkIfPersonHasExistingIntyg(PERSON_ID, mockedUser, null);

            final var actualMessage = certificateTypeMessageService.get(CERTIFICATE_TYPE_DOI, PERSON_ID);

            assertEquals(expectedMessage, actualMessage.get());
        }

        @Test
        void shallReturnMessageForExistingCertificateWithinSameCareProviderDifferentCareUnit() {
            final var expectedMessage = new CertificateMessage(CertificateMessageType.CERTIFICATE_ON_DIFFERENT_CARE_UNIT,
                "Det finns ett signerat dödsorsaksintyg för detta personnummer på annan vårdenhet. "
                    + "Du kan inte skapa ett nytt dödsorsaksintyg men kan däremot välja att ersätta det befintliga dödsorsaksintyget."
            );

            doReturn(
                Map.of(INTYG_INDICATOR,
                    Map.of(CERTIFICATE_TYPE_DOI,
                        PreviousIntyg.of(true, false, false, "", "123", LocalDateTime.now()))))
                .when(utkastService).checkIfPersonHasExistingIntyg(PERSON_ID, mockedUser, null);

            final var actualMessage = certificateTypeMessageService.get(CERTIFICATE_TYPE_DOI, PERSON_ID);

            assertEquals(expectedMessage, actualMessage.get());
        }

        @Test
        void shallReturnMessageForExistingCertificateDifferentCareProvider() {
            final var expectedMessage = new CertificateMessage(CertificateMessageType.CERTIFICATE_ON_DIFFERENT_CARE_PROVIDER,
                "Det finns ett signerat dödsorsaksintyg för detta personnummer hos annan vårdgivare. "
                    + "Senast skapade dödsorsaksintyg är det som gäller. "
                    + "Om du fortsätter och lämnar in dödsorsaksintyget så blir det därför detta dödsorsaksintyg som gäller."
            );

            doReturn(
                Map.of(INTYG_INDICATOR,
                    Map.of(CERTIFICATE_TYPE_DOI,
                        PreviousIntyg.of(false, false, false, "", "123", LocalDateTime.now()))))
                .when(utkastService).checkIfPersonHasExistingIntyg(PERSON_ID, mockedUser, null);

            final var actualMessage = certificateTypeMessageService.get(CERTIFICATE_TYPE_DOI, PERSON_ID);

            assertEquals(expectedMessage, actualMessage.get());
        }
    }
}
