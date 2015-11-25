package se.inera.intyg.webcert.web.service.intyg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.WebServiceException;

import org.apache.commons.io.IOUtils;
import org.apache.cxf.helpers.FileUtils;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

import se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateMetaData;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateResponse;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.webcert.persistence.utkast.model.Signatur;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.intyg.converter.IntygModuleFacade;
import se.inera.intyg.webcert.web.service.intyg.converter.IntygModuleFacadeException;
import se.inera.intyg.webcert.web.service.intyg.converter.IntygServiceConverter;
import se.inera.intyg.webcert.web.service.intyg.converter.IntygServiceConverterImpl;
import se.inera.intyg.webcert.web.service.intyg.decorator.UtkastIntygDecorator;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygItem;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygItemListResponse;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygPdf;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareType;

/**
 * @author andreaskaltenbach
 */
@RunWith(MockitoJUnitRunner.class)
public class IntygServiceTest {

    private static final String HSA_ID = "HSA-123";
    private static final String CREATED_BY_NAME = "Läkare Läkarsson";
    private static final String SENAST_SPARAD_NAME = "Spara Sparasson";
    private static final String CERTIFICATE_ID = "123";
    private static final String CERTIFICATE_TYPE = "fk7263";

    private static final String LOGICAL_ADDRESS = "<logicalAddress>";


    @Mock
    private ListCertificatesForCareResponderInterface listCertificatesForCareResponder;

    @Mock
    private IntygModuleFacade moduleFacade;

    @Mock
    private UtkastRepository intygRepository;

    @Mock
    private UtkastIntygDecorator utkastIntygDecorator;

    @Spy
    private IntygServiceConverter serviceConverter = new IntygServiceConverterImpl();

    @Mock
    private LogService logservice;

    @InjectMocks
    private IntygServiceImpl intygService = new IntygServiceImpl();

    private ListCertificatesForCareResponseType listResponse;

    private ListCertificatesForCareResponseType listErrorResponse;

    private String json;
    private VardpersonReferens vardpersonReferens = new VardpersonReferens();

    @Mock
    private WebCertUserService webCertUserService;

    @Mock
    private MonitoringLogService mockMonitoringService;

    @Before
    public void setupIntygstjanstResponse() throws Exception {

        vardpersonReferens.setHsaId(HSA_ID);
        vardpersonReferens.setNamn(CREATED_BY_NAME);

        json = FileUtils.getStringFromFile(new ClassPathResource("IntygServiceTest/utlatande.json").getFile());
        Utlatande utlatande = new CustomObjectMapper().readValue(json, Utlatande.class);
        CertificateMetaData metaData = new CertificateMetaData();
        metaData.setStatus(new ArrayList<Status>());
        CertificateResponse certificateResponse = new CertificateResponse(json, utlatande, metaData, false);
        when(moduleFacade.getCertificate(any(String.class), any(String.class))).thenReturn(certificateResponse);
    }

    @Before
    public void setupIntygstjanstListResponse() throws Exception {

        ClassPathResource response = new ClassPathResource("IntygServiceTest/response-list-certificates.xml");

        JAXBContext context = JAXBContext.newInstance(ListCertificatesForCareResponseType.class);
        listResponse = context.createUnmarshaller()
                .unmarshal(new StreamSource(response.getInputStream()), ListCertificatesForCareResponseType.class)
                .getValue();

        ClassPathResource errorResponse = new ClassPathResource("IntygServiceTest/response-list-certificates-error.xml");
        listErrorResponse = context.createUnmarshaller()
                .unmarshal(new StreamSource(errorResponse.getInputStream()), ListCertificatesForCareResponseType.class)
                .getValue();
    }

