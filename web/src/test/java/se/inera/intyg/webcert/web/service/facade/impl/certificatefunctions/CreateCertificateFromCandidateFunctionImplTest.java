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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.ag7804.support.Ag7804EntryPoint;
import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.facade.CertificateFacadeTestHelper;
import se.inera.intyg.webcert.web.service.facade.util.CandidateDataHelper;
import se.inera.intyg.webcert.web.service.utkast.dto.UtkastCandidateMetaData;
import se.inera.intyg.webcert.web.service.utkast.dto.UtkastCandidateMetaData.Builder;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@ExtendWith(MockitoExtension.class)
class CreateCertificateFromCandidateFunctionImplTest {

    @Mock
    private CandidateDataHelper candidateDataHelper;

    @InjectMocks
    private CreateCertificateFromCandidateFunctionImpl createCertificateFromCandidateFunction;

    @Nested
    class Fk7804CandidateForAg7804 {

        @Test
        void shallIncludeCreateCertificateFromCandidateWhenCandidateExists() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(Ag7804EntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);

            doReturn(Optional.of(createCandidateMetaData(LisjpEntryPoint.MODULE_ID, LocalDateTime.now())))
                .when(candidateDataHelper)
                .getCandidateMetadata(anyString(), anyString(), any(Personnummer.class));

            final var actualLink = createCertificateFromCandidateFunction.get(certificate);
            assertTrue(actualLink.isPresent());
        }

        @Test
        void shallExcludeCreateCertificateFromCandidateIfNoCandidate() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(Ag7804EntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            final var actualLink = createCertificateFromCandidateFunction.get(certificate);
            assertTrue(actualLink.isEmpty());
        }

        @Test
        void shallExcludeCreateCertificateFromCandidateIfNotVersion0() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(Ag7804EntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            certificate.getMetadata().setVersion(1);
            final var actualLink = createCertificateFromCandidateFunction.get(certificate);
            assertTrue(actualLink.isEmpty());
        }

        @Test
        void shallExcludeCreateCertificateFromCandidateIfParentRelationExists() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(Ag7804EntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            certificate.getMetadata().setRelations(CertificateRelations.builder().parent(CertificateRelation.builder().build()).build());
            final var actualLink = createCertificateFromCandidateFunction.get(certificate);
            assertTrue(actualLink.isEmpty());
        }

        @Test
        void shallIncludeResourceLinkDTOType() {
            final var expectedType = ResourceLinkTypeDTO.CREATE_CERTIFICATE_FROM_CANDIDATE;
            final var certificate = CertificateFacadeTestHelper.createCertificate(Ag7804EntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);

            doReturn(Optional.of(createCandidateMetaData(LisjpEntryPoint.MODULE_ID, LocalDateTime.now())))
                .when(candidateDataHelper)
                .getCandidateMetadata(anyString(), anyString(), any(Personnummer.class));

            final var actualLink = createCertificateFromCandidateFunction.get(certificate);
            assertEquals(expectedType, actualLink.get().getType());
        }

        @Test
        void shallIncludeResourceLinkDTOName() {
            final var expectedName = "Hjälp med ifyllnad?";
            final var certificate = CertificateFacadeTestHelper.createCertificate(Ag7804EntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);

            doReturn(Optional.of(createCandidateMetaData(LisjpEntryPoint.MODULE_ID, LocalDateTime.now())))
                .when(candidateDataHelper)
                .getCandidateMetadata(anyString(), anyString(), any(Personnummer.class));

            final var actualLink = createCertificateFromCandidateFunction.get(certificate);
            assertEquals(expectedName, actualLink.get().getName());
        }

