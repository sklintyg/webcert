/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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
import java.util.Timer;
import org.joda.time.DateTime;
import org.opensaml.saml2.metadata.provider.AbstractReloadingMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

/**
 * A metadata provider that reads metadata from a {#link {@link Resource}.
 */
public class SpringResourceBackedMetadataProvider extends AbstractReloadingMetadataProvider {

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
     * @throws MetadataProviderException thrown if there is a problem retrieving information about the resource
     */
    public SpringResourceBackedMetadataProvider(Timer timer, Resource resource) throws MetadataProviderException {
        super(timer);

        if (!resource.exists()) {
            throw new MetadataProviderException("Resource " + resource.toString() + " does not exist.");
        }
        metadataResource = resource;
    }

    /**
     * Gets whether cached metadata should be discarded if it expires and can not be refreshed.
     *
     * @return whether cached metadata should be discarded if it expires and can not be refreshed.
     * @deprecated use {@link #requireValidMetadata()} instead
     */
    public boolean maintainExpiredMetadata() {
        return !requireValidMetadata();
    }

    /**
     * Sets whether cached metadata should be discarded if it expires and can not be refreshed.
     *
     * @param maintain whether cached metadata should be discarded if it expires and can not be refreshed.
     * @deprecated use {@link #setRequireValidMetadata(boolean)} instead
     */
    public void setMaintainExpiredMetadata(boolean maintain) {
        setRequireValidMetadata(!maintain);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void destroy() {
        metadataResource = null;

        super.destroy();
    }

    /**
     * {@inheritDoc}
     */
    protected String getMetadataIdentifier() {
        return metadataResource.toString();
    }

    /**
     * {@inheritDoc}
     */
    protected byte[] fetchMetadata() throws MetadataProviderException {
        try {
            DateTime metadataUpdateTime = new DateTime(metadataResource.lastModified());
            log.debug("resource {} was last modified {}", getMetadataIdentifier(), metadataUpdateTime);
            if (getLastRefresh() == null || metadataUpdateTime.isAfter(getLastRefresh())) {
                return inputstreamToByteArray(metadataResource.getInputStream());
            }

            return null;
        } catch (IOException e) {
            String errorMsg = "Unable to read metadata file";
            log.error(errorMsg, e);
            throw new MetadataProviderException(errorMsg, e);
        }
    }
}