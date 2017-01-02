/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.integration.pu.stub;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import se.riv.population.residentmaster.types.v1.ResidentType;

public class PUBootstrapBean {
    private static final Logger LOG = LoggerFactory.getLogger(PUBootstrapBean.class);

    @Autowired
    private ResidentStore residentStore;

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    public void bootstrapPersoner() throws IOException {
        List<Resource> files = getResourceListing("bootstrap-personer/*.json");
        LOG.debug("Bootstrapping {} personer for PU stub ...", files.size());
        for (Resource res : files) {
            try {
                addPersoner(res);
            } catch (Exception e) {
                LOG.error("PUBootstrap", e);
            }
        }
    }

    private List<Resource> getResourceListing(String classpathResourcePath) {
        try {
            PathMatchingResourcePatternResolver r = new PathMatchingResourcePatternResolver();
            return Arrays.asList(r.getResources(classpathResourcePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addPersoner(Resource res) throws IOException {
        LOG.debug("Loading personer from " + res.getFilename());
        ResidentType resident = objectMapper.readValue(res.getInputStream(), ResidentType.class);
        residentStore.addUser(resident);
        LOG.debug("Loaded person " + resident.getPersonpost().getPersonId());
    }
}
