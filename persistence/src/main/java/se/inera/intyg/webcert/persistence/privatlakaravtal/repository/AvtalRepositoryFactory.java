/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.persistence.privatlakaravtal.repository;

import com.google.common.base.Charsets;
import java.io.IOException;
import java.time.LocalDateTime;
import javax.annotation.PostConstruct;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;
import se.inera.intyg.webcert.persistence.privatlakaravtal.model.Avtal;

/**
 * Created by eriklupander on 2015-08-05.
 */
@Service
public class AvtalRepositoryFactory {

    private static final Logger LOG = LoggerFactory.getLogger(AvtalRepositoryFactory.class);

    @Value("${privatepractitioner.defaultterms.file}")
    private String location;

    @Autowired
    private AvtalRepository avtalRepository;

    @Autowired
    ResourceLoader resourceLoader;

    @PostConstruct
    @Transactional
    public void populateStandardAvtal() {

        // FIXME: Legacy support, can be removed when local config has been substituted by refdata (INTYG-7701)
        if (!ResourceUtils.isUrl(location)) {
            location = "file:" + location;
        }

        Integer latestAvtalVersion = avtalRepository.getLatestAvtalVersion();
        if (latestAvtalVersion == -1) {
            try {
                final String avtalText = IOUtils.toString(resourceLoader.getResource(location).getInputStream(),
                    Charsets.UTF_8);
                final Avtal avtal = new Avtal();
                avtal.setAvtalText(avtalText);
                avtal.setAvtalVersion(1);
                avtal.setVersionDatum(LocalDateTime.now());
                avtalRepository.save(avtal);
                LOG.info("Persisted basic Avtal for privatlakare.");

            } catch (IOException ioe) {
                LOG.error("Something went wrong while persisting basic privatlakaravtal on startup. Message: " + ioe.getMessage());
                throw new RuntimeException(ioe);
            }
        }
    }
}
