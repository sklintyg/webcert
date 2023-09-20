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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.ts_bas.support.TsBasEntryPoint;
import se.inera.intyg.common.ts_diabetes.support.TsDiabetesEntryPoint;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.webcert.web.service.facade.CertificateFacadeTestHelper;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@ExtendWith(MockitoExtension.class)
class CertificateSignAndSendDescriptionFunctionImplTest {

    private final WebCertUserService webCertUserService = mock(WebCertUserService.class);

    @Mock
    private AuthoritiesHelper authoritiesHelper;

    @InjectMocks
    private CertificateSignAndSendDescriptionFunctionImpl certificateSignAndSendDescriptionFunction;

    
    @Test
    void shallIncludeSignAndSendCertificateWithDescriptionSkatteverketWhenCertificateTypeIsDb() {
        when(authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_SIGNERA_SKICKA_DIREKT, DbModuleEntryPoint.MODULE_ID))
            .thenReturn(true);
        when(webCertUserService.getUser()).thenReturn(getUserWithOrigin());

        final var certificate = CertificateFacadeTestHelper.createCertificate(DbModuleEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
        final var actualAvailableFunctions = certificateSignAndSendDescriptionFunction.get(certificate);

        assertEquals(actualAvailableFunctions.get().getType(), ResourceLinkTypeDTO.SIGN_CERTIFICATE);
        assertTrue(
            actualAvailableFunctions.stream().anyMatch(r -> r.getDescription().contains("Intyget skickas direkt till Skatteverket.")));
    }

    @Test
    void shallIncludeSignAndSendCertificateWithDescriptionSocialstyrelsenWhenCertificateTypeIsDoi() {
        when(authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_SIGNERA_SKICKA_DIREKT, DoiModuleEntryPoint.MODULE_ID))
            .thenReturn(true);
        when(webCertUserService.getUser()).thenReturn(getUserWithOrigin());

        final var certificate = CertificateFacadeTestHelper.createCertificate(DoiModuleEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
        final var actualAvailableFunctions = certificateSignAndSendDescriptionFunction.get(certificate);

        assertEquals(actualAvailableFunctions.get().getType(), ResourceLinkTypeDTO.SIGN_CERTIFICATE);
        assertTrue(
            actualAvailableFunctions.stream().anyMatch(r -> r.getDescription().contains("Intyget skickas direkt till Socialstyrelsen.")));
    }

    @Test
    void shallIncludeSignAndSendCertificateWithDescriptionArbetsformedlingenWhenCertificateTypeIsAf00213() {
        when(authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_SIGNERA_SKICKA_DIREKT, Af00213EntryPoint.MODULE_ID))
            .thenReturn(true);
        final var certificate = CertificateFacadeTestHelper.createCertificate(Af00213EntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
        final var actualAvailableFunctions = certificateSignAndSendDescriptionFunction.get(certificate);
        assertEquals(actualAvailableFunctions.get().getType(), ResourceLinkTypeDTO.SIGN_CERTIFICATE);
        assertTrue(
            actualAvailableFunctions.stream()
                .anyMatch(r -> r.getDescription().contains("Intyget skickas direkt till Arbetsförmedlingen.")));
    }

    @Nested
    class Forsakringskassan {

        @Test
        void shallIncludeSignAndSendCertificateWithDescriptionForsakringskassanWhenCertificateTypeIsFk7263() {
            when(authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_SIGNERA_SKICKA_DIREKT, Fk7263EntryPoint.MODULE_ID))
                .thenReturn(true);
            final var certificate = CertificateFacadeTestHelper.createCertificate(Fk7263EntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            final var actualAvailableFunctions = certificateSignAndSendDescriptionFunction.get(certificate);
            assertEquals(actualAvailableFunctions.get().getType(), ResourceLinkTypeDTO.SIGN_CERTIFICATE);
            assertTrue(
                actualAvailableFunctions.stream()
                    .anyMatch(r -> r.getDescription().contains("Intyget skickas direkt till Försäkringskassan.")));
        }

        @Test
        void shallIncludeSignAndSendCertificateWithDescriptionForsakringskassanWhenCertificateTypeIsLuse() {
            when(authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_SIGNERA_SKICKA_DIREKT, LuseEntryPoint.MODULE_ID))
                .thenReturn(true);
            final var certificate = CertificateFacadeTestHelper.createCertificate(LuseEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            final var actualAvailableFunctions = certificateSignAndSendDescriptionFunction.get(certificate);
            assertEquals(actualAvailableFunctions.get().getType(), ResourceLinkTypeDTO.SIGN_CERTIFICATE);
            assertTrue(
                actualAvailableFunctions.stream()
                    .anyMatch(r -> r.getDescription().contains("Intyget skickas direkt till Försäkringskassan.")));
        }

