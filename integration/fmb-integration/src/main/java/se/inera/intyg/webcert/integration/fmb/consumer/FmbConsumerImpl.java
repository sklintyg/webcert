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
package se.inera.intyg.webcert.integration.fmb.consumer;

import javax.annotation.PostConstruct;
import org.springframework.web.client.RestTemplate;
import se.inera.intyg.webcert.integration.fmb.model.fmdxinfo.FmdxInformation;
import se.inera.intyg.webcert.integration.fmb.model.typfall.Typfall;

public class FmbConsumerImpl implements FmbConsumer {

    private static final String TYPFALL_PATH = "/typfall?inlinehtmlmarkup=true";
    private static final String FMBINFO_PATH = "/forsakringsmedicinskdiagnosinformation?inlinehtmlmarkup=true";

    private String baseUrl;
    private RestTemplate restTemplate;

    public FmbConsumerImpl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @PostConstruct
    public void init() {
        restTemplate = new RestTemplate();
    }

    @Override
    public Typfall getTypfall() throws FailedToFetchFmbDataException {
        final String urlString = baseUrl + TYPFALL_PATH;
        return restTemplate.getForEntity(urlString, Typfall.class).getBody();
    }

    @Override
    public FmdxInformation getForsakringsmedicinskDiagnosinformation() throws FailedToFetchFmbDataException {
        final String urlString = baseUrl + FMBINFO_PATH;
        return restTemplate.getForEntity(urlString, FmdxInformation.class).getBody();
    }

}
