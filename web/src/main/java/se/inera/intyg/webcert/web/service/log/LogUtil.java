/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.log;

import se.inera.intyg.infra.integration.hsatk.model.legacy.SelectableVardenhet;
import se.inera.intyg.webcert.web.service.log.dto.LogUser;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

/**
 * @author Magnus Ekstrand on 2019-09-18.
 */
public final class LogUtil {

    private LogUtil() {
    }

    /**
     * Utility method to create a LogUser object.
     *
     * @param user the Webcert user to get values from
     * @return a LogUser object
     */
    public static LogUser getLogUser(WebCertUser user) {
        SelectableVardenhet valdVardenhet = user.getValdVardenhet();
        SelectableVardenhet valdVardgivare = user.getValdVardgivare();

        return new LogUser.Builder(user.getHsaId(), valdVardenhet.getId(), valdVardgivare.getId())
            .userName(user.getNamn())
            .userAssignment(user.getSelectedMedarbetarUppdragNamn())
            .userTitle(user.getTitel())
            .enhetsNamn(valdVardenhet.getNamn())
            .vardgivareNamn(valdVardgivare.getNamn())
            .build();
    }

}
