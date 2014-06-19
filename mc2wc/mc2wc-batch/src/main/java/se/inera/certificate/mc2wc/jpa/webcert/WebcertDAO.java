package se.inera.certificate.mc2wc.jpa.webcert;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

public class WebcertDAO {
    
    @PersistenceContext(unitName = "jpa.migration.webcert")
    private EntityManager entityManager;
    
    public long countNbrOfFragaSvar() {
        Query certificateQuery = entityManager.createQuery("select count(internReferens) from FragaSvar");
        return (Long) certificateQuery.getSingleResult();
    }
    
    
}
