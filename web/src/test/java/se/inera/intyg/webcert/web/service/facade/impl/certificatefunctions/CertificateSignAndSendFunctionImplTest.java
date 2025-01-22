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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.af00213.support.Af00213EntryPoint;
import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.luae_fs.support.LuaefsEntryPoint;
import se.inera.intyg.common.luae_na.support.LuaenaEntryPoint;
import se.inera.intyg.common.luse.support.LuseEntryPoint;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.common.ts_bas.support.TsBasEntryPoint;
import se.inera.intyg.common.ts_diabetes.support.TsDiabetesEntryPoint;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.webcert.web.service.facade.CertificateFacadeTestHelper;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@ExtendWith(MockitoExtension.class)
class CertificateSignAndSendFunctionImplTest {

    private static final String SIGN_AND_SEND_DESCRIPTION_ARBETSFORMEDLINGEN = "Intyget skickas direkt till Arbetsförmedlingen.";
    private static final String SIGN_AND_SEND_DESCRIPTION_FORSAKRINGSKASSAN = "Intyget skickas direkt till Försäkringskassan.";
    private static final String SIGN_AND_SEND_DESCRIPTION_SKATTEVERKET = "Intyget skickas direkt till Skatteverket.";
    private static final String SIGN_AND_SEND_DESCRIPTION_SOCIALSTYRELSEN = "Intyget skickas direkt till Socialstyrelsen.";
    private static final String SIGN_AND_SEND_DESCRIPTION_TRANSPORTSTYRELSEN = "Intyget skickas direkt till Transportstyrelsen.";
    private static final String SIGN_AND_SEND_NAME = "Signera och skicka";
    private static final String SIGN_NAME = "Signera intyget";

    private final WebCertUserService webCertUserService = mock(WebCertUserService.class);

    @Mock
    private AuthoritiesHelper authoritiesHelper;

    @InjectMocks
    private CertificateSignAndSendFunctionImpl certificateSignAndSendFunction;

    @Nested
    class TestType {

        @Nested
        class HasActiveFeature {

            Certificate certificate;

            @BeforeEach
            void setup() {
                when(authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_SIGNERA_SKICKA_DIREKT,
                    Af00213EntryPoint.MODULE_ID)).thenReturn(true);

                certificate = CertificateFacadeTestHelper.createCertificate(Af00213EntryPoint.MODULE_ID,
                    CertificateStatus.UNSIGNED);
            }

            @Test
            void shallIncludeSignAndSendCertificateIfRealPatient() {
                certificate.getMetadata().setPatient(Patient.builder().testIndicated(false).build());

                final var function = certificateSignAndSendFunction.get(certificate);

                assertEquals(function.get().getName(), SIGN_AND_SEND_NAME);
                assertEquals(function.get().getType(), ResourceLinkTypeDTO.SIGN_CERTIFICATE);
            }

            @Test
            void shallIncludeSignCertificateIfTestPatient() {
                certificate.getMetadata().setPatient(Patient.builder().testIndicated(true).build());

                final var function = certificateSignAndSendFunction.get(certificate);

                assertEquals(function.get().getName(), SIGN_NAME);
                assertEquals(function.get().getType(), ResourceLinkTypeDTO.SIGN_CERTIFICATE);
            }
        }

        @Nested
        class HasNoActiveFeatureAndIsNotComplementing {

            Certificate certificate;

            @BeforeEach
            void setup() {
                when(authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_SIGNERA_SKICKA_DIREKT,
                    Af00213EntryPoint.MODULE_ID)).thenReturn(false);

                certificate = CertificateFacadeTestHelper.createCertificate(Af00213EntryPoint.MODULE_ID,
                    CertificateStatus.UNSIGNED);
            }

            @Test
            void shallIncludeSignIfRealPatient() {
                final var function = certificateSignAndSendFunction.get(certificate);

                assertEquals(function.get().getName(), SIGN_NAME);
                assertEquals(function.get().getType(), ResourceLinkTypeDTO.SIGN_CERTIFICATE);
            }

