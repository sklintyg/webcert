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

package se.inera.intyg.webcert.web.service.notification;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.inera.intyg.common.support.modules.support.api.notification.NotificationVersion;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.integration.registry.IntegreradeEnheterRegistry;

@Component
public class DefaultSendNotificationStrategyImpl implements SendNotificationStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(SendNotificationStrategy.class);

    @Autowired
    private IntegreradeEnheterRegistry integreradeEnheterRegistry;

    private final List<String> allowedIntygsTyper = Collections.singletonList("fk7263");

    /*
     * (non-Javadoc)
     *
     * @see se.inera.intyg.webcert.web.service.notification.SendNotificationStrategy#decideNotificationForIntyg(se.inera.intyg.webcert.web.
     * persistence.utkast.model.Utkast)
     */
    @Override
    public Optional<NotificationVersion> decideNotificationForIntyg(Utkast utkast) {

        if (!isIntygsTypAllowed(utkast.getIntygsTyp())) {
            LOG.debug("Utkast '{}' is of type '{}' and is not allowed", utkast.getIntygsId(), utkast.getIntygsTyp());
            return Optional.empty();
        }

        if (!isEnhetIntegrerad(utkast.getEnhetsId())) {
            LOG.debug("Utkast '{}' belongs to a unit that is not integrated", utkast.getIntygsId());
            return Optional.empty();
        }

        return Optional.of(NotificationVersion.VERSION_1);
    }

    private boolean isIntygsTypAllowed(String intygsTyp) {
        return allowedIntygsTyper.contains(intygsTyp.toLowerCase());
    }

    private boolean isEnhetIntegrerad(String enhetsId) {
        return integreradeEnheterRegistry.isEnhetIntegrerad(enhetsId);
    }
}
