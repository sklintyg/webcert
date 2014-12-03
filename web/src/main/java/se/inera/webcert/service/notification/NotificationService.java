package se.inera.webcert.service.notification;

/**
 * Service that notifies a unit care of incoming changes.
 *
 * @author Magnus Ekstrand
 */
public interface NotificationService {

    void notify(String xml);

}
