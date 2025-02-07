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

package se.inera.intyg.webcert.web.csintegration.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateServiceTypeInfoDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;

@ExtendWith(MockitoExtension.class)
class CertificateTypeInfoConverterTest {

    @InjectMocks
    private static final CertificateTypeInfoConverter certificateTypeInfoConverter = new CertificateTypeInfoConverter();

    private CertificateServiceTypeInfoDTO createTypeInfo() {
        return CertificateServiceTypeInfoDTO.builder()
            .name("name")
            .description("description")
            .links(List.of(new ResourceLinkDTO()))
            .type("type")
            .build();
    }

    @Test
    void shouldConvertId() {
        final var typeInfo = createTypeInfo();
        final var response = certificateTypeInfoConverter.convert(typeInfo);

        assertEquals(typeInfo.getType(), response.getId());
    }

    @Test
    void shouldConvertLabel() {
        final var typeInfo = createTypeInfo();
        final var response = certificateTypeInfoConverter.convert(typeInfo);

        assertEquals(typeInfo.getName(), response.getLabel());
    }

    @Test
    void shouldConvertIssuerTypeId() {
        final var typeInfo = createTypeInfo();
        final var response = certificateTypeInfoConverter.convert(typeInfo);

        assertEquals(typeInfo.getType(), response.getIssuerTypeId());
    }

    @Test
    void shouldConvertDescription() {
        final var typeInfo = createTypeInfo();
        final var response = certificateTypeInfoConverter.convert(typeInfo);

        assertEquals(typeInfo.getDescription(), response.getDescription());
    }

    @Test
    void shouldConvertDetailedDescription() {
        final var typeInfo = createTypeInfo();
        final var response = certificateTypeInfoConverter.convert(typeInfo);

        assertEquals(typeInfo.getDescription(), response.getDetailedDescription());
    }

    @Test
    void shouldConvertLinks() {
        final var typeInfo = createTypeInfo();
        final var response = certificateTypeInfoConverter.convert(typeInfo);

        assertEquals(typeInfo.getLinks(), response.getLinks());
    }
}
