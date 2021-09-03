/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.web.controller.facade.util;

import java.net.URI;
import java.util.Collections;
import javax.ws.rs.core.UriInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ReactUriFactory {

    public static final String PARAM_CERT_ID = "certId";

    @Value("${certificate.view.url.react.integration.template}")
    private String urlReactTemplate;

    @Value("${certificate.view.host.react.client}")
    private String hostReactClient;

    public URI uriForCertificate(UriInfo uriInfo, String certificateId) {
        final var uriBuilder = uriInfo.getBaseUriBuilder().replacePath("/");
        final var urlParams = Collections.singletonMap(PARAM_CERT_ID, certificateId);
        return uriBuilder
            .host(hostReactClient)
            .path(urlReactTemplate)
            .buildFromMap(urlParams);
    }
}
