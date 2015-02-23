package se.inera.webcert.service.notification;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.inera.webcert.integration.registry.IntegreradeEnheterRegistry;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.persistence.utkast.model.Utkast;
import se.inera.webcert.persistence.utkast.repository.UtkastRepository;

@Component
public class DefaultSendNotificationStrategyImpl implements SendNotificationStrategy {

    @Autowired
    private IntegreradeEnheterRegistry integreradeEnheterRegistry;

    @Autowired
    private UtkastRepository utkastRepository;

    private List<String> allowedIntygsTyper = Arrays.asList("fk7263");

    /*
     * (non-Javadoc)
     * 
     * @see se.inera.webcert.service.notification.SendNotificationStrategy#decideNotificationForIntyg(se.inera.webcert.
     * persistence.utkast.model.Utkast)
     */
    @Override
    public boolean decideNotificationForIntyg(Utkast utkast) {
        if (!isIntygsTypAllowed(utkast.getIntygsTyp())) {
            return false;
        }

        return (isEnhetIntegrerad(utkast.getEnhetsId()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * se.inera.webcert.service.notification.SendNotificationStrategy#decideNotificationForFragaSvar(se.inera.webcert
     * .persistence.fragasvar.model.FragaSvar)
     */
    @Override
    public boolean decideNotificationForFragaSvar(FragaSvar fragaSvar) {
        String intygsTyp = fragaSvar.getIntygsReferens().getIntygsTyp();

        if (!isIntygsTypAllowed(intygsTyp)) {
            return false;
        }

        String intygsId = fragaSvar.getIntygsReferens().getIntygsId();
        String enhetsId = fragaSvar.getVardperson().getEnhetsId();

        return (isIntygPresent(intygsId) && isEnhetIntegrerad(enhetsId));
    }

    private boolean isIntygsTypAllowed(String intygsTyp) {
        return allowedIntygsTyper.contains(intygsTyp.toLowerCase());
    }

    private boolean isIntygPresent(String intygsId) {
        return utkastRepository.exists(intygsId);
    }

    private boolean isEnhetIntegrerad(String enhetsId) {
        return integreradeEnheterRegistry.isEnhetIntegrerad(enhetsId);
    }
}
