package se.inera.webcert.service.intyg;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.transform.stream.StreamSource;

import org.apache.cxf.helpers.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

import se.inera.certificate.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareResponderInterface;
import se.inera.certificate.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareType;
import se.inera.certificate.integration.json.CustomObjectMapper;
import se.inera.certificate.model.CertificateState;
import se.inera.certificate.model.Status;
import se.inera.certificate.model.common.internal.Utlatande;
import se.inera.certificate.modules.support.api.dto.CertificateMetaData;
import se.inera.certificate.modules.support.api.dto.CertificateResponse;
import se.inera.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.webcert.service.exception.WebCertServiceException;
import se.inera.webcert.service.intyg.converter.IntygModuleFacade;
import se.inera.webcert.service.intyg.converter.IntygModuleFacadeException;
import se.inera.webcert.service.intyg.converter.IntygServiceConverter;
import se.inera.webcert.service.intyg.converter.IntygServiceConverterImpl;
import se.inera.webcert.service.intyg.dto.IntygContentHolder;
import se.inera.webcert.service.intyg.dto.IntygItem;
import se.inera.webcert.service.log.LogService;
import se.inera.webcert.web.service.WebCertUserService;

/**
 * @author andreaskaltenbach
 */
@RunWith(MockitoJUnitRunner.class)
public class IntygServiceTest {

    private static final String CERTIFICATE_ID = "123";
    private static final String CERTIFICATE_TYPE = "fk7263";

    private static final String LOGICAL_ADDRESS = "<logicalAddress>";

    @Mock
    private ListCertificatesForCareResponderInterface listCertificatesForCareResponder;

    @Mock
    private IntygModuleFacade moduleFacade;

    @Mock
    private UtkastRepository intygRepository;

    @Spy
    private IntygServiceConverter serviceConverter = new IntygServiceConverterImpl();

    @Mock
    private LogService logservice;

    @InjectMocks
    private IntygServiceImpl intygService = new IntygServiceImpl();

    private ListCertificatesForCareResponseType listResponse;

    private ListCertificatesForCareResponseType listErrorResponse;

    private String json;
    private Utlatande utlatande;
    private CertificateResponse certificateResponse;

    @Mock
    private WebCertUserService webCertUserService;

    @Before
    public void setupIntygstjanstResponse() throws Exception {

        json = FileUtils.getStringFromFile(new ClassPathResource("IntygServiceTest/utlatande.json").getFile());
        utlatande = new CustomObjectMapper().readValue(json, Utlatande.class);
        CertificateMetaData metaData = new CertificateMetaData();
        metaData.setStatus(new ArrayList<Status>());
        certificateResponse = new CertificateResponse(json, utlatande, metaData, false);
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
        when(webCertUserService.isAuthorizedForUnit(any(String.class), eq(true))).thenReturn(true);
    }

    @Before
    public void setupLogicalAddress() {
        intygService.setLogicalAddress(LOGICAL_ADDRESS);
    }

    @Test
    public void testFetchIntyg() throws Exception {

        IntygContentHolder intygData = intygService.fetchIntygData(CERTIFICATE_ID, CERTIFICATE_TYPE);

        // ensure that correctcall is made to intygstjanst
        verify(moduleFacade).getCertificate(CERTIFICATE_ID, CERTIFICATE_TYPE);

        assertEquals(json, intygData.getContents());
        assertEquals(CERTIFICATE_ID, intygData.getUtlatande().getId());
        assertEquals("19121212-1212", intygData.getUtlatande().getGrundData().getPatient().getPersonId());

    }

    @Test(expected = WebCertServiceException.class)
    public void testFetchIntygWithFailingIntygstjanst() throws IntygModuleFacadeException {

        when(moduleFacade.getCertificate(any(String.class), any(String.class))).thenThrow(new IntygModuleFacadeException(""));

        intygService.fetchIntygData(CERTIFICATE_ID, CERTIFICATE_TYPE);
    }

    @Test(expected = WebCertServiceException.class)
    public void testFetchIntygWithFailingAuth() {
        when(webCertUserService.isAuthorizedForUnit(any(String.class), eq(true))).thenReturn(false);

        intygService.fetchIntygData(CERTIFICATE_ID, CERTIFICATE_TYPE);
    }

    @Test
    public void testFetchIntygData() throws Exception {

        intygService.fetchIntygData(CERTIFICATE_ID, CERTIFICATE_TYPE);

        // ensure that correct call is made to moduleFacade
        verify(moduleFacade).getCertificate(CERTIFICATE_ID, CERTIFICATE_TYPE);
    }

    @Test
    public void testListIntyg() {
        // setup intygstjansten WS mock to return intyg information
        ListCertificatesForCareType request = new ListCertificatesForCareType();
        request.setPersonId("19121212-1212");
        request.getEnhet().add("enhet-1");
        when(listCertificatesForCareResponder.listCertificatesForCare(LOGICAL_ADDRESS, request)).thenReturn(
                listResponse);

        List<IntygItem> list = intygService.listIntyg(Collections.singletonList("enhet-1"), "19121212-1212");

        verify(listCertificatesForCareResponder).listCertificatesForCare(LOGICAL_ADDRESS, request);

        assertEquals(2, list.size());

        IntygItem meta = list.get(0);

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
        when(listCertificatesForCareResponder.listCertificatesForCare(LOGICAL_ADDRESS, request)).thenReturn(
                listErrorResponse);

        intygService.listIntyg(Collections.singletonList("enhet-1"), "19121212-1212");
    }
}
