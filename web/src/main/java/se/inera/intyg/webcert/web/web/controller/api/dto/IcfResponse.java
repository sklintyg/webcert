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
package se.inera.intyg.webcert.web.web.controller.api.dto;

import java.util.List;

public class IcfResponse {

    private IcfDiagnoskodResponse gemensamma;
    private List<IcfDiagnoskodResponse> unika;

    public IcfResponse() {
    }

    private IcfResponse(final IcfDiagnoskodResponse gemensamma, final List<IcfDiagnoskodResponse> unika) {
        this.gemensamma = gemensamma;
        this.unika = unika;
    }

    public IcfDiagnoskodResponse getGemensamma() {
        return gemensamma;
    }

    public void setGemensamma(final IcfDiagnoskodResponse gemensamma) {
        this.gemensamma = gemensamma;
    }

    public List<IcfDiagnoskodResponse> getUnika() {
        return unika;
    }

    public void setUnika(final List<IcfDiagnoskodResponse> unika) {
        this.unika = unika;
    }

    public static IcfResponse of(final IcfDiagnoskodResponse gemensamma, final List<IcfDiagnoskodResponse> unika) {
        return new IcfResponse(gemensamma, unika);
    }
}
