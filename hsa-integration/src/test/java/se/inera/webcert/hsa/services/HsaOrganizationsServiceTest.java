package se.inera.webcert.hsa.services;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import se.inera.webcert.hsa.model.Mottagning;
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

        Collection<Vardgivare> vardgivare = service.getAuthorizedEnheterForHosPerson(PERSON_HSA_ID, CENTRUM_NORR);
        assertTrue(vardgivare.isEmpty());
    }

    @Test
    public void testSingleEnhetWithoutMottagningar() {
        addMedarbetaruppdrag(PERSON_HSA_ID, asList(CENTRUM_NORR));

        List<Vardgivare> vardgivare = service.getAuthorizedEnheterForHosPerson(PERSON_HSA_ID, CENTRUM_NORR);
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

        List<Vardgivare> vardgivare = service.getAuthorizedEnheterForHosPerson(PERSON_HSA_ID, CENTRUM_OST);
        assertEquals(1, vardgivare.size());

        Vardgivare vg = vardgivare.get(0);
        assertEquals(1, vg.getVardenheter().size());

        Vardenhet centrumOst = vg.getVardenheter().get(0);
        assertEquals(1, centrumOst.getMottagningar().size());
    }

    @Test
    public void testMultipleVardgivare() throws IOException {
        addVardgivare("HsaOrganizationsServiceTest/landstinget-kings-landing.json");

        addMedarbetaruppdrag("Gunilla", asList(CENTRUM_NORR, "red-keep"));

        List<Vardgivare> vardgivare = service.getAuthorizedEnheterForHosPerson(PERSON_HSA_ID, "red-keep");
        assertEquals(1, vardgivare.size());

        assertEquals("kings-landing", vardgivare.get(0).getId());
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
    public void testInactiveEnhetFiltering() throws IOException {

        addVardgivare("HsaOrganizationsServiceTest/landstinget-upp-och-ner.json");

        addMedarbetaruppdrag(PERSON_HSA_ID, asList("finito", "here-and-now", "futuro", "still-open", "will-shutdown"));

        // login with an inactive enhet 'finito'
        List<Vardgivare> vardgivareList = service.getAuthorizedEnheterForHosPerson(PERSON_HSA_ID, "finito");
        assertTrue(vardgivareList.isEmpty());

        // login with an active enhet 'here-and-now'
        vardgivareList = service.getAuthorizedEnheterForHosPerson(PERSON_HSA_ID, "here-and-now");
        assertEquals(1, vardgivareList.size());
        assertEquals("upp-och-ner", vardgivareList.get(0).getId());
        assertEquals(1, vardgivareList.get(0).getVardenheter().size());
        assertEquals("here-and-now", vardgivareList.get(0).getVardenheter().get(0).getId());

        // login with an enhet 'futuro' which will be active in the future
        vardgivareList = service.getAuthorizedEnheterForHosPerson(PERSON_HSA_ID, "futuro");
        assertTrue(vardgivareList.isEmpty());

        // login with an active enhet 'still-open' which will be shut down in the future
        vardgivareList = service.getAuthorizedEnheterForHosPerson(PERSON_HSA_ID, "still-open");
        assertEquals(1, vardgivareList.size());
        assertEquals("upp-och-ner", vardgivareList.get(0).getId());
        assertEquals(1, vardgivareList.get(0).getVardenheter().size());
        assertEquals("still-open", vardgivareList.get(0).getVardenheter().get(0).getId());

        // login with an active enhet 'will-shutdown' which has been shut down in the past
        vardgivareList = service.getAuthorizedEnheterForHosPerson(PERSON_HSA_ID, "will-shutdown");
        assertEquals(1, vardgivareList.size());
        assertEquals("upp-och-ner", vardgivareList.get(0).getId());
        assertEquals(1, vardgivareList.get(0).getVardenheter().size());
        assertEquals("will-shutdown", vardgivareList.get(0).getVardenheter().get(0).getId());
    }

    @Test
    public void testInactiveMottagningFiltering() throws IOException {
        addVardgivare("HsaOrganizationsServiceTest/landstinget-upp-och-ner.json");

        addMedarbetaruppdrag(PERSON_HSA_ID, asList("with-subs"));

        List<Vardgivare> vardgivareList = service.getAuthorizedEnheterForHosPerson(PERSON_HSA_ID, "with-subs");
        assertEquals(1, vardgivareList.size());

        Vardgivare vardgivare = vardgivareList.get(0);
        assertEquals(1, vardgivare.getVardenheter().size());

        List<Mottagning> mottagningar = vardgivare.getVardenheter().get(0).getMottagningar();
        assertEquals(3, mottagningar.size());

        assertEquals("mottagning-here-and-now", mottagningar.get(0).getId());
        assertEquals("mottagning-still-open", mottagningar.get(1).getId());
        assertEquals("mottagning-will-shutdown", mottagningar.get(2).getId());
    }

    @Test
    public void testUppdragFiltering() {

        // user has a different medarbetaruppdrag ändamål 'Animatör' in one enhet
        serviceStub.getMedarbetaruppdrag().add(new Medarbetaruppdrag(PERSON_HSA_ID, asList("centrum-ost"), "Animatör"));

        List<Vardgivare> vardgivareList = service.getAuthorizedEnheterForHosPerson(PERSON_HSA_ID, CENTRUM_OST);

        // no authorized vardgivere should be returned
        assertTrue(vardgivareList.isEmpty());
    }
}