            @Test
            void shallIncludeSignCertificateIfTestPatient() {
                certificate.getMetadata().setPatient(Patient.builder().testIndicated(true).build());

                final var function = certificateSignAndSendFunction.get(certificate);

                assertEquals(function.get().getName(), SIGN_NAME);
                assertEquals(function.get().getType(), ResourceLinkTypeDTO.SIGN_CERTIFICATE);
            }
        }

        @Nested
        class IsComplementingCertificate {

            CertificateRelation relation;

            Certificate certificate;

            @BeforeEach
            void setup() {

                when(authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_SIGNERA_SKICKA_DIREKT,
                    LisjpEntryPoint.MODULE_ID)).thenReturn(false);

                relation = CertificateRelation.builder()
                    .type(CertificateRelationType.COMPLEMENTED)
                    .build();

                certificate = CertificateFacadeTestHelper
                    .createCertificateWithParentRelation(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED, relation);
            }

            @Test
            void shallIncludeSignAndSendIfComplementingAndRealPatient() {
                certificate.getMetadata().setPatient(Patient.builder().testIndicated(false).build());

                final var function = certificateSignAndSendFunction.get(certificate);

                assertEquals(function.get().getName(), SIGN_AND_SEND_NAME);
                assertEquals(function.get().getType(), ResourceLinkTypeDTO.SIGN_CERTIFICATE);
            }

            @Test
            void shallIncludeSignIfComplementingAndTestPatient() {
                certificate.getMetadata().setPatient(Patient.builder().testIndicated(true).build());

                final var function = certificateSignAndSendFunction.get(certificate);

                assertEquals(function.get().getName(), SIGN_NAME);
                assertEquals(function.get().getType(), ResourceLinkTypeDTO.SIGN_CERTIFICATE);
            }
        }
    }

    @Nested
    class TestDescription {

        private void setupFeature(String id) {
            when(authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_SIGNERA_SKICKA_DIREKT, id))
                .thenReturn(true);
        }

        private WebCertUser getUserWithOrigin() {
            WebCertUser user = new WebCertUser();
            user.setOrigin("DJUPINTEGRATION");
            return user;
        }

        @Test
        void shallIncludeFunctionWithDescriptionSkatteverketWhenCertificateTypeIsDb() {
            setupFeature(DbModuleEntryPoint.MODULE_ID);
            when(webCertUserService.getUser()).thenReturn(getUserWithOrigin());
            final var certificate = CertificateFacadeTestHelper.createCertificate(DbModuleEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);

            final var function = certificateSignAndSendFunction.get(certificate);

            assertEquals(function.get().getDescription(), SIGN_AND_SEND_DESCRIPTION_SKATTEVERKET);
            assertEquals(function.get().getType(), ResourceLinkTypeDTO.SIGN_CERTIFICATE);
        }

        @Test
        void shallIncludeFunctionWithDescriptionSocialstyrelsenWhenCertificateTypeIsDoi() {
            setupFeature(DoiModuleEntryPoint.MODULE_ID);
            when(webCertUserService.getUser()).thenReturn(getUserWithOrigin());
            final var certificate = CertificateFacadeTestHelper.createCertificate(DoiModuleEntryPoint.MODULE_ID,
                CertificateStatus.UNSIGNED);

            final var function = certificateSignAndSendFunction.get(certificate);

            assertEquals(function.get().getDescription(), SIGN_AND_SEND_DESCRIPTION_SOCIALSTYRELSEN);
            assertEquals(function.get().getType(), ResourceLinkTypeDTO.SIGN_CERTIFICATE);
        }

        @Test
        void shallIncludeFunctionWithDescriptionArbetsformedlingenWhenCertificateTypeIsAf00213() {
            setupFeature(Af00213EntryPoint.MODULE_ID);
            final var certificate = CertificateFacadeTestHelper.createCertificate(Af00213EntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);

            final var function = certificateSignAndSendFunction.get(certificate);

            assertEquals(function.get().getDescription(), SIGN_AND_SEND_DESCRIPTION_ARBETSFORMEDLINGEN);
            assertEquals(function.get().getType(), ResourceLinkTypeDTO.SIGN_CERTIFICATE);
        }

        @Nested
        class Forsakringskassan {

            @Test
            void shallIncludeFunctionWithDescriptionForsakringskassanWhenCertificateTypeIsFk7263() {
                setupFeature(Fk7263EntryPoint.MODULE_ID);
                final var certificate = CertificateFacadeTestHelper.createCertificate(Fk7263EntryPoint.MODULE_ID,
                    CertificateStatus.UNSIGNED);

                final var function = certificateSignAndSendFunction.get(certificate);

                assertEquals(function.get().getDescription(), SIGN_AND_SEND_DESCRIPTION_FORSAKRINGSKASSAN);
                assertEquals(function.get().getType(), ResourceLinkTypeDTO.SIGN_CERTIFICATE);
            }

            @Test
            void shallIncludeFunctionWithDescriptionForsakringskassanWhenCertificateTypeIsLuse() {
                setupFeature(LuseEntryPoint.MODULE_ID);
                final var certificate = CertificateFacadeTestHelper.createCertificate(LuseEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);

                final var function = certificateSignAndSendFunction.get(certificate);

                assertEquals(function.get().getDescription(), SIGN_AND_SEND_DESCRIPTION_FORSAKRINGSKASSAN);
                assertEquals(function.get().getType(), ResourceLinkTypeDTO.SIGN_CERTIFICATE);
            }

            @Test
            void shallIncludeFunctionWithDescriptionForsakringskassanWhenCertificateTypeIsLisjp() {
                setupFeature(LisjpEntryPoint.MODULE_ID);
                final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID,
                    CertificateStatus.UNSIGNED);

                final var function = certificateSignAndSendFunction.get(certificate);

                assertEquals(function.get().getDescription(), SIGN_AND_SEND_DESCRIPTION_FORSAKRINGSKASSAN);
                assertEquals(function.get().getType(), ResourceLinkTypeDTO.SIGN_CERTIFICATE);
            }

            @Test
            void shallIncludeFunctionWithDescriptionForsakringskassanWhenCertificateTypeIsLuaefs() {
                setupFeature(LuaefsEntryPoint.MODULE_ID);
                final var certificate = CertificateFacadeTestHelper.createCertificate(LuaefsEntryPoint.MODULE_ID,
                    CertificateStatus.UNSIGNED);

                final var function = certificateSignAndSendFunction.get(certificate);

                assertEquals(function.get().getDescription(), SIGN_AND_SEND_DESCRIPTION_FORSAKRINGSKASSAN);
                assertEquals(function.get().getType(), ResourceLinkTypeDTO.SIGN_CERTIFICATE);
            }

            @Test
            void shallIncludeFunctionWithDescriptionForsakringskassanWhenCertificateTypeIsLuaena() {
                setupFeature(LuaenaEntryPoint.MODULE_ID);
                final var certificate = CertificateFacadeTestHelper.createCertificate(LuaenaEntryPoint.MODULE_ID,
                    CertificateStatus.UNSIGNED);

                final var function = certificateSignAndSendFunction.get(certificate);

                assertEquals(function.get().getDescription(), SIGN_AND_SEND_DESCRIPTION_FORSAKRINGSKASSAN);
                assertEquals(function.get().getType(), ResourceLinkTypeDTO.SIGN_CERTIFICATE);
            }
        }

        @Nested
        class Transportstyrelsen {

            @Test
            void shallIncludeFunctionWithDescriptionTransportstyrelsenWhenCertificateTypeIsTsBas() {
                setupFeature(TsBasEntryPoint.MODULE_ID);
                final var certificate = CertificateFacadeTestHelper.createCertificate(TsBasEntryPoint.MODULE_ID,
                    CertificateStatus.UNSIGNED);

                final var function = certificateSignAndSendFunction.get(certificate);

                assertEquals(function.get().getDescription(), SIGN_AND_SEND_DESCRIPTION_TRANSPORTSTYRELSEN);
                assertEquals(function.get().getType(), ResourceLinkTypeDTO.SIGN_CERTIFICATE);
            }

            @Test
            void shallIncludeFunctionWithDescriptionTransportstyrelsenWhenCertificateTypeIsTsDiabetes() {
                setupFeature(TsDiabetesEntryPoint.MODULE_ID);
                final var certificate = CertificateFacadeTestHelper.createCertificate(TsDiabetesEntryPoint.MODULE_ID,
                    CertificateStatus.UNSIGNED);

                final var function = certificateSignAndSendFunction.get(certificate);

                assertEquals(function.get().getDescription(), SIGN_AND_SEND_DESCRIPTION_TRANSPORTSTYRELSEN);
                assertEquals(function.get().getType(), ResourceLinkTypeDTO.SIGN_CERTIFICATE);
            }
        }

        @Test
        void shallIncludeFunctionWithNullDescriptionWhenCertificateTypeIsInvalid() {
            setupFeature("InvalidId");
            final var certificate = CertificateFacadeTestHelper.createCertificate("InvalidId", CertificateStatus.UNSIGNED);

            final var function = certificateSignAndSendFunction.get(certificate);

            assertNull(function.get().getDescription());
            assertEquals(function.get().getType(), ResourceLinkTypeDTO.SIGN_CERTIFICATE);
        }
    }

}

