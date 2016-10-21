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
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.Response;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.common.security.authorities.AuthoritiesException;
import se.inera.intyg.common.security.common.model.*;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.intygstyper.fk7263.model.internal.Utlatande;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.feature.WebcertFeature;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.*;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.CopyUtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.*;
import se.inera.intyg.webcert.web.web.controller.api.dto.CopyIntygRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.CopyIntygResponse;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.*;

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
    private CopyUtkastService copyUtkastService;

    @Mock
    private WebCertUserService webcertUserService;

    @InjectMocks
    private IntygModuleApiController moduleApiController = new IntygModuleApiController();

    @BeforeClass
    public static void setupCertificateData() throws IOException {
        Utlatande utlatande = new Utlatande();
        utlatande.setId(CERTIFICATE_ID);
        utlatande.setTyp(CERTIFICATE_TYPE);

        List<Status> status = new ArrayList<>();
        status.add(new Status(CertificateState.RECEIVED, "HV", LocalDateTime.now()));
        status.add(new Status(CertificateState.SENT, "FK", LocalDateTime.now()));
    }

    @Test
    public void testGetIntygAsPdf() throws Exception {

        final String intygType = "fk7263";
        setupUser(AuthoritiesConstants.PRIVILEGE_VISA_INTYG, intygType, WebcertFeature.UTSKRIFT);
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
        setupUser(AuthoritiesConstants.PRIVILEGE_VISA_INTYG, intygType, WebcertFeature.ARBETSGIVARUTSKRIFT);
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

        setupUser("", intygType, WebcertFeature.SKICKA_INTYG);

        when(intygService.sendIntyg(eq(CERTIFICATE_ID), eq(intygType), eq(recipient))).thenReturn(IntygServiceResult.OK);

        SendSignedIntygParameter param = new SendSignedIntygParameter();
        param.setRecipient(recipient);
        Response response = moduleApiController.sendSignedIntyg(intygType, CERTIFICATE_ID, param);

        verify(intygService, times(1)).sendIntyg(eq(CERTIFICATE_ID), eq(intygType), eq(recipient));
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
        final String revokeReason = "revokeReason";

        setupUser(AuthoritiesConstants.PRIVILEGE_MAKULERA_INTYG, intygType, WebcertFeature.MAKULERA_INTYG);

        when(intygService.revokeIntyg(CERTIFICATE_ID, intygType, revokeMessage, revokeReason)).thenReturn(IntygServiceResult.OK);

        RevokeSignedIntygParameter param = new RevokeSignedIntygParameter();
        param.setMessage(revokeMessage);
        param.setReason(revokeReason);
        Response response = moduleApiController.revokeSignedIntyg(intygType, CERTIFICATE_ID, param);

        verify(intygService, times(1)).revokeIntyg(CERTIFICATE_ID, intygType, revokeMessage, revokeReason);
        assertEquals(OK.getStatusCode(), response.getStatus());
        assertEquals(IntygServiceResult.OK, response.getEntity());
    }

    @Test(expected = AuthoritiesException.class)
    public void testRevokeSignedIntygNotAuthorised() {
        setupUser("", "");
        moduleApiController.revokeSignedIntyg("intygType", CERTIFICATE_ID, null);
    }

    @Test
    public void testRevokeReplaceSignedIntyg() {
        final String revokeMessage = "revokeMessage";
        final String revokeReason = "revokeReason";
        final String personnummer = "191212121212";
        final String newIntygId = "newIntygId";
        final String newPersonnummer = "newPersonnummer";
        final String efternamn = "efternamn";
        final String fornamn = "fornamn";
        final String mellannamn = "mellannamn";
        final String postadress = "postadress";
        final String postort = "postort";
        final String postnummer = "postnummer";

        RevokeReplaceSignedIntygRequest revokeReplaceRequest = new RevokeReplaceSignedIntygRequest();
        CopyIntygRequest copyIntygRequest = new CopyIntygRequest();
        copyIntygRequest.setPatientPersonnummer(new Personnummer(personnummer));
        copyIntygRequest.setNyttPatientPersonnummer(new Personnummer(newPersonnummer));
        copyIntygRequest.setEfternamn(efternamn);
        copyIntygRequest.setFornamn(fornamn);
        copyIntygRequest.setMellannamn(mellannamn);
        copyIntygRequest.setPostadress(postadress);
        copyIntygRequest.setPostort(postort);
        copyIntygRequest.setPostnummer(postnummer);
        revokeReplaceRequest.setCopyIntygRequest(copyIntygRequest);

        RevokeSignedIntygParameter revokeSignedIntygParameter = new RevokeSignedIntygParameter();
        revokeSignedIntygParameter.setMessage(revokeMessage);
        revokeSignedIntygParameter.setReason(revokeReason);
        revokeReplaceRequest.setRevokeSignedIntygParameter(revokeSignedIntygParameter);

        WebCertUser user = new WebCertUser();
        addFeatures(user, CERTIFICATE_TYPE, WebcertFeature.MAKULERA_INTYG, WebcertFeature.KOPIERA_INTYG);
        addPrivileges(user, CERTIFICATE_TYPE, AuthoritiesConstants.PRIVILEGE_KOPIERA_INTYG, AuthoritiesConstants.PRIVILEGE_MAKULERA_INTYG);
        user.setOrigin("NORMAL");

        when(intygService.revokeIntyg(eq(CERTIFICATE_ID), eq(CERTIFICATE_TYPE), eq(revokeMessage), eq(revokeReason))).thenReturn(IntygServiceResult.OK);
        ArgumentCaptor<CreateNewDraftCopyRequest> captor = ArgumentCaptor.forClass(CreateNewDraftCopyRequest.class);
        when(copyUtkastService.createReplacementCopy(captor.capture())).thenReturn(new CreateNewDraftCopyResponse(CERTIFICATE_TYPE, newIntygId));
        when(webcertUserService.getUser()).thenReturn(user);

        Response response = moduleApiController.revokeReplaceSignedIntyg(CERTIFICATE_TYPE, CERTIFICATE_ID, revokeReplaceRequest);

        verify(intygService, times(1)).revokeIntyg(eq(CERTIFICATE_ID), eq(CERTIFICATE_TYPE), eq(revokeMessage), eq(revokeReason));
        verifyNoMoreInteractions(intygService);
        verify(copyUtkastService, times(1)).createReplacementCopy(any());
        verifyNoMoreInteractions(copyUtkastService);
        assertEquals(newIntygId, ((CopyIntygResponse) response.getEntity()).getIntygsUtkastId());
        assertEquals(personnummer, captor.getValue().getPatient().getPersonId().getPersonnummer());
        assertEquals(newPersonnummer, captor.getValue().getNyttPatientPersonnummer().getPersonnummer());
        assertEquals(fornamn, captor.getValue().getPatient().getFornamn());
        assertEquals(efternamn, captor.getValue().getPatient().getEfternamn());
        assertEquals(mellannamn, captor.getValue().getPatient().getMellannamn());
        assertEquals(postadress, captor.getValue().getPatient().getPostadress());
        assertEquals(postnummer, captor.getValue().getPatient().getPostnummer());
        assertEquals(postort, captor.getValue().getPatient().getPostort());
    }

    @Test(expected = AuthoritiesException.class)
    public void testRevokeReplaceSignedIntygMissingFeatureMakuleraIntyg() {
        WebCertUser user = new WebCertUser();
        addFeatures(user, CERTIFICATE_TYPE, WebcertFeature.KOPIERA_INTYG);
        addPrivileges(user, CERTIFICATE_TYPE, AuthoritiesConstants.PRIVILEGE_KOPIERA_INTYG, AuthoritiesConstants.PRIVILEGE_MAKULERA_INTYG);
        user.setOrigin("NORMAL");
        when(webcertUserService.getUser()).thenReturn(user);

        try {
            moduleApiController.revokeReplaceSignedIntyg(CERTIFICATE_TYPE, CERTIFICATE_ID, null);
        } finally {
            verifyZeroInteractions(intygService);
            verifyZeroInteractions(copyUtkastService);
        }
    }

    @Test(expected = AuthoritiesException.class)
    public void testRevokeReplaceSignedIntygMissingFeatureKopieraIntyg() {
        WebCertUser user = new WebCertUser();
        addFeatures(user, CERTIFICATE_TYPE, WebcertFeature.MAKULERA_INTYG);
        addPrivileges(user, CERTIFICATE_TYPE, AuthoritiesConstants.PRIVILEGE_KOPIERA_INTYG, AuthoritiesConstants.PRIVILEGE_MAKULERA_INTYG);
        user.setOrigin("NORMAL");
        when(webcertUserService.getUser()).thenReturn(user);

        try {
            moduleApiController.revokeReplaceSignedIntyg(CERTIFICATE_TYPE, CERTIFICATE_ID, null);
        } finally {
            verifyZeroInteractions(intygService);
            verifyZeroInteractions(copyUtkastService);
        }
    }

    @Test(expected = AuthoritiesException.class)
    public void testRevokeReplaceSignedIntygMissingPrivilegeKopieraIntyg() {
        WebCertUser user = new WebCertUser();
        addFeatures(user, CERTIFICATE_TYPE, WebcertFeature.MAKULERA_INTYG, WebcertFeature.KOPIERA_INTYG);
        addPrivileges(user, CERTIFICATE_TYPE, AuthoritiesConstants.PRIVILEGE_MAKULERA_INTYG);
        user.setOrigin("NORMAL");
        when(webcertUserService.getUser()).thenReturn(user);

        try {
            moduleApiController.revokeReplaceSignedIntyg(CERTIFICATE_TYPE, CERTIFICATE_ID, null);
        } finally {
            verifyZeroInteractions(intygService);
            verifyZeroInteractions(copyUtkastService);
        }
    }

    @Test(expected = AuthoritiesException.class)
    public void testRevokeReplaceSignedIntygMissingPrivilegeMakuleraIntyg() {
        WebCertUser user = new WebCertUser();
        addFeatures(user, CERTIFICATE_TYPE, WebcertFeature.MAKULERA_INTYG, WebcertFeature.KOPIERA_INTYG);
        addPrivileges(user, CERTIFICATE_TYPE, AuthoritiesConstants.PRIVILEGE_KOPIERA_INTYG);
        user.setOrigin("NORMAL");
        when(webcertUserService.getUser()).thenReturn(user);

        try {
            moduleApiController.revokeReplaceSignedIntyg(CERTIFICATE_TYPE, CERTIFICATE_ID, null);
        } finally {
            verifyZeroInteractions(intygService);
            verifyZeroInteractions(copyUtkastService);
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void testRevokeReplaceSignedIntygMissingPersonnummer() {
        WebCertUser user = new WebCertUser();
        addFeatures(user, CERTIFICATE_TYPE, WebcertFeature.MAKULERA_INTYG, WebcertFeature.KOPIERA_INTYG);
        addPrivileges(user, CERTIFICATE_TYPE, AuthoritiesConstants.PRIVILEGE_KOPIERA_INTYG, AuthoritiesConstants.PRIVILEGE_MAKULERA_INTYG);
        user.setOrigin("NORMAL");

        RevokeReplaceSignedIntygRequest revokeReplaceRequest = new RevokeReplaceSignedIntygRequest();
        revokeReplaceRequest.setCopyIntygRequest(new CopyIntygRequest());

        when(webcertUserService.getUser()).thenReturn(user);
        try {
            moduleApiController.revokeReplaceSignedIntyg(CERTIFICATE_TYPE, CERTIFICATE_ID, revokeReplaceRequest);
        } finally {
            verifyZeroInteractions(intygService);
            verifyZeroInteractions(copyUtkastService);
        }
    }

    @Test
    public void testCreateNewCopy() {
        final String personnummer = "191212121212";
        final String newIntygId = "newIntygId";

        CopyIntygRequest copyIntygRequest = new CopyIntygRequest();
        copyIntygRequest.setPatientPersonnummer(new Personnummer(personnummer));

        WebCertUser user = new WebCertUser();
        addFeatures(user, CERTIFICATE_TYPE, WebcertFeature.KOPIERA_INTYG);
        addPrivileges(user, CERTIFICATE_TYPE, AuthoritiesConstants.PRIVILEGE_KOPIERA_INTYG);
        user.setOrigin("NORMAL");

        ArgumentCaptor<CreateNewDraftCopyRequest> captor = ArgumentCaptor.forClass(CreateNewDraftCopyRequest.class);
        when(copyUtkastService.createCopy(captor.capture())).thenReturn(new CreateNewDraftCopyResponse(CERTIFICATE_TYPE, newIntygId));
        when(webcertUserService.getUser()).thenReturn(user);

        Response response = moduleApiController.createNewCopy(copyIntygRequest, CERTIFICATE_TYPE, CERTIFICATE_ID);

        verify(copyUtkastService, times(1)).createCopy(any());
        verifyNoMoreInteractions(copyUtkastService);
        assertEquals(newIntygId, ((CopyIntygResponse) response.getEntity()).getIntygsUtkastId());
        assertEquals(personnummer, captor.getValue().getPatient().getPersonId().getPersonnummer());
        assertNull(captor.getValue().getPatient().getFornamn());
        assertNull(captor.getValue().getPatient().getEfternamn());
        assertNull(captor.getValue().getPatient().getMellannamn());
        assertNull(captor.getValue().getPatient().getPostadress());
        assertNull(captor.getValue().getPatient().getPostnummer());
        assertNull(captor.getValue().getPatient().getPostort());
        assertNull(captor.getValue().getNyttPatientPersonnummer());
    }

    @Test
    public void testCreateNewCopyWithNewPatientInfo() {
        final String personnummer = "191212121212";
        final String newIntygId = "newIntygId";
        final String efternamn = "efternamn";
        final String fornamn = "fornamn";
        final String mellannamn = "mellannamn";
        final String postadress = "postadress";
        final String postort = "postort";
        final String postnummer = "postnummer";
        final String newPersonnummer = "newPersonnummer";

        CopyIntygRequest copyIntygRequest = new CopyIntygRequest();
        copyIntygRequest.setPatientPersonnummer(new Personnummer(personnummer));
        copyIntygRequest.setNyttPatientPersonnummer(new Personnummer(newPersonnummer));
        copyIntygRequest.setEfternamn(efternamn);
        copyIntygRequest.setFornamn(fornamn);
        copyIntygRequest.setMellannamn(mellannamn);
        copyIntygRequest.setPostadress(postadress);
        copyIntygRequest.setPostort(postort);
        copyIntygRequest.setPostnummer(postnummer);

        WebCertUser user = new WebCertUser();
        addFeatures(user, CERTIFICATE_TYPE, WebcertFeature.KOPIERA_INTYG);
        addPrivileges(user, CERTIFICATE_TYPE, AuthoritiesConstants.PRIVILEGE_KOPIERA_INTYG);
        user.setOrigin("NORMAL");

        ArgumentCaptor<CreateNewDraftCopyRequest> captor = ArgumentCaptor.forClass(CreateNewDraftCopyRequest.class);
        when(copyUtkastService.createCopy(captor.capture())).thenReturn(new CreateNewDraftCopyResponse(CERTIFICATE_TYPE, newIntygId));
        when(webcertUserService.getUser()).thenReturn(user);

        Response response = moduleApiController.createNewCopy(copyIntygRequest, CERTIFICATE_TYPE, CERTIFICATE_ID);

        verify(copyUtkastService, times(1)).createCopy(any());
        verifyNoMoreInteractions(copyUtkastService);
        assertEquals(newIntygId, ((CopyIntygResponse) response.getEntity()).getIntygsUtkastId());
        assertEquals(personnummer, captor.getValue().getPatient().getPersonId().getPersonnummer());
        assertEquals(fornamn, captor.getValue().getPatient().getFornamn());
        assertEquals(efternamn, captor.getValue().getPatient().getEfternamn());
        assertEquals(mellannamn, captor.getValue().getPatient().getMellannamn());
        assertEquals(postadress, captor.getValue().getPatient().getPostadress());
        assertEquals(postnummer, captor.getValue().getPatient().getPostnummer());
        assertEquals(postort, captor.getValue().getPatient().getPostort());
        assertEquals(newPersonnummer, captor.getValue().getNyttPatientPersonnummer().getPersonnummer());
    }

    @Test(expected = AuthoritiesException.class)
    public void testCreateNewCopyMissingFeature() {
        CopyIntygRequest copyIntygRequest = new CopyIntygRequest();

        WebCertUser user = new WebCertUser();
        addPrivileges(user, CERTIFICATE_TYPE, AuthoritiesConstants.PRIVILEGE_KOPIERA_INTYG);
        user.setOrigin("NORMAL");
        when(webcertUserService.getUser()).thenReturn(user);

        try {
            moduleApiController.createNewCopy(copyIntygRequest, CERTIFICATE_TYPE, CERTIFICATE_ID);
        } finally {
            verifyZeroInteractions(copyUtkastService);
        }
    }

    @Test(expected = AuthoritiesException.class)
    public void testCreateNewCopyMissingPrivilege() {
        CopyIntygRequest copyIntygRequest = new CopyIntygRequest();

        WebCertUser user = new WebCertUser();
        addFeatures(user, CERTIFICATE_TYPE, WebcertFeature.KOPIERA_INTYG);
        user.setOrigin("NORMAL");
        when(webcertUserService.getUser()).thenReturn(user);

        try {
            moduleApiController.createNewCopy(copyIntygRequest, CERTIFICATE_TYPE, CERTIFICATE_ID);
        } finally {
            verifyZeroInteractions(copyUtkastService);
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void testCreateNewCopyInvalidRequest() {
        CopyIntygRequest copyIntygRequest = new CopyIntygRequest();

        WebCertUser user = new WebCertUser();
        addFeatures(user, CERTIFICATE_TYPE, WebcertFeature.KOPIERA_INTYG);
        addPrivileges(user, CERTIFICATE_TYPE, AuthoritiesConstants.PRIVILEGE_KOPIERA_INTYG);
        user.setOrigin("NORMAL");

        when(webcertUserService.getUser()).thenReturn(user);

        try {
            moduleApiController.createNewCopy(copyIntygRequest, CERTIFICATE_TYPE, CERTIFICATE_ID);
        } finally {
            verifyZeroInteractions(copyUtkastService);
        }
    }

    @Test
    public void testCreateNewCompletion() {
        final String personnummer = "191212121212";
        final String newIntygId = "newIntygId";
        final String meddelandeId = "meddelandeId";
        final String newPersonnummer = "newPersonnummer";
        final String efternamn = "efternamn";
        final String fornamn = "fornamn";
        final String mellannamn = "mellannamn";
        final String postadress = "postadress";
        final String postort = "postort";
        final String postnummer = "postnummer";

        final CopyIntygRequest request = new CopyIntygRequest();
        request.setPatientPersonnummer(new Personnummer(personnummer));
        request.setNyttPatientPersonnummer(new Personnummer(newPersonnummer));
        request.setEfternamn(efternamn);
        request.setFornamn(fornamn);
        request.setMellannamn(mellannamn);
        request.setPostadress(postadress);
        request.setPostort(postort);
        request.setPostnummer(postnummer);

        WebCertUser user = new WebCertUser();
        addFeatures(user, CERTIFICATE_TYPE, WebcertFeature.KOPIERA_INTYG);
        addPrivileges(user, CERTIFICATE_TYPE, AuthoritiesConstants.PRIVILEGE_SVARA_MED_NYTT_INTYG);
        user.setOrigin("NORMAL");

        ArgumentCaptor<CreateCompletionCopyRequest> captor = ArgumentCaptor.forClass(CreateCompletionCopyRequest.class);
        when(copyUtkastService.createCompletion(captor.capture())).thenReturn(new CreateCompletionCopyResponse(CERTIFICATE_TYPE, newIntygId, CERTIFICATE_ID));
        when(webcertUserService.getUser()).thenReturn(user);

        Response response = moduleApiController.createCompletion(request, CERTIFICATE_TYPE, CERTIFICATE_ID, meddelandeId);

        verify(copyUtkastService, times(1)).createCompletion(any());
        verifyNoMoreInteractions(copyUtkastService);
        assertEquals(newIntygId, ((CopyIntygResponse) response.getEntity()).getIntygsUtkastId());
        assertEquals(personnummer, captor.getValue().getPatient().getPersonId().getPersonnummer());
        assertEquals(fornamn, captor.getValue().getPatient().getFornamn());
        assertEquals(efternamn, captor.getValue().getPatient().getEfternamn());
        assertEquals(mellannamn, captor.getValue().getPatient().getMellannamn());
        assertEquals(postadress, captor.getValue().getPatient().getPostadress());
        assertEquals(postnummer, captor.getValue().getPatient().getPostnummer());
        assertEquals(postort, captor.getValue().getPatient().getPostort());
        assertEquals(newPersonnummer, captor.getValue().getNyttPatientPersonnummer().getPersonnummer());
    }

    @Test(expected = AuthoritiesException.class)
    public void testCreateNewCompletionMissingFeature() {
        WebCertUser user = new WebCertUser();
        addPrivileges(user, CERTIFICATE_TYPE, AuthoritiesConstants.PRIVILEGE_SVARA_MED_NYTT_INTYG);
        user.setOrigin("NORMAL");

        when(webcertUserService.getUser()).thenReturn(user);

        try {
            moduleApiController.createCompletion(null, CERTIFICATE_TYPE, null, null);
        } finally {
            verifyZeroInteractions(copyUtkastService);
        }
    }

    @Test(expected = AuthoritiesException.class)
    public void testCreateNewCompletionMissingPrivilege() {
        WebCertUser user = new WebCertUser();
        addFeatures(user, CERTIFICATE_TYPE, WebcertFeature.KOPIERA_INTYG);
        user.setOrigin("NORMAL");

        when(webcertUserService.getUser()).thenReturn(user);

        try {
            moduleApiController.createCompletion(null, CERTIFICATE_TYPE, null, null);
        } finally {
            verifyZeroInteractions(copyUtkastService);
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void testCreateNewCompletionMissingRequest() {
        WebCertUser user = new WebCertUser();
        addFeatures(user, CERTIFICATE_TYPE, WebcertFeature.KOPIERA_INTYG);
        addPrivileges(user, CERTIFICATE_TYPE, AuthoritiesConstants.PRIVILEGE_SVARA_MED_NYTT_INTYG);
        user.setOrigin("NORMAL");

        when(webcertUserService.getUser()).thenReturn(user);

        try {
            moduleApiController.createCompletion(new CopyIntygRequest(), CERTIFICATE_TYPE, null, null);
        } finally {
            verifyZeroInteractions(copyUtkastService);
        }
    }

    @Test
    public void testCreateRenewal() {
        final String newDraftIntygId = "newDraftIntygId";
        final String personnummer = "191212121212";
        final String newPersonnummer = "newPersonnummer";
        final String efternamn = "efternamn";
        final String fornamn = "fornamn";
        final String mellannamn = "mellannamn";
        final String postadress = "postadress";
        final String postort = "postort";
        final String postnummer = "postnummer";

        CopyIntygRequest request = new CopyIntygRequest();
        request.setPatientPersonnummer(new Personnummer(personnummer));
        request.setNyttPatientPersonnummer(new Personnummer(newPersonnummer));
        request.setEfternamn(efternamn);
        request.setFornamn(fornamn);
        request.setMellannamn(mellannamn);
        request.setPostadress(postadress);
        request.setPostort(postort);
        request.setPostnummer(postnummer);

        WebCertUser user = new WebCertUser();
        addFeatures(user, CERTIFICATE_TYPE, WebcertFeature.KOPIERA_INTYG);
        addPrivileges(user, CERTIFICATE_TYPE, AuthoritiesConstants.PRIVILEGE_KOPIERA_INTYG);
        user.setOrigin("NORMAL");

        ArgumentCaptor<CreateRenewalCopyRequest> captor = ArgumentCaptor.forClass(CreateRenewalCopyRequest.class);
        when(copyUtkastService.createRenewalCopy(captor.capture())).thenReturn(new CreateRenewalCopyResponse(CERTIFICATE_TYPE, newDraftIntygId, CERTIFICATE_ID));
        when(webcertUserService.getUser()).thenReturn(user);

        Response response = moduleApiController.createRenewal(request, CERTIFICATE_TYPE, CERTIFICATE_ID);

        verify(copyUtkastService, times(1)).createRenewalCopy(any());
        verifyNoMoreInteractions(copyUtkastService);
        assertEquals(newDraftIntygId, ((CopyIntygResponse) response.getEntity()).getIntygsUtkastId());
        assertEquals(personnummer, captor.getValue().getPatient().getPersonId().getPersonnummer());
        assertEquals(fornamn, captor.getValue().getPatient().getFornamn());
        assertEquals(efternamn, captor.getValue().getPatient().getEfternamn());
        assertEquals(mellannamn, captor.getValue().getPatient().getMellannamn());
        assertEquals(postadress, captor.getValue().getPatient().getPostadress());
        assertEquals(postnummer, captor.getValue().getPatient().getPostnummer());
        assertEquals(postort, captor.getValue().getPatient().getPostort());
        assertEquals(newPersonnummer, captor.getValue().getNyttPatientPersonnummer().getPersonnummer());
    }

    @Test(expected = AuthoritiesException.class)
    public void testCreateRenewalMissingFeature() {
        WebCertUser user = new WebCertUser();
        addPrivileges(user, CERTIFICATE_TYPE, AuthoritiesConstants.PRIVILEGE_KOPIERA_INTYG);
        user.setOrigin("NORMAL");

        when(webcertUserService.getUser()).thenReturn(user);

        try {
            moduleApiController.createRenewal(null, CERTIFICATE_TYPE, CERTIFICATE_ID);
        } finally {
            verifyZeroInteractions(copyUtkastService);
        }
    }

    @Test(expected = AuthoritiesException.class)
    public void testCreateRenewalMissingPrivilege() {
        WebCertUser user = new WebCertUser();
        addFeatures(user, CERTIFICATE_TYPE, WebcertFeature.KOPIERA_INTYG);
        user.setOrigin("NORMAL");

        when(webcertUserService.getUser()).thenReturn(user);

        try {
            moduleApiController.createRenewal(null, CERTIFICATE_TYPE, CERTIFICATE_ID);
        } finally {
            verifyZeroInteractions(copyUtkastService);
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void testCreateRenewalInvalidRequest() {
        WebCertUser user = new WebCertUser();
        addFeatures(user, CERTIFICATE_TYPE, WebcertFeature.KOPIERA_INTYG);
        addPrivileges(user, CERTIFICATE_TYPE, AuthoritiesConstants.PRIVILEGE_KOPIERA_INTYG);
        user.setOrigin("NORMAL");

        when(webcertUserService.getUser()).thenReturn(user);

        try {
            moduleApiController.createRenewal(new CopyIntygRequest(), CERTIFICATE_TYPE, CERTIFICATE_ID);
        } finally {
            verifyZeroInteractions(copyUtkastService);
        }
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

    private void addFeatures(WebCertUser user, String intygType, WebcertFeature... features) {
        user.setFeatures(Stream.of(features).map(WebcertFeature::getName).collect(Collectors.toSet()));
        user.getFeatures().addAll(Stream.of(features).map(f -> f.getName() + "." + intygType).collect(Collectors.toSet()));
    }

    private void addPrivileges(WebCertUser user, String intygType, String... privileges) {
        user.setAuthorities(new HashMap<>());
        Privilege privilege = new Privilege();
        privilege.setIntygstyper(Arrays.asList(intygType));
        RequestOrigin requestOrigin = new RequestOrigin();
        requestOrigin.setName("NORMAL");
        requestOrigin.setIntygstyper(privilege.getIntygstyper());
        privilege.setRequestOrigins(Arrays.asList(requestOrigin));
        for (String privilegeString : privileges) {
            user.getAuthorities().put(privilegeString, privilege);
        }
    }
}
