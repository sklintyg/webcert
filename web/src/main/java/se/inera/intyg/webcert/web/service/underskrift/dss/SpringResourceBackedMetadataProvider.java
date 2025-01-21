/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.underskrift.dss;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Timer;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.opensaml.saml.metadata.resolver.impl.AbstractReloadingMetadataResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

/**
 * A metadata provider that reads metadata from a {#link {@link Resource}.
 */
public class SpringResourceBackedMetadataProvider extends AbstractReloadingMetadataResolver {

    /**
     * Class logger.
     */
    private final Logger log = LoggerFactory.getLogger(SpringResourceBackedMetadataProvider.class);

    /**
     * Resource from which metadata is read.
     */
    private Resource metadataResource;

    /**
     * Constructor.
     *
     * @param resource resource from which to read the metadata file.
     * @param timer task timer used to schedule metadata refresh tasks
     * @throws ComponentInitializationException thrown if there is a problem retrieving information about the resource
     */
    public SpringResourceBackedMetadataProvider(Timer timer, Resource resource) throws ComponentInitializationException {
        super(timer);
        if (!resource.exists()) {
            throw new ComponentInitializationException("Resource " + resource.toString() + " does not exist.");
        }

        metadataResource = resource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void doDestroy() {
        metadataResource = null;

        super.destroy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getMetadataIdentifier() {
        return metadataResource.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected byte[] fetchMetadata() throws ResolverException {
        try {
            final var metadataUpdateTime = ZonedDateTime
                .ofInstant(Instant.ofEpochSecond(metadataResource.lastModified()), ZoneId.systemDefault());
            log.debug("resource {} was last modified {}", getMetadataIdentifier(), metadataUpdateTime);

            if (getLastRefresh() == null || metadataUpdateTime.isAfter(ZonedDateTime.ofInstant(getLastRefresh(), ZoneId.systemDefault()))) {
                return inputstreamToByteArray(metadataResource.getInputStream());
            }

            return new byte[0];
        } catch (IOException e) {
            String errorMsg = "Unable to read metadata file";
            throw new ResolverException(errorMsg, e);
        }
    }
}
