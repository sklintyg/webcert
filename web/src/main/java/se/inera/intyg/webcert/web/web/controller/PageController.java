/**
 * Copyright (C) 2012 Inera AB (http://www.inera.se)
 *
 * This file is part of Inera Certificate Web (http://code.google.com/p/inera-certificate-web).
 *
 * Inera Certificate Web is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Inera Certificate Web is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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
import org.springframework.web.servlet.ModelAndView;
import se.inera.intyg.webcert.web.service.feature.WebcertFeature;
import se.inera.intyg.webcert.web.service.feature.WebcertFeatureService;
import se.inera.intyg.webcert.web.service.maillink.MailLinkService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import java.net.URI;

/**
 * @author marced
 */
@Controller
@RequestMapping(value = "")
public class PageController {

    public static final String ADMIN_VIEW = "dashboard#/unhandled-qa";
    public static final String ADMIN_VIEW_REDIRECT = "redirect:/web/" + ADMIN_VIEW;

    public static final String DASHBOARD_VIEW = "dashboard";
    public static final String DASHBOARD_VIEW_REDIRECT = "redirect:/web/" + DASHBOARD_VIEW;

    public static final String ABOUT_VIEW = "dashboard#/webcert/about";
    public static final String ABOUT_VIEW_REDIRECT = "redirect:/web/" + ABOUT_VIEW;

    private static final Logger LOG = LoggerFactory.getLogger(PageController.class);

    @Autowired
    private WebCertUserService webCertUserService;

    @Autowired
    private WebcertFeatureService webcertFeatureService;

    @Autowired
    private MailLinkService mailLinkService;

    @RequestMapping(value = "/start", method = RequestMethod.GET)
    public ModelAndView displayStart() {
        WebCertUser user = webCertUserService.getUser();
        LOG.debug("displayStart for user " + user.getNamn());
        return new ModelAndView(resolveStartView(user));
    }

    /**
     * Select Starting point view depending on user properties.
     *
     * @param user user
     * @return String
     */
    protected String resolveStartView(WebCertUser user) {
        if (user.isLakare() && webcertFeatureService.isFeatureActive(WebcertFeature.HANTERA_INTYGSUTKAST)) {
            return DASHBOARD_VIEW_REDIRECT;
        } else if (webcertFeatureService.isFeatureActive(WebcertFeature.HANTERA_FRAGOR)) {
            return ADMIN_VIEW_REDIRECT;
        } else {
            return ABOUT_VIEW_REDIRECT;
        }
    }

    @RequestMapping(value = "/dashboard", method = RequestMethod.GET)
    public ModelAndView displayDashBoard() {
        ModelAndView modelAndView = new ModelAndView(DASHBOARD_VIEW);
        populateUseMinifiedJavaScript(modelAndView);
        return modelAndView;
    }

    @RequestMapping(value = "/adminview", method = RequestMethod.GET)
    public ModelAndView displayAdminView() {
        return new ModelAndView(ADMIN_VIEW);
    }

    public void populateUseMinifiedJavaScript(ModelAndView model) {
        final WebCertUser user = webCertUserService.getUser();
        if (user != null) {
            model.addObject("useMinifiedJavaScript", user.isFeatureActive(WebcertFeature.JS_MINIFIED));
        } else {
            model.addObject("useMinifiedJavaScript", webcertFeatureService.isFeatureActive(WebcertFeature.JS_MINIFIED));
        }
    }

    @RequestMapping(value = "/maillink/intyg/{typ}/{intygId}", method = RequestMethod.GET)
    public ResponseEntity<Object> redirectToIntyg(@PathVariable("intygId") String intygId, @PathVariable("typ") String typ) {
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
