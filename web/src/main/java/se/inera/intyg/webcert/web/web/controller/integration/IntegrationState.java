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
package se.inera.intyg.webcert.web.web.controller.integration;

/**
 * The class is used to hold information (state) regarding a user
 * has been redirected to Webcert's 'enhetsväljare'.
 *
 * In the scenario 'djupintegration' a user might have access to multiple
 * healt care units when requesting a certificate from Webcert. If none is
 * provided with the request, the user must select a healt care unit. This
 * is done by redirecting the user to 'enhetsväljaren'.
 *
 * When user has selected the healt care unit, user will be redirected to
 * the certificate he/she requested.
 *
 * To prevent requests that intentional without to be
 *
 * To prevent requests that intentionally try to request the certificate without
 * selecting a unit, we need to keep this information in a state..
 *
 * @author Magnus Ekstrand on 2017-10-12.
 */
public class IntegrationState {

    private boolean redirectToEnhetsval;


    /**
     *  If true, user has been redirected to 'enhetsväljaren' to select
     *  the healt care unit on which the certificate is written.
     *
     * @return true if user has been redirected to 'enhetsväljaren', otherwise false
     */
    public boolean hasUserBeenRedirectedToEnhetsval() {
        return redirectToEnhetsval;
    }

    /**
     * Sets if user has been redirected to 'enhetsväljaren' or not.
     */
    void setRedirectToEnhetsval(boolean redirectToEnhetsval) {
        this.redirectToEnhetsval = redirectToEnhetsval;
    }
}
