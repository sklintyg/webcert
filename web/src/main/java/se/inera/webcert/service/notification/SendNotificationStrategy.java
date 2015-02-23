package se.inera.webcert.service.notification;

import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.persistence.utkast.model.Utkast;

public interface SendNotificationStrategy {

    public abstract boolean decideNotificationForIntyg(Utkast utkast);

    public abstract boolean decideNotificationForFragaSvar(FragaSvar fragaSvar);

}
