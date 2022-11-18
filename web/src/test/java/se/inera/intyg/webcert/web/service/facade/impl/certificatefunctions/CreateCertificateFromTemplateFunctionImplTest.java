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

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.inera.intyg.common.ag7804.support.Ag7804EntryPoint;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.webcert.web.service.facade.CertificateFacadeTestHelper;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

class CreateCertificateFromTemplateFunctionImplTest {

    private CreateCertificateFromTemplateFunctionImpl createCertificateFromTemplateFunction;
    private WebCertUser webCertUser;

    @BeforeEach
    void setUp() {
        createCertificateFromTemplateFunction = new CreateCertificateFromTemplateFunctionImpl();
        webCertUser = new WebCertUser();
        webCertUser.setOrigin(UserOriginType.NORMAL.name());
    }

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
}