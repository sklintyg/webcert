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

package se.inera.intyg.webcert.web.integration.interactions.listcertificatesforcarewithqa;

import static se.inera.intyg.common.support.Constants.KV_AMNE_CODE_SYSTEM;
import static se.inera.intyg.common.support.Constants.KV_HANDELSE_CODE_SYSTEM;

import se.inera.intyg.common.support.modules.converter.InternalConverterUtil;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Amneskod;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Handelsekod;
import se.riv.clinicalprocess.healthcond.certificate.v3.Handelse;

public class HandelseFactory {

    private HandelseFactory() {
        throw new IllegalStateException("Utility class");
    }

    public static Handelse toHandelse(se.inera.intyg.webcert.persistence.handelse.model.Handelse e) {
        Handelse res = new Handelse();

        Handelsekod code = new Handelsekod();
        code.setCodeSystem(KV_HANDELSE_CODE_SYSTEM);
        code.setCode(e.getCode().value());
        res.setHandelsekod(code);
        if (e.getAmne() != null) {
            res.setAmne(buildAmne(e.getAmne()));
        }
        res.setSistaDatumForSvar(e.getSistaDatumForSvar());
        res.setTidpunkt(e.getTimestamp());
        if (e.getHanteratAv() != null) {
            res.setHanteratAv(InternalConverterUtil.getHsaId(e.getHanteratAv()));
        }

        return res;
    }

    private static Amneskod buildAmne(ArendeAmne arende) {
        Amneskod amneskod = new Amneskod();
        amneskod.setCode(arende.name());
        amneskod.setCodeSystem(KV_AMNE_CODE_SYSTEM);
        amneskod.setDisplayName(arende.getDescription());
        return amneskod;
    }
}
