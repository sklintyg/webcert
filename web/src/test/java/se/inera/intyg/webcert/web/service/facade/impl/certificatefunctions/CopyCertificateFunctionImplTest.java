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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static se.inera.intyg.webcert.web.service.utkast.UtkastServiceImpl.UTKAST_INDICATOR;

import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.facade.CertificateFacadeTestHelper;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.PreviousIntyg;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@ExtendWith(MockitoExtension.class)
class CopyCertificateFunctionImplTest {

    @Mock
    private UtkastService utkastService;

    @Mock
    private WebCertUser webCertUser;

    @InjectMocks
    private CopyCertificateFunctionImpl copyCertificateFunction;

    @Test
    void shallIncludeCopyCertificateIfLocked() {
        final var expected = ResourceLinkDTO.create(
            ResourceLinkTypeDTO.COPY_CERTIFICATE,
            "Kopiera",
            "Skapar en redigerbar kopia av utkastet på den enheten du är inloggad på.",
            true
        );

        final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.LOCKED);

        final var actual = copyCertificateFunction.get(certificate, webCertUser).orElseThrow();
        assertEquals(expected, actual);
    }

    @Test
    void shallIncludeCopyContinueCertificate() {
        final var expected = ResourceLinkDTO.create(
            ResourceLinkTypeDTO.COPY_CERTIFICATE_CONTINUE,
            "Kopiera",
            "Skapar en redigerbar kopia av utkastet på den enheten du är inloggad på.",
            true
        );

        final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.LOCKED);
        CertificateRelation copied = CertificateRelation.builder().type(CertificateRelationType.COPIED)
            .status(CertificateStatus.UNSIGNED).build();
        final var children = new CertificateRelation[]{copied};
        certificate.getMetadata().setRelations(CertificateRelations.builder().children(children).build());

        final var actual = copyCertificateFunction.get(certificate, webCertUser).orElseThrow();
        assertEquals(expected, actual);
    }

    @Test
    void shallNotIncludeAnyCopyCertificateIfCertificateIsNotLocked() {
        final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);

        final var actual = copyCertificateFunction.get(certificate, webCertUser);
        assertTrue(actual.isEmpty());
    }

    @Nested
    class CopyCertificateForDb {

        @Test
        void shallIncludeEnabledCopyCertificateIfLocked() {
            final var expected = ResourceLinkDTO.create(
                ResourceLinkTypeDTO.COPY_CERTIFICATE,
                "Kopiera",
                "Skapar en redigerbar kopia av utkastet på den enheten du är inloggad på.",
                true
            );

            final var certificate = CertificateFacadeTestHelper.createCertificate(DbModuleEntryPoint.MODULE_ID, CertificateStatus.LOCKED);

            final var actual = copyCertificateFunction.get(certificate, webCertUser).orElseThrow();
            assertEquals(expected, actual);
        }

        @Test
        void shallIncludeDisabledCopyCertificateIfLockedAndDraftAlreadyExists() {
            final var expected = ResourceLinkDTO.create(
                ResourceLinkTypeDTO.COPY_CERTIFICATE,
                "Kopiera",
                "Det finns ett utkast på dödsbevis för detta personnummer. Du kan inte skapa ett nytt utkast men kan "
                    + "däremot välja att fortsätta med det befintliga utkastet.",
                false
            );

            final var certificate = CertificateFacadeTestHelper.createCertificate(DbModuleEntryPoint.MODULE_ID, CertificateStatus.LOCKED);

            final var previousDraftSameUnit = Map.of(
                UTKAST_INDICATOR,
                Map.of(
                    DbModuleEntryPoint.MODULE_ID,
                    PreviousIntyg.of(true, true, false, "ENHET", "123", LocalDateTime.now())
                )
            );

            doReturn(previousDraftSameUnit)
                .when(utkastService)
                .checkIfPersonHasExistingIntyg(Personnummer.createPersonnummer("191212121212").orElseThrow(), webCertUser,
                    certificate.getMetadata().getId());

            final var actual = copyCertificateFunction.get(certificate, webCertUser).orElseThrow();
            assertEquals(expected, actual);
        }
    }

    @Nested
    class CopyCertificateForDoi {

        @Test
        void shallIncludeEnabledCopyCertificateIfLocked() {
            final var expected = ResourceLinkDTO.create(
                ResourceLinkTypeDTO.COPY_CERTIFICATE,
                "Kopiera",
                "Skapar en redigerbar kopia av utkastet på den enheten du är inloggad på.",
                true
            );

            final var certificate = CertificateFacadeTestHelper.createCertificate(DoiModuleEntryPoint.MODULE_ID, CertificateStatus.LOCKED);

            final var actual = copyCertificateFunction.get(certificate, webCertUser).orElseThrow();
            assertEquals(expected, actual);
        }

        @Test
        void shallIncludeDisabledCopyCertificateIfLockedAndDraftAlreadyExists() {
            final var expected = ResourceLinkDTO.create(
                ResourceLinkTypeDTO.COPY_CERTIFICATE,
                "Kopiera",
                "Det finns ett utkast på dödsorsaksintyg för detta personnummer. Du kan inte skapa ett nytt utkast men kan "
                    + "däremot välja att fortsätta med det befintliga utkastet.",
                false
            );

            final var certificate = CertificateFacadeTestHelper.createCertificate(DoiModuleEntryPoint.MODULE_ID, CertificateStatus.LOCKED);

            final var previousDraftSameUnit = Map.of(
                UTKAST_INDICATOR,
                Map.of(
                    DoiModuleEntryPoint.MODULE_ID,
                    PreviousIntyg.of(true, true, false, "ENHET", "123", LocalDateTime.now())
                )
            );

            doReturn(previousDraftSameUnit)
                .when(utkastService)
                .checkIfPersonHasExistingIntyg(Personnummer.createPersonnummer("191212121212").orElseThrow(), webCertUser,
                    certificate.getMetadata().getId());

            final var actual = copyCertificateFunction.get(certificate, webCertUser).orElseThrow();
            assertEquals(expected, actual);
        }
    }
}