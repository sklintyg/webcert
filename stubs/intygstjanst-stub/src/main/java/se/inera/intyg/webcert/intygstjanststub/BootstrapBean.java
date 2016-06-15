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

package se.inera.intyg.webcert.intygstjanststub;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.fasterxml.jackson.databind.ObjectMapper;

import se.inera.intyg.common.support.modules.support.api.CertificateHolder;
import se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper;

public class BootstrapBean {
    private static final Logger LOG = LoggerFactory.getLogger(BootstrapBean.class);

    @Autowired
    private IntygStore intygStore;

    private ObjectMapper objectMapper = new CustomObjectMapper();

    @PostConstruct
    public void initData() {
        try {
            LOG.debug("Intygstjanst Stub : initializing intyg data...");

            List<Resource> files = getResourceListing("bootstrap-intyg/*.json");
            for (Resource res : files) {
                addIntyg(res);
            }

            List<Resource> contentTemplates = getResourceListing("content/*.xml");
            for (Resource res : contentTemplates) {
                addContentTemplate(res);
            }

        } catch (JAXBException | IOException e) {
            throw new RuntimeException("Could not bootstrap intygsdata for intygstjanststub", e);
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

    private void addIntyg(Resource res) throws JAXBException, IOException {
        CertificateHolder response = objectMapper.readValue(res.getInputStream(), CertificateHolder.class);
        intygStore.addIntyg(response);
    }

    private void addContentTemplate(Resource res) {
        try {
            String contentTemplate = IOUtils.toString(res.getInputStream(), "UTF-8");
            intygStore.addContentTemplate(res.getFilename(), contentTemplate);
        } catch (IOException e) {
            LOG.error("Error converting content template file {}: {}", res.getFilename(), e.getMessage());
        }
    }
}