    @Before
    public void setupDefaultAuthorization() {
        when(webCertUserService.isAuthorizedForUnit(any(String.class), any(String.class), eq(true))).thenReturn(true);

        WebCertUser lakareUser = mock(WebCertUser.class);
        Set<String> set = new HashSet<>();
        set.add("fk7263");
        when(lakareUser.getIntygsTyper()).thenReturn(set);
        when(webCertUserService.getUser()).thenReturn(lakareUser);
    }

    @Before
    public void setupLogicalAddress() {
        intygService.setLogicalAddress(LOGICAL_ADDRESS);
    }

    @Before
    public void setupObjectMapperForIntygServiceConverter() {
        ((IntygServiceConverterImpl) serviceConverter).setObjectMapper(new CustomObjectMapper());
    }

    @Test
    public void testFetchIntyg() throws Exception {

        IntygContentHolder intygData = intygService.fetchIntygData(CERTIFICATE_ID, CERTIFICATE_TYPE);

        // ensure that correctcall is made to intygstjanst
        verify(moduleFacade).getCertificate(CERTIFICATE_ID, CERTIFICATE_TYPE);

        verify(mockMonitoringService).logIntygRead(CERTIFICATE_ID, CERTIFICATE_TYPE);

        assertEquals(json, intygData.getContents());
        assertEquals(CERTIFICATE_ID, intygData.getUtlatande().getId());
        assertEquals("19121212-1212", intygData.getUtlatande().getGrundData().getPatient().getPersonId().getPersonnummer());

    }

    @Test(expected = WebCertServiceException.class)
    public void testFetchIntygWithFailingIntygstjanst() throws IntygModuleFacadeException {

        when(moduleFacade.getCertificate(any(String.class), any(String.class))).thenThrow(new IntygModuleFacadeException(""));

        intygService.fetchIntygData(CERTIFICATE_ID, CERTIFICATE_TYPE);
    }

    @Test(expected = WebCertServiceException.class)
    public void testFetchIntygWithFailingAuth() {
        when(webCertUserService.isAuthorizedForUnit(any(String.class), any(String.class), eq(true))).thenReturn(false);

        intygService.fetchIntygData(CERTIFICATE_ID, CERTIFICATE_TYPE);
    }

    @Test
    public void testFetchIntygData() throws Exception {

        intygService.fetchIntygData(CERTIFICATE_ID, CERTIFICATE_TYPE);

        // ensure that correct call is made to moduleFacade
        verify(moduleFacade).getCertificate(CERTIFICATE_ID, CERTIFICATE_TYPE);
        // Assert pdl log
        verify(logservice).logReadIntyg(any(LogRequest.class));
        // Assert monitoring log
        verify(mockMonitoringService).logIntygRead(CERTIFICATE_ID, CERTIFICATE_TYPE);
    }

    @Test
    public void testListIntyg() {
        // setup intygstjansten WS mock to return intyg information
        ListCertificatesForCareType request = new ListCertificatesForCareType();
        request.setPersonId("19121212-1212");
        request.getEnhet().add("enhet-1");

        when(listCertificatesForCareResponder.listCertificatesForCare(eq(LOGICAL_ADDRESS), any(ListCertificatesForCareType.class))).thenReturn(
                listResponse);

        IntygItemListResponse intygItemListResponse = intygService.listIntyg(Collections.singletonList("enhet-1"), new Personnummer("19121212-1212"));

        ArgumentCaptor<ListCertificatesForCareType> argument = ArgumentCaptor.forClass(ListCertificatesForCareType.class);

        verify(listCertificatesForCareResponder).listCertificatesForCare(eq(LOGICAL_ADDRESS), argument.capture());

        ListCertificatesForCareType actualRequest = argument.getValue();
        assertEquals(request.getPersonId(), actualRequest.getPersonId());
        assertEquals(request.getEnhet(), actualRequest.getEnhet());

        assertEquals(2, intygItemListResponse.getIntygItemList().size());

        IntygItem meta = intygItemListResponse.getIntygItemList().get(0);

        assertEquals("1", meta.getId());
        assertEquals("fk7263", meta.getType());
        assertEquals("2012-01-01", meta.getFromDate().toString());
        assertEquals("2012-02-02", meta.getTomDate().toString());
        assertEquals(1, meta.getStatuses().size());
        assertEquals("FK", meta.getStatuses().get(0).getTarget());
        assertEquals(CertificateState.SENT, meta.getStatuses().get(0).getType());
        assertEquals("2012-01-01T10:00:00.000", meta.getStatuses().get(0).getTimestamp().toString());
    }

