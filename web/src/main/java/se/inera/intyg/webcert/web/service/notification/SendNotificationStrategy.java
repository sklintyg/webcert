package se.inera.intyg.webcert.web.service.notification;

import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;

public interface SendNotificationStrategy {

    Utkast decideNotificationForIntyg(String intygsId);

    Utkast decideNotificationForIntyg(Utkast utkast);

    Utkast decideNotificationForFragaSvar(FragaSvar fragaSvar);

}