        @Test
        void shallIncludeSignAndSendCertificateWithDescriptionForsakringskassanWhenCertificateTypeIsLisjp() {
            when(authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_SIGNERA_SKICKA_DIREKT, LisjpEntryPoint.MODULE_ID))
                .thenReturn(true);
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            final var actualAvailableFunctions = certificateSignAndSendDescriptionFunction.get(certificate);
            assertEquals(actualAvailableFunctions.get().getType(), ResourceLinkTypeDTO.SIGN_CERTIFICATE);
            assertTrue(
                actualAvailableFunctions.stream()
                    .anyMatch(r -> r.getDescription().contains("Intyget skickas direkt till Försäkringskassan.")));
        }

        @Test
        void shallIncludeSignAndSendCertificateWithDescriptionForsakringskassanWhenCertificateTypeIsLuaefs() {
            when(authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_SIGNERA_SKICKA_DIREKT, LuaefsEntryPoint.MODULE_ID))
                .thenReturn(true);
            final var certificate = CertificateFacadeTestHelper.createCertificate(LuaefsEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            final var actualAvailableFunctions = certificateSignAndSendDescriptionFunction.get(certificate);
            assertEquals(actualAvailableFunctions.get().getType(), ResourceLinkTypeDTO.SIGN_CERTIFICATE);
            assertTrue(
                actualAvailableFunctions.stream()
                    .anyMatch(r -> r.getDescription().contains("Intyget skickas direkt till Försäkringskassan.")));
        }

        @Test
        void shallIncludeSignAndSendCertificateWithDescriptionForsakringskassanWhenCertificateTypeIsLuaena() {
            when(authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_SIGNERA_SKICKA_DIREKT, LuaenaEntryPoint.MODULE_ID))
                .thenReturn(true);
            final var certificate = CertificateFacadeTestHelper.createCertificate(LuaenaEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            final var actualAvailableFunctions = certificateSignAndSendDescriptionFunction.get(certificate);
            assertEquals(actualAvailableFunctions.get().getType(), ResourceLinkTypeDTO.SIGN_CERTIFICATE);
            assertTrue(
                actualAvailableFunctions.stream()
                    .anyMatch(r -> r.getDescription().contains("Intyget skickas direkt till Försäkringskassan.")));
        }
    }

    @Nested
    class Transportstyrelsen {

        @Test
        void shallIncludeSignAndSendCertificateWithDescriptionTransportstyrelsenWhenCertificateTypeIsTsBas() {
            when(authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_SIGNERA_SKICKA_DIREKT, TsBasEntryPoint.MODULE_ID))
                .thenReturn(true);
            final var certificate = CertificateFacadeTestHelper.createCertificate(TsBasEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            final var actualAvailableFunctions = certificateSignAndSendDescriptionFunction.get(certificate);
            assertEquals(actualAvailableFunctions.get().getType(), ResourceLinkTypeDTO.SIGN_CERTIFICATE);
            assertTrue(
                actualAvailableFunctions.stream()
                    .anyMatch(r -> r.getDescription().contains("Intyget skickas direkt till Transportstyrelsen.")));
        }

        @Test
        void shallIncludeSignAndSendCertificateWithDescriptionTransportstyrelsenWhenCertificateTypeIsTsDiabetes() {
            when(authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_SIGNERA_SKICKA_DIREKT, TsDiabetesEntryPoint.MODULE_ID))
                .thenReturn(true);
            final var certificate = CertificateFacadeTestHelper.createCertificate(TsDiabetesEntryPoint.MODULE_ID,
                CertificateStatus.UNSIGNED);
            final var actualAvailableFunctions = certificateSignAndSendDescriptionFunction.get(certificate);
            assertEquals(actualAvailableFunctions.get().getType(), ResourceLinkTypeDTO.SIGN_CERTIFICATE);
            assertTrue(
                actualAvailableFunctions.stream()
                    .anyMatch(r -> r.getDescription().contains("Intyget skickas direkt till Transportstyrelsen.")));
        }
    }

    @Test
    void shallIncludeSignAndSendCertificateWithNullDescriptionWhenCertificateTypeIsInvalid() {
        when(authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_SIGNERA_SKICKA_DIREKT, "InvalidId"))
            .thenReturn(true);
        final var certificate = CertificateFacadeTestHelper.createCertificate("InvalidId", CertificateStatus.UNSIGNED);
        final var actualAvailableFunctions = certificateSignAndSendDescriptionFunction.get(certificate);
        assertEquals(actualAvailableFunctions.get().getType(), ResourceLinkTypeDTO.SIGN_CERTIFICATE);
        assertTrue(
            actualAvailableFunctions.stream()
                .anyMatch(r -> r.getDescription() == null));
    }

    private WebCertUser getUserWithOrigin() {
        WebCertUser user = new WebCertUser();
        user.setOrigin("DJUPINTEGRATION");
        return user;
    }
}

