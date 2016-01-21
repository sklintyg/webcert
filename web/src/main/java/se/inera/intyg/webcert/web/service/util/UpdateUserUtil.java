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

import java.util.stream.Collectors;

import se.inera.intyg.common.support.modules.support.api.dto.HoSPersonal;
import se.inera.intyg.common.support.modules.support.api.dto.Vardenhet;
import se.inera.intyg.common.integration.hsa.model.AbstractVardenhet;
import se.inera.intyg.common.integration.hsa.model.SelectableVardenhet;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.web.service.dto.HoSPerson;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

public final class UpdateUserUtil {

    private UpdateUserUtil() {
    }

    /**
     * Create a user object from WebCertUser.
     *
     * Note that befattningar (a List) is concatenated into a string with ", " as separator.
     *
     * @param user {@link WebCertUser}
     */
    public static HoSPersonal createUserObject(WebCertUser user) {
        SelectableVardenhet valdVardgivare = user.getValdVardgivare();
        se.inera.intyg.common.support.modules.support.api.dto.Vardgivare vardgivare = new se.inera.intyg.common.support.modules.support.api.dto.Vardgivare(
                valdVardgivare.getId(), valdVardgivare.getNamn());

        AbstractVardenhet valdVardenhet = (AbstractVardenhet) user.getValdVardenhet();
        Vardenhet vardenhet = new se.inera.intyg.common.support.modules.support.api.dto.Vardenhet(
                valdVardenhet.getId(), valdVardenhet.getNamn(), valdVardenhet.getPostadress(), valdVardenhet.getPostnummer(),
                valdVardenhet.getPostort(), valdVardenhet.getTelefonnummer(), valdVardenhet.getEpost(), valdVardenhet.getArbetsplatskod(), vardgivare);

        String befattning = user.getBefattningar().stream().collect(Collectors.joining(", "));
        HoSPersonal hosPerson = new HoSPersonal(
                user.getHsaId(),
                user.getNamn(), user.getForskrivarkod(), befattning, user.getSpecialiseringar(), vardenhet);
        return hosPerson;
    }

    public static VardpersonReferens createVardpersonFromWebCertUser(WebCertUser user) {
        VardpersonReferens vardPerson = new VardpersonReferens();
        vardPerson.setNamn(user.getNamn());
        vardPerson.setHsaId(user.getHsaId());

        return vardPerson;
    }

    public static VardpersonReferens createVardpersonFromHosPerson(HoSPerson hosPerson) {
        VardpersonReferens vardPerson = new VardpersonReferens();
        vardPerson.setNamn(hosPerson.getNamn());
        vardPerson.setHsaId(hosPerson.getHsaId());
        return vardPerson;
    }

}
