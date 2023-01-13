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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.webcert.web.service.facade.CertificateFacadeTestHelper;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

class CopyCertificateFunctionImplTest {

    private CopyCertificateFunctionImpl copyCertificateFunction;

    @BeforeEach
    void setUp() {
        copyCertificateFunction = new CopyCertificateFunctionImpl();
    }

    @Test
    void shallIncludeCopyCertificateIfLocked() {
        final var expected = ResourceLinkDTO.create(
            ResourceLinkTypeDTO.COPY_CERTIFICATE,
            "Kopiera",
            "Skapar en redigerbar kopia av utkastet på den enheten du är inloggad på.",
            true
        );

        final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.LOCKED);

        final var actual = copyCertificateFunction.get(certificate).orElseThrow();
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

        final var actual = copyCertificateFunction.get(certificate).orElseThrow();
        assertEquals(expected, actual);
    }

    @Test
    void shallNotIncludeAnyCopyCertificateIfCertificateIsNotLocked() {
        final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);

        final var actual = copyCertificateFunction.get(certificate);
        assertTrue(actual.isEmpty());
    }
}