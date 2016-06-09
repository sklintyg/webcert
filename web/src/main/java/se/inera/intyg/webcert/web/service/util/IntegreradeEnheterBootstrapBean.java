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

package se.inera.intyg.webcert.web.service.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper;
import se.inera.intyg.webcert.persistence.integreradenhet.model.IntegreradEnhet;
import se.inera.intyg.webcert.persistence.integreradenhet.repository.IntegreradEnhetRepository;

public class IntegreradeEnheterBootstrapBean {
    private static final Logger LOG = LoggerFactory.getLogger(IntegreradeEnheterBootstrapBean.class);

    @Autowired
    private IntegreradEnhetRepository integreradEnhetRepository;

    @PostConstruct
    public void initData() {
        List<Resource> files = getResourceListing("bootstrap-integrerade-enheter/*.json");
        for (Resource res : files) {
            LOG.debug("Loading resource " + res.getFilename());
            addIntegreradEnhet(res);
        }
    }

    private void addIntegreradEnhet(Resource res) {
        try {
            IntegreradEnhet integreradEnhet = new CustomObjectMapper().readValue(res.getInputStream(), IntegreradEnhet.class);
            integreradEnhet.setSkapadDatum(LocalDateTime.now());
            integreradEnhetRepository.save(integreradEnhet);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
}
