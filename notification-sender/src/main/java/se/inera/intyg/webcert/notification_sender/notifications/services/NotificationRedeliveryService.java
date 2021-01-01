/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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

import java.util.List;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationWSResultMessage;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;

public interface NotificationRedeliveryService {

    /**
     * Handles database operations and monitor logging upon successful delivery of status update to care.
     * @param resultMessage Message from caller collecting information necessary for operations.
     * @param event The event object to persist summarizing info from the status update sent to care.
     */
    void handleNotificationSuccess(NotificationWSResultMessage resultMessage, Handelse event);

    /**
     * Handles database operations and monitor logging for status updates that failed, but have been flagged for redelivery.
     * @param resultMessage Message from caller collecting information necessary for operations.
     * @param event The event object to persist summarizing info from the status update sent to care.
     */
    void handleNotificationResend(NotificationWSResultMessage resultMessage, Handelse event);

    /**
     * Handles database operations and monitor logging for status updates that failed and will not be redelivered.
     * @param resultMessage Message from caller collecting information necessary for operations.
     * @param event The event object to persist summarizing info from the status update sent to care.
     */
    void handleNotificationFailure(NotificationWSResultMessage resultMessage, Handelse event);

    /**
     * Collects and returns the redeliveries that, based in their redelivery time, are scheduled for resend.
     * @return A list of NotificationRedeliveries to be resent.
     */
    List<NotificationRedelivery> getNotificationsForRedelivery();

    Handelse getEventById(Long id);

    boolean isRedundantRedelivery(Handelse event);

    void discardRedundantRedelivery(Handelse event, NotificationRedelivery redelivery);

    void handleManualNotificationResend(Long eventId);
}
