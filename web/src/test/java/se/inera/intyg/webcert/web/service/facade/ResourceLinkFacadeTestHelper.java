/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.facade;

import se.inera.intyg.common.support.facade.builder.CertificateBuilder;
import se.inera.intyg.common.support.facade.model.*;
import se.inera.intyg.common.support.facade.model.config.CertificateDataConfigSickLeavePeriod;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.common.support.facade.model.metadata.Unit;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDateRange;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDateRangeList;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ResourceLinkFacadeTestHelper {

    public static void assertInclude(List<ResourceLinkDTO> availableFunctions, ResourceLinkTypeDTO type) {
        final var actualResourceLink = get(availableFunctions, type);
        assertNotNull(actualResourceLink, () -> String.format("Expected resource link with type '%s'", type));
    }

    public static void assertInclude(ResourceLinkDTO[] availableFunctions, ResourceLinkTypeDTO type) {
        assertInclude(Arrays.asList(availableFunctions), type);
    }

    public static void assertExclude(List<ResourceLinkDTO> availableFunctions, ResourceLinkTypeDTO type) {
        final var actualResourceLink = get(availableFunctions, type);
        assertNull(actualResourceLink, () -> String.format("Don't expect resource link with type '%s'", type));
    }

    public static void assertExclude(ResourceLinkDTO[] availableFunctions, ResourceLinkTypeDTO type) {
        assertExclude(Arrays.asList(availableFunctions), type);
    }

    public static void assertDisabled(List<ResourceLinkDTO> availableFunctions, ResourceLinkTypeDTO type) {
        final var actualResourceLink = get(availableFunctions, type);
        assertFalse(actualResourceLink.isEnabled());
    }

    public static void assertEnabled(List<ResourceLinkDTO> availableFunctions, ResourceLinkTypeDTO type) {
        final var actualResourceLink = get(availableFunctions, type);
        assertTrue(actualResourceLink.isEnabled());
    }

    public static ResourceLinkDTO get(List<ResourceLinkDTO> resourceLinks, ResourceLinkTypeDTO type) {
        return resourceLinks.stream()
                .filter(resourceLinkDTO -> resourceLinkDTO.getType().equals(type))
                .findFirst()
                .orElse(null);
    }
}
