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

package se.inera.intyg.webcert.web.service.texts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

import se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper;
import se.inera.intyg.webcert.web.service.texts.repo.IntygTextsRepository;

@Service
public class IntygTextsServiceImpl implements IntygTextsService {

    private static final Logger LOG = LoggerFactory.getLogger(IntygTextsService.class);

    @Autowired
    private IntygTextsRepository repo;

    @Autowired
    @Qualifier("customObjectMapper")
    private CustomObjectMapper mapper;

    @Override
    public String getIntygTexts(String intygsTyp, String version) {
        try {
            return mapper.writeValueAsString(repo.getTexts(intygsTyp, version));
        } catch (JsonProcessingException e) {
            LOG.error("Could not write texts as JSON for intygType {} with version {}", intygsTyp, version);
        }
        return "";
    }

    @Override
    public String getLatestVersion(String intygsTyp) {
        return repo.getLatestVersion(intygsTyp);
    }

}
