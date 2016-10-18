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

package se.inera.intyg.webcert.notification_sender.notifications.services;

import org.apache.camel.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.annotations.VisibleForTesting;

import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.common.support.modules.support.api.notification.SchemaVersion;
import se.inera.intyg.intygstyper.fk7263.model.converter.Fk7263InternalToNotification;
import se.inera.intyg.intygstyper.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders;

public class NotificationTransformer {
    private static final Logger LOG = LoggerFactory.getLogger(NotificationTransformer.class);

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Autowired
    private Fk7263InternalToNotification fk7263Transform;

    @VisibleForTesting
    void setModuleRegistry(IntygModuleRegistry moduleRegistry) {
        this.moduleRegistry = moduleRegistry;
    }

    public void process(Message message) throws Exception {
        LOG.debug("Receiving message: {}", message.getMessageId());

        NotificationMessage notificationMessage = message.getBody(NotificationMessage.class);

        message.setHeader(NotificationRouteHeaders.LOGISK_ADRESS, notificationMessage.getLogiskAdress());
        message.setHeader(NotificationRouteHeaders.INTYGS_ID, notificationMessage.getIntygsId());

        // Note that this header have been set already by the original sender to accommodate header-based routing
        // in the aggreagatorRoute. It is 100% safe to overwrite it at this point.

        message.setHeader(NotificationRouteHeaders.HANDELSE, notificationMessage.getHandelse().value());

        if (notificationMessage.getVersion() != null) {
            message.setHeader(NotificationRouteHeaders.VERSION, notificationMessage.getVersion().name());
        } else {
            message.setHeader(NotificationRouteHeaders.VERSION, SchemaVersion.VERSION_1.name());
        }

        if (SchemaVersion.VERSION_2.equals(notificationMessage.getVersion())) {
            ModuleApi moduleApi = moduleRegistry.getModuleApi(notificationMessage.getIntygsTyp());
            message.setBody(CertificateStatusUpdateForCareTypeConverter.convert(notificationMessage,
                    moduleApi.getIntygFromUtlatande(moduleApi.getUtlatandeFromJson(notificationMessage.getUtkast()))));
        } else if (Fk7263EntryPoint.MODULE_ID.equals(notificationMessage.getIntygsTyp())) {
            message.setBody(fk7263Transform.createCertificateStatusUpdateForCareType(notificationMessage));
        } else {
            throw new IllegalArgumentException("Unsupported combination of version '" + notificationMessage.getVersion() + "' and type '"
                    + notificationMessage.getIntygsTyp() + "'");

        }
    }
}
