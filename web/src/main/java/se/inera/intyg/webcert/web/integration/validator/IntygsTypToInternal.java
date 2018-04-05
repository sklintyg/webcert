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
package se.inera.intyg.webcert.web.integration.validator;

/**
 * Created by eriklupander on 2017-09-11.
 */
public final class IntygsTypToInternal {

    private IntygsTypToInternal() {

    }

    public static String convertToInternalIntygsTyp(String code) {
        if (code == null) {
            throw new IllegalArgumentException("Cannot pass null code to internal intygstyp converter.");
        }

        switch (code.toLowerCase()) {
            case "tstrk1007":
                return "ts-bas";
            case "tstrk1031":
                return "ts-diabetes";
            default:
                return code.toLowerCase();
        }
    }

}
