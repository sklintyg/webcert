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

package se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.inera.intyg.webcert.web.service.facade.ResourceLinkFacadeTestHelper.assertInclude;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.luae_na.support.LuaenaEntryPoint;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.common.ts_bas.support.TsBasEntryPoint;
import se.inera.intyg.webcert.web.service.facade.CertificateFacadeTestHelper;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@ExtendWith(MockitoExtension.class)
class SendCertificateFunctionImplTest {

    private static final Certificate LUAE_NA = new Certificate();
    private static Certificate TS_BAS = new Certificate();
    private static final Certificate SENT_LUAE_NA = new Certificate();
    private static final Certificate REPLACED_LUAE_NA = new Certificate();

    @InjectMocks
    SendFunctionImpl sendCertificateToFK;

    @BeforeEach
    void setup() {
        TS_BAS = new Certificate();
    }

    @Nested
    class LuaeNa {

        @Test
        void shouldAddResourceLink() {
            final var expectedResourceLink = Optional.of(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.SEND_CERTIFICATE,
                    "Skicka till Försäkringskassan",
                    "Öppnar ett fönster där du kan välja att skicka intyget till Försäkringskassan.",
                    "<p>Om du går vidare kommer intyget skickas direkt till "
                        + "Försäkringskassans system vilket ska göras i samråd med patienten.</p>",
                    true));

            LUAE_NA.setMetadata(CertificateMetadata.builder()
                .type(LuaenaEntryPoint.MODULE_ID)
                .build()
            );

            final var actualResourceLink = sendCertificateToFK.get(LUAE_NA);

            assertEquals(expectedResourceLink, actualResourceLink);
        }

        @Test
        void shouldNotAddResourceLinkIfWrongType() {
            final var expectedResourceLink = Optional.empty();

            LUAE_NA.setMetadata(CertificateMetadata.builder()
                .type(DoiModuleEntryPoint.MODULE_ID)
                .build()
            );

            final var actualResourceLink = sendCertificateToFK.get(LUAE_NA);

            assertEquals(expectedResourceLink, actualResourceLink);
        }

        @Test
        void shouldNotAddResourceLinkIfCertificateIsSent() {
            final var expectedResourceLink = Optional.empty();

            SENT_LUAE_NA.setMetadata(CertificateMetadata.builder()
                .type(LuaenaEntryPoint.MODULE_ID)
                .sent(true)
                .build()
            );

            final var actualResourceLink = sendCertificateToFK.get(SENT_LUAE_NA);

            assertEquals(expectedResourceLink, actualResourceLink);
        }

        @Test
        void shouldNotAddResourceLinkIfCertificateIsReplacedAndSigned() {
            final var expectedResourceLink = Optional.empty();

            REPLACED_LUAE_NA.setMetadata(CertificateMetadata.builder()
                .type(LuaenaEntryPoint.MODULE_ID)
                .relations(CertificateRelations.builder()
                    .children(new CertificateRelation[]{
                        CertificateRelation.builder()
                            .type(CertificateRelationType.REPLACED)
                            .status(CertificateStatus.SIGNED)
                            .build()
                    }).build())
                .build()
            );

            final var actualResourceLink = sendCertificateToFK.get(REPLACED_LUAE_NA);

            assertEquals(expectedResourceLink, actualResourceLink);
        }

        @Test
        void shouldAddResourceLinkIfCertificateIsReplacedAndNotSigned() {
            final var expectedResourceLink = Optional.of(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.SEND_CERTIFICATE,
                    "Skicka till Försäkringskassan",
                    "Öppnar ett fönster där du kan välja att skicka intyget till Försäkringskassan.",
                    "<p>Om du går vidare kommer intyget skickas direkt till "
                        + "Försäkringskassans system vilket ska göras i samråd med patienten.</p>",
                    true));

            REPLACED_LUAE_NA.setMetadata(CertificateMetadata.builder()
                .type(LuaenaEntryPoint.MODULE_ID)
                .relations(CertificateRelations.builder()
                    .children(new CertificateRelation[]{
                        CertificateRelation.builder()
                            .type(CertificateRelationType.REPLACED)
                            .status(CertificateStatus.UNSIGNED)
                            .build()
                    }).build())
                .build()
            );

