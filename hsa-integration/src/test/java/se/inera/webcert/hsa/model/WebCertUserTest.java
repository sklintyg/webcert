package se.inera.webcert.hsa.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class WebCertUserTest {

    private WebCertUser wcu;
    
    @Test
    public void testGetAsJson() {
        String res = wcu.getAsJson();
        assertNotNull(res);
        assertTrue(res.length() > 0);
        System.out.println(res);
    }
    
    @Test
    public void testChangeValdVardenhetWithNullParam() {
        boolean res = wcu.changeValdVardenhet(null);
        assertFalse(res);
    }
    
    @Test
    public void testChangeValdVardenhetThatIsAVardenhet() {
        boolean res = wcu.changeValdVardenhet("VG1VE2");
        assertTrue(res);
        assertEquals("Vardenhet 2", wcu.getValdVardenhet().getNamn());
        assertEquals("Vardgivare 1", wcu.getValdVardgivare().getNamn());
    }
    
    @Test
    public void testChangeValdVardenhetThatIsAMottagning() {
        boolean res = wcu.changeValdVardenhet("VG2VE1M1");
        assertTrue(res);
        assertEquals("Mottagning 1", wcu.getValdVardenhet().getNamn());
        assertEquals("Vardgivare 2", wcu.getValdVardgivare().getNamn());
    }
    
    @Test
    public void testGetVardenheterIdsWithMottagningSelected() {
        
        // Set a Vardenhet that has no Mottagningar as selected
        boolean res = wcu.changeValdVardenhet("VG1VE1");
        assertTrue(res);
        
        List<String> ids = wcu.getIdsOfSelectedVardenhet();
        assertNotNull(ids);
        assertEquals(1, ids.size());
    }
    
    @Test
    public void testGetVardenheterIdsWithVardenhetSelected() {
        
        // Set the Vardenhet that has a Mottagning attached as selected
        boolean res = wcu.changeValdVardenhet("VG2VE1");
        assertTrue(res);
        
        List<String> ids = wcu.getIdsOfSelectedVardenhet();
        assertNotNull(ids);
        assertEquals(2, ids.size());
    }
    
    @Test
    public void testGetIdsOfAllVardenheter() {
        
        List<String> ids = wcu.getIdsOfAllVardenheter();
        assertNotNull(ids);
        assertEquals(5, ids.size());
    }
    
    @Before
    public void setup() {
        this.wcu = createWebCertUser();
    }
    
    private WebCertUser createWebCertUser() {
        
        WebCertUser wcu = new WebCertUser();
        
        wcu.setNamn("A Name");
        wcu.setHsaId("HSA-id");
        wcu.setForskrivarkod("Forskrivarkod");
        wcu.setAuthenticationScheme("AuthScheme");
        wcu.setLakare(true);
        
        List<Vardgivare> vardgivare = new ArrayList<Vardgivare>();
        
        Vardgivare vg1 = buildVardgivare("VG1","Vardgivare 1");
        
        Vardenhet vg1ve1 = buildVardenhet("VG1VE1", "Vardenhet 1");
        vg1.getVardenheter().add(vg1ve1);
        
        Vardenhet vg1ve2 = buildVardenhet("VG1VE2", "Vardenhet 2");
        vg1.getVardenheter().add(vg1ve2);
        
        Vardgivare vg2 = buildVardgivare("VG2","Vardgivare 2");
        
        Vardenhet vg2ve1 = buildVardenhet("VG2VE1", "Vardenhet 3");
        vg2.getVardenheter().add(vg2ve1);
        
        Vardenhet vg2ve2 = buildVardenhet("VG2VE2", "Vardenhet 4");
        vg2.getVardenheter().add(vg2ve2);
        
        Mottagning vg2ve2m1 = buildMottagning("VG2VE1M1", "Mottagning 1");
        vg2ve1.getMottagningar().add(vg2ve2m1);
        
        vardgivare.add(vg1);        
        vardgivare.add(vg2);
        
        wcu.setVardgivare(vardgivare);
        
        wcu.setValdVardenhet(vg2ve2m1);
        wcu.setValdVardgivare(vg2);
        
        return wcu;
    }
    
    private Mottagning buildMottagning(String id, String namn) {
        Mottagning m = new Mottagning();
        m.setId(id);
        m.setNamn(namn);
        return m;
    }
    
    private Vardenhet buildVardenhet(String id, String namn) {
        Vardenhet vg1ve1 = new Vardenhet();
        vg1ve1.setId(id);
        vg1ve1.setNamn(namn);
        return vg1ve1;
    }

    private Vardgivare buildVardgivare(String id, String namn) {
        Vardgivare vg1 = new Vardgivare();
        vg1.setId(id);
        vg1.setNamn(namn);
        return vg1;
    }
    
}
