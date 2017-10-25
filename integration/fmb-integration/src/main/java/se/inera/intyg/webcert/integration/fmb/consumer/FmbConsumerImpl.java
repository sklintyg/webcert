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
package se.inera.intyg.webcert.integration.fmb.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import se.inera.intyg.webcert.integration.fmb.model.fmdxinfo.FmdxInformation;
import se.inera.intyg.webcert.integration.fmb.model.typfall.Typfall;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class FmbConsumerImpl implements FmbConsumer {

    private String baseUrl;

    public FmbConsumerImpl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public Typfall getTypfall() throws FailedToFetchFmbData {
        final String urlString = baseUrl + "/typfall?inlinehtmlmarkup=true";
        return fetchFmbData(urlString, Typfall.class);
    }

    @Override
    public FmdxInformation getForsakringsmedicinskDiagnosinformation() throws FailedToFetchFmbData {
        final String urlString = baseUrl + "/forsakringsmedicinskdiagnosinformation?inlinehtmlmarkup=true";
        return fetchFmbData(urlString, FmdxInformation.class);
    }

    private <T> T fetchFmbData(String urlString, Class<T> valueType) throws FailedToFetchFmbData {
        try {
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            final int timeout = 5000;
            con.setConnectTimeout(timeout);
            con.setReadTimeout(timeout);
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(con.getInputStream(), valueType);
        } catch (IOException e) {
            throw new FailedToFetchFmbData(baseUrl, e);
        }
    }

}