            final var actualResourceLink = sendCertificateToFK.get(REPLACED_LUAE_NA);

            assertEquals(expectedResourceLink, actualResourceLink);
        }
    }

    @Nested
    class Lisjp {

        @Test
        void shallIncludeSend() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualAvailableFunctions = sendCertificateToFK.get(certificate);
            assertInclude(List.of(actualAvailableFunctions.get()), ResourceLinkTypeDTO.SEND_CERTIFICATE);
        }

        @Test
        void shallIncludeSendWithWarningIfSickleavePeriodIsShorterThan15Days() {
            final var certificate = CertificateFacadeTestHelper.createCertificateWithSickleavePeriod(14);
            final var actualAvailableFunctions = sendCertificateToFK.get(certificate);
            assertInclude(List.of(actualAvailableFunctions.get()), ResourceLinkTypeDTO.SEND_CERTIFICATE);
            assertTrue(actualAvailableFunctions
                .stream()
                .anyMatch(link -> link.getBody() != null && link.getBody()
                    .contains("Om sjukperioden är kortare än 15 dagar ska intyget inte skickas")));
        }

        @Test
        void shallNotIncludeSendWithWarningIfSickleavePeriodIs15Days() {
            final var certificate = CertificateFacadeTestHelper.createCertificateWithSickleavePeriod(15);
            final var actualAvailableFunctions = sendCertificateToFK.get(certificate);
            assertInclude(List.of(actualAvailableFunctions.get()), ResourceLinkTypeDTO.SEND_CERTIFICATE);
            assertFalse(actualAvailableFunctions
                .stream()
                .anyMatch(link ->
                    link.getBody() != null && link.getBody().contains("Om sjukperioden är kortare än 15 dagar ska intyget inte skickas")));
        }

        @Test
        void shallNotIncludeSendWithWarningIfSickleavePeriodIsLongerThan15Days() {
            final var certificate = CertificateFacadeTestHelper.createCertificateWithSickleavePeriod(100);
            final var actualAvailableFunctions = sendCertificateToFK.get(certificate);
            assertInclude(List.of(actualAvailableFunctions.get()), ResourceLinkTypeDTO.SEND_CERTIFICATE);
            assertFalse(actualAvailableFunctions
                .stream()
                .anyMatch(link -> link.getBody() != null && link.getBody()
                    .contains("Om sjukperioden är kortare än 15 dagar ska intyget inte skickas")));
        }

        @Test
        void shallExcludeSendIfAlreadySent() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            certificate.getMetadata().setSent(true);
            final var actualAvailableFunctions = sendCertificateToFK.get(certificate);
            assertTrue(actualAvailableFunctions.isEmpty());
        }

        @Test
        void shallExcludeSendIfReplacedBySignedCertificate() {
            final var relation = CertificateRelation.builder()
                .certificateId("xxxxx-yyyyy-zzzzz-uuuuu")
                .created(LocalDateTime.now())
                .status(CertificateStatus.SIGNED)
                .type(CertificateRelationType.REPLACED)
                .build();

            final var certificate = CertificateFacadeTestHelper
                .createCertificateWithChildRelation(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED, relation);
            final var actualAvailableFunctions = sendCertificateToFK.get(certificate);
            assertTrue(actualAvailableFunctions.isEmpty());
        }

        @Test
        void shallIncludeSendIfReplacedByUnsignedCertificate() {
            final var relation = CertificateRelation.builder()
                .certificateId("xxxxx-yyyyy-zzzzz-uuuuu")
                .created(LocalDateTime.now())
                .status(CertificateStatus.UNSIGNED)
                .type(CertificateRelationType.REPLACED)
                .build();

            final var certificate = CertificateFacadeTestHelper
                .createCertificateWithChildRelation(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED, relation);

            final var actualAvailableFunctions = sendCertificateToFK.get(certificate);
            assertInclude(List.of(actualAvailableFunctions.get()), ResourceLinkTypeDTO.SEND_CERTIFICATE);
        }
    }

    @Nested
    class TsBas {

        @Test
        void shouldAddResourceLink() {
            final var expectedResourceLink = Optional.of(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.SEND_CERTIFICATE,
                    "Skicka till Transportstyrelsen",
                    "Öppnar ett fönster där du kan välja att skicka intyget till Transportstyrelsen.",
                    "<p>Om du går vidare kommer intyget skickas direkt till "
                        + "Transportstyrelsens system vilket ska göras i samråd med patienten.</p>",
                    true));

            TS_BAS.setMetadata(CertificateMetadata.builder()
                .latestMajorVersion(true)
                .type(TsBasEntryPoint.MODULE_ID)
                .build()
            );

            final var actualResourceLink = sendCertificateToFK.get(TS_BAS);

            assertEquals(expectedResourceLink, actualResourceLink);
        }

        @Test
        void shouldNotAddResourceLinkIfWrongType() {
            final var expectedResourceLink = Optional.empty();

            TS_BAS.setMetadata(CertificateMetadata.builder()
                .type(DoiModuleEntryPoint.MODULE_ID)
                .build()
            );

            final var actualResourceLink = sendCertificateToFK.get(TS_BAS);

            assertEquals(expectedResourceLink, actualResourceLink);
        }

        @Test
        void shouldNotAddResourceLinkIfNotLatestMajorVersion() {
            final var expectedResourceLink = Optional.empty();

            TS_BAS.setMetadata(CertificateMetadata.builder()
                .type(DoiModuleEntryPoint.MODULE_ID)
                .latestMajorVersion(false)
                .build()
            );

            final var actualResourceLink = sendCertificateToFK.get(TS_BAS);

            assertEquals(expectedResourceLink, actualResourceLink);
        }

        @Test
        void shouldNotAddResourceLinkIfCertificateIsSent() {
            final var expectedResourceLink = Optional.empty();

            TS_BAS.setMetadata(CertificateMetadata.builder()
                .type(TsBasEntryPoint.MODULE_ID)
                .sent(true)
                .build()
            );

            final var actualResourceLink = sendCertificateToFK.get(TS_BAS);

            assertEquals(expectedResourceLink, actualResourceLink);
        }

        @Test
        void shouldNotAddResourceLinkIfCertificateIsReplacedAndSigned() {
            final var expectedResourceLink = Optional.empty();

            TS_BAS.setMetadata(CertificateMetadata.builder()
                .type(TsBasEntryPoint.MODULE_ID)
                .relations(CertificateRelations.builder()
                    .children(new CertificateRelation[]{
                        CertificateRelation.builder()
                            .type(CertificateRelationType.REPLACED)
                            .status(CertificateStatus.SIGNED)
                            .build()
                    }).build())
                .build()
            );

            final var actualResourceLink = sendCertificateToFK.get(TS_BAS);

            assertEquals(expectedResourceLink, actualResourceLink);
        }

        @Test
        void shouldAddResourceLinkIfCertificateIsReplacedAndNotSigned() {
            final var expectedResourceLink = Optional.of(
                ResourceLinkDTO.create(
                    ResourceLinkTypeDTO.SEND_CERTIFICATE,
                    "Skicka till Transportstyrelsen",
                    "Öppnar ett fönster där du kan välja att skicka intyget till Transportstyrelsen.",
                    "<p>Om du går vidare kommer intyget skickas direkt till "
                        + "Transportstyrelsens system vilket ska göras i samråd med patienten.</p>",
                    true));

            TS_BAS.setMetadata(CertificateMetadata.builder()
                .type(TsBasEntryPoint.MODULE_ID)
                .latestMajorVersion(true)
                .relations(CertificateRelations.builder()
                    .children(new CertificateRelation[]{
                        CertificateRelation.builder()
                            .type(CertificateRelationType.REPLACED)
                            .status(CertificateStatus.UNSIGNED)
                            .build()
                    }).build())
                .build()
            );

            final var actualResourceLink = sendCertificateToFK.get(TS_BAS);

            assertEquals(expectedResourceLink, actualResourceLink);
        }
    }
}