    @Test(expected = WebCertServiceException.class)
    public void testListIntygWithIntygstjanstReturningError() {

        // setup intygstjansten WS mock to return intyg information
        ListCertificatesForCareType request = new ListCertificatesForCareType();
        request.setPersonId("19121212-1212");
        request.getEnhet().add("enhet-1");
        when(listCertificatesForCareResponder.listCertificatesForCare(eq(LOGICAL_ADDRESS), any(ListCertificatesForCareType.class))).thenReturn(
                listErrorResponse);

        intygService.listIntyg(Collections.singletonList("enhet-1"), new Personnummer("19121212-1212"));
    }

    @Test
    public void testListIntygWithIntygstjanstUnavailable() throws IOException {

        // setup intygstjansten WS mock to throw WebServiceException
        ListCertificatesForCareType request = new ListCertificatesForCareType();
        request.setPersonId("19121212-1212");
        request.getEnhet().add("enhet-1");
        when(listCertificatesForCareResponder.listCertificatesForCare(eq(LOGICAL_ADDRESS), any(ListCertificatesForCareType.class))).thenThrow(
                WebServiceException.class);
        when(intygRepository.findDraftsByPatientAndEnhetAndStatus(anyString(), anyList(), anyList(), anySet())).thenReturn(buildDraftList(false, null, null));

        IntygItemListResponse intygItemListResponse = intygService.listIntyg(Collections.singletonList("enhet-1"), new Personnummer("19121212-1212"));
        assertNotNull(intygItemListResponse);
        assertEquals(1, intygItemListResponse.getIntygItemList().size());

        // Assert pdl log not performed, e.g. listing is not a PDL loggable op.
        verifyZeroInteractions(logservice);
    }

    @Test
    public void testFetchIntygDataWhenIntygstjanstIsUnavailable() throws Exception {
        when(moduleFacade.getCertificate(CERTIFICATE_ID, CERTIFICATE_TYPE)).thenThrow(WebServiceException.class);
        when(intygRepository.findOne(CERTIFICATE_ID)).thenReturn(getDraft(CERTIFICATE_ID, null, null));
        IntygContentHolder intygContentHolder = intygService.fetchIntygData(CERTIFICATE_ID, CERTIFICATE_TYPE);
        assertEquals(intygContentHolder.getStatuses().size(), 1);
        assertNotNull(intygContentHolder.getUtlatande());

        // ensure that correct call is made to moduleFacade
        verify(moduleFacade).getCertificate(CERTIFICATE_ID, CERTIFICATE_TYPE);
        // Assert pdl log
        verify(logservice).logReadIntyg(any(LogRequest.class));
    }

    @Test
    public void testFetchIntygDataHasSentStatusWhenIntygstjanstIsUnavailableAndDraftHadSentDate() throws Exception {
        when(moduleFacade.getCertificate(CERTIFICATE_ID, CERTIFICATE_TYPE)).thenThrow(WebServiceException.class);
        when(intygRepository.findOne(CERTIFICATE_ID)).thenReturn(getDraft(CERTIFICATE_ID, LocalDateTime.now(), null));
        IntygContentHolder intygContentHolder = intygService.fetchIntygData(CERTIFICATE_ID, CERTIFICATE_TYPE);
        assertEquals(intygContentHolder.getStatuses().size(), 2);
        assertEquals(intygContentHolder.getStatuses().get(0).getType(), CertificateState.SENT);
        assertNotNull(intygContentHolder.getUtlatande());

        // ensure that correct call is made to moduleFacade
        verify(moduleFacade).getCertificate(CERTIFICATE_ID, CERTIFICATE_TYPE);
        // Assert pdl log
        verify(logservice).logReadIntyg(any(LogRequest.class));
    }

