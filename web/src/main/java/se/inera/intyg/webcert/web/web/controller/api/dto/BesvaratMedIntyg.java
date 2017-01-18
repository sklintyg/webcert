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
package se.inera.intyg.webcert.web.web.controller.api.dto;

import java.time.LocalDateTime;

import com.google.auto.value.AutoValue;

import se.inera.intyg.webcert.persistence.utkast.model.Utkast;

@AutoValue
public abstract class BesvaratMedIntyg {

    public abstract String getIntygsId();

    public abstract String getSigneratAv();

    public abstract LocalDateTime getSigneratDatum();

    public abstract LocalDateTime getSkickatDatum();

    public abstract String getNamnetPaSkapareAvIntyg();

    public static BesvaratMedIntyg create(String intygsId, String signeratAv, LocalDateTime signeratDatum, LocalDateTime skickatDatum,
            String namnetPaSkapareAvIntyg) {
        return new AutoValue_BesvaratMedIntyg(intygsId, signeratAv, signeratDatum, skickatDatum, namnetPaSkapareAvIntyg);
    }

    public static BesvaratMedIntyg create(Utkast intyg) {
        return new AutoValue_BesvaratMedIntyg(intyg.getIntygsId(), intyg.getSignatur().getSigneradAv(),
                intyg.getSignatur().getSigneringsDatum(), intyg.getSkickadTillMottagareDatum(), intyg.getSkapadAv().getNamn());
    }
}
