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
import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.facade.CertificateFacadeTestHelper;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.PreviousIntyg;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@ExtendWith(MockitoExtension.class)
class ShowRelatedCertificateFunctionImplTest {

    @Mock
    private WebCertUser webCertUser;

    @Mock
    private UtkastService utkastService;

    @InjectMocks
    private ShowRelatedCertificateFunctionImpl showRelatedCertificateFunction;

    @Nested
    class ShowDoiFromDb {

        private Certificate certificate;
        private Personnummer personNummer;

        @BeforeEach
        void setup() {
            certificate = CertificateFacadeTestHelper.createCertificate(DbModuleEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            personNummer = Personnummer.createPersonnummer(certificate.getMetadata().getPatient().getPersonId().getId()).get();
        }

        @Test
        void shallIncludeShowRelatedCertificateIfShowDoiIsTrueForPreviousDraft() {
            final var dbWithinCareProvider = Map.of(
                UTKAST_INDICATOR,
                Map.of(
                    DoiModuleEntryPoint.MODULE_ID,
                    PreviousIntyg.of(true, false, true, "ENHET", "xxxxx-yyyyyy-zzzzz", LocalDateTime.now())
                )
            );

            doReturn(dbWithinCareProvider).when(utkastService)
                .checkIfPersonHasExistingIntyg(personNummer, webCertUser, certificate.getMetadata().getId());

            final var actualLink = showRelatedCertificateFunction.get(certificate, webCertUser);
            assertTrue(actualLink.isPresent());
        }

        @Test
        void shallExcludeShowRelatedCertificateIfShowDoiIsFalseForPreviousDraft() {
            final var dbWithinCareProvider = Map.of(
                UTKAST_INDICATOR,
                Map.of(
                    DoiModuleEntryPoint.MODULE_ID,
                    PreviousIntyg.of(true, false, false, "ENHET", "xxxxx-yyyyyy-zzzzz", LocalDateTime.now())
                )
            );

            doReturn(dbWithinCareProvider).when(utkastService)
                .checkIfPersonHasExistingIntyg(personNummer, webCertUser, certificate.getMetadata().getId());

            final var certificate = CertificateFacadeTestHelper.createCertificate(DbModuleEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualLink = showRelatedCertificateFunction.get(certificate, webCertUser);
            assertTrue(actualLink.isEmpty());
        }

        @Test
        void shallIncludeShowRelatedCertificateIfShowDoiIsTrueForPreviousCertificate() {
            final var dbWithinCareProvider = Map.of(
                INTYG_INDICATOR,
                Map.of(
                    DoiModuleEntryPoint.MODULE_ID,
                    PreviousIntyg.of(true, false, true, "ENHET", "xxxxx-yyyyyy-zzzzz", LocalDateTime.now())
                )
            );

            doReturn(dbWithinCareProvider).when(utkastService)
                .checkIfPersonHasExistingIntyg(personNummer, webCertUser, certificate.getMetadata().getId());

            final var certificate = CertificateFacadeTestHelper.createCertificate(DbModuleEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualLink = showRelatedCertificateFunction.get(certificate, webCertUser);
            assertTrue(actualLink.isPresent());
        }

        @Test
        void shallExcludeShowRelatedCertificateIfShowDoiIsFalseForPreviousCertificate() {
            final var dbWithinCareProvider = Map.of(
                INTYG_INDICATOR,
                Map.of(
                    DoiModuleEntryPoint.MODULE_ID,
                    PreviousIntyg.of(true, false, false, "ENHET", "xxxxx-yyyyyy-zzzzz", LocalDateTime.now())
                )
            );

            doReturn(dbWithinCareProvider).when(utkastService)
                .checkIfPersonHasExistingIntyg(personNummer, webCertUser, certificate.getMetadata().getId());

            final var certificate = CertificateFacadeTestHelper.createCertificate(DbModuleEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualLink = showRelatedCertificateFunction.get(certificate, webCertUser);
            assertTrue(actualLink.isEmpty());
        }

        @Test
        void shallIncludeResourceLinkDTOType() {
            final var dbWithinCareProvider = Map.of(
                INTYG_INDICATOR,
                Map.of(
                    DoiModuleEntryPoint.MODULE_ID,
                    PreviousIntyg.of(true, false, true, "ENHET", "xxxxx-yyyyyy-zzzzz", LocalDateTime.now())
                )
            );

            doReturn(dbWithinCareProvider).when(utkastService)
                .checkIfPersonHasExistingIntyg(personNummer, webCertUser, certificate.getMetadata().getId());

            final var expectedType = ResourceLinkTypeDTO.SHOW_RELATED_CERTIFICATE;
            final var certificate = CertificateFacadeTestHelper.createCertificate(DbModuleEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualLink = showRelatedCertificateFunction.get(certificate, webCertUser);
            assertEquals(expectedType, actualLink.get().getType());
        }

        @Test
        void shallIncludeResourceLinkDTOName() {
            final var dbWithinCareProvider = Map.of(
                INTYG_INDICATOR,
                Map.of(
                    DoiModuleEntryPoint.MODULE_ID,
                    PreviousIntyg.of(true, false, true, "ENHET", "xxxxx-yyyyyy-zzzzz", LocalDateTime.now())
                )
            );

            doReturn(dbWithinCareProvider).when(utkastService)
                .checkIfPersonHasExistingIntyg(personNummer, webCertUser, certificate.getMetadata().getId());

            final var expectedName = "Visa dödsorsaksintyg";
            final var certificate = CertificateFacadeTestHelper.createCertificate(DbModuleEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualLink = showRelatedCertificateFunction.get(certificate, webCertUser);
            assertEquals(expectedName, actualLink.get().getName());
        }

        @Test
        void shallIncludeResourceLinkDTODescription() {
            final var dbWithinCareProvider = Map.of(
                INTYG_INDICATOR,
                Map.of(
                    DoiModuleEntryPoint.MODULE_ID,
                    PreviousIntyg.of(true, false, true, "ENHET", "xxxxx-yyyyyy-zzzzz", LocalDateTime.now())
                )
            );

            doReturn(dbWithinCareProvider).when(utkastService)
                .checkIfPersonHasExistingIntyg(personNummer, webCertUser, certificate.getMetadata().getId());

            final var expectedDescription = "Visa det dödsorsaksintyg som har skapats från dödsbeviset.";
            final var certificate = CertificateFacadeTestHelper.createCertificate(DbModuleEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualLink = showRelatedCertificateFunction.get(certificate, webCertUser);
            assertEquals(expectedDescription, actualLink.get().getDescription());
        }

        @Test
        void shallIncludeResourceLinkDTOEnabled() {
            final var dbWithinCareProvider = Map.of(
                INTYG_INDICATOR,
                Map.of(
                    DoiModuleEntryPoint.MODULE_ID,
                    PreviousIntyg.of(true, false, true, "ENHET", "xxxxx-yyyyyy-zzzzz", LocalDateTime.now())
                )
            );

            doReturn(dbWithinCareProvider).when(utkastService)
                .checkIfPersonHasExistingIntyg(personNummer, webCertUser, certificate.getMetadata().getId());

            final var expectedEnabled = true;
            final var certificate = CertificateFacadeTestHelper.createCertificate(DbModuleEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualLink = showRelatedCertificateFunction.get(certificate, webCertUser);
            assertEquals(expectedEnabled, actualLink.get().isEnabled());
        }
    }
}
