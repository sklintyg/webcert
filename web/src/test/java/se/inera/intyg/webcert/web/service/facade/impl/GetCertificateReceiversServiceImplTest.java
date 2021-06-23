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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.receiver.CertificateReceiverService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;

@ExtendWith(MockitoExtension.class)
public class GetCertificateReceiversServiceImplTest {

    @Mock
    private UtkastService utkastService;
    @Mock
    private CertificateReceiverService certificateReceiverService;

    @InjectMocks
    private GetCertificateReceiversFacadeServiceImpl getCertificateReceiversFacadeService;

    private final static String CERTIFICATE_ID = "certificateId";
    private final static String CERTIFICATE_TYPE = "lisjp";

    @BeforeEach
    void setup() {
        final var draft = new Utkast();
        draft.setIntygsId(CERTIFICATE_ID);
        draft.setIntygsTyp(CERTIFICATE_TYPE);

        when(utkastService.getDraft(eq(CERTIFICATE_ID), eq(false))).thenReturn(draft);
    }

    @Test
    void shallGetCertificateReceivers() {
        getCertificateReceiversFacadeService.getCertificateReceivers(CERTIFICATE_ID);
        verify(certificateReceiverService).listPossibleReceivers(CERTIFICATE_TYPE);
    }

}
