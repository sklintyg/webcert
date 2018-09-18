/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.maillink;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;

/**
 * Created by pebe on 2015-10-05.
 */
@Service
public class MailLinkServiceImpl implements MailLinkService {

    private static final Logger LOG = LoggerFactory.getLogger(MailLinkServiceImpl.class);

    private static final String PARAM_CERT_TYPE = "certType";
    private static final String PARAM_CERT_TYPE_VERSION = "certTypeVersion";
    private static final String PARAM_CERT_ID = "certId";

    @Value("${certificate.view.url.base}")
    private String urlBaseTemplate;

    @Value("${certificate.view.url.utkast.integration.template}")
    private String urlUtkastFragmentTemplate;

    @Override
    public URI intygRedirect(String typ, String intygTypeVersion, String intygId) {
        if (Strings.nullToEmpty(intygId).trim().isEmpty()) {
            LOG.error("Path parameter 'intygId' was either whitespace, empty (\"\") or null");
            return null;
        }
        if (Strings.nullToEmpty(typ).trim().isEmpty()) {
            LOG.error("Path parameter 'typ' was either whitespace, empty (\"\") or null");
            return null;
        }
        if (Strings.nullToEmpty(intygTypeVersion).trim().isEmpty()) {
            LOG.error("Parameter 'intygTypeVersion' was either whitespace, empty (\"\") or null");
            return null;
        }

        LOG.debug("Redirecting to view intyg {} of type {}", intygId, typ);

        Map<String, Object> urlParams = new HashMap<>();
        urlParams.put(PARAM_CERT_TYPE, typ);
        urlParams.put(PARAM_CERT_TYPE_VERSION, intygTypeVersion);
        urlParams.put(PARAM_CERT_ID, intygId);

        return UriBuilder.fromPath(urlBaseTemplate).fragment(urlUtkastFragmentTemplate).buildFromMap(urlParams);
    }
}
