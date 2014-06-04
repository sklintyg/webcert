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
package se.inera.webcert.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.web.service.WebCertUserService;

/**
 * @author marced
 */
@Controller
@RequestMapping(value = "")
public class PageController {

    @Autowired
    private Environment environment;

    public static final String ADMIN_VIEW = "dashboard#/unhandled-qa";
    public static final String ADMIN_VIEW_REDIRECT = "redirect:/web/" + ADMIN_VIEW;

    public static final String DASHBOARD_VIEW = "dashboard";
    public static final String DASHBOARD_VIEW_REDIRECT = "redirect:/web/" + DASHBOARD_VIEW;

    private static final Logger LOG = LoggerFactory.getLogger(PageController.class);

    @Autowired
    private WebCertUserService webCertUserService;

    @RequestMapping(value = "/start", method = RequestMethod.GET)
    public ModelAndView displayStart() {
        WebCertUser user = webCertUserService.getWebCertUser();
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
        if (user.isLakare()) {
            return DASHBOARD_VIEW_REDIRECT;
        } else {
            return ADMIN_VIEW_REDIRECT;
        }
    }

    @RequestMapping(value = "/dashboard", method = RequestMethod.GET)
    public ModelAndView displayDashBoard() {
        ModelAndView modelAndView = new ModelAndView(DASHBOARD_VIEW);
        modelAndView.addObject("requireDevMode",environment.getProperty("webcert.require.devmode", "false"));
        return modelAndView;
    }

    @RequestMapping(value = "/adminview", method = RequestMethod.GET)
    public ModelAndView displayAdminView() {
        return new ModelAndView(ADMIN_VIEW);
    }

}
