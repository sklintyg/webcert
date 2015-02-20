package se.inera.webcert.notifications.process;

import org.apache.camel.Message;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.certificate.modules.registry.IntygModuleRegistry;
import se.inera.certificate.modules.support.api.ModuleApi;
import se.inera.certificate.modules.support.api.notification.NotificationMessage;
import se.inera.webcert.notifications.routes.RouteHeaders;

public class CreateAndInitCertificateStatusRequestProcessor {

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    public void process(Message message) throws Exception {
        NotificationMessage notificationMessage = message.getBody(NotificationMessage.class);

        ModuleApi moduleApi = moduleRegistry.getModuleApi(notificationMessage.getIntygsTyp());
        message.setBody(moduleApi.createNotification(notificationMessage));
        message.setHeader(RouteHeaders.LOGISK_ADRESS, notificationMessage.getLogiskAdress());
    }

}
