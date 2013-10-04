package se.inera.webcert.service;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.w3.wsaddressing10.AttributedURIType;
import se.inera.certificate.common.v1.ObjectFactory;
import se.inera.certificate.integration.exception.ExternalWebServiceCallFailedException;
import se.inera.certificate.integration.rest.ModuleRestApi;
import se.inera.certificate.integration.rest.ModuleRestApiFactory;
import se.inera.certificate.integration.rest.dto.CertificateContentHolder;
import se.inera.certificate.integration.rest.exception.ModuleCallFailedException;
import se.inera.ifv.insuranceprocess.healthreporting.getcertificateforcare.v1.rivtabp20.GetCertificateForCareResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.getcertificateforcareresponder.v1.GetCertificateForCareRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.getcertificateforcareresponder.v1.GetCertificateForCareResponseType;
import se.inera.webcert.test.NamespacePrefixNameIgnoringListener;

/**
 * @author andreaskaltenbach
 */
@RunWith(MockitoJUnitRunner.class)
public class IntygServiceTest {

    private static final String CERTIFICATE_ID = "<id>";
    private static final String CERTIFICATE_TYPE = "fk7263";

    @Mock
    private GetCertificateForCareResponderInterface getCertificateForCareResponder;

    @Mock
    private ModuleRestApiFactory moduleRestApiFactory;

    @Mock
    private ModuleRestApi moduleRestApi;

    @InjectMocks
    private IntygService intygService = new IntygServiceImpl();

    private GetCertificateForCareResponseType intygtjanstResponse;
    private GetCertificateForCareResponseType intygtjanstErrorResponse;
    private String intygXml;

    @Before
    public void setupIntygstjanstResponse() throws Exception {

        ClassPathResource response = new ClassPathResource("IntygServiceTest/response-intygstjanst.xml");
        JAXBContext context = JAXBContext.newInstance(GetCertificateForCareResponseType.class);
        intygtjanstResponse = context.createUnmarshaller()
                .unmarshal(new StreamSource(response.getInputStream()), GetCertificateForCareResponseType.class)
                .getValue();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        context.createMarshaller().marshal(new ObjectFactory().createUtlatande(intygtjanstResponse.getCertificate()),
                outputStream);
        intygXml = new String(outputStream.toByteArray());

        ClassPathResource errorResponse = new ClassPathResource("IntygServiceTest/response-intygstjanst-error.xml");
        intygtjanstErrorResponse = context.createUnmarshaller()
                .unmarshal(new StreamSource(errorResponse.getInputStream()), GetCertificateForCareResponseType.class)
                .getValue();

    }

