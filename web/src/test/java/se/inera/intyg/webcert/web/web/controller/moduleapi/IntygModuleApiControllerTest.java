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
package se.inera.intyg.webcert.web.web.controller.moduleapi;

import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.core.Response;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import se.inera.intyg.common.fk7263.model.internal.Fk7263Utlatande;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Feature;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.RequestOrigin;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygPdf;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygServiceResult;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.CopyUtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateCompletionCopyRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateCompletionCopyResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateRenewalCopyRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateRenewalCopyResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateReplacementCopyRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateReplacementCopyResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateUtkastFromTemplateRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateUtkastFromTemplateResponse;
import se.inera.intyg.webcert.web.service.utkast.util.CopyUtkastServiceHelper;
import se.inera.intyg.webcert.web.web.controller.api.dto.CopyIntygRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.CopyIntygResponse;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.RevokeSignedIntygParameter;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.SendSignedIntygParameter;
import se.inera.intyg.webcert.web.web.util.resourcelinks.ResourceLinkHelper;

/**
 * @author andreaskaltenbach
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class IntygModuleApiControllerTest {

    private static final String CERTIFICATE_ID = "123456";
    private static final String CERTIFICATE_TYPE = "fk7263";

    private static final byte[] PDF_DATA = "<pdf-data>".getBytes();
    private static final String PDF_NAME = "the-file.pdf";

    private static final String CONTENT_DISPOSITION = "Content-Disposition";
    private static final String PERSON_ID = "19121212-1212";
    private static final String CERTIFICATE_VERSION = "1.0";

    private static Fk7263Utlatande utlatande = null;
    private static Patient patient;

    @Mock
    private IntygService intygService;
    @Mock
    private CopyUtkastService copyUtkastService;
    @Mock
    private WebCertUserService webcertUserService;
    @Mock
    private PatientDetailsResolver patientDetailsResolver;
    @Mock
    private AuthoritiesHelper authoritiesHelper;
    @Mock
    private ArendeService arendeService;
    @Mock
    private IntygTextsService intygTextService;
    @Mock
    private ResourceLinkHelper resourceLinkHelper;

    @Spy
    private CopyUtkastServiceHelper copyUtkastServiceHelper = new CopyUtkastServiceHelper();

    @InjectMocks
    private IntygModuleApiController moduleApiController = new IntygModuleApiController();

    @BeforeClass
    public static void setupCertificateData() {
        utlatande = new Fk7263Utlatande();
        utlatande.setId(CERTIFICATE_ID);
        utlatande.setTyp(CERTIFICATE_TYPE);
        GrundData grundData = new GrundData();
        patient = new Patient();
        patient.setPersonId(createPnr(PERSON_ID));
        patient.setFornamn("Fornamn");
        patient.setEfternamn("Efternamn");
        grundData.setPatient(patient);
        utlatande.setGrundData(grundData);

        List<Status> status = new ArrayList<>();
        status.add(new Status(CertificateState.RECEIVED, "HSVARD", LocalDateTime.now()));
        status.add(new Status(CertificateState.SENT, "FKASSA", LocalDateTime.now()));
    }

    @Before
    public void setup() {
        copyUtkastServiceHelper.setWebCertUserService(webcertUserService);
    }

    @Test
    public void testGetIntygAsPdfForInternetExplorer() {
        final String intygType = "fk7263";
        setupUser(AuthoritiesConstants.PRIVILEGE_VISA_INTYG, intygType, false, true, AuthoritiesConstants.FEATURE_UTSKRIFT);
        IntygPdf pdfResponse = new IntygPdf(PDF_DATA, PDF_NAME);

        final var request = new MockHttpServletRequest();
        request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; Touch; rv:11.0) like Gecko");

        when(intygService.fetchIntygAsPdf(CERTIFICATE_ID, intygType, false)).thenReturn(pdfResponse);

        Response response = moduleApiController.getIntygAsPdf(intygType, CERTIFICATE_ID, request);

        verify(intygService).fetchIntygAsPdf(CERTIFICATE_ID, intygType, false);

        assertEquals(OK.getStatusCode(), response.getStatus());
        assertEquals(PDF_DATA, response.getEntity());
        assertEquals(List.of("attachment; filename=\"" + PDF_NAME + "\""), response.getHeaders().get(CONTENT_DISPOSITION));
    }

    @Test
    public void testGetIntygAsPdf() {
        final String intygType = "lisjp";
        setupUser(AuthoritiesConstants.PRIVILEGE_VISA_INTYG, intygType, false, true, AuthoritiesConstants.FEATURE_UTSKRIFT);
        IntygPdf pdfResponse = new IntygPdf(PDF_DATA, PDF_NAME);

        final var request = new MockHttpServletRequest();
        request.addHeader("User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36");

        when(intygService.fetchIntygAsPdf(CERTIFICATE_ID, intygType, false)).thenReturn(pdfResponse);

        Response response = moduleApiController.getIntygAsPdf(intygType, CERTIFICATE_ID, request);

        verify(intygService).fetchIntygAsPdf(CERTIFICATE_ID, intygType, false);

        assertEquals(OK.getStatusCode(), response.getStatus());
        assertEquals(PDF_DATA, response.getEntity());
        assertEquals(List.of("inline"), response.getHeaders().get(CONTENT_DISPOSITION));
    }

    @Test
    public void testGetIntygAsPdfForEmployerForInternetExplorer() {
        final String intygType = "fk7263";
        setupUser(AuthoritiesConstants.PRIVILEGE_VISA_INTYG, intygType, false, true, AuthoritiesConstants.FEATURE_ARBETSGIVARUTSKRIFT);
        IntygPdf pdfResponse = new IntygPdf(PDF_DATA, PDF_NAME);

        final var request = new MockHttpServletRequest();
        request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; Touch; rv:11.0) like Gecko");

        when(intygService.fetchIntygAsPdf(CERTIFICATE_ID, intygType, true)).thenReturn(pdfResponse);

        Response response = moduleApiController.getIntygAsPdfForEmployer(intygType, CERTIFICATE_ID, request);

        verify(intygService).fetchIntygAsPdf(CERTIFICATE_ID, intygType, true);

        assertEquals(OK.getStatusCode(), response.getStatus());
        assertEquals(PDF_DATA, response.getEntity());
        assertEquals(List.of("attachment; filename=\"" + PDF_NAME + "\""), response.getHeaders().get(CONTENT_DISPOSITION));
    }

    @Test
    public void testGetIntygAsPdfForEmployer() {
        final String intygType = "lisjp";
        setupUser(AuthoritiesConstants.PRIVILEGE_VISA_INTYG, intygType, false, true, AuthoritiesConstants.FEATURE_ARBETSGIVARUTSKRIFT);
        IntygPdf pdfResponse = new IntygPdf(PDF_DATA, PDF_NAME);

        final var request = new MockHttpServletRequest();
        request.addHeader("User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36");

        when(intygService.fetchIntygAsPdf(CERTIFICATE_ID, intygType, true)).thenReturn(pdfResponse);

        Response response = moduleApiController.getIntygAsPdfForEmployer(intygType, CERTIFICATE_ID, request);

        verify(intygService).fetchIntygAsPdf(CERTIFICATE_ID, intygType, true);

        assertEquals(OK.getStatusCode(), response.getStatus());
        assertEquals(PDF_DATA, response.getEntity());
        assertEquals(List.of("inline"), response.getHeaders().get(CONTENT_DISPOSITION));
    }

    @Test
    public void testGetIntyg() {
        final String intygsTyp = "fk7263";
        final String intygContent = "CONTENTS";

        setupUser(AuthoritiesConstants.PRIVILEGE_VISA_INTYG, intygsTyp, false, true);

        IntygContentHolder content = mock(IntygContentHolder.class);
        when(content.getContents()).thenReturn(intygContent);
        when(intygService.fetchIntygDataWithRelations(eq(CERTIFICATE_ID), eq(intygsTyp))).thenReturn(content);

        Response response = moduleApiController.getIntyg(intygsTyp, CERTIFICATE_ID);

        assertEquals(OK.getStatusCode(), response.getStatus());
        assertEquals(intygContent, ((IntygContentHolder) response.getEntity()).getContents());
        verify(intygService).fetchIntygDataWithRelations(eq(CERTIFICATE_ID), eq(intygsTyp));
    }

    @Test
    public void testGetIntygWithCoherentJournaling() {
        final String intygsTyp = "fk7263";
        final String intygContent = "CONTENTS";

        setupUser(AuthoritiesConstants.PRIVILEGE_VISA_INTYG, intygsTyp, true, true);

        IntygContentHolder content = mock(IntygContentHolder.class);
        when(content.getContents()).thenReturn(intygContent);
        when(intygService.fetchIntygDataWithRelations(eq(CERTIFICATE_ID), eq(intygsTyp))).thenReturn(content);

        Response response = moduleApiController.getIntyg(intygsTyp, CERTIFICATE_ID);

        assertEquals(OK.getStatusCode(), response.getStatus());
        assertEquals(intygContent, ((IntygContentHolder) response.getEntity()).getContents());
        verify(intygService).fetchIntygDataWithRelations(eq(CERTIFICATE_ID), eq(intygsTyp));
    }

    @Test
    public void testSendSignedIntyg() {
        final String intygType = "fk7263";
        final String recipient = "recipient";

        setupUser("", intygType, false, true, AuthoritiesConstants.FEATURE_SKICKA_INTYG);

        when(intygService.sendIntyg(eq(CERTIFICATE_ID), eq(intygType), eq(recipient), eq(false))).thenReturn(IntygServiceResult.OK);

        SendSignedIntygParameter param = new SendSignedIntygParameter();
        param.setRecipient(recipient);
        Response response = moduleApiController.sendSignedIntyg(intygType, CERTIFICATE_ID, param);

        verify(intygService).sendIntyg(eq(CERTIFICATE_ID), eq(intygType), eq(recipient), eq(false));
        assertEquals(OK.getStatusCode(), response.getStatus());
        assertEquals(IntygServiceResult.OK, response.getEntity());
    }

    @Test
    public void testRevokeSignedIntyg() {
        final String intygType = "fk7263";
        final String revokeMessage = "revokeMessage";
        final String revokeReason = "revokeReason";

        setupUser(AuthoritiesConstants.PRIVILEGE_MAKULERA_INTYG, intygType, false, true, AuthoritiesConstants.FEATURE_MAKULERA_INTYG);

        when(intygService.revokeIntyg(CERTIFICATE_ID, intygType, revokeMessage, revokeReason)).thenReturn(IntygServiceResult.OK);

        RevokeSignedIntygParameter param = new RevokeSignedIntygParameter();
        param.setMessage(revokeMessage);
        param.setReason(revokeReason);
        Response response = moduleApiController.revokeSignedIntyg(intygType, CERTIFICATE_ID, param);

        verify(intygService).revokeIntyg(CERTIFICATE_ID, intygType, revokeMessage, revokeReason);
        assertEquals(OK.getStatusCode(), response.getStatus());
        assertEquals(IntygServiceResult.OK, response.getEntity());
    }

    @Test(expected = WebCertServiceException.class)
    public void testRevokeSignedIntygMissingParameter() {
        final String intygType = "fk7263";
        final String revokeMessage = "";
        final String revokeReason = "";

        setupUser(AuthoritiesConstants.PRIVILEGE_MAKULERA_INTYG, intygType, false, true, AuthoritiesConstants.FEATURE_MAKULERA_INTYG,
            AuthoritiesConstants.FEATURE_MAKULERA_INTYG_KRAVER_ANLEDNING);

        RevokeSignedIntygParameter param = new RevokeSignedIntygParameter();
        param.setMessage(revokeMessage);
        param.setReason(revokeReason);
        moduleApiController.revokeSignedIntyg(intygType, CERTIFICATE_ID, param);
    }

    @Test
    public void testRevokeSignedIntygReasonNotRequired() {
        final String intygType = "fk7263";
        final String revokeMessage = "";
        final String revokeReason = "";

        setupUser(AuthoritiesConstants.PRIVILEGE_MAKULERA_INTYG, intygType, false, true, AuthoritiesConstants.FEATURE_MAKULERA_INTYG);

        when(intygService.revokeIntyg(CERTIFICATE_ID, intygType, revokeMessage, revokeReason)).thenReturn(IntygServiceResult.OK);

        RevokeSignedIntygParameter param = new RevokeSignedIntygParameter();
        param.setMessage(revokeMessage);
        param.setReason(revokeReason);
        Response response = moduleApiController.revokeSignedIntyg(intygType, CERTIFICATE_ID, param);

        verify(intygService).revokeIntyg(CERTIFICATE_ID, intygType, revokeMessage, revokeReason);
        assertEquals(OK.getStatusCode(), response.getStatus());
        assertEquals(IntygServiceResult.OK, response.getEntity());
    }

    @Test
    public void testCreateRenewalWithNewPatientInfo() {
        final String personnummer = "191212121212";
        final String newIntygId = "newIntygId";
        final String efternamn = "efternamn";
        final String fornamn = "fornamn";
        final String mellannamn = "mellannamn";
        final String postadress = "postadress";
        final String postort = "postort";
        final String postnummer = "postnummer";
        final String newPersonnummer = "201212121212";

        CopyIntygRequest copyIntygRequest = new CopyIntygRequest();
        copyIntygRequest.setPatientPersonnummer(createPnr(personnummer));

        WebCertUser user = new WebCertUser();
        user.setParameters(new IntegrationParameters(null, null,
            newPersonnummer, fornamn, mellannamn, efternamn, postadress, postnummer, postort,
            false, false, false, true));
        addFeatures(user, CERTIFICATE_TYPE, AuthoritiesConstants.FEATURE_FORNYA_INTYG);
        addPrivileges(user, CERTIFICATE_TYPE, AuthoritiesConstants.PRIVILEGE_FORNYA_INTYG);
        user.setOrigin("NORMAL");

        ArgumentCaptor<CreateRenewalCopyRequest> captor = ArgumentCaptor.forClass(CreateRenewalCopyRequest.class);
        when(copyUtkastService.createRenewalCopy(captor.capture()))
            .thenReturn(new CreateRenewalCopyResponse(CERTIFICATE_TYPE, CERTIFICATE_VERSION, newIntygId, CERTIFICATE_ID));
        when(webcertUserService.getUser()).thenReturn(user);

        Response response = moduleApiController.createRenewal(copyIntygRequest, CERTIFICATE_TYPE, CERTIFICATE_ID);

        verify(copyUtkastService).createRenewalCopy(any());
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

    /**
     * Verify that a non-valid personnr/samordningsnummer (i.e a "reservnummer") supplied as NyttPatientPersonnummer
     * will not be applied to the new utkast.
     */
    @Test
    public void testCreateRenewalCopyWithNewPatientReservnummerDefaultsToPreviousPersonnummer() {
        final String personnummer = "191212121212";
        final String newIntygId = "newIntygId";
        final String efternamn = "efternamn";
        final String fornamn = "fornamn";
        final String mellannamn = "mellannamn";
        final String postadress = "postadress";
        final String postort = "postort";
        final String postnummer = "postnummer";
        final String newReservnummer = "A20090122";

        CopyIntygRequest copyIntygRequest = new CopyIntygRequest();
        copyIntygRequest.setPatientPersonnummer(createPnr(personnummer));

        WebCertUser user = new WebCertUser();
        user.setParameters(new IntegrationParameters(null, null, newReservnummer, fornamn, mellannamn, efternamn, postadress, postnummer,
            postort, false, false, false, true));
        addFeatures(user, CERTIFICATE_TYPE, AuthoritiesConstants.FEATURE_FORNYA_INTYG);
        addPrivileges(user, CERTIFICATE_TYPE, AuthoritiesConstants.PRIVILEGE_FORNYA_INTYG);
        user.setOrigin("NORMAL");

        ArgumentCaptor<CreateRenewalCopyRequest> captor = ArgumentCaptor.forClass(CreateRenewalCopyRequest.class);
        when(copyUtkastService.createRenewalCopy(captor.capture()))
            .thenReturn(new CreateRenewalCopyResponse(CERTIFICATE_TYPE, CERTIFICATE_VERSION, newIntygId, CERTIFICATE_ID));
        when(webcertUserService.getUser()).thenReturn(user);

        Response response = moduleApiController.createRenewal(copyIntygRequest, CERTIFICATE_TYPE, CERTIFICATE_ID);

        verify(copyUtkastService).createRenewalCopy(any());
        verifyNoMoreInteractions(copyUtkastService);
        assertEquals(newIntygId, ((CopyIntygResponse) response.getEntity()).getIntygsUtkastId());
        assertEquals(personnummer, captor.getValue().getPatient().getPersonId().getPersonnummer());
        assertEquals(fornamn, captor.getValue().getPatient().getFornamn());
        assertEquals(efternamn, captor.getValue().getPatient().getEfternamn());
        assertEquals(mellannamn, captor.getValue().getPatient().getMellannamn());
        assertEquals(postadress, captor.getValue().getPatient().getPostadress());
        assertEquals(postnummer, captor.getValue().getPatient().getPostnummer());
        assertEquals(postort, captor.getValue().getPatient().getPostort());
        assertNull(captor.getValue().getNyttPatientPersonnummer());
    }

    @Test(expected = WebCertServiceException.class)
    public void testCreateRenewalCopyInvalidRequest() {
        CopyIntygRequest copyIntygRequest = new CopyIntygRequest();

        WebCertUser user = new WebCertUser();
        addFeatures(user, CERTIFICATE_TYPE, AuthoritiesConstants.FEATURE_FORNYA_INTYG);
        addPrivileges(user, CERTIFICATE_TYPE, AuthoritiesConstants.PRIVILEGE_FORNYA_INTYG);
        user.setOrigin("NORMAL");

        when(webcertUserService.getUser()).thenReturn(user);

        try {
            moduleApiController.createRenewal(copyIntygRequest, CERTIFICATE_TYPE, CERTIFICATE_ID);
        } finally {
            verifyNoInteractions(copyUtkastService);
        }
    }

    @Test
    public void testReplaceIntyg() {
        final String personnummer = "191212121212";
        final String newIntygId = "newIntygId";

        CopyIntygRequest copyIntygRequest = new CopyIntygRequest();
        copyIntygRequest.setPatientPersonnummer(createPnr(personnummer));

        WebCertUser user = new WebCertUser();
        addFeatures(user, CERTIFICATE_TYPE, AuthoritiesConstants.FEATURE_FORNYA_INTYG);
        addPrivileges(user, CERTIFICATE_TYPE, AuthoritiesConstants.PRIVILEGE_ERSATTA_INTYG);
        user.setOrigin("NORMAL");

        ArgumentCaptor<CreateReplacementCopyRequest> captor = ArgumentCaptor.forClass(CreateReplacementCopyRequest.class);
        when(copyUtkastService.createReplacementCopy(captor.capture()))
            .thenReturn(new CreateReplacementCopyResponse(CERTIFICATE_TYPE, CERTIFICATE_VERSION, newIntygId, CERTIFICATE_ID));
        when(webcertUserService.getUser()).thenReturn(user);

        Response response = moduleApiController.createReplacement(copyIntygRequest, CERTIFICATE_TYPE, CERTIFICATE_ID);

        verify(copyUtkastService).createReplacementCopy(any());
        verifyNoMoreInteractions(copyUtkastService);
        assertEquals(newIntygId, ((CopyIntygResponse) response.getEntity()).getIntygsUtkastId());
    }

    @Test
    public void testCreateNewCompletion() {
        final String personnummer = "191212121212";
        final String newIntygId = "newIntygId";
        final String meddelandeId = "meddelandeId";
        final String newPersonnummer = "201212121212";
        final String efternamn = "efternamn";
        final String fornamn = "fornamn";
        final String mellannamn = "mellannamn";
        final String postadress = "postadress";
        final String postort = "postort";
        final String postnummer = "postnummer";

        final CopyIntygRequest request = new CopyIntygRequest();
        request.setPatientPersonnummer(createPnr(personnummer));

        WebCertUser user = new WebCertUser();
        user.setParameters(new IntegrationParameters(null, null, newPersonnummer, fornamn, mellannamn, efternamn, postadress, postnummer,
            postort, false, false, false, true));
        addFeatures(user, CERTIFICATE_TYPE, AuthoritiesConstants.FEATURE_FORNYA_INTYG);
        addPrivileges(user, CERTIFICATE_TYPE, AuthoritiesConstants.PRIVILEGE_SVARA_MED_NYTT_INTYG);
        user.setOrigin("NORMAL");

        ArgumentCaptor<CreateCompletionCopyRequest> captor = ArgumentCaptor.forClass(CreateCompletionCopyRequest.class);
        when(copyUtkastService.createCompletion(captor.capture()))
            .thenReturn(new CreateCompletionCopyResponse(CERTIFICATE_TYPE, CERTIFICATE_VERSION, newIntygId, CERTIFICATE_ID));
        when(webcertUserService.getUser()).thenReturn(user);

        Response response = moduleApiController.createCompletion(request, CERTIFICATE_TYPE, CERTIFICATE_ID);

        verify(copyUtkastService).createCompletion(any());
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

    @Test(expected = WebCertServiceException.class)
    public void testCreateNewCompletionMissingRequest() {
        WebCertUser user = new WebCertUser();
        addFeatures(user, CERTIFICATE_TYPE, AuthoritiesConstants.FEATURE_FORNYA_INTYG);
        addPrivileges(user, CERTIFICATE_TYPE, AuthoritiesConstants.PRIVILEGE_SVARA_MED_NYTT_INTYG);
        user.setOrigin("NORMAL");

        when(webcertUserService.getUser()).thenReturn(user);

        try {
            moduleApiController.createCompletion(new CopyIntygRequest(), CERTIFICATE_TYPE, null);
        } finally {
            verifyNoInteractions(copyUtkastService);
        }
    }

    @Test
    public void testCreateRenewal() {
        final String newDraftIntygId = "newDraftIntygId";
        final String personnummer = "191212121212";
        final String newPersonnummer = "201212121212";
        final String efternamn = "efternamn";
        final String fornamn = "fornamn";
        final String mellannamn = "mellannamn";
        final String postadress = "postadress";
        final String postort = "postort";
        final String postnummer = "postnummer";

        CopyIntygRequest request = new CopyIntygRequest();
        request.setPatientPersonnummer(createPnr(personnummer));

        WebCertUser user = new WebCertUser();
        user.setParameters(new IntegrationParameters(null, null, newPersonnummer, fornamn, mellannamn, efternamn, postadress, postnummer,
            postort, false, false, false, true));
        addFeatures(user, CERTIFICATE_TYPE, AuthoritiesConstants.FEATURE_FORNYA_INTYG);
        addPrivileges(user, CERTIFICATE_TYPE, AuthoritiesConstants.PRIVILEGE_FORNYA_INTYG);
        user.setOrigin("NORMAL");

        ArgumentCaptor<CreateRenewalCopyRequest> captor = ArgumentCaptor.forClass(CreateRenewalCopyRequest.class);
        when(copyUtkastService.createRenewalCopy(captor.capture()))
            .thenReturn(new CreateRenewalCopyResponse(CERTIFICATE_TYPE, CERTIFICATE_VERSION, newDraftIntygId, CERTIFICATE_ID));
        when(webcertUserService.getUser()).thenReturn(user);

        Response response = moduleApiController.createRenewal(request, CERTIFICATE_TYPE, CERTIFICATE_ID);

        verify(copyUtkastService).createRenewalCopy(any());
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

    @Test(expected = WebCertServiceException.class)
    public void testCreateRenewalInvalidRequest() {
        WebCertUser user = new WebCertUser();
        addFeatures(user, CERTIFICATE_TYPE, AuthoritiesConstants.FEATURE_FORNYA_INTYG);
        addPrivileges(user, CERTIFICATE_TYPE, AuthoritiesConstants.PRIVILEGE_FORNYA_INTYG);
        user.setOrigin("NORMAL");

        when(webcertUserService.getUser()).thenReturn(user);

        try {
            moduleApiController.createRenewal(new CopyIntygRequest(), CERTIFICATE_TYPE, CERTIFICATE_ID);
        } finally {
            verifyNoInteractions(copyUtkastService);
        }
    }

    @Test
    public void testCreateUtkastFromTemplate() {
        final String personnummer = "191212121212";
        final String newIntygId = "newIntygId";
        final String newCertificateType = "newCertificateType";

        CopyIntygRequest copyIntygRequest = new CopyIntygRequest();
        copyIntygRequest.setPatientPersonnummer(createPnr(personnummer));

        WebCertUser user = new WebCertUser();
        addFeatures(user, newCertificateType, AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST);
        addPrivileges(user, newCertificateType, AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG);
        user.setOrigin("NORMAL");

        ArgumentCaptor<CreateUtkastFromTemplateRequest> captor = ArgumentCaptor.forClass(CreateUtkastFromTemplateRequest.class);
        when(copyUtkastService.createUtkastFromSignedTemplate(captor.capture()))
            .thenReturn(new CreateUtkastFromTemplateResponse(newCertificateType, CERTIFICATE_VERSION, newIntygId, CERTIFICATE_ID));
        when(webcertUserService.getUser()).thenReturn(user);

        Response response = moduleApiController
            .createUtkastFromSignedTemplate(copyIntygRequest, CERTIFICATE_TYPE, CERTIFICATE_ID, newCertificateType);

        verify(copyUtkastService).createUtkastFromSignedTemplate(any());
        verifyNoMoreInteractions(copyUtkastService);
        assertEquals(newIntygId, ((CopyIntygResponse) response.getEntity()).getIntygsUtkastId());
        assertEquals(newCertificateType, ((CopyIntygResponse) response.getEntity()).getIntygsTyp());
    }

    @Test(expected = WebCertServiceException.class)
    public void testCreateUtkastFromTemplateInvalidRequest() {
        WebCertUser user = new WebCertUser();
        addFeatures(user, CERTIFICATE_TYPE, AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST);
        addPrivileges(user, CERTIFICATE_TYPE, AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG);
        user.setOrigin("NORMAL");

        when(webcertUserService.getUser()).thenReturn(user);

        try {
            moduleApiController.createUtkastFromSignedTemplate(new CopyIntygRequest(), CERTIFICATE_TYPE, CERTIFICATE_ID, CERTIFICATE_TYPE);
        } finally {
            verifyNoInteractions(copyUtkastService);
        }
    }

    private void setupUser(String privilegeString, String intygType, boolean coherentJournaling, boolean fornyaOk,
        String... features) {
        WebCertUser user = new WebCertUser();
        user.setAuthorities(new HashMap<>());
        addFeatures(user, intygType, features);
        user.setParameters(
            new IntegrationParameters(null, null, null, null, null, null, null, null, null, coherentJournaling, false, false,
                fornyaOk));
        Privilege privilege = new Privilege();
        privilege.setIntygstyper(Collections.singletonList(intygType));
        RequestOrigin requestOrigin = new RequestOrigin();
        requestOrigin.setName("NORMAL");
        requestOrigin.setIntygstyper(privilege.getIntygstyper());
        privilege.setRequestOrigins(Collections.singletonList(requestOrigin));
        user.getAuthorities().put(privilegeString, privilege);
        user.setOrigin("NORMAL");
        when(webcertUserService.getUser()).thenReturn(user);
    }

    private void addFeatures(WebCertUser user, String intygType, String... features) {
        user.setFeatures(Stream.of(features).collect(Collectors.toMap(Function.identity(), s -> {
            Feature feature = new Feature();
            feature.setName(s);
            feature.setIntygstyper(Collections.singletonList(intygType));
            feature.setGlobal(true);
            return feature;
        })));
    }

    private void addPrivileges(WebCertUser user, String intygType, String... privileges) {
        user.setAuthorities(new HashMap<>());
        Privilege privilege = new Privilege();
        privilege.setIntygstyper(Collections.singletonList(intygType));
        RequestOrigin requestOrigin = new RequestOrigin();
        requestOrigin.setName("NORMAL");
        requestOrigin.setIntygstyper(privilege.getIntygstyper());
        privilege.setRequestOrigins(Collections.singletonList(requestOrigin));
        for (String privilegeString : privileges) {
            user.getAuthorities().put(privilegeString, privilege);
        }
    }

    private static Personnummer createPnr(String personId) {
        return Personnummer.createPersonnummer(personId)
            .orElseThrow(() -> new IllegalArgumentException("Could not parse passed personnummer: " + personId));
    }

}
