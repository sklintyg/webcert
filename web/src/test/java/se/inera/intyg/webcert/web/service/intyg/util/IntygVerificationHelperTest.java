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
package se.inera.intyg.webcert.web.service.intyg.util;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static se.inera.intyg.webcert.web.service.intyg.util.IntygVerificationHelper.verifyIsNotSent;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateMetaData;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateResponse;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.intyg.IntygServiceImpl.IntygOperation;

@RunWith(MockitoJUnitRunner.class)
public class IntygVerificationHelperTest {

    @Test
    public void testVerifyIsNotSent() {
        Utkast utkast = mock(Utkast.class);
        doReturn(null).when(utkast).getSkickadTillMottagare();
        verifyIsNotSent(utkast, IntygOperation.SEND);
    }

    @Test(expected = WebCertServiceException.class)
    public void testVerifyIsSent() throws Exception {
        Utkast utkast = mock(Utkast.class);
        doReturn("FKASSA").when(utkast).getSkickadTillMottagare();
        verifyIsNotSent(utkast, IntygOperation.SEND);
    }

    @Test
    public void testVerifyIsNotSentEmptyMottagare() throws Exception {
        Utkast utkast = mock(Utkast.class);
        doReturn("").when(utkast).getSkickadTillMottagare();
        verifyIsNotSent(utkast, IntygOperation.SEND);
    }

    @Test
    public void testVerifyIsNotSentBlankspaceMottagare() throws Exception {
        Utkast utkast = mock(Utkast.class);
        doReturn(" ").when(utkast).getSkickadTillMottagare();
        verifyIsNotSent(utkast, IntygOperation.SEND);
    }

    @Test
    public void testVerifyIsNotSentEmptyList() {
        CertificateResponse certificate = mock(CertificateResponse.class);
        verifyIsNotSent(certificate, IntygOperation.SEND);
    }

    @Test(expected = WebCertServiceException.class)
    public void testVerifyIsSentFilledList() {
        CertificateResponse certificate = mock(CertificateResponse.class);
        Utlatande myUtlatande = mock(Utlatande.class);
        CertificateMetaData metaData = mock(CertificateMetaData.class);

        List<Status> myList = new LinkedList<>();
        myList.add(new Status(CertificateState.SENT, "target", LocalDateTime.now()));

        doReturn(metaData).when(certificate).getMetaData();
        doReturn(myList).when(metaData).getStatus();
        doReturn("123").when(myUtlatande).getId();
        doReturn(myUtlatande).when(certificate).getUtlatande();

        verifyIsNotSent(certificate, IntygOperation.SEND);
    }
}
