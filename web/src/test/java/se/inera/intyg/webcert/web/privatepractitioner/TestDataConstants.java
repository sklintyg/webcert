/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.privatepractitioner;

import java.util.List;
import se.inera.intyg.webcert.integration.privatepractitioner.model.Code;
import se.inera.intyg.webcert.web.web.controller.api.dto.privatepractitioner.CodeDTO;

public class TestDataConstants {

    private TestDataConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String DR_KRANSTEGE_PERSON_ID = "197705232382";
    public static final String DR_KRANSTEGE_HSA_ID = "SE165565594230-WEBCERT00001";
    public static final String DR_KRANSTEGE_NAME = "Frida Kranstege";
    public static final String DR_KRANSTEGE_POSITION = "Överläkare";
    public static final String DR_KRANSTEGE_CARE_UNIT_NAME = "Kransteges specialistmottagning";
    public static final String DR_KRANSTEGE_OWNERSHIP_TYPE = "Privat";
    public static final String DR_KRANSTEGE_TYPE_OF_CARE = "01";
    public static final String DR_KRANSTEGE_HEALTHCARE_SERVICE_TYPE = "11";
    public static final String DR_KRANSTEGE_WORKPLACE_CODE = "555";
    public static final String DR_KRANSTEGE_PHONE_NUMBER = "0123-456789";
    public static final String DR_KRANSTEGE_EMAIL = "frida@kranstege.se";
    public static final String DR_KRANSTEGE_ADDRESS = "Addressgatan 1";
    public static final String DR_KRANSTEGE_ZIP_CODE = "12345";
    public static final String DR_KRANSTEGE_CITY = "Stad";
    public static final String DR_KRANSTEGE_MUNICIPALITY = "Kommun";
    public static final String DR_KRANSTEGE_COUNTY = "Län";
    public static final String DR_KRANSTEGE_PRESCRIPTION_CODE = "12345";
    public static final List<CodeDTO> DR_KRANSTEGE_SPECIALITIES = List.of(
        new CodeDTO("32", "Klinisk fysiologi"),
        new CodeDTO("74", "Nukleärmedicin")
    );
    public static final List<Code> DR_KRANSTEGE_LICENSED_HEALTHCARE_PROFESSIONS =
        List.of(new Code("LK", "Läkare"));

    public static final long CONSENT_FORM_VERSION = 1L;

    public static final List<Code> POSITIONS = List.of(new Code("203090", "Läkare legitimerad, annan"),
        new Code("201010", "Överläkare"));

    public static final List<Code> HEALTHCARE_SERVICE_TYPES = List.of(
        new Code("11", "Medicinsk verksamhet"),
        new Code("13", "Opererande verksamhet")
    );

    public static final List<Code> TYPE_OF_CARE = List.of(
        new Code("01", "Öppenvård"),
        new Code("02", "Slutenvård")
    );


    public static final List<CodeDTO> POSITIONS_DTO = List.of(
        new CodeDTO("203090", "Läkare legitimerad, annan"),
        new CodeDTO("201010", "Överläkare"));

    public static final List<CodeDTO> HEALTHCARE_SERVICE_TYPES_DTO = List.of(
        new CodeDTO("11", "Medicinsk verksamhet"),
        new CodeDTO("13", "Opererande verksamhet")
    );

    public static final List<CodeDTO> TYPE_OF_CARE_DTO = List.of(
        new CodeDTO("01", "Öppenvård"),
        new CodeDTO("02", "Slutenvård")
    );


}
