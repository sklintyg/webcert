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
package se.inera.intyg.webcert.integration.pp.stub;

import se.riv.infrastructure.directory.privatepractitioner.v1.HoSPersonType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Magnus Ekstrand on 18/06/15.
 */
public class HoSPersonStub {

    private Map<String, HoSPersonType> personer = new HashMap<>();

    public void add(HoSPersonType person) {
        personer.put(person.getPersonId().getExtension(), person);
    }

    public HoSPersonType get(String id) {
        return personer.get(id);
    }

    public HoSPersonType getByHsaId(String hsaId) {
        if (hsaId != null) {
            for (HoSPersonType person : personer.values()) {
                if (hsaId.equalsIgnoreCase(person.getHsaId().getExtension())) {
                    return person;
                }
            }
        }
        return null;
    }

}
