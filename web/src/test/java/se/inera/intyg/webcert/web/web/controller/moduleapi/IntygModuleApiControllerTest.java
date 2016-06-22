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

package se.inera.intyg.webcert.web.web.controller.moduleapi;

import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.Response;

import org.joda.time.LocalDateTime;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.common.security.authorities.AuthoritiesException;
import se.inera.intyg.common.security.common.model.*;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.intygstyper.fk7263.model.internal.Utlatande;
import se.inera.intyg.webcert.web.service.feature.WebcertFeature;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.*;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.RevokeSignedIntygParameter;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.SendSignedIntygParameter;

/**
 * @author andreaskaltenbach
 */
@RunWith(MockitoJUnitRunner.class)
public class IntygModuleApiControllerTest {

    private static final String CERTIFICATE_ID = "123456";
    private static final String CERTIFICATE_TYPE = "fk7263";

    private static final byte[] PDF_DATA = "<pdf-data>".getBytes();
    private static final String PDF_NAME = "the-file.pdf";

    private static final String CONTENT_DISPOSITION = "Content-Disposition";

    @Mock
    private IntygService intygService;

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @Mock
    private ModuleApi moduleApi;

    @Mock
    private LogService logService;

    @Mock
    private WebCertUserService webcertUserService;

    @InjectMocks
    private IntygModuleApiController moduleApiController = new IntygModuleApiController();

    @BeforeClass
    public static void setupCertificateData() throws IOException {
        Utlatande utlatande = new Utlatande();
        utlatande.setId(CERTIFICATE_ID);
        utlatande.setTyp(CERTIFICATE_TYPE);

        List<Status> status = new ArrayList<Status>();
        status.add(new Status(CertificateState.RECEIVED, "HV", LocalDateTime.now()));
        status.add(new Status(CertificateState.SENT, "FK", LocalDateTime.now()));
    }

    @Test
    public void testGetIntygAsPdf() throws Exception {

        final String intygType = "fk7263";
        setupUser(AuthoritiesConstants.PRIVILEGE_VISA_INTYG, intygType);
        IntygPdf pdfResponse = new IntygPdf(PDF_DATA, PDF_NAME);

        when(intygService.fetchIntygAsPdf(CERTIFICATE_ID, intygType, false)).thenReturn(pdfResponse);

        Response response = moduleApiController.getIntygAsPdf(intygType, CERTIFICATE_ID);

        verify(intygService).fetchIntygAsPdf(CERTIFICATE_ID, intygType, false);

        assertEquals(OK.getStatusCode(), response.getStatus());
        assertEquals(PDF_DATA, response.getEntity());
        assertNotNull(response.getHeaders().get(CONTENT_DISPOSITION));
    }

    @Test(expected = AuthoritiesException.class)
    public void testGetIntygAsPdfNotAuthorised() throws Exception {
        setupUser("", "");
        moduleApiController.getIntygAsPdf("fk7263", CERTIFICATE_ID);

    }

    @Test
    public void testGetIntygAsPdfForEmployer() throws Exception {

        final String intygType = "fk7263";
        setupUser(AuthoritiesConstants.PRIVILEGE_VISA_INTYG, intygType);
        IntygPdf pdfResponse = new IntygPdf(PDF_DATA, PDF_NAME);

        when(intygService.fetchIntygAsPdf(CERTIFICATE_ID, intygType, true)).thenReturn(pdfResponse);

        Response response = moduleApiController.getIntygAsPdfForEmployer(intygType, CERTIFICATE_ID);

        verify(intygService).fetchIntygAsPdf(CERTIFICATE_ID, intygType, true);

        assertEquals(OK.getStatusCode(), response.getStatus());
        assertEquals(PDF_DATA, response.getEntity());
        assertNotNull(response.getHeaders().get(CONTENT_DISPOSITION));
    }

    @Test(expected = AuthoritiesException.class)
    public void testGetIntygAsPdfForEmployerNotAuthorised() throws Exception {
        setupUser("", "");
        moduleApiController.getIntygAsPdf("fk7263", CERTIFICATE_ID);
    }

