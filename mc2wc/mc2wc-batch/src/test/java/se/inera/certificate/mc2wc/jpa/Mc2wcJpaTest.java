package se.inera.certificate.mc2wc.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.certificate.mc2wc.dbunit.AbstractDbUnitSpringTest;
import se.inera.certificate.mc2wc.dbunit.CustomFlatXmlDataSetLoader;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;

@DbUnitConfiguration(databaseConnection = "mc2wcDataSource", dataSetLoader = CustomFlatXmlDataSetLoader.class)
@DatabaseSetup({"/data/mc2wc.xml"})
public class Mc2wcJpaTest extends AbstractDbUnitSpringTest {

	@PersistenceContext(unitName="jpa.migration.mc2wc")
    private EntityManager em;
	
	@Autowired
	private Mc2wcDAO dao;

    @Test
    public void testThatWeCanAccessDb() {

        TypedQuery<Long> query = em.createQuery("select count(mc) from MigratedCertificate mc", Long.class);
        Long nbrOfCerts = query.getSingleResult();
        assertNotNull(nbrOfCerts);
        assertEquals(3L, nbrOfCerts.longValue());
    }
	
    @Test
    public void testCountMigratedCertificatesInDao() {

        Long res = dao.countMigratedCertificates();
        
        assertNotNull(res);
        assertEquals(3L, res.longValue());
    }
}