        @Test
        void shallIncludeResourceLinkDTOBody() {
            final var expectedBody = "<p>Det finns ett Läkarintyg för sjukpenning för denna patient som är utfärdat "
                + "<span class='iu-fw-bold'>2022-01-01</span> "
                + "på en enhet som du har åtkomst till. Vill du kopiera de svar som givits i det intyget till detta intygsutkast?</p>";
            final var certificate = CertificateFacadeTestHelper.createCertificate(Ag7804EntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);

            doReturn(Optional.of(
                createCandidateMetaData(LisjpEntryPoint.MODULE_ID, LocalDateTime.of(LocalDate.parse("2022-01-01"), LocalTime.now()))))
                .when(candidateDataHelper)
                .getCandidateMetadata(anyString(), anyString(), any(Personnummer.class));

            final var actualLink = createCertificateFromCandidateFunction.get(certificate);
            assertEquals(expectedBody, actualLink.get().getBody());
        }

        @Test
        void shallIncludeResourceLinkDTOEnabled() {
            final var expectedEnabled = true;
            final var certificate = CertificateFacadeTestHelper.createCertificate(Ag7804EntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);

            doReturn(Optional.of(createCandidateMetaData(LisjpEntryPoint.MODULE_ID, LocalDateTime.now())))
                .when(candidateDataHelper)
                .getCandidateMetadata(anyString(), anyString(), any(Personnummer.class));

            final var actualLink = createCertificateFromCandidateFunction.get(certificate);
            assertEquals(expectedEnabled, actualLink.get().isEnabled());
        }
    }

    @Nested
    class DbCandidateForDoi {

        @Test
        void shallIncludeCreateCertificateFromCandidateWhenCandidateExists() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(DoiModuleEntryPoint.MODULE_ID,
                CertificateStatus.UNSIGNED);

            doReturn(Optional.of(createCandidateMetaData(DbModuleEntryPoint.MODULE_ID, LocalDateTime.now())))
                .when(candidateDataHelper)
                .getCandidateMetadata(anyString(), anyString(), any(Personnummer.class));

            final var actualLink = createCertificateFromCandidateFunction.get(certificate);
            assertTrue(actualLink.isPresent());
        }

        @Test
        void shallExcludeCreateCertificateFromCandidateIfNoCandidate() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(DoiModuleEntryPoint.MODULE_ID,
                CertificateStatus.UNSIGNED);
            final var actualLink = createCertificateFromCandidateFunction.get(certificate);
            assertTrue(actualLink.isEmpty());
        }

        @Test
        void shallExcludeCreateCertificateFromCandidateIfNotVersion0() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(DoiModuleEntryPoint.MODULE_ID,
                CertificateStatus.UNSIGNED);
            certificate.getMetadata().setVersion(1);
            final var actualLink = createCertificateFromCandidateFunction.get(certificate);
            assertTrue(actualLink.isEmpty());
        }

        @Test
        void shallExcludeCreateCertificateFromCandidateIfParentRelationExists() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(DoiModuleEntryPoint.MODULE_ID,
                CertificateStatus.UNSIGNED);
            certificate.getMetadata().setRelations(CertificateRelations.builder().parent(CertificateRelation.builder().build()).build());
            final var actualLink = createCertificateFromCandidateFunction.get(certificate);
            assertTrue(actualLink.isEmpty());
        }

        @Test
        void shallIncludeResourceLinkDTOType() {
            final var expectedType = ResourceLinkTypeDTO.CREATE_CERTIFICATE_FROM_CANDIDATE;
            final var certificate = CertificateFacadeTestHelper.createCertificate(DoiModuleEntryPoint.MODULE_ID,
                CertificateStatus.UNSIGNED);

            doReturn(Optional.of(createCandidateMetaData(DbModuleEntryPoint.MODULE_ID, LocalDateTime.now())))
                .when(candidateDataHelper)
                .getCandidateMetadata(anyString(), anyString(), any(Personnummer.class));

            final var actualLink = createCertificateFromCandidateFunction.get(certificate);
            assertEquals(expectedType, actualLink.get().getType());
        }

        @Test
        void shallIncludeResourceLinkDTOName() {
            final var expectedName = "Hjälp med ifyllnad?";
            final var certificate = CertificateFacadeTestHelper.createCertificate(DoiModuleEntryPoint.MODULE_ID,
                CertificateStatus.UNSIGNED);

            doReturn(Optional.of(createCandidateMetaData(DbModuleEntryPoint.MODULE_ID, LocalDateTime.now())))
                .when(candidateDataHelper)
                .getCandidateMetadata(anyString(), anyString(), any(Personnummer.class));

            final var actualLink = createCertificateFromCandidateFunction.get(certificate);
            assertEquals(expectedName, actualLink.get().getName());
        }

        @Test
        void shallIncludeResourceLinkDTOBody() {
            final var expectedBody = "<p>Det finns ett signerat dödsbevis "
                + "(från den <span class='iu-fw-bold'>2022-01-01</span>) "
                + "för detta personnummer på samma enhet som du är inloggad. "
                + "Vill du kopiera de svar som givits i det intyget till detta intygsutkast?</p>";
            final var certificate = CertificateFacadeTestHelper.createCertificate(DoiModuleEntryPoint.MODULE_ID,
                CertificateStatus.UNSIGNED);

            doReturn(Optional.of(
                createCandidateMetaData(DbModuleEntryPoint.MODULE_ID, LocalDateTime.of(LocalDate.parse("2022-01-01"), LocalTime.now()))))
                .when(candidateDataHelper)
                .getCandidateMetadata(anyString(), anyString(), any(Personnummer.class));

            final var actualLink = createCertificateFromCandidateFunction.get(certificate);
            assertEquals(expectedBody, actualLink.get().getBody());
        }

        @Test
        void shallIncludeResourceLinkDTOEnabled() {
            final var expectedEnabled = true;
            final var certificate = CertificateFacadeTestHelper.createCertificate(DoiModuleEntryPoint.MODULE_ID,
                CertificateStatus.UNSIGNED);

            doReturn(Optional.of(createCandidateMetaData(DbModuleEntryPoint.MODULE_ID, LocalDateTime.now())))
                .when(candidateDataHelper)
                .getCandidateMetadata(anyString(), anyString(), any(Personnummer.class));

            final var actualLink = createCertificateFromCandidateFunction.get(certificate);
            assertEquals(expectedEnabled, actualLink.get().isEnabled());
        }

        @Test
        void shallIncludeResourceLinkCandidateMessage() {
            final var expectedResourceLinkType = ResourceLinkTypeDTO.CREATE_CERTIFICATE_FROM_CANDIDATE_WITH_MESSAGE;
            final var certificate = CertificateFacadeTestHelper.createCertificate(DoiModuleEntryPoint.MODULE_ID,
                CertificateStatus.UNSIGNED);

            doReturn(Optional.of(createCandidateMetaDataNotSameVardenhet(DbModuleEntryPoint.MODULE_ID, LocalDateTime.now())))
                .when(candidateDataHelper)
                .getCandidateMetadata(anyString(), anyString(), any(Personnummer.class));

            final var actualLink = createCertificateFromCandidateFunction.get(certificate);
            assertEquals(expectedResourceLinkType, actualLink.get().getType());
        }
    }

    private UtkastCandidateMetaData createCandidateMetaData(String certificateTypeOfCandidate, LocalDateTime intygCreated) {
        return new Builder()
            .with(builder -> {
                builder.intygType = certificateTypeOfCandidate;
                builder.intygCreated = intygCreated;
                builder.sameVardenhet = true;
            })
            .create();
    }

    private UtkastCandidateMetaData createCandidateMetaDataNotSameVardenhet(String certificateTypeOfCandidate, LocalDateTime intygCreated) {
        return new Builder()
            .with(builder -> {
                builder.intygType = certificateTypeOfCandidate;
                builder.intygCreated = intygCreated;
                builder.sameVardenhet = false;
            })
            .create();
    }
}
