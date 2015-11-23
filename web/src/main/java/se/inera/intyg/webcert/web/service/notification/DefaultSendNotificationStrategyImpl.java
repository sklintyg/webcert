package se.inera.intyg.webcert.web.service.notification;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.inera.intyg.webcert.web.integration.registry.IntegreradeEnheterRegistry;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;

@Component
public class DefaultSendNotificationStrategyImpl implements SendNotificationStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(SendNotificationStrategy.class);

    @Autowired
    private IntegreradeEnheterRegistry integreradeEnheterRegistry;

    @Autowired
    private UtkastRepository utkastRepository;

    private final List<String> allowedIntygsTyper = Collections.singletonList("fk7263");

    /* (non-Javadoc)
     * @see se.inera.intyg.webcert.web.service.notification.SendNotificationStrategy#decideNotificationForIntyg(java.lang.String)
     */
    @Override
    public Utkast decideNotificationForIntyg(String intygsId) {

        Utkast utkast = utkastRepository.findOne(intygsId);

        if (utkast == null) {
            LOG.debug("No Utkast with id '{}' was found", intygsId);
            return null;
        }

        return decideNotificationForIntyg(utkast);
    }

    /*
     * (non-Javadoc)
     *
     * @see se.inera.intyg.webcert.web.service.notification.SendNotificationStrategy#decideNotificationForIntyg(se.inera.intyg.webcert.web.
     * persistence.utkast.model.Utkast)
     */
    @Override
    public Utkast decideNotificationForIntyg(Utkast utkast) {

        if (!isIntygsTypAllowed(utkast.getIntygsTyp())) {
            LOG.debug("Utkast '{}' is of type '{}' and is not allowed", utkast.getIntygsId(), utkast.getIntygsTyp());
            return null;
        }

        if (!isEnhetIntegrerad(utkast.getEnhetsId())) {
            LOG.debug("Utkast '{}' belongs to a unit that is not integrated", utkast.getIntygsId());
            return null;
        }

        return utkast;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * se.inera.intyg.webcert.web.service.notification.SendNotificationStrategy#decideNotificationForFragaSvar(se.inera.intyg.webcert.web
     * .persistence.fragasvar.model.FragaSvar)
     */
    @Override
    public Utkast decideNotificationForFragaSvar(FragaSvar fragaSvar) {
        String intygsId = fragaSvar.getIntygsReferens().getIntygsId();
        return decideNotificationForIntyg(intygsId);
    }

    private boolean isIntygsTypAllowed(String intygsTyp) {
        return allowedIntygsTyper.contains(intygsTyp.toLowerCase());
    }

    private boolean isEnhetIntegrerad(String enhetsId) {
        return integreradeEnheterRegistry.isEnhetIntegrerad(enhetsId);
    }
}
