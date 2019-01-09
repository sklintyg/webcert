/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.common.service.notification;

import static se.inera.intyg.common.support.Constants.KV_AMNE_CODE_SYSTEM;

import org.springframework.util.Assert;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Amneskod;

/**
 * @author Magnus Ekstrand on 2017-04-05.
 */
public final class AmneskodCreator {

    // It's an utility class, hide the default constructor
    private AmneskodCreator() {
    }

    public static Amneskod create(String code, String displayName) {
        Assert.notNull(code, "The code argument must not be null");

        Amneskod amne = new Amneskod();
        amne.setCode(code);
        amne.setCodeSystem(KV_AMNE_CODE_SYSTEM);
        amne.setDisplayName(displayName);

        return amne;
    }

}
