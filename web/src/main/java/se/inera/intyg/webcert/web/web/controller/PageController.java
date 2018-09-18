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
package se.inera.intyg.webcert.web.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.maillink.MailLinkService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * @author marced
 */
@Controller
@RequestMapping(value = "")
public class PageController {

    private static final Logger LOG = LoggerFactory.getLogger(PageController.class);

    @Autowired
    private WebCertUserService webCertUserService;

    @Autowired
    private MailLinkService mailLinkService;

    @Autowired
    private IntygService intygService;

    @RequestMapping(value = "/maillink/intyg/{typ}/{intygId}", method = RequestMethod.GET)
    @PrometheusTimeMethod
    public ResponseEntity<Object> redirectToIntyg(@PathVariable("intygId") String intygId, @PathVariable("typ") String typ) {
        // WC 5.0 new: change vårdenhet
        String enhetHsaId = intygService.getIssuingVardenhetHsaId(intygId, typ);
        if (enhetHsaId == null) {
            LOG.error("Could not redirect user to utkast using /maillink for intygsId '" + intygId
                    + "'. No enhetsId found for utkast. Does the utkast exist?");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        WebCertUser user = webCertUserService.getUser();
        if (!user.changeValdVardenhet(enhetHsaId)) {
            HttpHeaders httpHeaders = new HttpHeaders();
            URI uri = UriBuilder.fromPath("/error.jsp")
                    .queryParam("reason", "enhet.auth.exception")
                    .queryParam("enhetHsaId", enhetHsaId)
                    .build();
            httpHeaders.setLocation(uri);
            return new ResponseEntity<>(httpHeaders, HttpStatus.FOUND);
        }

        URI uri = mailLinkService.intygRedirect(typ, intygId);

        if (uri == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setLocation(uri);
            return new ResponseEntity<>(httpHeaders, HttpStatus.SEE_OTHER);
        }
    }

}
