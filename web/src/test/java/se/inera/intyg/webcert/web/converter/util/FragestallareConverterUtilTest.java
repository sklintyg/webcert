package se.inera.intyg.webcert.web.converter.util;

import static org.junit.Assert.assertEquals;

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

    @Test(expected = WebCertServiceException.class)
    public void testInvalidPartKod() {
        FragestallareConverterUtil.partToFrageStallarKod("INVALID");
    }
}
