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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

import se.inera.intyg.common.support.modules.support.api.notification.NotificationVersion;
import se.inera.intyg.webcert.persistence.integreradenhet.model.SchemaVersion;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.integration.registry.IntegreradeEnheterRegistry;
import se.inera.intyg.webcert.web.web.controller.util.CertificateTypes;

@Component
public class DefaultSendNotificationStrategyImpl implements SendNotificationStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(SendNotificationStrategy.class);

    @Autowired
    private IntegreradeEnheterRegistry integreradeEnheterRegistry;

    private final List<String> blacklisted = Arrays.asList(CertificateTypes.TSBAS.toString(), CertificateTypes.TSDIABETES.toString());

    private final Map<String, SchemaVersion> certificateVersionMap = ImmutableMap.of(CertificateTypes.FK7263.toString(), SchemaVersion.V1,
            CertificateTypes.LUSE.toString(), SchemaVersion.V2, CertificateTypes.LISU.toString(), SchemaVersion.V2, CertificateTypes.LUAE_NA.toString(), SchemaVersion.V2);

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

        Optional<SchemaVersion> schemaVersion = integreradeEnheterRegistry.getSchemaVersion(utkast.getEnhetsId());
        if (!schemaVersion.isPresent()) {
            LOG.debug("Utkast '{}' belongs to a unit '{}' that is not integrated", utkast.getIntygsId(), utkast.getEnhetsId());
            return Optional.empty();
        }

        if (!isSchemaVersionAllowed(utkast.getIntygsTyp(), schemaVersion.get())) {
            LOG.debug("Schema version '{}' for unit '{}' is not valid for '{}'", schemaVersion.get(), utkast.getEnhetsId(), utkast.getIntygsTyp());
            return Optional.empty();
        }

        Optional<NotificationVersion> ret = NotificationVersion.fromString(schemaVersion.get().name());
        if (!ret.isPresent()) {
            LOG.error("Schema version '{}' for unit '{}' is not valid", schemaVersion.get(), utkast.getEnhetsId());
        }
        return ret;
    }

    private boolean isIntygsTypAllowed(String intygsTyp) {
        return !blacklisted.contains(intygsTyp.toLowerCase());
    }

    private boolean isSchemaVersionAllowed(String intygsTyp, SchemaVersion schemaVersion) {
        return schemaVersion.equals(certificateVersionMap.get(intygsTyp.toLowerCase()));
    }
}
