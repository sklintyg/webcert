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
package se.inera.intyg.webcert.web.converter.util;

import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.fragasvar.dto.FrageStallare;

public final class FragestallareConverterUtil {
    private FragestallareConverterUtil() {
    }

    public static String partToFrageStallarKod(String partCode) {
        try {
            PartToFragestallare partToFragestallare = PartToFragestallare.valueOf(partCode);
            FrageStallare ret = FrageStallare.getByKod(partToFragestallare.fragestallarKod);
            return ret.getKod();
        } catch (IllegalArgumentException ie) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM,
                    "Invalid PartCode found when converting Arende");
        }
    }

    /**
     * This enum matches Part.code (the name of the enum constants) to the internal representation FrageStallare
     * used in Webcert (the fragestallarKod i.e the value).
     *
     * Please observe that any changes made to the values in Part.code must be echoed in the enum constant names here.
     */
    private enum PartToFragestallare {
        FKASSA("FK"),
        HSVARD("WC");

        private final String fragestallarKod;

        PartToFragestallare(String fragestallarKod) {
            this.fragestallarKod = fragestallarKod;
        }

    }
}
