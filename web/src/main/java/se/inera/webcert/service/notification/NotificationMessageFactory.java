package se.inera.webcert.service.notification;

import se.inera.certificate.modules.support.api.notification.HandelseType;
import se.inera.certificate.modules.support.api.notification.NotificationMessage;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.persistence.utkast.model.Utkast;

public interface NotificationMessageFactory {

    public abstract NotificationMessage createNotificationMessage(Utkast utkast, HandelseType handelse);

    public abstract NotificationMessage createNotificationMessage(FragaSvar fragaSvar, HandelseType handelse);

}
