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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.inera.intyg.webcert.web.service.facade.ResourceLinkFacadeTestHelper.assertInclude;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.luae_fs.support.LuaefsEntryPoint;
import se.inera.intyg.common.luae_na.support.LuaenaEntryPoint;
import se.inera.intyg.common.luse.support.LuseEntryPoint;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.common.ts_bas.support.TsBasEntryPoint;
import se.inera.intyg.common.ts_diabetes.support.TsDiabetesEntryPoint;
import se.inera.intyg.webcert.web.service.facade.CertificateFacadeTestHelper;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@ExtendWith(MockitoExtension.class)
class SendCertificateFunctionImplTest {

    @InjectMocks
    SendCertificateFunctionImpl sendCertificateFunction;

    @Nested
    class LuaeNa {

        private final Certificate luaeNa = new Certificate();
        private final Certificate replacedLuaeNa = new Certificate();
        private final Certificate sentLuaeNa = new Certificate();

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

            luaeNa.setMetadata(CertificateMetadata.builder()
                .type(LuaenaEntryPoint.MODULE_ID)
                .build()
            );

            final var actualResourceLink = sendCertificateFunction.get(luaeNa);

            assertEquals(expectedResourceLink, actualResourceLink);
        }

        @Test
        void shouldNotAddResourceLinkIfWrongType() {
            final var expectedResourceLink = Optional.empty();

            luaeNa.setMetadata(CertificateMetadata.builder()
                .type(DoiModuleEntryPoint.MODULE_ID)
                .build()
            );

            final var actualResourceLink = sendCertificateFunction.get(luaeNa);

            assertEquals(expectedResourceLink, actualResourceLink);
        }

        @Test
        void shouldNotAddResourceLinkIfCertificateIsSent() {
            final var expectedResourceLink = Optional.empty();

            sentLuaeNa.setMetadata(CertificateMetadata.builder()
                .type(LuaenaEntryPoint.MODULE_ID)
                .sent(true)
                .build()
            );

            final var actualResourceLink = sendCertificateFunction.get(sentLuaeNa);

            assertEquals(expectedResourceLink, actualResourceLink);
        }

        @Test
        void shouldNotAddResourceLinkIfCertificateIsReplacedAndSigned() {
            final var expectedResourceLink = Optional.empty();

            replacedLuaeNa.setMetadata(CertificateMetadata.builder()
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

            final var actualResourceLink = sendCertificateFunction.get(replacedLuaeNa);

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

            replacedLuaeNa.setMetadata(CertificateMetadata.builder()
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

            final var actualResourceLink = sendCertificateFunction.get(replacedLuaeNa);

            assertEquals(expectedResourceLink, actualResourceLink);
        }
    }

    @Nested
    class Lisjp {

        @Test
        void shallIncludeSend() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualAvailableFunctions = sendCertificateFunction.get(certificate);
            assertInclude(List.of(actualAvailableFunctions.get()), ResourceLinkTypeDTO.SEND_CERTIFICATE);
        }

        @Test
        void shallIncludeSendWithWarningIfSickleavePeriodIsShorterThan15Days() {
            final var certificate = CertificateFacadeTestHelper.createCertificateWithSickleavePeriod(14);
            final var actualAvailableFunctions = sendCertificateFunction.get(certificate);
            assertInclude(List.of(actualAvailableFunctions.get()), ResourceLinkTypeDTO.SEND_CERTIFICATE);
            assertTrue(actualAvailableFunctions
                .stream()
                .anyMatch(link -> link.getBody() != null && link.getBody()
                    .contains("Om sjukperioden är kortare än 15 dagar ska intyget inte skickas")));
        }

        @Test
        void shallNotIncludeSendWithWarningIfSickleavePeriodIs15Days() {
            final var certificate = CertificateFacadeTestHelper.createCertificateWithSickleavePeriod(15);
            final var actualAvailableFunctions = sendCertificateFunction.get(certificate);
            assertInclude(List.of(actualAvailableFunctions.get()), ResourceLinkTypeDTO.SEND_CERTIFICATE);
            assertFalse(actualAvailableFunctions
                .stream()
                .anyMatch(link ->
                    link.getBody() != null && link.getBody().contains("Om sjukperioden är kortare än 15 dagar ska intyget inte skickas")));
        }

        @Test
        void shallNotIncludeSendWithWarningIfSickleavePeriodIsLongerThan15Days() {
            final var certificate = CertificateFacadeTestHelper.createCertificateWithSickleavePeriod(100);
            final var actualAvailableFunctions = sendCertificateFunction.get(certificate);
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
            final var actualAvailableFunctions = sendCertificateFunction.get(certificate);
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
            final var actualAvailableFunctions = sendCertificateFunction.get(certificate);
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

            final var actualAvailableFunctions = sendCertificateFunction.get(certificate);
            assertInclude(List.of(actualAvailableFunctions.get()), ResourceLinkTypeDTO.SEND_CERTIFICATE);
        }
    }

