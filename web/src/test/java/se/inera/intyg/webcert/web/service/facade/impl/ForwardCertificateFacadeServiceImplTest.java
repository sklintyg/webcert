/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.builder.CertificateBuilder;
import se.inera.intyg.common.support.facade.builder.CertificateMetadataBuilder;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.facade.GetCertificateFacadeService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;

@ExtendWith(MockitoExtension.class)
class ForwardCertificateFacadeServiceImplTest {

    @Mock
    private UtkastService utkastService;

    @Mock
    private GetCertificateFacadeService getCertificateFacadeService;

    @InjectMocks
    private ForwardCertificateFacadeServiceImpl forwardCertificateFacadeService;

    private final static String CERTIFICATE_ID = "XXXXX-YYYYY-ZZZZZ";
    private final static long VERSION = 100L;
    private Certificate certificate;

    @BeforeEach
    void setup() {
        certificate = CertificateBuilder.create()
            .metadata(
                CertificateMetadataBuilder.create()
                    .id(CERTIFICATE_ID)
                    .build()
            )
            .build();

        final var draft = new Utkast();
        draft.setIntygsId(CERTIFICATE_ID);

        doReturn(draft)
            .when(utkastService)
            .setNotifiedOnDraft(eq(draft.getIntygsId()), eq(VERSION), anyBoolean());

        doReturn(certificate)
            .when(getCertificateFacadeService)
            .getCertificate(draft.getIntygsId());
    }

    @Test
    void shallForwardCertificate() {
        final var expectedForward = true;

        certificate.getMetadata().setForwarded(expectedForward);

        final var actualCertificate = forwardCertificateFacadeService.forwardCertificate(
            CERTIFICATE_ID, VERSION, expectedForward);

        assertEquals(expectedForward, actualCertificate.getMetadata().isForwarded());
    }

    @Test
    void shallNotForwardCertificate() {
        final var expectedForward = false;

        certificate.getMetadata().setForwarded(expectedForward);

        final var actualCertificate = forwardCertificateFacadeService.forwardCertificate(
            CERTIFICATE_ID, VERSION, expectedForward);

        assertEquals(expectedForward, actualCertificate.getMetadata().isForwarded());
    }
}