    @Test
    public void testGetIntyg() {
        final String intygsTyp = "fk7263";
        final String intygContent = "CONTENTS";

        setupUser(AuthoritiesConstants.PRIVILEGE_VISA_INTYG, intygsTyp);

        IntygContentHolder content = mock(IntygContentHolder.class);
        when(content.getContents()).thenReturn(intygContent);
        when(intygService.fetchIntygDataWithRelations(eq(CERTIFICATE_ID), eq(intygsTyp), eq(false))).thenReturn(content);

        Response response = moduleApiController.getIntyg(intygsTyp, CERTIFICATE_ID, false);

        assertEquals(OK.getStatusCode(), response.getStatus());
        assertEquals(intygContent, ((IntygContentHolder) response.getEntity()).getContents());
        verify(intygService, times(1)).fetchIntygDataWithRelations(eq(CERTIFICATE_ID), eq(intygsTyp), eq(false));
    }

    @Test(expected = AuthoritiesException.class)
    public void testGetIntygNotAuthorised() {
        setupUser("", "");
        moduleApiController.getIntyg("fk7263", CERTIFICATE_ID, false);
    }

    @Test
    public void testSendSignedIntyg() {
        final String intygType = "fk7263";
        final String recipient = "recipient";
        final boolean consent = true;

        setupUser("", intygType, WebcertFeature.SKICKA_INTYG);

        when(intygService.sendIntyg(eq(CERTIFICATE_ID), eq(intygType), eq(recipient), eq(consent))).thenReturn(IntygServiceResult.OK);

        SendSignedIntygParameter param = new SendSignedIntygParameter();
        param.setPatientConsent(consent);
        param.setRecipient(recipient);
        Response response = moduleApiController.sendSignedIntyg(intygType, CERTIFICATE_ID, param);

        verify(intygService, times(1)).sendIntyg(eq(CERTIFICATE_ID), eq(intygType), eq(recipient), eq(consent));
        assertEquals(OK.getStatusCode(), response.getStatus());
        assertEquals(IntygServiceResult.OK, response.getEntity());
    }

    @Test(expected = AuthoritiesException.class)
    public void testSendSignedIntygNotAuthorised() {
        setupUser("", "");
        moduleApiController.sendSignedIntyg("intygType", CERTIFICATE_ID, null);
    }
    @Test
    public void testRevokeSignedIntyg() {
        final String intygType = "fk7263";
        final String revokeMessage = "revokeMessage";

        setupUser(AuthoritiesConstants.PRIVILEGE_MAKULERA_INTYG, intygType, WebcertFeature.MAKULERA_INTYG);

        when(intygService.revokeIntyg(eq(CERTIFICATE_ID), eq(intygType), eq(revokeMessage))).thenReturn(IntygServiceResult.OK);

        RevokeSignedIntygParameter param = new RevokeSignedIntygParameter();
        param.setRevokeMessage(revokeMessage);
        Response response = moduleApiController.revokeSignedIntyg(intygType, CERTIFICATE_ID, param);

        verify(intygService, times(1)).revokeIntyg(eq(CERTIFICATE_ID), eq(intygType), eq(revokeMessage));
        assertEquals(OK.getStatusCode(), response.getStatus());
        assertEquals(IntygServiceResult.OK, response.getEntity());
    }

    @Test(expected = AuthoritiesException.class)
    public void testRevokeSignedIntygNotAuthorised() {
        setupUser("", "");
        moduleApiController.revokeSignedIntyg("intygType", CERTIFICATE_ID, null);
    }

    private void setupUser(String privilegeString, String intygType, WebcertFeature... features) {
        WebCertUser user = new WebCertUser();
        user.setAuthorities(new HashMap<>());
        user.setFeatures(Stream.of(features).map(WebcertFeature::getName).collect(Collectors.toSet()));
        user.getFeatures().addAll(Stream.of(features).map(f -> f.getName() + "." + intygType).collect(Collectors.toSet()));
        Privilege privilege = new Privilege();
        privilege.setIntygstyper(Arrays.asList(intygType));
        RequestOrigin requestOrigin = new RequestOrigin();
        requestOrigin.setName("NORMAL");
        requestOrigin.setIntygstyper(privilege.getIntygstyper());
        privilege.setRequestOrigins(Arrays.asList(requestOrigin));
        user.getAuthorities().put(privilegeString, privilege);
        user.setOrigin("NORMAL");
        when(webcertUserService.getUser()).thenReturn(user);
    }

}
