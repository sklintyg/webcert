/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.notification_sender.certificatesender.services;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.xml.ws.WebServiceException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ExternalServiceCallException;
import se.inera.intyg.webcert.common.common.Constants;
import se.inera.intyg.webcert.common.sender.exception.PermanentException;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;

/**
 * Created by eriklupander on 2015-05-22.
 */
@RunWith(MockitoJUnitRunner.class)
public class CertificateRevokeProcessorTest {

    private static final String BODY = "body";
    private static final String INTYGS_ID1 = "intygs-id-1";
    private static final String LOGICAL_ADDRESS1 = "logicalAddress1";
    private static final String INTYGS_TYP = "fk7263";

    @Mock
    private IntygModuleRegistry registry;

    @InjectMocks
    CertificateRevokeProcessor certificateRevokeProcessor = new CertificateRevokeProcessor();

    @Test
    public void testRevokeCertificate() throws Exception {
        ModuleApi moduleApi = mock(ModuleApi.class);
        when(registry.getModuleApi(eq(INTYGS_TYP))).thenReturn(moduleApi);

        certificateRevokeProcessor.process(BODY, INTYGS_ID1, LOGICAL_ADDRESS1, INTYGS_TYP);

        verify(moduleApi).revokeCertificate(eq(BODY), eq(LOGICAL_ADDRESS1));
    }

    @Test(expected = TemporaryException.class)
    public void testRevokeCertificateWhenWebServiceExceptionIsThrown() throws Exception {
        ModuleApi moduleApi = mock(ModuleApi.class);
        when(registry.getModuleApi(eq(INTYGS_TYP))).thenReturn(moduleApi);
        doThrow(new WebServiceException())
                .when(moduleApi)
                .revokeCertificate(eq(BODY), eq(LOGICAL_ADDRESS1));

        certificateRevokeProcessor.process(BODY, INTYGS_ID1, LOGICAL_ADDRESS1, INTYGS_TYP);
    }

    @Test(expected = TemporaryException.class)
    public void testRevokeCertificateOnApplicationErrorResponse() throws Exception {
        ModuleApi moduleApi = mock(ModuleApi.class);
        when(registry.getModuleApi(eq(INTYGS_TYP))).thenReturn(moduleApi);
        doThrow(new ExternalServiceCallException("message", ExternalServiceCallException.ErrorIdEnum.APPLICATION_ERROR))
                .when(moduleApi)
                .revokeCertificate(eq(BODY), eq(LOGICAL_ADDRESS1));

        certificateRevokeProcessor.process(BODY, INTYGS_ID1, LOGICAL_ADDRESS1, INTYGS_TYP);
    }

    @Test(expected = TemporaryException.class)
    public void testRevokeCertificateOnTechnicalErrorResponse() throws Exception {
        ModuleApi moduleApi = mock(ModuleApi.class);
        when(registry.getModuleApi(eq(INTYGS_TYP))).thenReturn(moduleApi);
        doThrow(new ExternalServiceCallException("message", ExternalServiceCallException.ErrorIdEnum.TECHNICAL_ERROR))
                .when(moduleApi)
                .revokeCertificate(eq(BODY), eq(LOGICAL_ADDRESS1));

        certificateRevokeProcessor.process(BODY, INTYGS_ID1, LOGICAL_ADDRESS1, INTYGS_TYP);
    }

    @Test(expected = PermanentException.class)
    public void testRevokeCertificateOnValidationErrorResponse() throws Exception {
        ModuleApi moduleApi = mock(ModuleApi.class);
        when(registry.getModuleApi(eq(INTYGS_TYP))).thenReturn(moduleApi);
        doThrow(new ExternalServiceCallException("message", ExternalServiceCallException.ErrorIdEnum.VALIDATION_ERROR))
                .when(moduleApi)
                .revokeCertificate(eq(BODY), eq(LOGICAL_ADDRESS1));

        certificateRevokeProcessor.process(BODY, INTYGS_ID1, LOGICAL_ADDRESS1, INTYGS_TYP);

    }

    @Test(expected = PermanentException.class)
    public void testRevokeCertificateOnTransformationErrorResponse() throws Exception {
        ModuleApi moduleApi = mock(ModuleApi.class);
        when(registry.getModuleApi(eq(INTYGS_TYP))).thenReturn(moduleApi);
        doThrow(new ExternalServiceCallException("message", ExternalServiceCallException.ErrorIdEnum.TRANSFORMATION_ERROR))
                .when(moduleApi)
                .revokeCertificate(eq(BODY), eq(LOGICAL_ADDRESS1));

        certificateRevokeProcessor.process(BODY, INTYGS_ID1, LOGICAL_ADDRESS1, INTYGS_TYP);

    }

    @Test
    public void testIntygsIdIsMissing() throws Exception {
        try {
            certificateRevokeProcessor.process(BODY, null, LOGICAL_ADDRESS1, INTYGS_TYP);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains(Constants.INTYGS_ID));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLogicalAddressIsMissing() throws Exception {
        certificateRevokeProcessor.process(BODY, INTYGS_ID1, null, INTYGS_TYP);
    }

}