    @Test
    public void testFetchIntygDataHasSentAndRevokedStatusesWhenIntygstjanstIsUnavailableAndDraftHadSentDateAndRevokedDate() throws Exception {
        when(moduleFacade.getCertificate(CERTIFICATE_ID, CERTIFICATE_TYPE)).thenThrow(WebServiceException.class);
        when(intygRepository.findOne(CERTIFICATE_ID)).thenReturn(getDraft(CERTIFICATE_ID, LocalDateTime.now(), LocalDateTime.now()));
        IntygContentHolder intygContentHolder = intygService.fetchIntygData(CERTIFICATE_ID, CERTIFICATE_TYPE);
        assertEquals(intygContentHolder.getStatuses().size(), 3);
        assertEquals(intygContentHolder.getStatuses().get(0).getType(), CertificateState.SENT);
        assertEquals(intygContentHolder.getStatuses().get(1).getType(), CertificateState.CANCELLED);
        assertEquals(intygContentHolder.getStatuses().get(2).getType(), CertificateState.RECEIVED);
        assertNotNull(intygContentHolder.getUtlatande());

        // ensure that correct call is made to moduleFacade
        verify(moduleFacade).getCertificate(CERTIFICATE_ID, CERTIFICATE_TYPE);
        // Assert pdl log
        verify(logservice).logReadIntyg(any(LogRequest.class));
    }

    @Test(expected = WebCertServiceException.class)
    public void testFetchIntygDataFailsWhenIntygstjanstIsUnavailableAndUtkastInNotFound() throws Exception {
        when(moduleFacade.getCertificate(CERTIFICATE_ID, CERTIFICATE_TYPE)).thenThrow(WebServiceException.class);
        when(intygRepository.findOne(CERTIFICATE_ID)).thenReturn(null);
        try {
            intygService.fetchIntygData(CERTIFICATE_ID, CERTIFICATE_TYPE);
        } catch (Exception e) {
            // ensure that correct call is made to moduleFacade
            verify(moduleFacade).getCertificate(CERTIFICATE_ID, CERTIFICATE_TYPE);
            verify(intygRepository).findOne(CERTIFICATE_ID);
            // Assert pdl log
            verifyZeroInteractions(logservice);
            throw e;
        }
    }

    @Test
    public void testDraftAddedToListResponseIfUnique() throws Exception {
        when(intygRepository.findDraftsByPatientAndEnhetAndStatus(anyString(), anyList(), anyList(), anySet())).thenReturn(buildDraftList(true, null, null));

        when(listCertificatesForCareResponder.listCertificatesForCare(eq(LOGICAL_ADDRESS), any(ListCertificatesForCareType.class))).thenReturn(
                listResponse);

        IntygItemListResponse intygItemListResponse = intygService.listIntyg(Collections.singletonList("enhet-1"), new Personnummer("19121212-1212"));
        assertEquals(3, intygItemListResponse.getIntygItemList().size());
        verify(intygRepository).findDraftsByPatientAndEnhetAndStatus(anyString(), anyList(), anyList(), anySet());
    }

    @Test
    public void testDraftNotAddedToListResponseIfNotUnique() throws Exception {
        when(intygRepository.findDraftsByPatientAndEnhetAndStatus(anyString(), anyList(), anyList(), anySet())).thenReturn(buildDraftList(false, null, null));

        when(listCertificatesForCareResponder.listCertificatesForCare(eq(LOGICAL_ADDRESS), any(ListCertificatesForCareType.class))).thenReturn(
                listResponse);

        IntygItemListResponse intygItemListResponse = intygService.listIntyg(Collections.singletonList("enhet-1"), new Personnummer("19121212-1212"));
        assertEquals("Dr. Who", intygItemListResponse.getIntygItemList().get(0).getSignedBy());
        assertEquals(2, intygItemListResponse.getIntygItemList().size());
        verify(intygRepository).findDraftsByPatientAndEnhetAndStatus(anyString(), anyList(), anyList(), anySet());
    }

