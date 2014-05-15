package se.inera.certificate.mc2wc.jpa;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

public class Mc2wcDAO {

	@PersistenceContext(unitName="jpa.migration.mc2wc")
    private EntityManager entityManager;
	
	public Long countMigratedCertificates() {
		Query certificateQuery = entityManager.createQuery("select count(id) from MigratedCertificate");
	    return (Long) certificateQuery.getSingleResult();
	}

}
