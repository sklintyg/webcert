package se.inera.webcert.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.transform.stream.StreamSource;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareRequestType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponderInterface;
import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareResponderInterface;
import se.inera.certificate.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ObjectFactory;
import se.inera.certificate.integration.json.CustomObjectMapper;
import se.inera.certificate.model.Utlatande;
import se.inera.certificate.model.common.MinimalUtlatande;
import se.inera.certificate.modules.support.api.ModuleApi;
import se.inera.certificate.modules.support.api.dto.ExternalModelHolder;
import se.inera.certificate.modules.support.api.dto.ExternalModelResponse;
import se.inera.certificate.modules.support.api.dto.InternalModelResponse;
import se.inera.certificate.modules.support.api.dto.TransportModelHolder;
import se.inera.certificate.modules.support.api.exception.ModuleException;
import se.inera.webcert.modules.registry.IntygModuleRegistry;
import se.inera.webcert.service.dto.IntygContentHolder;
import se.inera.webcert.service.dto.IntygItem;
import se.inera.webcert.service.dto.UtlatandeCommonModelHolder;
import se.inera.webcert.service.exception.WebCertServiceException;
import se.inera.webcert.service.log.LogService;
import se.inera.webcert.test.NamespacePrefixNameIgnoringListener;
import se.inera.webcert.web.service.WebCertUserService;

/**
 * @author andreaskaltenbach
 */
@RunWith( MockitoJUnitRunner.class )
public class IntygServiceTest {

    private static final String CERTIFICATE_ID = "<id>";
    private static final String CERTIFICATE_TYPE = "fk7263";

    private static final String LOGICAL_ADDRESS = "<logicalAddress>";

    @Mock
    private GetCertificateForCareResponderInterface getCertificateForCareResponder;

    @Mock
    private ListCertificatesForCareResponderInterface listCertificatesForCareResponder;

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @Mock
    private ModuleApi moduleApi;

    @InjectMocks
    private IntygServiceImpl intygService = new IntygServiceImpl();

    @Mock
    private LogService logservice;

    private GetCertificateForCareResponseType intygtjanstResponse;

    private GetCertificateForCareResponseType intygtjanstErrorResponse;

    private ListCertificatesForCareResponseType listResponse;

    private ListCertificatesForCareResponseType listErrorResponse;

    private Utlatande utlatande;

    private String intygXml;

    @Mock
    private WebCertUserService webCertUserService;

    @Before
    public void setupIntygstjanstResponse() throws Exception {

        ClassPathResource response = new ClassPathResource("IntygServiceTest/response-get-certificate.xml");

        JAXBContext context = JAXBContext.newInstance(GetCertificateForCareResponseType.class);
        intygtjanstResponse = context.createUnmarshaller()
                .unmarshal(new StreamSource(response.getInputStream()), GetCertificateForCareResponseType.class)
                .getValue();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        context.createMarshaller().marshal(new ObjectFactory().createUtlatande(intygtjanstResponse.getCertificate()),
                outputStream);
        intygXml = new String(outputStream.toByteArray());

        ClassPathResource errorResponse = new ClassPathResource("IntygServiceTest/response-get-certificate-error.xml");
        intygtjanstErrorResponse = context.createUnmarshaller()
                .unmarshal(new StreamSource(errorResponse.getInputStream()), GetCertificateForCareResponseType.class)
                .getValue();

        utlatande = new CustomObjectMapper().readValue(
                new ClassPathResource("IntygServiceTest/utlatande.json").getFile(), MinimalUtlatande.class);
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
        when(webCertUserService.isAuthorizedForUnit(any(String.class))).thenReturn(true);
    }

    @Before
    public void setupLogicalAddress() {
        intygService.logicalAddress = LOGICAL_ADDRESS;
    }

