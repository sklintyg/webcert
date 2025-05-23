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
package se.inera.intyg.webcert.web.converter.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.fragasvar.dto.FrageStallare;

public class FragestallareConverterUtilTest {

    private static final String PARTCODE_FKASSA = "FKASSA";
    private static final String PARTCODE_HSVARD = "HSVARD";

    @Test
    public void testPartCodeToFrageStallareKod() {
        assertEquals(FrageStallare.FORSAKRINGSKASSAN.getKod(), FragestallareConverterUtil
            .partToFrageStallarKod(PARTCODE_FKASSA));
        assertEquals(FrageStallare.WEBCERT.getKod(), FragestallareConverterUtil
            .partToFrageStallarKod(PARTCODE_HSVARD));
    }

    @Test
    public void testPartNameToFrageStallareNamn() {
        assertTrue(FrageStallare.FORSAKRINGSKASSAN.isNameEqual("Försäkringskassan"));
        assertTrue(FrageStallare.WEBCERT.isNameEqual("Webcert"));
    }

    @Test(expected = WebCertServiceException.class)
    public void testInvalidPartKod() {
        FragestallareConverterUtil.partToFrageStallarKod("INVALID");
    }
}
