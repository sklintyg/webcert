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
package se.inera.intyg.webcert.web.service.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
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
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.support.facade.builder.CertificateBuilder;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.facade.model.PersonId;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.facade.GetCertificateFacadeService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.PreviousIntyg;

@ExtendWith(MockitoExtension.class)
class GetRelatedCertificateFacadeServiceImplTest {

    private static final String CERTIFICATE_ID = "certificateId";
    private static final String PATIENT_ID = "19121212-1212";
    private static final Personnummer PERSON_NUMMER = Personnummer.createPersonnummer(PATIENT_ID).get();

    @Mock
    private GetCertificateFacadeService getCertificateFacadeService;

    @Mock
    private UtkastService utkastService;

    @Mock
    private WebCertUserService webCertUserService;

    @InjectMocks
    private GetRelatedCertificateFacadeServiceImpl getRelatedCertificateFacadeService;

    @Nested
    class NoRelatedCertificate {

        @BeforeEach
        void setUp() {
            final var certificate = CertificateBuilder.create()
                .metadata(
                    CertificateMetadata.builder()
                        .id(CERTIFICATE_ID)
                        .type(LisjpEntryPoint.MODULE_ID)
                        .patient(
                            Patient.builder()
                                .personId(
                                    PersonId.builder()
                                        .id(PATIENT_ID)
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                .build();
            doReturn(certificate).when(getCertificateFacadeService).getCertificate(CERTIFICATE_ID, false, true);
        }

        @Test
        void shallNotReturnRelatedCertificateIdWhenNotRelevantForACertificateType() {
            final String expectedRelatedCertificateId = null;

            final var actualRelatedCertificateId = getRelatedCertificateFacadeService.get(CERTIFICATE_ID);
            assertEquals(expectedRelatedCertificateId, actualRelatedCertificateId);
        }
    }

    @Nested
    class DoiRelatedToDb {

        private WebCertUser webCertUser;

        @BeforeEach
        void setUp() {
            final var certificate = CertificateBuilder.create()
                .metadata(
                    CertificateMetadata.builder()
                        .id(CERTIFICATE_ID)
                        .type(DbModuleEntryPoint.MODULE_ID)
                        .patient(
                            Patient.builder()
                                .personId(
                                    PersonId.builder()
                                        .id(PATIENT_ID)
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                .build();
            doReturn(certificate).when(getCertificateFacadeService).getCertificate(CERTIFICATE_ID, false, true);

            webCertUser = mock(WebCertUser.class);
            doReturn(webCertUser).when(webCertUserService).getUser();
        }

        @Test
        void shallReturnRelatedCertificateIdIfShowDoiIsSetToTrueOnExistingDoiDraft() {
            final var expectedRelatedCertificateId = "relatedCertificateId";

            final var dbWithinCareProvider = Map.of(
                UTKAST_INDICATOR,
                Map.of(
                    DoiModuleEntryPoint.MODULE_ID,
                    PreviousIntyg.of(true, false, true, "ENHET", expectedRelatedCertificateId, LocalDateTime.now())
                )
            );

            doReturn(dbWithinCareProvider).when(utkastService).checkIfPersonHasExistingIntyg(PERSON_NUMMER, webCertUser, CERTIFICATE_ID);

            final var actualRelatedCertificateId = getRelatedCertificateFacadeService.get(CERTIFICATE_ID);
            assertEquals(expectedRelatedCertificateId, actualRelatedCertificateId);
        }

        @Test
        void shallNotReturnRelatedCertificateIdIfShowDoiIsSetToFalseOnExistingDoiDraft() {
            final String expectedRelatedCertificateId = null;

            final var dbWithinCareProvider = Map.of(
                UTKAST_INDICATOR,
                Map.of(
                    DoiModuleEntryPoint.MODULE_ID,
                    PreviousIntyg.of(false, false, false, "ENHET", "idOnDifferentCareProvider", LocalDateTime.now())
                )
            );

            doReturn(dbWithinCareProvider).when(utkastService).checkIfPersonHasExistingIntyg(PERSON_NUMMER, webCertUser, CERTIFICATE_ID);

            final var actualRelatedCertificateId = getRelatedCertificateFacadeService.get(CERTIFICATE_ID);
            assertEquals(expectedRelatedCertificateId, actualRelatedCertificateId);
        }

        @Test
        void shallReturnRelatedCertificateIdIfShowDoiIsSetToTrueOnExistingDoiCertificate() {
            final var expectedRelatedCertificateId = "relatedCertificateId";

            final var dbWithinCareProvider = Map.of(
                INTYG_INDICATOR,
                Map.of(
                    DoiModuleEntryPoint.MODULE_ID,
                    PreviousIntyg.of(true, false, true, "ENHET", expectedRelatedCertificateId, LocalDateTime.now())
                )
            );

            doReturn(dbWithinCareProvider).when(utkastService).checkIfPersonHasExistingIntyg(PERSON_NUMMER, webCertUser, CERTIFICATE_ID);

            final var actualRelatedCertificateId = getRelatedCertificateFacadeService.get(CERTIFICATE_ID);
            assertEquals(expectedRelatedCertificateId, actualRelatedCertificateId);
        }

        @Test
        void shallNotReturnRelatedCertificateIdIfShowDoiIsSetToFalseOnExistingDoiCertificate() {
            final String expectedRelatedCertificateId = null;

            final var dbWithinCareProvider = Map.of(
                UTKAST_INDICATOR,
                Map.of(
                    DoiModuleEntryPoint.MODULE_ID,
                    PreviousIntyg.of(false, false, false, "ENHET", "idOnDifferentCareProvider", LocalDateTime.now())
                )
            );

            doReturn(dbWithinCareProvider).when(utkastService).checkIfPersonHasExistingIntyg(PERSON_NUMMER, webCertUser, CERTIFICATE_ID);

            final var actualRelatedCertificateId = getRelatedCertificateFacadeService.get(CERTIFICATE_ID);
            assertEquals(expectedRelatedCertificateId, actualRelatedCertificateId);
        }
    }
}
