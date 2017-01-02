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
package se.inera.intyg.webcert.web.service.util;

import java.util.Comparator;

import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;

/**
 * Compare senasteHandelseDatum (latest first) for two FragaSvar entities in a null safe manner.
 *
 * @author marced
 */
public class FragaSvarSenasteHandelseDatumComparator implements Comparator<FragaSvar> {

    @Override
    public int compare(FragaSvar f1, FragaSvar f2) {
        if (f1.getSenasteHandelseDatum() == null && f2.getSenasteHandelseDatum() == null) {
            return 0;
        } else if (f1.getSenasteHandelseDatum() == null) {
            return -1;
        } else if (f2.getSenasteHandelseDatum() == null) {
            return 1;
        } else {
            return f2.getSenasteHandelseDatum().compareTo(f1.getSenasteHandelseDatum());
        }
    }
}