    @Nested
    class TsBas {

        private Certificate tsBas = new Certificate();

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

            tsBas.setMetadata(CertificateMetadata.builder()
                .latestMajorVersion(true)
                .type(TsBasEntryPoint.MODULE_ID)
                .build()
            );

            final var actualResourceLink = sendCertificateFunction.get(tsBas);

            assertEquals(expectedResourceLink, actualResourceLink);
        }

        @Test
        void shouldNotAddResourceLinkIfWrongType() {
            final var expectedResourceLink = Optional.empty();

            tsBas.setMetadata(CertificateMetadata.builder()
                .type(DoiModuleEntryPoint.MODULE_ID)
                .build()
            );

            final var actualResourceLink = sendCertificateFunction.get(tsBas);

            assertEquals(expectedResourceLink, actualResourceLink);
        }

        @Test
        void shouldNotAddResourceLinkIfNotLatestMajorVersion() {
            final var expectedResourceLink = Optional.empty();

            tsBas.setMetadata(CertificateMetadata.builder()
                .type(DoiModuleEntryPoint.MODULE_ID)
                .latestMajorVersion(false)
                .build()
            );

            final var actualResourceLink = sendCertificateFunction.get(tsBas);

            assertEquals(expectedResourceLink, actualResourceLink);
        }

        @Test
        void shouldNotAddResourceLinkIfCertificateIsSent() {
            final var expectedResourceLink = Optional.empty();

            tsBas.setMetadata(CertificateMetadata.builder()
                .type(TsBasEntryPoint.MODULE_ID)
                .sent(true)
                .build()
            );

            final var actualResourceLink = sendCertificateFunction.get(tsBas);

            assertEquals(expectedResourceLink, actualResourceLink);
        }

        @Test
        void shouldNotAddResourceLinkIfCertificateIsReplacedAndSigned() {
            final var expectedResourceLink = Optional.empty();

            tsBas.setMetadata(CertificateMetadata.builder()
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

            final var actualResourceLink = sendCertificateFunction.get(tsBas);

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

            tsBas.setMetadata(CertificateMetadata.builder()
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

            final var actualResourceLink = sendCertificateFunction.get(tsBas);

            assertEquals(expectedResourceLink, actualResourceLink);
        }
    }

    @Nested
    class Luse {

        private final Certificate luse = new Certificate();

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

            luse.setMetadata(CertificateMetadata.builder()
                .type(LuseEntryPoint.MODULE_ID)
                .build()
            );

            final var actualResourceLink = sendCertificateFunction.get(luse);

            assertEquals(expectedResourceLink, actualResourceLink);
        }

        @Test
        void shouldNotAddResourceLinkIfWrongType() {
            final var expectedResourceLink = Optional.empty();

            luse.setMetadata(CertificateMetadata.builder()
                .type(DoiModuleEntryPoint.MODULE_ID)
                .build()
            );

            final var actualResourceLink = sendCertificateFunction.get(luse);

            assertEquals(expectedResourceLink, actualResourceLink);
        }

        @Test
        void shouldNotAddResourceLinkIfCertificateIsSent() {
            final var expectedResourceLink = Optional.empty();

            luse.setMetadata(CertificateMetadata.builder()
                .type(LuseEntryPoint.MODULE_ID)
                .sent(true)
                .build()
            );

            final var actualResourceLink = sendCertificateFunction.get(luse);

            assertEquals(expectedResourceLink, actualResourceLink);
        }

        @Test
        void shouldNotAddResourceLinkIfCertificateIsReplacedAndSigned() {
            final var expectedResourceLink = Optional.empty();

            luse.setMetadata(CertificateMetadata.builder()
                .type(LuseEntryPoint.MODULE_ID)
                .relations(CertificateRelations.builder()
                    .children(new CertificateRelation[]{
                        CertificateRelation.builder()
                            .type(CertificateRelationType.REPLACED)
                            .status(CertificateStatus.SIGNED)
                            .build()
                    }).build())
                .build()
            );

            final var actualResourceLink = sendCertificateFunction.get(luse);

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

            luse.setMetadata(CertificateMetadata.builder()
                .type(LuseEntryPoint.MODULE_ID)
                .relations(CertificateRelations.builder()
                    .children(new CertificateRelation[]{
                        CertificateRelation.builder()
                            .type(CertificateRelationType.REPLACED)
                            .status(CertificateStatus.UNSIGNED)
                            .build()
                    }).build())
                .build()
            );

            final var actualResourceLink = sendCertificateFunction.get(luse);

            assertEquals(expectedResourceLink, actualResourceLink);
        }
    }

    @Nested
    class LuaeFs {

