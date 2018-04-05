/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.intyg;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.common.model.UtkastStatus;
import se.inera.intyg.webcert.web.service.certificatesender.CertificateSenderException;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygServiceResult;

@RunWith(MockitoJUnitRunner.class)
public class IntygServiceStoreTest extends AbstractIntygServiceTest {

    @Test
    public void testStoreIntyg() throws Exception {

        IntygServiceResult res = intygService.storeIntyg(createUtkast());
        assertEquals(IntygServiceResult.OK, res);

        verify(certificateSenderService, times(1)).storeCertificate(INTYG_ID, INTYG_TYP_FK, json);
        verify(monitoringService).logIntygRegistered(INTYG_ID, INTYG_TYP_FK);
    }

    @Test(expected = WebCertServiceException.class)
    public void testStoreIntygThrowsCertificateSenderException() throws Exception {
        doThrow(new CertificateSenderException("")).when(certificateSenderService).storeCertificate(eq(INTYG_ID), eq(INTYG_TYP_FK), anyString());
        intygService.storeIntyg(createUtkast());
    }

    private Utkast createUtkast() {
        Utkast utkast = new Utkast();
        utkast.setIntygsId(INTYG_ID);
        utkast.setIntygsTyp(INTYG_TYP_FK);
        utkast.setStatus(UtkastStatus.SIGNED);
        utkast.setModel(json);
        return utkast;
    }
}