    @Test
    public void testFetchIntyg() throws Exception {

        // setup intygstjansten WS mock to return intyg information
        GetCertificateForCareRequestType request = new GetCertificateForCareRequestType();
        request.setCertificateId(CERTIFICATE_ID);
        when(getCertificateForCareResponder.getCertificateForCare(LOGICAL_ADDRESS, request)).thenReturn(
                intygtjanstResponse);

        // setup module Rest API factory to return a mocked module Rest API
        when(moduleRegistry.getModuleApi(CERTIFICATE_TYPE)).thenReturn(moduleApi);

        ExternalModelResponse unmarshallResponse = new ExternalModelResponse("<external-json/>", utlatande);
        // setup module API behavior for conversion from transport to external
        when(moduleApi.unmarshall(any(TransportModelHolder.class))).thenReturn(unmarshallResponse);

        InternalModelResponse convertResponse = new InternalModelResponse("<internal-json/>");
        // setup module API behavior for conversion from external to internal
        when(moduleApi.convertExternalToInternal(any(ExternalModelHolder.class))).thenReturn(convertResponse);

        IntygContentHolder intygData = intygService.fetchIntygData(CERTIFICATE_ID);

        // ensure that correct WS call is made to intygstjanst
        verify(getCertificateForCareResponder).getCertificateForCare(LOGICAL_ADDRESS, request);

        // ensure correct module lookup is done with module registry
        verify(moduleRegistry, times(2)).getModuleApi("fk7263");

        // ensure that correct utlatande XML is sent to module to convert from transport to external format
        //verify(moduleApi).unmarshall(argThat(new UtlatandeXmlMatcher()));

        // ensure that correct JSON data is sent to module to convert from external to internal format
//        ArgumentCaptor<IntygContentHolder> captor = ArgumentCaptor.forClass(IntygContentHolder.class);
//        verify(moduleApi).convertExternalToInternal(captor.capture());
//        assertEquals("<externalJson>", captor.getValue().getCertificateContent());
//        assertEquals("123", captor.getValue().getCertificateContentMeta().getId());
//        assertEquals("fk7263", captor.getValue().getCertificateContentMeta().getType());

        assertEquals("<internal-json/>", intygData.getContents());
    }

    @Test( expected = WebCertServiceException.class )
    public void testFetchIntygWithFailingIntygstjanst() {

        // setup intygstjansten WS mock to return error response
        GetCertificateForCareRequestType request = new GetCertificateForCareRequestType();
        request.setCertificateId(CERTIFICATE_ID);
        when(getCertificateForCareResponder.getCertificateForCare(LOGICAL_ADDRESS, request)).thenReturn(
                intygtjanstErrorResponse);

        intygService.fetchIntygData(CERTIFICATE_ID);
    }

    @Test( expected = WebCertServiceException.class )
    public void testFetchIntygWithFailingAuth() {
        // setup intygstjansten WS mock to return success response
        GetCertificateForCareRequestType request = new GetCertificateForCareRequestType();
        request.setCertificateId(CERTIFICATE_ID);
        when(getCertificateForCareResponder.getCertificateForCare(LOGICAL_ADDRESS, request)).thenReturn(
                intygtjanstResponse);
        when(webCertUserService.isAuthorizedForUnit(any(String.class))).thenReturn(false);

        intygService.fetchIntygData(CERTIFICATE_ID);
    }

    @SuppressWarnings("unchecked")
    @Test( expected = WebCertServiceException.class )
    public void testFetchIntygWithFailingUnmarshalling() throws Exception {

        // setup intygstjansten WS mock to return intyg information
        GetCertificateForCareRequestType request = new GetCertificateForCareRequestType();
        request.setCertificateId(CERTIFICATE_ID);
        when(getCertificateForCareResponder.getCertificateForCare(LOGICAL_ADDRESS, request)).thenReturn(
                intygtjanstResponse);

        // setup module Rest API factory to return a mocked module Rest API
        when(moduleRegistry.getModuleApi(CERTIFICATE_TYPE)).thenReturn(moduleApi);

        when(moduleApi.unmarshall(any(TransportModelHolder.class))).thenThrow(ModuleException.class);

        intygService.fetchIntygData(CERTIFICATE_ID);
    }

    @SuppressWarnings("unchecked")
    @Test( expected = WebCertServiceException.class )
    public void testFetchIntygWithFailingExternalToInternalTransformation() throws Exception {

        // setup intygstjansten WS mock to return intyg information
        GetCertificateForCareRequestType request = new GetCertificateForCareRequestType();
        request.setCertificateId(CERTIFICATE_ID);
        when(getCertificateForCareResponder.getCertificateForCare(LOGICAL_ADDRESS, request)).thenReturn(
                intygtjanstResponse);

        // setup module Rest API factory to return a mocked module Rest API
        when(moduleRegistry.getModuleApi(CERTIFICATE_TYPE)).thenReturn(moduleApi);

        // setup module API behaviour for conversion from transport to external
        ExternalModelResponse unmarshallResponse = new ExternalModelResponse("<external-json/>", utlatande);
        when(moduleApi.unmarshall(any(TransportModelHolder.class))).thenReturn(unmarshallResponse);

        // setup module API behaviour for conversion from external to internal
        when(moduleApi.convertExternalToInternal(any(ExternalModelHolder.class))).thenThrow(ModuleException.class);

        intygService.fetchIntygData(CERTIFICATE_ID);
    }

