package se.inera.webcert.persistence.intyg.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.model.IntygsStatus;
import se.inera.webcert.persistence.intyg.model.VardpersonReferens;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:repository-context.xml" })
@ActiveProfiles("dev")
@Transactional
public class IntygRepositoryTest {

    @Autowired
    private IntygRepository intygRepository;

    @PersistenceContext
    private EntityManager em;

    private static String ENHET_1_ID = "ENHET_1_ID";
    private static String ENHET_2_ID = "ENHET_2_ID";
    private static String ENHET_3_ID = "ENHET_3_ID";

    private static final String PERSON_NUMMER = "19121212-1212";

    private static final String INTYGSTYP_FK7263 = "FK7263";
    
    private static final String MODEL = "This is the JSON model of this Intyg " +
    		"with some interesting scandinavian characters like Å, Ä and ö added";
    
    @Test
    public void testFindOne() {
        Intyg saved = intygRepository.save(buildIntyg(ENHET_1_ID));
        Intyg read = intygRepository.findOne(saved.getIntygsId());
       
        assertThat(read.getIntygsId(), is(equalTo(saved.getIntygsId())));
        assertThat(read.getPatientPersonnummer(), is(equalTo(saved.getPatientPersonnummer())));
              
        assertThat(read.getEnhetsId(), is(notNullValue()));
        
        assertThat(read.getModel(), is(equalTo(MODEL)));
    }

    @Test
    public void testFindByEnhetsIdDontReturnSigned() {

        Intyg intyg1 = intygRepository.save(buildIntyg(ENHET_1_ID, IntygsStatus.WORK_IN_PROGRESS));
        Intyg intyg2 = intygRepository.save(buildIntyg(ENHET_1_ID, IntygsStatus.WORK_IN_PROGRESS));
        Intyg intyg3 = intygRepository.save(buildIntyg(ENHET_3_ID, IntygsStatus.WORK_IN_PROGRESS));
        intygRepository.save(buildIntyg(ENHET_1_ID, IntygsStatus.SIGNED));
        intygRepository.save(buildIntyg(ENHET_2_ID, IntygsStatus.SIGNED));

        List<Intyg> result = intygRepository.findUnsignedByEnhetsId(Arrays.asList(ENHET_1_ID, ENHET_3_ID));
        
        assertThat(result.size(), is(3));
        
        assertThat(intyg1, isIn(result));
        assertThat(intyg2, isIn(result));
        assertThat(intyg3, isIn(result));

    }

    @Test
    public void testCountUnsignedByEnhetsId() {

        intygRepository.save(buildIntyg(ENHET_1_ID, IntygsStatus.WORK_IN_PROGRESS));
        intygRepository.save(buildIntyg(ENHET_2_ID, IntygsStatus.WORK_IN_PROGRESS));
        intygRepository.save(buildIntyg(ENHET_3_ID, IntygsStatus.SIGNED));
        intygRepository.save(buildIntyg(ENHET_1_ID, IntygsStatus.SIGNED));
        intygRepository.save(buildIntyg(ENHET_2_ID, IntygsStatus.WORK_IN_PROGRESS));

        long result = intygRepository.countUnsignedForEnhetsIds(Arrays.asList(ENHET_1_ID, ENHET_2_ID));
        assertThat(result, is(3L));

    }

    @Test
    public void testFindDraftsByPatientPnrAndEnhetsId() {
        
        intygRepository.save(buildIntyg(ENHET_1_ID, IntygsStatus.SIGNED));
        intygRepository.save(buildIntyg(ENHET_1_ID, IntygsStatus.DRAFT_COMPLETE));
        intygRepository.save(buildIntyg(ENHET_1_ID, IntygsStatus.DRAFT_DISCARDED));
        intygRepository.save(buildIntyg(ENHET_1_ID, IntygsStatus.DRAFT_INCOMPLETE));
                
        List<String> enhetIds = Arrays.asList(ENHET_1_ID);
        List<Intyg> results = intygRepository.findDraftsByPatientPnrAndEnhetsId(enhetIds, PERSON_NUMMER);
        
        assertThat(results.size(), is(3));
    }
    
    private Intyg buildIntyg(String enhetsId) {
        return buildIntyg(enhetsId, IntygsStatus.WORK_IN_PROGRESS, INTYGSTYP_FK7263, PERSON_NUMMER, MODEL);
    }

    private Intyg buildIntyg(String enhetsId, IntygsStatus status) {
        return buildIntyg(enhetsId, status, INTYGSTYP_FK7263, PERSON_NUMMER, MODEL);
    }

    private Intyg buildIntyg(String enhetsId, IntygsStatus status, String type, String personNummer, String model) {
        Intyg intyg = new Intyg();
        intyg.setIntygsId(UUID.randomUUID().toString());
        intyg.setIntygsTyp(type);
        intyg.setEnhetsId(enhetsId);
        intyg.setPatientPersonnummer(personNummer);
        VardpersonReferens vardpersonReferens = new VardpersonReferens();
        vardpersonReferens.setHsaId(enhetsId);
        vardpersonReferens.setNamn(enhetsId + "-namn");
        intyg.setSenastSparadAv(vardpersonReferens);
        intyg.setSkapadAv(vardpersonReferens);

        intyg.setStatus(status);
        
        intyg.setModel(model);

        return intyg;
    }

}
