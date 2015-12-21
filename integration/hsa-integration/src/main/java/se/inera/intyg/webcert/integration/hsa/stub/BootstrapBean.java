/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.integration.hsa.stub;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import se.inera.intyg.webcert.integration.hsa.model.Vardgivare;

import com.fasterxml.jackson.databind.ObjectMapper;

public class BootstrapBean {
    private static final Logger LOG = LoggerFactory.getLogger(BootstrapBean.class);

    @Autowired
    private HsaServiceStub hsaServiceStub;

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    public void bootstrapVardgivare() throws IOException {

        List<Resource> files = getResourceListing("bootstrap-vardgivare/*.json");
        LOG.debug("Bootstrapping {} vardgivare for HSA stub ...", files.size());
        for (Resource res : files) {
            addVardgivare(res);
        }

        files = getResourceListing("bootstrap-medarbetaruppdrag/*.json");
        LOG.debug("Bootstrapping {} medarbetare for HSA stub ...", files.size());
        for (Resource res : files) {
            addPerson(res);
            addMedarbetaruppdrag(res);
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

    private void addMedarbetaruppdrag(Resource res) throws IOException {
        LOG.debug("Loading medarbetaruppdrag from " + res.getFilename());
        Medarbetaruppdrag medarbetaruppdrag = objectMapper.readValue(res.getFile(), Medarbetaruppdrag.class);
        hsaServiceStub.getMedarbetaruppdrag().add(medarbetaruppdrag);
        LOG.debug("Loaded medarbetaruppdrag for " + medarbetaruppdrag.getHsaId());
    }

    private void addPerson(Resource res) throws IOException {
        HsaPerson hsaPerson = objectMapper.readValue(res.getFile(), HsaPerson.class);
        hsaServiceStub.addHsaPerson(hsaPerson);
    }

    private void addVardgivare(Resource res) throws IOException {
        Vardgivare vardgivare = objectMapper.readValue(res.getFile(), Vardgivare.class);
        hsaServiceStub.getVardgivare().add(vardgivare);
        LOG.debug("Loaded vardgivare " + vardgivare.getId());
    }
}
