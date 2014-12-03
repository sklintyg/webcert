package se.inera.webcert.service.notification;

import se.inera.webcert.notifications.message.v1.NotificationRequestType;

/**
 * Service that notifies a unit care of incoming changes.
 *
 * @author Magnus Ekstrand
 */
public interface NotificationService {

    /**
     * Notify unit cares when there's have been a change in one of
     * <ul>
     *     <li>Intyg</li>
     *     <li>Intygsutkast</li>
     *     <li>FragaSvar</li>
     * </ul>
     */
    void notify(NotificationRequestType notificationRequestType);

}
