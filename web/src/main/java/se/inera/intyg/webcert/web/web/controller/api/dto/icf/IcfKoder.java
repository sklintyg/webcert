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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import java.util.List;

public abstract class IcfKoder {

    private List<String> icd10Koder;
    private List<IcfKod> icfKoder;

    public IcfKoder() {
    }

    IcfKoder(
            final List<String> icd10Koder,
            final List<IcfKod> icfKoder) {
        this.icd10Koder = CollectionUtils.isEmpty(icd10Koder) ? null : icd10Koder;
        this.icfKoder = CollectionUtils.isEmpty(icfKoder) ? null : icfKoder;
    }

    public List<String> getIcd10Koder() {
        return icd10Koder;
    }

    public void setIcd10Koder(final List<String> icd10Koder) {
        this.icd10Koder = icd10Koder;
    }

    public List<IcfKod> getIcfKoder() {
        return icfKoder;
    }

    public void setIcfKoder(final List<IcfKod> icfKoder) {
        this.icfKoder = icfKoder;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("icd10Koder", icd10Koder)
                .append("icfKoder", icfKoder)
                .toString();
    }
}
