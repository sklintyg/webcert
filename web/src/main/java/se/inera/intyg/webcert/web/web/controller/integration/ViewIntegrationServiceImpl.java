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
package se.inera.intyg.webcert.web.web.controller.integration;

import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;


/**
 * @author Magnus Ekstrand on 2017-10-09.
 */
@Service
public class ViewIntegrationServiceImpl extends IntegrationServiceImpl {

    @Override
    protected void ensurePreparation(String intygTyp, String intygId, Utkast utkast, WebCertUser user) {

        if (utkast != null) {
            // INTYG-4086: If the intyg / utkast is authored in webcert, we can check for sekretessmarkering here.
            // If the intyg was authored elsewhere, the check has to be performed after the redirect when the actual intyg
            // is loaded from Intygstjänsten.
            verifySekretessmarkering(utkast, user);
        }

    }

}
