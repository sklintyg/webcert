package se.inera.webcert.persistence.intyg.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.model.Signatur;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:repository-context.xml" })
@ActiveProfiles({ "dev", "unit-testing" })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class })
@DatabaseSetup("/data/intyg-data.xml")
public class SignaturAndIntygRepositoryTest {
    
    @Autowired
    private IntygRepository intygRepository;
    
    @Test
    public void testFind() {
        
        Intyg intyg = intygRepository.findOne("intyg-1");
        assertThat(intyg, notNullValue());
        
        assertThat(intyg.getSignaturer(), hasSize(1));
    }
    
    @Test
    public void testGetLatestSignatur() {
        
        Intyg intyg = intygRepository.findOne("intyg-2");
        assertThat(intyg, notNullValue());
        assertThat(intyg.getSignaturer(), hasSize(2));
        
        Signatur latestSignatur = intyg.getLatestSignatur();
        assertThat(latestSignatur, notNullValue());
        assertThat(latestSignatur.getIntygId(), is("intyg-2"));
        assertThat(latestSignatur.getSignaturId(), is(3L));
    }
}
