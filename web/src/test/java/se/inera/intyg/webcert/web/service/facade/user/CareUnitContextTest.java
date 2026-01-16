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
package se.inera.intyg.webcert.web.service.facade.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Mottagning;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

class CareUnitContextTest {

    private static final String CARE_PROVIDER_ID = "CARE_PROVIDER";
    private static final String CARE_UNIT_ID = "CARE_UNIT";
    private static final String CARE_UNIT_ID_2 = "CARE_UNIT_2";
    private static final String SUB_UNIT_ID = "SUB_UNIT";
    private static final int MAX_COMMISSIONS = 15;

    private WebCertUser user;

    @BeforeEach
    void setUp() {
        user = mock(WebCertUser.class);
    }

    @Nested
    class WhenUserHasNoCareProviders {

        @Test
        void shouldReturnNullWhenVardgivareIsNull() {
            doReturn(null).when(user).getVardgivare();

            final var result = CareUnitContext.build(user, MAX_COMMISSIONS);

            assertNull(result);
        }

        @Test
        void shouldReturnNullWhenVardgivareIsEmpty() {
            doReturn(List.of()).when(user).getVardgivare();

            final var result = CareUnitContext.build(user, MAX_COMMISSIONS);

            assertNull(result);
        }
    }

    @Nested
    class WhenUserHasCareProviders {

        @BeforeEach
        void setUp() {
            final Vardgivare careProvider = new Vardgivare();
            careProvider.setId(CARE_PROVIDER_ID);

            final var careUnit = new Vardenhet(CARE_UNIT_ID, "Care Unit Name");
            careUnit.setMottagningar(List.of(new Mottagning(SUB_UNIT_ID, "Sub Unit Name")));
            careProvider.setVardenheter(List.of(careUnit));

            doReturn(List.of(careProvider)).when(user).getVardgivare();
            doReturn(List.of(CARE_UNIT_ID, SUB_UNIT_ID)).when(user).getIdsOfAllVardenheter();
            doReturn(List.of(CARE_UNIT_ID, SUB_UNIT_ID)).when(user).getIdsOfSelectedVardenhet();
        }

        @Test
        void shouldReturnNonNullContext() {
            final var result = CareUnitContext.build(user, MAX_COMMISSIONS);

            assertNotNull(result);
        }

        @Test
        void shouldContainCareUnitId() {
            final var result = CareUnitContext.build(user, MAX_COMMISSIONS);

            assertNotNull(result);
            assertTrue(result.careUnitIds().contains(CARE_UNIT_ID));
        }

        @Test
        void shouldNotContainSubUnitInCareUnitIds() {
            final var result = CareUnitContext.build(user, MAX_COMMISSIONS);

            assertNotNull(result);
            assertFalse(result.careUnitIds().contains(SUB_UNIT_ID));
        }

        @Test
        void shouldMapSubUnitsCorrectly() {
            final var result = CareUnitContext.build(user, MAX_COMMISSIONS);

            assertNotNull(result);
            assertEquals(List.of(SUB_UNIT_ID), result.getSubUnitsFor(CARE_UNIT_ID));
        }

        @Test
        void shouldReturnEmptyListForUnknownCareUnit() {
            final var result = CareUnitContext.build(user, MAX_COMMISSIONS);

            assertNotNull(result);
            assertEquals(List.of(), result.getSubUnitsFor("UNKNOWN"));
        }

        @Test
        void shouldMapCareProviderIdCorrectly() {
            final var result = CareUnitContext.build(user, MAX_COMMISSIONS);

            assertNotNull(result);
            assertEquals(CARE_PROVIDER_ID, result.getCareProviderIdFor(CARE_UNIT_ID));
        }

        @Test
        void shouldReturnNullCareProviderIdForUnknownUnit() {
            final var result = CareUnitContext.build(user, MAX_COMMISSIONS);

            assertNotNull(result);
            assertNull(result.getCareProviderIdFor("UNKNOWN"));
        }

        @Test
        void shouldNotExceedMaxCommissionsWhenUnderLimit() {
            final var result = CareUnitContext.build(user, MAX_COMMISSIONS);

            assertNotNull(result);
            assertFalse(result.maxCommissionsExceeded());
        }

        @Test
        void shouldContainAllUnitIdsFromUser() {
            final var result = CareUnitContext.build(user, MAX_COMMISSIONS);

            assertNotNull(result);
            assertEquals(List.of(CARE_UNIT_ID, SUB_UNIT_ID), result.allUnitIds());
        }

        @Test
        void shouldContainSelectedUnitIds() {
            final var result = CareUnitContext.build(user, MAX_COMMISSIONS);

            assertNotNull(result);
            assertTrue(result.selectedUnitIds().contains(CARE_UNIT_ID));
            assertTrue(result.selectedUnitIds().contains(SUB_UNIT_ID));
        }
    }

    @Nested
    class WhenMaxCommissionsExceeded {

        @BeforeEach
        void setUp() {
            final var careProvider = new Vardgivare();
            careProvider.setId(CARE_PROVIDER_ID);

            final var careUnit1 = new Vardenhet(CARE_UNIT_ID, "Care Unit 1");
            final var careUnit2 = new Vardenhet(CARE_UNIT_ID_2, "Care Unit 2");
            careProvider.setVardenheter(List.of(careUnit1, careUnit2));

            doReturn(List.of(careProvider)).when(user).getVardgivare();
            doReturn(List.of(CARE_UNIT_ID)).when(user).getIdsOfSelectedVardenhet();
        }