    @Test
    public void testDraftAddedWithSkapadAvNameIfMatching() throws Exception {
        when(intygRepository.findDraftsByPatientAndEnhetAndStatus(anyString(), anyList(), anyList(), anySet())).thenReturn(buildDraftList(true, vardpersonReferens, null));

        when(listCertificatesForCareResponder.listCertificatesForCare(eq(LOGICAL_ADDRESS), any(ListCertificatesForCareType.class))).thenReturn(
                listResponse);

        IntygItemListResponse intygItemListResponse = intygService.listIntyg(Collections.singletonList("enhet-1"), new Personnummer("19121212-1212"));
        assertEquals(3, intygItemListResponse.getIntygItemList().size());

        // Se till att posten vi lade till från "drafts" har fått namnet från Utkastet, inte signaturen där HsaId står.
        assertEquals(CREATED_BY_NAME, intygItemListResponse.getIntygItemList().get(2).getSignedBy());
        verify(intygRepository).findDraftsByPatientAndEnhetAndStatus(anyString(), anyList(), anyList(), anySet());
    }

    @Test
    public void testDraftAddedWithSenastSparadAvNameIfMatching() throws Exception {
        vardpersonReferens.setNamn(SENAST_SPARAD_NAME);
        when(intygRepository.findDraftsByPatientAndEnhetAndStatus(anyString(), anyList(), anyList(), anySet())).thenReturn(buildDraftList(true, null, vardpersonReferens));

        when(listCertificatesForCareResponder.listCertificatesForCare(eq(LOGICAL_ADDRESS), any(ListCertificatesForCareType.class))).thenReturn(
                listResponse);

        IntygItemListResponse intygItemListResponse = intygService.listIntyg(Collections.singletonList("enhet-1"), new Personnummer("19121212-1212"));
        assertEquals(3, intygItemListResponse.getIntygItemList().size());

        // Se till att posten vi lade till från "drafts" har fått namnet från Utkastet, inte signaturen där HsaId står.
        assertEquals(SENAST_SPARAD_NAME, intygItemListResponse.getIntygItemList().get(2).getSignedBy());
        verify(intygRepository).findDraftsByPatientAndEnhetAndStatus(anyString(), anyList(), anyList(), anySet());
    }

    @Test
    public void testDraftAddedWithHsaIdIfNoneMatching() throws Exception {
        vardpersonReferens.setNamn(SENAST_SPARAD_NAME);
        when(intygRepository.findDraftsByPatientAndEnhetAndStatus(anyString(), anyList(), anyList(), anySet())).thenReturn(buildDraftList(true, null, null));

        when(listCertificatesForCareResponder.listCertificatesForCare(eq(LOGICAL_ADDRESS), any(ListCertificatesForCareType.class))).thenReturn(
                listResponse);

        IntygItemListResponse intygItemListResponse = intygService.listIntyg(Collections.singletonList("enhet-1"), new Personnummer("19121212-1212"));
        assertEquals(3, intygItemListResponse.getIntygItemList().size());

        // Se till att posten vi lade till från "drafts" har fått namnet från Utkastet, inte signaturen där HsaId står.
        assertEquals(HSA_ID, intygItemListResponse.getIntygItemList().get(2).getSignedBy());
        verify(intygRepository).findDraftsByPatientAndEnhetAndStatus(anyString(), anyList(), anyList(), anySet());
    }

