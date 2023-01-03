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
package se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.webcert.web.service.facade.CertificateFacadeTestHelper;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

class DisplayPatientAddressInCertificateImplTest {

    private DisplayPatientAddressInCertificateImpl displayPatientAddressInCertificate;

    @BeforeEach
    void setUp() {
        displayPatientAddressInCertificate = new DisplayPatientAddressInCertificateImpl();
    }

    @Test
    void shallIncludeDisplayPatientAddressIfDb() {
        final var expectedResourceLink = ResourceLinkDTO.create(
            ResourceLinkTypeDTO.DISPLAY_PATIENT_ADDRESS_IN_CERTIFICATE,
            "Patientuppgifter",
            "Presenterar patientens adressuppgifter",
            false
        );
        final var certificate = CertificateFacadeTestHelper.createCertificate(DbModuleEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED,
            true);
        final var actualResourceLink = displayPatientAddressInCertificate.get(certificate);
        assertEquals(expectedResourceLink, actualResourceLink.get());
    }

    @Test
    void shallIncludeDisplayPatientAddressIfDoi() {
        final var expectedResourceLink = ResourceLinkDTO.create(
            ResourceLinkTypeDTO.DISPLAY_PATIENT_ADDRESS_IN_CERTIFICATE,
            "Patientuppgifter",
            "Presenterar patientens adressuppgifter",
            false
        );
        final var certificate = CertificateFacadeTestHelper.createCertificate(DoiModuleEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED,
            true);
        final var actualResourceLink = displayPatientAddressInCertificate.get(certificate);
        assertEquals(expectedResourceLink, actualResourceLink.get());
    }

    @Test
    void shallExcludeDisplayPatientAddressIfNotDb() {
        final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
        final var actualAvailableFunctions = displayPatientAddressInCertificate.get(certificate);
        assertFalse(actualAvailableFunctions.isPresent(), "Should not return a resource link!");
    }

    @Test
    void shallIncludeDisplayPatientAddresIfDbWithEnableFalseWhenAddressFromPU() {
        final var certificate = CertificateFacadeTestHelper.createCertificate(DbModuleEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED,
            true);
        final var actualResourceLink = displayPatientAddressInCertificate.get(certificate);
        assertFalse(actualResourceLink.get().isEnabled(), "Should not be enable if address is from PU!");
    }

    @Test
    void shallIncludeDisplayPatientAddresIfDbWithEnableTrueWhenAddressMissingFromPU() {
        final var certificate = CertificateFacadeTestHelper.createCertificate(DbModuleEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED,
            false);
        final var actualResourceLink = displayPatientAddressInCertificate.get(certificate);
        assertTrue(actualResourceLink.get().isEnabled(), "Should be enable if address is missing from PU!");
    }
}