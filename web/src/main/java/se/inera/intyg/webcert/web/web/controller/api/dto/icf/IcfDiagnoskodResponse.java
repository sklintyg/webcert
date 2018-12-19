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
package se.inera.intyg.webcert.web.web.controller.api.dto.icf;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class IcfDiagnoskodResponse {

    private String icd10Kod;
    private FunktionsNedsattningsKoder funktionsNedsattningsKoder;
    private AktivitetsBegransningsKoder aktivitetsBegransningsKoder;

    public IcfDiagnoskodResponse() {
    }

    private IcfDiagnoskodResponse(
            final String icd10Kod,
            final FunktionsNedsattningsKoder funktionsNedsattningsKoder,
            final AktivitetsBegransningsKoder aktivitetsBegransningsKoder) {
        this.icd10Kod = icd10Kod;
        this.funktionsNedsattningsKoder = funktionsNedsattningsKoder;
        this.aktivitetsBegransningsKoder = aktivitetsBegransningsKoder;
    }

    public String getIcd10Kod() {
        return icd10Kod;
    }

    public void setIcd10Kod(final String icd10Kod) {
        this.icd10Kod = icd10Kod;
    }

    public FunktionsNedsattningsKoder getFunktionsNedsattningsKoder() {
        return funktionsNedsattningsKoder;
    }

    public void setFunktionsNedsattningsKoder(final FunktionsNedsattningsKoder funktionsNedsattningsKoder) {
        this.funktionsNedsattningsKoder = funktionsNedsattningsKoder;
    }

    public AktivitetsBegransningsKoder getAktivitetsBegransningsKoder() {
        return aktivitetsBegransningsKoder;
    }

    public void setAktivitetsBegransningsKoder(final AktivitetsBegransningsKoder aktivitetsBegransningsKoder) {
        this.aktivitetsBegransningsKoder = aktivitetsBegransningsKoder;
    }

    public static IcfDiagnoskodResponse of(
            final String icf10Kod,
            final IcfKoder funktionsNedsattningsKoder,
            final IcfKoder aktivitetsBegransningsKoder) {

        if (funktionsNedsattningsKoder != null && !(funktionsNedsattningsKoder instanceof FunktionsNedsattningsKoder)) {
            throw new IllegalArgumentException("funktionsNedsattningsKoder must be of type FunktionsNedsattningsKoder");
        }

        if (aktivitetsBegransningsKoder != null && !(aktivitetsBegransningsKoder instanceof AktivitetsBegransningsKoder)) {
            throw new IllegalArgumentException("aktivitetsBegransningsKoder must be of type AktivitetsBegransningsKoder");
        }

        if (funktionsNedsattningsKoder == null && aktivitetsBegransningsKoder == null) {
            return null;
        }

        return new IcfDiagnoskodResponse(
                icf10Kod,
                (FunktionsNedsattningsKoder) funktionsNedsattningsKoder,
                (AktivitetsBegransningsKoder) aktivitetsBegransningsKoder);
    }

    public static IcfDiagnoskodResponse of(
            final IcfKoder funktionsNedsattningsKoder,
            final IcfKoder aktivitetsBegransningsKoder) {

        if (funktionsNedsattningsKoder != null && !(funktionsNedsattningsKoder instanceof FunktionsNedsattningsKoder)) {
            throw new IllegalArgumentException("funktionsNedsattningsKoder must be of type FunktionsNedsattningsKoder");
        }

        if (aktivitetsBegransningsKoder != null && !(aktivitetsBegransningsKoder instanceof AktivitetsBegransningsKoder)) {
            throw new IllegalArgumentException("aktivitetsBegransningsKoder must be of type AktivitetsBegransningsKoder");
        }

        return new IcfDiagnoskodResponse(
                null,
                (FunktionsNedsattningsKoder) funktionsNedsattningsKoder,
                (AktivitetsBegransningsKoder) aktivitetsBegransningsKoder);
    }

    public static IcfDiagnoskodResponse empty() {
        return new IcfDiagnoskodResponse(null, null, null);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("icd10Kod", icd10Kod)
                .append("funktionsNedsattningsKoder", funktionsNedsattningsKoder)
                .append("aktivitetsBegransningsKoder", aktivitetsBegransningsKoder)
                .toString();
    }
}