        private final Certificate luaeFs = new Certificate();

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

            luaeFs.setMetadata(CertificateMetadata.builder()
                .type(LuaefsEntryPoint.MODULE_ID)
                .build()
            );

            final var actualResourceLink = sendCertificateFunction.get(luaeFs);

            assertEquals(expectedResourceLink, actualResourceLink);
        }

        @Test
        void shouldNotAddResourceLinkIfCertificateIsSent() {
            final var expectedResourceLink = Optional.empty();

            luaeFs.setMetadata(CertificateMetadata.builder()
                .type(LuaefsEntryPoint.MODULE_ID)
                .sent(true)
                .build()
            );

            final var actualResourceLink = sendCertificateFunction.get(luaeFs);

            assertEquals(expectedResourceLink, actualResourceLink);
        }

        @Test
        void shouldNotAddResourceLinkIfCertificateIsReplacedAndSigned() {
            final var expectedResourceLink = Optional.empty();

            luaeFs.setMetadata(CertificateMetadata.builder()
                .type(LuaefsEntryPoint.MODULE_ID)
                .relations(CertificateRelations.builder()
                    .children(new CertificateRelation[]{
                        CertificateRelation.builder()
                            .type(CertificateRelationType.REPLACED)
                            .status(CertificateStatus.SIGNED)
                            .build()
                    }).build())
                .build()
            );

            final var actualResourceLink = sendCertificateFunction.get(luaeFs);

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

            luaeFs.setMetadata(CertificateMetadata.builder()
                .type(LuaefsEntryPoint.MODULE_ID)
                .relations(CertificateRelations.builder()
                    .children(new CertificateRelation[]{
                        CertificateRelation.builder()
                            .type(CertificateRelationType.REPLACED)
                            .status(CertificateStatus.UNSIGNED)
                            .build()
                    }).build())
                .build()
            );

            final var actualResourceLink = sendCertificateFunction.get(luaeFs);

            assertEquals(expectedResourceLink, actualResourceLink);
        }
    }

    @Nested
    class TsDiabetesV4 {

        private Certificate tsDiabetes = new Certificate();

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

            tsDiabetes.setMetadata(CertificateMetadata.builder()
                .latestMajorVersion(true)
                .type(TsDiabetesEntryPoint.MODULE_ID)
                .build()
            );

            final var actualResourceLink = sendCertificateFunction.get(tsDiabetes);

            assertEquals(expectedResourceLink, actualResourceLink);
        }

        @Test
        void shouldNotAddResourceLinkIfWrongType() {
            final var expectedResourceLink = Optional.empty();

            tsDiabetes.setMetadata(CertificateMetadata.builder()
                .type(DoiModuleEntryPoint.MODULE_ID)
                .build()
            );

            final var actualResourceLink = sendCertificateFunction.get(tsDiabetes);

            assertEquals(expectedResourceLink, actualResourceLink);
        }

        @Test
        void shouldNotAddResourceLinkIfNotLatestMajorVersion() {
            final var expectedResourceLink = Optional.empty();

            tsDiabetes.setMetadata(CertificateMetadata.builder()
                .type(TsDiabetesEntryPoint.MODULE_ID)
                .latestMajorVersion(false)
                .build()
            );

            final var actualResourceLink = sendCertificateFunction.get(tsDiabetes);

            assertEquals(expectedResourceLink, actualResourceLink);
        }

        @Test
        void shouldNotAddResourceLinkIfCertificateIsSent() {
            final var expectedResourceLink = Optional.empty();

            tsDiabetes.setMetadata(CertificateMetadata.builder()
                .type(TsDiabetesEntryPoint.MODULE_ID)
                .sent(true)
                .build()
            );

            final var actualResourceLink = sendCertificateFunction.get(tsDiabetes);

            assertEquals(expectedResourceLink, actualResourceLink);
        }

        @Test
        void shouldNotAddResourceLinkIfCertificateIsReplacedAndSigned() {
            final var expectedResourceLink = Optional.empty();

            tsDiabetes.setMetadata(CertificateMetadata.builder()
                .type(TsDiabetesEntryPoint.MODULE_ID)
                .relations(CertificateRelations.builder()
                    .children(new CertificateRelation[]{
                        CertificateRelation.builder()
                            .type(CertificateRelationType.REPLACED)
                            .status(CertificateStatus.SIGNED)
                            .build()
                    }).build())
                .build()
            );

            final var actualResourceLink = sendCertificateFunction.get(tsDiabetes);

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

            tsDiabetes.setMetadata(CertificateMetadata.builder()
                .type(TsDiabetesEntryPoint.MODULE_ID)
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

            final var actualResourceLink = sendCertificateFunction.get(tsDiabetes);

            assertEquals(expectedResourceLink, actualResourceLink);
        }
    }
}
