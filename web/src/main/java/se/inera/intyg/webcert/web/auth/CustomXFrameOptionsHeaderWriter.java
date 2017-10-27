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
package se.inera.intyg.webcert.web.auth;

import static org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter.XFRAME_OPTIONS_HEADER;
import static se.inera.intyg.infra.security.common.model.UserOriginType.READONLY;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.header.HeaderWriter;

import se.inera.intyg.webcert.web.service.user.WebCertUserService;

/**
 * Created by marced on 2017-10-25.
 */
public class CustomXFrameOptionsHeaderWriter implements HeaderWriter {

    private static final Logger LOG = LoggerFactory.getLogger(CustomXFrameOptionsHeaderWriter.class);

    @Autowired
    private WebCertUserService userService;

    @Override
    public void writeHeaders(HttpServletRequest request, HttpServletResponse response) {

        if (shouldSkipHeader()) {
            LOG.debug("Skipping  " + XFRAME_OPTIONS_HEADER + " header for request to " + request.getRequestURI());
        } else {
            response.addHeader(XFRAME_OPTIONS_HEADER, "DENY");
        }
    }

    private boolean shouldSkipHeader() {
        if (!userService.hasAuthenticationContext() || READONLY.name().equals(userService.getUser().getOrigin())) {
            return true;
        }
        // Default is to add the header
        return false;

    }

}
