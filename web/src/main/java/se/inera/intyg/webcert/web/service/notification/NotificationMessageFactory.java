package se.inera.intyg.webcert.web.service.notification;

import se.inera.intyg.common.support.modules.support.api.notification.HandelseType;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;

public interface NotificationMessageFactory {

    NotificationMessage createNotificationMessage(String intygsId, HandelseType handelse);

    NotificationMessage createNotificationMessage(Utkast utkast, HandelseType handelse);

    NotificationMessage createNotificationMessage(FragaSvar fragaSvar, HandelseType handelse);

}
