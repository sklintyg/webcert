package se.inera.webcert.hsa.services;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import se.inera.webcert.hsa.model.Vardenhet;
import se.inera.webcert.hsa.model.Vardgivare;
import se.inera.webcert.hsa.stub.HsaServiceStub;
import se.inera.webcert.hsa.stub.Medarbetaruppdrag;

/**
 * @author andreaskaltenbach
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:HsaOrganizationsServiceTest/test-context.xml")
public class HsaOrganizationsServiceTest {

    private static final String PERSON_HSA_ID = "Gunilla";

    private static final String CENTRUM_VAST = "centrum-vast";
    private static final String CENTRUM_OST = "centrum-ost";
    private static final String CENTRUM_NORR = "centrum-norr";

    @Autowired
    private HsaOrganizationsService service;

    @Autowired
    private HsaServiceStub serviceStub;

    @Test
    public void testEmptyResultSet() {

        Collection<Vardgivare> vardgivare = service.getAuthorizedEnheterForHosPerson(PERSON_HSA_ID);
        assertTrue(vardgivare.isEmpty());
    }

    @Test
    public void testSingleEnhetWithoutMottagningar() {
        addMedarbetaruppdrag(PERSON_HSA_ID, asList(CENTRUM_NORR));

        List<Vardgivare> vardgivare = service.getAuthorizedEnheterForHosPerson(PERSON_HSA_ID);
        assertEquals(1, vardgivare.size());

        Vardgivare vg = vardgivare.get(0);
        assertEquals("vastmanland", vg.getId());
        assertEquals("Landstinget Västmanland", vg.getNamn());
        assertEquals(1, vg.getVardenheter().size());

        Vardenhet enhet = vg.getVardenheter().get(0);
        assertEquals("centrum-norr", enhet.getId());
        assertEquals("Vårdcentrum i Norr", enhet.getNamn());
        assertTrue(enhet.getMottagningar().isEmpty());
    }

    @Test
    public void testMultipleEnheter() {
        addMedarbetaruppdrag(PERSON_HSA_ID, asList(CENTRUM_VAST, CENTRUM_OST, CENTRUM_NORR));

        List<Vardgivare> vardgivare = service.getAuthorizedEnheterForHosPerson(PERSON_HSA_ID);
        assertEquals(1, vardgivare.size());

        Vardgivare vg = vardgivare.get(0);
        assertEquals(3, vg.getVardenheter().size());

        Vardenhet centrumVast = vg.getVardenheter().get(0);
        assertEquals(2, centrumVast.getMottagningar().size());

        Vardenhet centrumOst = vg.getVardenheter().get(1);
        assertEquals(1, centrumOst.getMottagningar().size());

        Vardenhet centrumNorr = vg.getVardenheter().get(2);
        assertEquals(0, centrumNorr.getMottagningar().size());
    }

    @Test
    public void testMultipleVardgivare() throws IOException {
        addVardgivare("HsaOrganizationsServiceTest/landstinget-kings-landing.json");

        addMedarbetaruppdrag("Gunilla", asList(CENTRUM_NORR, "red-keep"));

        List<Vardgivare> vardgivare = service.getAuthorizedEnheterForHosPerson(PERSON_HSA_ID);
        assertEquals(2, vardgivare.size());

        assertEquals("vastmanland", vardgivare.get(0).getId());
        assertEquals("kings-landing", vardgivare.get(1).getId());
    }

    private void addMedarbetaruppdrag(String hsaId, List<String> enhetIds) {
        serviceStub.getMedarbetaruppdrag().add(new Medarbetaruppdrag(hsaId, enhetIds));
    }

    private void addVardgivare(String file) throws IOException {
        Vardgivare vardgivare = new ObjectMapper().readValue(new ClassPathResource(file).getFile(), Vardgivare.class);
        serviceStub.getVardgivare().add(vardgivare);
    }

    @Before
    public void setupVardgivare() throws IOException {
        addVardgivare("HsaOrganizationsServiceTest/landstinget-vastmanland.json");
    }

    @After
    public void cleanupServiceStub() {
        serviceStub.getVardgivare().clear();
        serviceStub.getMedarbetaruppdrag().clear();
    }

    @Test
    @Ignore
    public void testUppdragFiltering() {
        fail();
    }

    @Test
    @Ignore
    public void testInactiveEnhetFiltering() {
        fail();
        // before and after date
    }

    @Test
    @Ignore
    public void testInactiveMottagningFiltering() {
        fail();
        // before and after date
    }
}
