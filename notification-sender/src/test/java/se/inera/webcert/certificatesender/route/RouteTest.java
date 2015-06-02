package se.inera.webcert.certificatesender.route;

import static org.apache.camel.component.mock.MockEndpoint.assertIsSatisfied;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import com.google.common.collect.ImmutableMap;
import org.apache.camel.*;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.apache.camel.test.spring.MockEndpointsAndSkip;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;

import se.inera.webcert.common.Constants;
import se.inera.webcert.notifications.service.exception.CertificateStatusUpdateServiceException;
import se.inera.webcert.notifications.service.exception.NonRecoverableCertificateStatusUpdateServiceException;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareType;


@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration({"/certificates/test-certificate-sender-config.xml", "/certificates/unit-test-properties-context.xml" })
@MockEndpointsAndSkip("bean:certificateStoreProcessor|bean:certificateSendProcessor|bean:certificateRevokeProcessor|direct:certPermanentErrorHandlerEndpoint|direct:certTemporaryErrorHandlerEndpoint")
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class RouteTest {
    
    private static final Logger LOG = LoggerFactory.getLogger(RouteTest.class);

    private static final String STORE_MESSAGE = "store";

    @Produce(uri = "direct:receiveCertificateTransferEndpoint")
    private ProducerTemplate producerTemplate;

    @EndpointInject(uri = "mock:bean:certificateStoreProcessor")
    private MockEndpoint storeProcessor;

    @EndpointInject(uri = "mock:bean:certificateSendProcessor")
    private MockEndpoint sendProcessor;

    @EndpointInject(uri = "mock:bean:certificateRevokeProcessor")
    private MockEndpoint revokeProcessor;

    @EndpointInject(uri = "mock:direct:errorHandlerEndpoint")
    private MockEndpoint permanentErrorHandlerEndpoint;

    @EndpointInject(uri = "mock:direct:errorHandlerEndpoint")
    private MockEndpoint temporaryErrorHandlerEndpoint;

    @Test
    public void testNormalStoreRoute() throws InterruptedException {
        // Given
        storeProcessor.expectedMessageCount(1);
        sendProcessor.expectedMessageCount(0);
        revokeProcessor.expectedMessageCount(0);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);
        permanentErrorHandlerEndpoint.expectedMessageCount(0);

        // When
        producerTemplate.sendBodyAndHeaders(STORE_MESSAGE, ImmutableMap.<String, Object>of(Constants.MESSAGE_TYPE, Constants.STORE_MESSAGE));

        // Then
        assertIsSatisfied(storeProcessor);
        assertIsSatisfied(sendProcessor);
        assertIsSatisfied(revokeProcessor);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
    }

}
