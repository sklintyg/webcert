/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.log.factory;

import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

public interface LogRequestFactory {

    LogRequest createLogRequestFromUtkast(Utkast utkast);

    LogRequest createLogRequestFromUtkast(Utkast utkast, boolean coherentJournaling);

    LogRequest createLogRequestFromUtlatande(Utlatande utlatande);

    LogRequest createLogRequestFromUtlatande(Utlatande utlatande, boolean coherentJournaling);

    LogRequest createLogRequestFromUser(WebCertUser user, String patientId);
}
