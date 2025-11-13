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
package se.inera.intyg.webcert.web.service.facade.modal.typeinfo;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.inera.intyg.webcert.web.service.facade.impl.PreviousCertificateInfo;

class DoiTypeInfoModalProviderTest {
    
    private DoiTypeInfoModalProvider provider;

    @BeforeEach
    void setUp() {
        provider = new DoiTypeInfoModalProvider();
    }

    @Test
    void shouldReturnEmptyWhenSameCareProviderAndSameUnit() {
        final var previousCertificateInfo = PreviousCertificateInfo.builder()
            .careUnitName("Test Unit")
            .careUnitHsaId("HSA-123")
            .careProviderName("Test Provider")
            .isDraft(false)
            .isSameCareProvider(true)
            .isSameUnit(true)
            .build();

        final var result = provider.create(previousCertificateInfo);

        assertFalse(result.isPresent());
    }

    @Test
    void shouldReturnModalForDraftOnDifferentUnitButSameCareProvider() {
        final var careProviderName = "Vårdgivare AB";
        final var careUnitName = "Vårdenhet 1";
        final var careUnitHsaId = "HSA-VE-001";

        final var previousCertificateInfo = PreviousCertificateInfo.builder()
            .careUnitName(careUnitName)
            .careUnitHsaId(careUnitHsaId)
            .careProviderName(careProviderName)
            .isDraft(true)
            .isSameCareProvider(true)
            .isSameUnit(false)
            .build();

        final var result = provider.create(previousCertificateInfo);

        assertAll(
            () -> assertTrue(result.isPresent()),
            () -> assertEquals("Utkast på dödsorsaksintyg på annan vårdenhet", result.get().getTitle()),
            () -> assertEquals("Visa vårdenhetens namn och HSA-id", result.get().getLink()),
            () -> assertTrue(result.get().getDescription().contains(careProviderName)),
            () -> assertTrue(result.get().getDescription().contains(careUnitName)),
            () -> assertTrue(result.get().getDescription().contains(careUnitHsaId))
        );
    }

    @Test
    void shouldReturnModalForSignedCertificateOnDifferentUnitButSameCareProvider() {
        final var careProviderName = "Vårdgivare CD";
        final var careUnitName = "Vårdenhet 2";
        final var careUnitHsaId = "HSA-VE-002";

        final var previousCertificateInfo = PreviousCertificateInfo.builder()
            .careUnitName(careUnitName)
            .careUnitHsaId(careUnitHsaId)
            .careProviderName(careProviderName)
            .isDraft(false)
            .isSameCareProvider(true)
            .isSameUnit(false)
            .build();

        final var result = provider.create(previousCertificateInfo);

        assertAll(
            () -> assertTrue(result.isPresent()),
            () -> assertEquals("Signerat dödsorsaksintyg på annan vårdenhet", result.get().getTitle()),
            () -> assertEquals("Visa vårdenhetens namn och HSA-id", result.get().getLink()),
            () -> assertTrue(result.get().getDescription().contains(careProviderName)),
            () -> assertTrue(result.get().getDescription().contains(careUnitName)),
            () -> assertTrue(result.get().getDescription().contains(careUnitHsaId))
        );
    }

    @Test
    void shouldReturnModalForDraftOnDifferentCareProvider() {
        final var careProviderName = "Vårdgivare EF";
        final var careUnitName = "Vårdenhet 3";
        final var careUnitHsaId = "HSA-VE-003";

        final var previousCertificateInfo = PreviousCertificateInfo.builder()
            .careUnitName(careUnitName)
            .careUnitHsaId(careUnitHsaId)
            .careProviderName(careProviderName)
            .isDraft(true)
            .isSameCareProvider(false)
            .isSameUnit(false)
            .build();

        final var result = provider.create(previousCertificateInfo);

        assertAll(
            () -> assertTrue(result.isPresent()),
            () -> assertEquals("Utkast på dödsorsaksintyg hos annan vårdgivare", result.get().getTitle()),
            () -> assertEquals("Visa vårdenhetens namn och HSA-id", result.get().getLink())
        );
    }

    @Test
    void shouldReturnModalForSignedCertificateOnDifferentCareProvider() {
        final var careProviderName = "Vårdgivare GH";
        final var careUnitName = "Vårdenhet 4";
        final var careUnitHsaId = "HSA-VE-004";

        final var previousCertificateInfo = PreviousCertificateInfo.builder()
            .careUnitName(careUnitName)
            .careUnitHsaId(careUnitHsaId)
            .careProviderName(careProviderName)
            .isDraft(false)
            .isSameCareProvider(false)
            .isSameUnit(false)
            .build();

        final var result = provider.create(previousCertificateInfo);

        assertAll(
            () -> assertTrue(result.isPresent()),
            () -> assertEquals("Signerat dödsorsaksintyg hos annan vårdgivare", result.get().getTitle()),
            () -> assertEquals("Visa vårdenhetens namn och HSA-id", result.get().getLink())
        );
    }

    @Test
    void shouldIncludeCorrectDescriptionFormat() {
        final var careProviderName = "Test Vårdgivare";
        final var careUnitName = "Test Vårdenhet";
        final var careUnitHsaId = "HSA-TEST-123";

        final var previousCertificateInfo = PreviousCertificateInfo.builder()
            .careUnitName(careUnitName)
            .careUnitHsaId(careUnitHsaId)
            .careProviderName(careProviderName)
            .isDraft(true)
            .isSameCareProvider(false)
            .isSameUnit(false)
            .build();

        final var result = provider.create(previousCertificateInfo);

        final var expectedDescription = "<p><strong>Vårdgivare</strong><br/>" + careProviderName + "</p>"
            + "<p><strong>Vårdenhet</strong><br/>" + careUnitName + "</p>"
            + "<p><strong>Vårdenhetens HSA-id</strong><br/>" + careUnitHsaId + "</p>";

        assertAll(
            () -> assertTrue(result.isPresent()),
            () -> assertEquals(expectedDescription, result.get().getDescription())
        );
    }
}

