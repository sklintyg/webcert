package se.inera.intyg.webcert.web.converter.util;

import org.junit.Test;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.fragasvar.dto.FrageStallare;

import static org.junit.Assert.assertEquals;

public class FragestallareConverterUtilTest {

    private static final String PARTCODE_FKASSA = "FKASSA";
    private static final String PARTCODE_HSVARD = "HSVARD";

    @Test
    public void testFrageStallareToPartCode() {
        assertEquals(PARTCODE_FKASSA, FragestallareConverterUtil.frageStallareToPartKod(
                FrageStallare.FORSAKRINGSKASSAN.getKod()));
        assertEquals(PARTCODE_HSVARD, FragestallareConverterUtil.frageStallareToPartKod(
                FrageStallare.WEBCERT.getKod()));
    }

    @Test
    public void testPartCodeToFrageStallareKod() {
        assertEquals(FrageStallare.FORSAKRINGSKASSAN.getKod(), FragestallareConverterUtil
                .partToFrageStallarKod(PARTCODE_FKASSA));
        assertEquals(FrageStallare.WEBCERT.getKod(), FragestallareConverterUtil
                .partToFrageStallarKod(PARTCODE_HSVARD));
    }

    @Test(expected = WebCertServiceException.class)
    public void testInvalidFragestallare() {
        FragestallareConverterUtil.frageStallareToPartKod("INVALID");
    }

    @Test(expected = WebCertServiceException.class)
    public void testInvalidPartKod(){
        FragestallareConverterUtil.partToFrageStallarKod("INVALID");
    }
}