        @Test
        void shouldExceedMaxCommissionsWhenOverLimit() {
            final var result = CareUnitContext.build(user, 1);

            assertNotNull(result);
            assertTrue(result.maxCommissionsExceeded());
        }

        @Test
        void shouldUseSelectedUnitWhenExceededAndHasValdVardenhet() {
            final var valdVardenhet = mock(Vardenhet.class);
            doReturn(List.of(CARE_UNIT_ID)).when(valdVardenhet).getHsaIds();
            doReturn(valdVardenhet).when(user).getValdVardenhet();

            final var result = CareUnitContext.build(user, 1);

            assertNotNull(result);
            assertEquals(List.of(CARE_UNIT_ID), result.allUnitIds());
        }

        @Test
        void shouldReturnEmptyAllUnitIdsWhenExceededAndNoValdVardenhet() {
            doReturn(null).when(user).getValdVardenhet();

            final var result = CareUnitContext.build(user, 1);

            assertNotNull(result);
            assertTrue(result.allUnitIds().isEmpty());
        }
    }

    @Nested
    class WhenUnitsHaveNullIds {

        @Test
        void shouldSkipCareUnitsWithNullId() {
            final var careProvider = new Vardgivare();
            careProvider.setId(CARE_PROVIDER_ID);

            final var careUnitWithNullId = new Vardenhet(null, "Care Unit Without Id");
            final var careUnitWithId = new Vardenhet(CARE_UNIT_ID, "Care Unit With Id");
            careProvider.setVardenheter(List.of(careUnitWithNullId, careUnitWithId));

            doReturn(List.of(careProvider)).when(user).getVardgivare();
            doReturn(List.of(CARE_UNIT_ID)).when(user).getIdsOfAllVardenheter();
            doReturn(List.of()).when(user).getIdsOfSelectedVardenhet();

            final var result = CareUnitContext.build(user, MAX_COMMISSIONS);

            assertNotNull(result);
            assertEquals(1, result.careUnitIds().size());
            assertEquals(CARE_UNIT_ID, result.careUnitIds().getFirst());
        }

        @Test
        void shouldSkipSubUnitsWithNullId() {
            final var careProvider = new Vardgivare();
            careProvider.setId(CARE_PROVIDER_ID);

            final var careUnit = new Vardenhet(CARE_UNIT_ID, "Care Unit");
            careUnit.setMottagningar(List.of(
                new Mottagning(null, "Sub Unit Without Id"),
                new Mottagning(SUB_UNIT_ID, "Sub Unit With Id")
            ));
            careProvider.setVardenheter(List.of(careUnit));

            doReturn(List.of(careProvider)).when(user).getVardgivare();
            doReturn(List.of(CARE_UNIT_ID, SUB_UNIT_ID)).when(user).getIdsOfAllVardenheter();
            doReturn(List.of()).when(user).getIdsOfSelectedVardenhet();

            final var result = CareUnitContext.build(user, MAX_COMMISSIONS);

            assertNotNull(result);
            assertEquals(List.of(SUB_UNIT_ID), result.getSubUnitsFor(CARE_UNIT_ID));
        }
    }

    @Nested
    class GetNotSelectedUnitIds {

        @Test
        void shouldReturnUnitsNotInSelectedSet() {
            final var careProvider = new Vardgivare();
            careProvider.setId(CARE_PROVIDER_ID);

            final var careUnit = new Vardenhet(CARE_UNIT_ID, "Care Unit");
            final var careUnit2 = new Vardenhet(CARE_UNIT_ID_2, "Care Unit 2");
            careProvider.setVardenheter(List.of(careUnit, careUnit2));

            doReturn(List.of(careProvider)).when(user).getVardgivare();
            doReturn(List.of(CARE_UNIT_ID, CARE_UNIT_ID_2)).when(user).getIdsOfAllVardenheter();
            doReturn(List.of(CARE_UNIT_ID)).when(user).getIdsOfSelectedVardenhet();

            final var result = CareUnitContext.build(user, MAX_COMMISSIONS);

            assertNotNull(result);
            assertEquals(List.of(CARE_UNIT_ID_2), result.getNotSelectedUnitIds());
        }

        @Test
        void shouldReturnEmptyListWhenAllUnitsSelected() {
            final var careProvider = new Vardgivare();
            careProvider.setId(CARE_PROVIDER_ID);

            final var careUnit = new Vardenhet(CARE_UNIT_ID, "Care Unit");
            careProvider.setVardenheter(List.of(careUnit));

            doReturn(List.of(careProvider)).when(user).getVardgivare();
            doReturn(List.of(CARE_UNIT_ID)).when(user).getIdsOfAllVardenheter();
            doReturn(List.of(CARE_UNIT_ID)).when(user).getIdsOfSelectedVardenhet();

            final var result = CareUnitContext.build(user, MAX_COMMISSIONS);

            assertNotNull(result);
            assertTrue(result.getNotSelectedUnitIds().isEmpty());
        }
    }
}