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

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.inera.intyg.common.support.modules.support.api.notification.SchemaVersion;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.integration.registry.IntegreradeEnheterRegistry;

@Component
public class DefaultSendNotificationStrategyImpl implements SendNotificationStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(SendNotificationStrategy.class);

    @Autowired
    private IntegreradeEnheterRegistry integreradeEnheterRegistry;

    /*
     * (non-Javadoc)
     *
     * @see
     * se.inera.intyg.webcert.web.service.notification.SendNotificationStrategy#decideNotificationForIntyg(se.inera.
     * intyg.webcert.web.
     * persistence.utkast.model.Utkast)
     */
    @Override
    public Optional<SchemaVersion> decideNotificationForIntyg(Utkast utkast) {

        Optional<SchemaVersion> schemaVersion = integreradeEnheterRegistry.getSchemaVersion(utkast.getEnhetsId(), utkast.getIntygsTyp());
        if (!schemaVersion.isPresent()) {
            LOG.debug("Utkast '{}' (type = '{}', unit = '{}') will not spawn notifications", utkast.getIntygsId(), utkast.getIntygsTyp(),
                    utkast.getEnhetsId());
        }
        return schemaVersion;
    }
}
