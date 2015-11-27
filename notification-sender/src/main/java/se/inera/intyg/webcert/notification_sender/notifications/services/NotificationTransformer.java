package se.inera.intyg.webcert.notification_sender.notifications.services;

import org.apache.camel.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.webcert.notification_sender.notifications.routes.RouteHeaders;

import com.google.common.annotations.VisibleForTesting;

public class NotificationTransformer {
    private static final Logger LOG = LoggerFactory.getLogger(NotificationTransformer.class);

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @VisibleForTesting
    void setModuleRegistry(IntygModuleRegistry moduleRegistry) {
        this.moduleRegistry = moduleRegistry;
    }

    public void process(Message message) throws Exception {
        LOG.debug("Receiving message: {}", message.getMessageId());

        NotificationMessage notificationMessage = message.getBody(NotificationMessage.class);

        message.setHeader(RouteHeaders.LOGISK_ADRESS, notificationMessage.getLogiskAdress());
        message.setHeader(RouteHeaders.INTYGS_ID, notificationMessage.getIntygsId());
        message.setHeader(RouteHeaders.HANDELSE, notificationMessage.getHandelse().value());

        ModuleApi moduleApi = moduleRegistry.getModuleApi(notificationMessage.getIntygsTyp());
        message.setBody(moduleApi.createNotification(notificationMessage));
    }

}
