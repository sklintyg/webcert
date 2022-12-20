/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.facade.util;

import java.util.Map;

public class RecipientConverter {

    public static final String FKASSA = "FKASSA";
    public static final String TRANSP = "TRANSP";
    public static final String SKV = "SKV";
    public static final String SOS = "SOS";
    public static final String AF = "AF";
    public static final String AG = "AG";
    
    private static final Map<String, String> recipientMap = Map.of(
        FKASSA, "Försäkringskassan",
        TRANSP, "Transportstyrelsen",
        SKV, "Skatteverket",
        SOS, "Socialstyrelsen",
        AF, "Arbetsförmedlingen",
        AG, "Arbetsgivaren"
    );

    public static String getRecipientName(String recipientCode) {
        return recipientMap.getOrDefault(recipientCode, "okänd mottagare");
    }
}