    @Ignore
    @Test
    public void testFetchIntygCommonModel() throws Exception {

        // setup intygstjansten WS mock to return intyg information
        GetCertificateForCareRequestType request = new GetCertificateForCareRequestType();
        request.setCertificateId(CERTIFICATE_ID);
        when(getCertificateForCareResponder.getCertificateForCare(LOGICAL_ADDRESS, request)).thenReturn(
                intygtjanstResponse);

        // setup module Rest API factory to return a mocked module Rest API
        when(moduleRegistry.getModuleApi(CERTIFICATE_TYPE)).thenReturn(moduleApi);

        // setup module API behaviour for conversion from transport to external
        ExternalModelResponse unmarshallResponse = new ExternalModelResponse("<external-json/>", utlatande);
        when(moduleApi.unmarshall(any(TransportModelHolder.class))).thenReturn(unmarshallResponse);

        UtlatandeCommonModelHolder intygData = intygService.fetchIntygCommonModel(CERTIFICATE_ID);

        // ensure that correct WS call is made to intygstjanst
        verify(getCertificateForCareResponder).getCertificateForCare(LOGICAL_ADDRESS, request);

        // ensure correct module lookup is done with module Rest API factory
        verify(moduleRegistry).getIntygModule("fk7263");

        // ensure that correct utlatande XML is sent to module to convert from transport to external format
        //verify(moduleApi).unmarshall(argThat(new UtlatandeXmlMatcher()));

        //assertEquals(utlatandeAsString, new CustomObjectMapper().writeValueAsString(intygData.getUtlatande()));
    }

    @Test( expected = WebCertServiceException.class )
    public void testFetchIntygCommonModelWithFailingIntygstjanst() {

        // setup intygstjansten WS mock to return error response
        GetCertificateForCareRequestType request = new GetCertificateForCareRequestType();
        request.setCertificateId(CERTIFICATE_ID);
        when(getCertificateForCareResponder.getCertificateForCare(LOGICAL_ADDRESS, request)).thenReturn(
                intygtjanstErrorResponse);

        intygService.fetchIntygCommonModel(CERTIFICATE_ID);
    }

    @Test( expected = WebCertServiceException.class )
    public void testFetchIntygCommonModelWithFailingAuth() {
        // setup intygstjansten WS mock to return success response
        GetCertificateForCareRequestType request = new GetCertificateForCareRequestType();
        request.setCertificateId(CERTIFICATE_ID);
        when(getCertificateForCareResponder.getCertificateForCare(LOGICAL_ADDRESS, request)).thenReturn(
                intygtjanstResponse);
        when(webCertUserService.isAuthorizedForUnit(any(String.class))).thenReturn(false);
        intygService.fetchIntygCommonModel(CERTIFICATE_ID);
    }

    @SuppressWarnings("unchecked")
    @Test( expected = WebCertServiceException.class )
    public void testFetchIntygCommonModelWithFailingUnmarshalling() throws Exception {

        // setup intygstjansten WS mock to return intyg information
        GetCertificateForCareRequestType request = new GetCertificateForCareRequestType();
        request.setCertificateId(CERTIFICATE_ID);
        when(getCertificateForCareResponder.getCertificateForCare(LOGICAL_ADDRESS, request)).thenReturn(
                intygtjanstResponse);

        // setup module Rest API factory to return a mocked module Rest API
        when(moduleRegistry.getModuleApi(CERTIFICATE_TYPE)).thenReturn(moduleApi);

        // setup module API behaviour for conversion from transport to external
        when(moduleApi.unmarshall(any(TransportModelHolder.class))).thenThrow(ModuleException.class);

        intygService.fetchIntygCommonModel(CERTIFICATE_ID);
    }

    @Test
    public void testListIntyg() {
        // setup intygstjansten WS mock to return intyg information
        ListCertificatesForCareType request = new ListCertificatesForCareType();
        request.setNationalIdentityNumber("19121212-1212");
        request.getCareUnit().add("enhet-1");
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
        assertEquals("SENT", meta.getStatuses().get(0).getType());
        assertEquals("2012-01-01T10:00:00.000", meta.getStatuses().get(0).getTimestamp().toString());
    }

    @Test( expected = WebCertServiceException.class )
    public void testListIntygWithIntygstjanstReturningError() {

         // setup intygstjansten WS mock to return intyg information
        ListCertificatesForCareType request = new ListCertificatesForCareType();
        request.setNationalIdentityNumber("19121212-1212");
        request.getCareUnit().add("enhet-1");
        when(listCertificatesForCareResponder.listCertificatesForCare(LOGICAL_ADDRESS, request)).thenReturn(
                listErrorResponse);

        intygService.listIntyg(Collections.singletonList("enhet-1"), "19121212-1212");
    }

    private class UtlatandeXmlMatcher extends ArgumentMatcher<String> {

        public boolean matches(Object o) {
            String xml = (String) o;

            XMLUnit.setIgnoreWhitespace(true);
            XMLUnit.setNormalizeWhitespace(true);

            try {
                Diff diff = new Diff(intygXml, xml);
                diff.overrideDifferenceListener(new NamespacePrefixNameIgnoringListener());
                return diff.identical();
            } catch (Exception e) {
                return false;
            }
        }
    }

}
