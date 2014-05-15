package se.inera.certificate.mc2wc.medcert.jpa;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Component;

@Component
public class MedcertDAO {
	
	@PersistenceContext(unitName="jpa.migration.medcert")
    private EntityManager entityManager;
	
	public Long countCertificatesWithContents() {
		Query certificateQuery = entityManager.createQuery("select count(id) from Certificate where document is not null");
	    return (Long) certificateQuery.getSingleResult();
	}
	
	public Long countQuestions() {
		Query questionQuery = entityManager.createQuery("select count(id) from Question where subject is not null and text is not null");
		return (Long) questionQuery.getSingleResult();
	}
    
	public Long countAnswers() {
		Query answerQuery = entityManager.createQuery("select count(id) from Answer where text is not null");
		return (Long) answerQuery.getSingleResult();
	}
}
