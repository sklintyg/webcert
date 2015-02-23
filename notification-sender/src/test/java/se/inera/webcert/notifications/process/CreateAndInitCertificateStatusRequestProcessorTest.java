package se.inera.webcert.notifications.process;

import org.apache.camel.Message;
import org.apache.camel.impl.DefaultMessage;
import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.mockito.Mockito;

import se.inera.certificate.modules.registry.IntygModuleRegistry;
import se.inera.certificate.modules.support.api.ModuleApi;
import se.inera.certificate.modules.support.api.notification.FragaSvar;
import se.inera.certificate.modules.support.api.notification.HandelseType;
import se.inera.certificate.modules.support.api.notification.NotificationMessage;
import se.inera.webcert.notifications.routes.RouteHeaders;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CreateAndInitCertificateStatusRequestProcessorTest {

    public static final String EXPECTED_BODY = "Body";
    public static final String INTYGS_ID = "intyg1";
    public static final String LOGISK_ADRESS = "address1";

    @Test
    public void testSend() throws Exception {
        // Given
        NotificationMessage notificationMessage = new NotificationMessage(INTYGS_ID, "FK7263", new LocalDateTime(),
                HandelseType.INTYGSUTKAST_SKAPAT, LOGISK_ADRESS, "{ }", new FragaSvar(0, 0, 0, 0));
        Message message = new DefaultMessage();
        message.setBody(notificationMessage);

        IntygModuleRegistry moduleRegistry = mock(IntygModuleRegistry.class);
        ModuleApi moduleApi = mock(ModuleApi.class);
        when(moduleRegistry.getModuleApi(Mockito.anyString())).thenReturn(moduleApi);
        when(moduleApi.createNotification(Mockito.any(NotificationMessage.class))).thenReturn(EXPECTED_BODY);

        CreateAndInitCertificateStatusRequestProcessor processor = new CreateAndInitCertificateStatusRequestProcessor();
        processor.setModuleRegistry(moduleRegistry);

        // When
        processor.process(message);

        // Then
        assertEquals(EXPECTED_BODY, message.getBody());
        assertEquals(message.getHeader(RouteHeaders.HANDELSE), HandelseType.INTYGSUTKAST_SKAPAT.value());
        assertEquals(message.getHeader(RouteHeaders.INTYGS_ID), INTYGS_ID);
        assertEquals(message.getHeader(RouteHeaders.LOGISK_ADRESS), LOGISK_ADRESS);
    }

}
