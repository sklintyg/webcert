package se.inera.certificate.mc2wc.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;

import se.inera.certificate.mc2wc.dbunit.AbstractDbUnitSpringTest;
import se.inera.certificate.mc2wc.dbunit.CustomFlatXmlDataSetLoader;
import se.inera.certificate.mc2wc.medcert.jpa.model.Certificate;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;

@DbUnitConfiguration(databaseConnection = "medcertDataSource", dataSetLoader = CustomFlatXmlDataSetLoader.class)
@DatabaseSetup({"/data/question.xml"})
@ActiveProfiles({"export","export-unittest"})
public class MedcertJpaTest extends AbstractDbUnitSpringTest {

    @PersistenceContext(unitName="jpa.migration.medcert")
    private EntityManager em;

    @Test
    public void testSomething() {

        TypedQuery<Certificate> query = em.createQuery("select c from Certificate c", Certificate.class);
        Certificate cert = query.getSingleResult();
        assertNotNull(cert);
        assertEquals(1, cert.getQuestions().size());

    }

}