    @Test
    public void testFetchIntygAsPdfFromWebCertDraft() throws IOException, IntygModuleFacadeException {
        when(intygRepository.findOne(CERTIFICATE_ID)).thenReturn(getDraft(CERTIFICATE_ID, LocalDateTime.now(), LocalDateTime.now()));
        when(moduleFacade.convertFromInternalToPdfDocument(anyString(), anyString(), anyList(), anyBoolean())).thenReturn(buildPdfDocument());
        IntygPdf intygPdf = intygService.fetchIntygAsPdf(CERTIFICATE_ID, CERTIFICATE_TYPE, false);
        assertNotNull(intygPdf);

        verify(intygRepository, times(1)).findOne(anyString());
        verify(logservice).logPrintIntygAsPDF(any(LogRequest.class));
        verify(moduleFacade, times(0)).getCertificate(CERTIFICATE_ID, CERTIFICATE_TYPE);
    }

    @Test
    public void testFetchIntygAsPdfFromIntygstjansten() throws IOException, IntygModuleFacadeException {
        when(intygRepository.findOne(CERTIFICATE_ID)).thenReturn(null);
        when(moduleFacade.convertFromInternalToPdfDocument(anyString(), anyString(), anyList(), anyBoolean())).thenReturn(buildPdfDocument());
        IntygPdf intygPdf = intygService.fetchIntygAsPdf(CERTIFICATE_ID, CERTIFICATE_TYPE, false);
        assertNotNull(intygPdf);

        verify(logservice).logPrintIntygAsPDF(any(LogRequest.class));
        verify(intygRepository, times(1)).findOne(anyString());
        verify(moduleFacade, times(1)).getCertificate(CERTIFICATE_ID, CERTIFICATE_TYPE);
    }

    @Test(expected = WebCertServiceException.class)
    public void testFetchIntygAsPdfNoIntygFound() throws IntygModuleFacadeException {
        when(intygRepository.findOne(CERTIFICATE_ID)).thenReturn(null);
        when(moduleFacade.getCertificate(anyString(), anyString())).thenThrow(IntygModuleFacadeException.class);

        try {
            intygService.fetchIntygAsPdf(CERTIFICATE_ID, CERTIFICATE_TYPE, false);
        } catch (Exception e) {
            verify(moduleFacade, times(1)).getCertificate(anyString(), anyString());
            verify(intygRepository, times(2)).findOne(CERTIFICATE_ID);
            verifyZeroInteractions(logservice);
            throw e;
        }
    }

    private IntygPdf buildPdfDocument() {
        IntygPdf pdf = new IntygPdf("fake".getBytes(), "fakepdf.pdf");
        return pdf;
    }

    private List<Utkast> buildDraftList(boolean unique, VardpersonReferens skapadAv, VardpersonReferens senastSparadAv) throws IOException {
        List<Utkast> draftList = new ArrayList<>();
        Utkast draft = getDraft(unique ? "LONG-UNIQUE-ID" : "1", LocalDateTime.now(), null);
        draft.setSkapadAv(skapadAv);
        draft.setSenastSparadAv(senastSparadAv);
        draftList.add(draft);
        return draftList;
    }

    private Utkast getDraft(String intygsId, LocalDateTime sendDate, LocalDateTime revokeDate) throws IOException {
        Utkast utkast = new Utkast();
        String json = IOUtils.toString(new ClassPathResource(
                "FragaSvarServiceImplTest/utlatande.json").getInputStream(), "UTF-8");
        utkast.setModel(json);
        utkast.setIntygsId(intygsId);
        utkast.setSkickadTillMottagareDatum(sendDate);
        utkast.setAterkalladDatum(revokeDate);
        utkast.setStatus(UtkastStatus.SIGNED);
        Signatur signatur = new Signatur(LocalDateTime.now(), HSA_ID, CERTIFICATE_ID, "", "", "");
        utkast.setSignatur(signatur);

        return utkast;
    }
}
