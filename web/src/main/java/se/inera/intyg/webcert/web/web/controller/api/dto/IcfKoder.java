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

import org.apache.commons.collections.CollectionUtils;
import java.util.List;

public abstract class IcfKoder {

    private List<String> icd10Koder;
    private List<IcfKod> centralaKoder;
    private List<IcfKod> kompletterandeKoder;

    public IcfKoder() {
    }

    IcfKoder(
            final List<String> icd10Koder,
            final List<IcfKod> centralaKoder,
            final List<IcfKod> kompletterandeKoder) {
        this.icd10Koder = CollectionUtils.isEmpty(icd10Koder) ? null : icd10Koder;
        this.centralaKoder = CollectionUtils.isEmpty(centralaKoder) ? null : centralaKoder;
        this.kompletterandeKoder = CollectionUtils.isEmpty(kompletterandeKoder) ? null : kompletterandeKoder;
    }

    public List<String> getIcd10Koder() {
        return icd10Koder;
    }

    public void setIcd10Koder(final List<String> icd10Koder) {
        this.icd10Koder = icd10Koder;
    }

    public List<IcfKod> getCentralaKoder() {
        return centralaKoder;
    }

    public void setCentralaKoder(final List<IcfKod> centralaKoder) {
        this.centralaKoder = centralaKoder;
    }

    public List<IcfKod> getKompletterandeKoder() {
        return kompletterandeKoder;
    }

    public void setKompletterandeKoder(final List<IcfKod> kompletterandeKoder) {
        this.kompletterandeKoder = kompletterandeKoder;
    }
}
