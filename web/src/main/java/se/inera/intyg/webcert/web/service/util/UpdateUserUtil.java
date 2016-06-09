/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.util;

import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

public final class UpdateUserUtil {

    private UpdateUserUtil() {
    }

    public static VardpersonReferens createVardpersonFromWebCertUser(WebCertUser user) {
        VardpersonReferens vardPerson = new VardpersonReferens();
        vardPerson.setNamn(user.getNamn());
        vardPerson.setHsaId(user.getHsaId());

        return vardPerson;
    }

    public static VardpersonReferens createVardpersonFromHosPerson(HoSPersonal hosPerson) {
        VardpersonReferens vardPerson = new VardpersonReferens();
        vardPerson.setNamn(hosPerson.getFullstandigtNamn());
        vardPerson.setHsaId(hosPerson.getPersonId());
        return vardPerson;
    }

}
