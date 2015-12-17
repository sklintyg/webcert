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

package se.inera.intyg.webcert.web.auth;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author andreaskaltenbach
 */
public class HttpClientFactory {

    private static final int HTTPS_PORT = 443;

    @Value("${sakerhetstjanst.saml.truststore.file}")
    private org.springframework.core.io.Resource trustStoreFile;

    @Value("${sakerhetstjanst.saml.truststore.password}")
    private String trustStorePassword;

    public HttpClient createInstance() {
        HttpClient httpClient = new HttpClient();

        ProtocolSocketFactory factory = new KeystoreBasedSocketFactory(trustStoreFile, trustStorePassword);
        Protocol protocol = new Protocol("https", factory, HTTPS_PORT);
        Protocol.registerProtocol("https", protocol);

        return httpClient;
    }
}
