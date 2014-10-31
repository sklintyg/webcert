package se.inera.webcert.web.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.Assert;
import org.junit.Test;

/**
 * @author johannesc
 */
public class HsaServiceImplTest {

    HsaServiceImpl service = new HsaServiceImpl();


    @Test
    public void testGetVardenheterMedMedarbetaruppdrag() throws Exception {
        Vardenheter vardenheter = service.getVardenheterMedMedarbetaruppdrag("SOME_USER_HSA_ID");

        Vardenheter result = new ObjectMapper().readValue(vardenheter.stringify(), Vardenheter.class);

        // Test some aspects of the object model
        Assert.assertEquals(vardenheter.getVardenheter().size(), result.getVardenheter().size());
        Assert.assertEquals(vardenheter.getVardenheter().get(0).getMottagningar().get(0).getNamn(),
                result.getVardenheter().get(0).getMottagningar().get(0).getNamn());
    }
}