    @Test
    public void testFetchIntyg() {

        // setup intygstjansten WS mock to return intyg information
        GetCertificateForCareRequestType request = new GetCertificateForCareRequestType();
        request.setCertificateId(CERTIFICATE_ID);
        when(getCertificateForCareResponder.getCertificateForCare(any(AttributedURIType.class), eq(request)))
                .thenReturn(intygtjanstResponse);

        // setup module Rest API factory to return a mocked module Rest API
        when(moduleRestApiFactory.getModuleRestService(CERTIFICATE_TYPE)).thenReturn(moduleRestApi);

        // setup module API behaviour for conversion from transport to external
        Response unmarshallResponse = mock(Response.class);
        when(unmarshallResponse.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        when(unmarshallResponse.readEntity(String.class)).thenReturn("<externalJson>");
        when(moduleRestApi.unmarshall(any(String.class))).thenReturn(unmarshallResponse);

        // setup module API behaviour for conversion from external to internal
        Response externalToInternalResponse = mock(Response.class);
        when(externalToInternalResponse.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        when(externalToInternalResponse.readEntity(String.class)).thenReturn("<internalJson>");
        when(moduleRestApi.convertExternalToInternal(any(CertificateContentHolder.class))).thenReturn(
                externalToInternalResponse);

        String intygData = intygService.fetchIntygData(CERTIFICATE_ID);

        // ensure that correct WS call is made to intygstjanst
        verify(getCertificateForCareResponder).getCertificateForCare(any(AttributedURIType.class), eq(request));

        // ensure correct module lookup is done with module Rest API factory
        verify(moduleRestApiFactory).getModuleRestService("fk7263");

        // ensure that correct utlatande XML is sent to module to convert from transport to external format
        verify(moduleRestApi).unmarshall(argThat(new UtlatandeXmlMatcher()));

        // ensure that correct JSON data is sent to module to convert from external to internal format
        ArgumentCaptor<CertificateContentHolder> captor = ArgumentCaptor.forClass(CertificateContentHolder.class);
        verify(moduleRestApi).convertExternalToInternal(captor.capture());
        assertEquals("<externalJson>", captor.getValue().getCertificateContent());
        assertEquals("123", captor.getValue().getCertificateContentMeta().getId());
        assertEquals("fk7263", captor.getValue().getCertificateContentMeta().getType());

        assertEquals("<internalJson>", intygData);
    }

    @Test(expected = ExternalWebServiceCallFailedException.class)
    public void testFetchIntygWithFailingIntygstjanst() {

        // setup intygstjansten WS mock to return error response
        GetCertificateForCareRequestType request = new GetCertificateForCareRequestType();
        request.setCertificateId(CERTIFICATE_ID);
        when(getCertificateForCareResponder.getCertificateForCare(any(AttributedURIType.class), eq(request)))
                .thenReturn(intygtjanstErrorResponse);

        intygService.fetchIntygData(CERTIFICATE_ID);
    }

    @Test(expected = ModuleCallFailedException.class)
    public void testFetchIntygWithFailingUnmarshalling() {

        // setup intygstjansten WS mock to return intyg information
        GetCertificateForCareRequestType request = new GetCertificateForCareRequestType();
        request.setCertificateId(CERTIFICATE_ID);
        when(getCertificateForCareResponder.getCertificateForCare(any(AttributedURIType.class), eq(request)))
                .thenReturn(intygtjanstResponse);

        // setup module Rest API factory to return a mocked module Rest API
        when(moduleRestApiFactory.getModuleRestService(CERTIFICATE_TYPE)).thenReturn(moduleRestApi);

        // setup module API behaviour for conversion from transport to external
        Response unmarshallResponse = mock(Response.class);
        when(unmarshallResponse.getStatus()).thenReturn(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        when(moduleRestApi.unmarshall(any(String.class))).thenReturn(unmarshallResponse);

        intygService.fetchIntygData(CERTIFICATE_ID);
    }

    @Test(expected = ModuleCallFailedException.class)
    public void testFetchIntygWithFailingExternalToInternalTransformation() {

        // setup intygstjansten WS mock to return intyg information
        GetCertificateForCareRequestType request = new GetCertificateForCareRequestType();
        request.setCertificateId(CERTIFICATE_ID);
        when(getCertificateForCareResponder.getCertificateForCare(any(AttributedURIType.class), eq(request)))
                .thenReturn(intygtjanstResponse);

        // setup module Rest API factory to return a mocked module Rest API
        when(moduleRestApiFactory.getModuleRestService(CERTIFICATE_TYPE)).thenReturn(moduleRestApi);

        // setup module API behaviour for conversion from transport to external
        Response unmarshallResponse = mock(Response.class);
        when(unmarshallResponse.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        when(unmarshallResponse.readEntity(String.class)).thenReturn("<externalJson>");
        when(moduleRestApi.unmarshall(any(String.class))).thenReturn(unmarshallResponse);

        // setup module API behaviour for conversion from external to internal
        Response externalToInternalResponse = mock(Response.class);
        when(externalToInternalResponse.getStatus()).thenReturn(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        when(moduleRestApi.convertExternalToInternal(any(CertificateContentHolder.class))).thenReturn(
                externalToInternalResponse);

        intygService.fetchIntygData(CERTIFICATE_ID);
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
