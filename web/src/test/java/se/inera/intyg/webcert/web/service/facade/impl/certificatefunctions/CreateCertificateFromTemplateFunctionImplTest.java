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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import se.inera.intyg.common.ag7804.support.Ag7804EntryPoint;
import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.webcert.web.service.facade.CertificateFacadeTestHelper;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

class CreateCertificateFromTemplateFunctionImplTest {

    private CreateCertificateFromTemplateFunctionImpl createCertificateFromTemplateFunction;
    private WebCertUser webCertUser;

    @BeforeEach
    void setUp() {
        createCertificateFromTemplateFunction = new CreateCertificateFromTemplateFunctionImpl();
        webCertUser = new WebCertUser();
        webCertUser.setOrigin(UserOriginType.NORMAL.name());
    }

    @Nested
    class CreateAg7804FromFk7804 {

        @Test
        void shallIncludeCreateCertificateFromTemplate() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualLink = createCertificateFromTemplateFunction.get(certificate, webCertUser);
            assertTrue(actualLink.isPresent());
        }

        @Test
        void shallExcludeCreateCertificateFromTemplateIfDraft() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            final var actualLink = createCertificateFromTemplateFunction.get(certificate, webCertUser);
            assertTrue(actualLink.isEmpty());
        }

        @Test
        void shallExcludeCreateCertificateFromTemplateIfOriginDjupintegrerad() {
            webCertUser.setOrigin(UserOriginType.DJUPINTEGRATION.name());
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualLink = createCertificateFromTemplateFunction.get(certificate, webCertUser);
            assertTrue(actualLink.isEmpty());
        }

        @Test
        void shallExcludeCreateCertificateFromTemplateIfNotLisjp() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(Ag7804EntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualLink = createCertificateFromTemplateFunction.get(certificate, webCertUser);
            assertTrue(actualLink.isEmpty());
        }

        @Test
        void shallExcludeCreateCertificateFromTemplateIfRevoked() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.REVOKED);
            final var actualLink = createCertificateFromTemplateFunction.get(certificate, webCertUser);
            assertTrue(actualLink.isEmpty());
        }

        @Test
        void shallExcludeCreateCertificateFromTemplateIfReplacedBySignedCertificate() {
            final var certificate = CertificateFacadeTestHelper.createCertificateWithChildRelation(
                LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED, CertificateRelation.builder()
                    .status(CertificateStatus.SIGNED)
                    .type(CertificateRelationType.REPLACED)
                    .build()
            );
            final var actualLink = createCertificateFromTemplateFunction.get(certificate, webCertUser);
            assertTrue(actualLink.isEmpty());
        }

        @Test
        void shallExcludeCreateCertificateFromTemplateIfComplementedBySignedCertificate() {
            final var certificate = CertificateFacadeTestHelper.createCertificateWithChildRelation(
                LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED, CertificateRelation.builder()
                    .status(CertificateStatus.SIGNED)
                    .type(CertificateRelationType.COMPLEMENTED)
                    .build()
            );
            final var actualLink = createCertificateFromTemplateFunction.get(certificate, webCertUser);
            assertTrue(actualLink.isEmpty());
        }

        @Test
        void shallIncludeCreateCertificateFromTemplateIfComplementedByUnignedCertificate() {
            final var certificate = CertificateFacadeTestHelper.createCertificateWithChildRelation(
                LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED, CertificateRelation.builder()
                    .status(CertificateStatus.UNSIGNED)
                    .type(CertificateRelationType.COMPLEMENTED)
                    .build()
            );
            final var actualLink = createCertificateFromTemplateFunction.get(certificate, webCertUser);
            assertTrue(actualLink.isPresent());
        }

        @Test
        void shallIncludeResourceLinkDTOType() {
            final var expectedType = ResourceLinkTypeDTO.CREATE_CERTIFICATE_FROM_TEMPLATE;
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualLink = createCertificateFromTemplateFunction.get(certificate, webCertUser);
            assertEquals(expectedType, actualLink.get().getType());
        }

        @Test
        void shallIncludeResourceLinkDTOName() {
            final var expectedName = "Skapa AG7804";
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualLink = createCertificateFromTemplateFunction.get(certificate, webCertUser);
            assertEquals(expectedName, actualLink.get().getName());
        }

        @Test
        void shallIncludeResourceLinkDTODescription() {
            final var expectedDescription = "Skapar ett intyg till arbetsgivaren utifrån Försäkringskassans intyg.";
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualLink = createCertificateFromTemplateFunction.get(certificate, webCertUser);
            assertEquals(expectedDescription, actualLink.get().getDescription());
        }

        @Test
        void shallIncludeResourceLinkDTOBody() {
            final var expectedBody = "<div><div class=\"ic-alert ic-alert--status ic-alert--info\">\n"
                + "<i class=\"ic-alert__icon ic-info-icon\"></i>\n"
                + "Kom ihåg att stämma av med patienten om hen vill att du skickar Läkarintyget för sjukpenning till Försäkringskassan. "
                + "Gör detta i så fall först.</div>"
                + "<p class='iu-pt-400'>Skapa ett Läkarintyg om arbetsförmåga - arbetsgivaren (AG7804)"
                + " utifrån ett Läkarintyg för sjukpenning innebär att "
                + "informationsmängder som är gemensamma för båda intygen automatiskt förifylls.\n"
                + "</p></div>";
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualLink = createCertificateFromTemplateFunction.get(certificate, webCertUser);
            assertEquals(expectedBody, actualLink.get().getBody());
        }

        @Test
        void shallIncludeResourceLinkDTOEnabled() {
            final var expectedEnabled = true;
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualLink = createCertificateFromTemplateFunction.get(certificate, webCertUser);
            assertEquals(expectedEnabled, actualLink.get().isEnabled());
        }
    }

    @Nested
    class CreateDoiFromDb {

        @Test
        void shallIncludeCreateCertificateFromTemplate() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(DbModuleEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualLink = createCertificateFromTemplateFunction.get(certificate, webCertUser);
            assertTrue(actualLink.isPresent());
        }

        @Test
        void shallExcludeCreateCertificateFromTemplateIfDraft() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(DbModuleEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            final var actualLink = createCertificateFromTemplateFunction.get(certificate, webCertUser);
            assertTrue(actualLink.isEmpty());
        }

        @Test
        void shallExcludeCreateCertificateFromTemplateIfOriginDjupintegrerad() {
            webCertUser.setOrigin(UserOriginType.DJUPINTEGRATION.name());
            final var certificate = CertificateFacadeTestHelper.createCertificate(DbModuleEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualLink = createCertificateFromTemplateFunction.get(certificate, webCertUser);
            assertTrue(actualLink.isEmpty());
        }

        @Test
        void shallExcludeCreateCertificateFromTemplateIfRevoked() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(DbModuleEntryPoint.MODULE_ID, CertificateStatus.REVOKED);
            final var actualLink = createCertificateFromTemplateFunction.get(certificate, webCertUser);
            assertTrue(actualLink.isEmpty());
        }

        @Test
        void shallIncludeResourceLinkDTOType() {
            final var expectedType = ResourceLinkTypeDTO.CREATE_CERTIFICATE_FROM_TEMPLATE;
            final var certificate = CertificateFacadeTestHelper.createCertificate(DbModuleEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualLink = createCertificateFromTemplateFunction.get(certificate, webCertUser);
            assertEquals(expectedType, actualLink.get().getType());
        }

        @Test
        void shallIncludeResourceLinkDTOName() {
            final var expectedName = "Skapa dödsorsaksintyg";
            final var certificate = CertificateFacadeTestHelper.createCertificate(DbModuleEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualLink = createCertificateFromTemplateFunction.get(certificate, webCertUser);
            assertEquals(expectedName, actualLink.get().getName());
        }

        @Test
        void shallIncludeResourceLinkDTODescription() {
            final var expectedDescription = "Skapar ett dödsorsaksintyg utifrån dödsbeviset.";
            final var certificate = CertificateFacadeTestHelper.createCertificate(DbModuleEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualLink = createCertificateFromTemplateFunction.get(certificate, webCertUser);
            assertEquals(expectedDescription, actualLink.get().getDescription());
        }

        @Test
        void shallIncludeResourceLinkDTOBody() {
            final var expectedBody =
                "Skapa ett dödsorsaksintyg utifrån ett dödsbevis innebär att informationsmängder som är gemensamma för "
                    + "båda intygen, automatiskt förifylls.";
            final var certificate = CertificateFacadeTestHelper.createCertificate(DbModuleEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualLink = createCertificateFromTemplateFunction.get(certificate, webCertUser);
            assertEquals(expectedBody, actualLink.get().getBody());
        }

        @Test
        void shallIncludeResourceLinkDTOEnabled() {
            final var expectedEnabled = true;
            final var certificate = CertificateFacadeTestHelper.createCertificate(DbModuleEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualLink = createCertificateFromTemplateFunction.get(certificate, webCertUser);
            assertEquals(expectedEnabled, actualLink.get().isEnabled());
        }
    }

}
