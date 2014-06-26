package se.inera.certificate.mc2wc.jpa.webcert;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebcertDAO {
    
    private static final String DELETE_FRAGASVAR = "delete from FragaSvar fs where fs.intygsReferens.intygsId = :certId";
    
    private static final String DELETE_MIGRERAT_MEDCERTINTYG = "delete from MigreratMedcertIntyg m where m.intygsId = :certId";
    
    private static final String COUNT_FRAGASVAR = "select count(internReferens) from FragaSvar";
    
    private static final String COUNT_MIGRERAT_MEDCERTINTYG = "select count(intygsId) from MigreratMedcertIntyg";
    
    public static Logger log = LoggerFactory.getLogger(WebcertDAO.class);
    
    @PersistenceContext(unitName = "jpa.migration.webcert")
    private EntityManager entityManager;
    
    public long countNbrOfFragaSvar() {
        Query certificateQuery = entityManager.createQuery(COUNT_FRAGASVAR);
        return (Long) certificateQuery.getSingleResult();
    }
    
    public long countNbrOfMigreratMedcertIntyg() {
        Query certificateQuery = entityManager.createQuery(COUNT_MIGRERAT_MEDCERTINTYG);
        return (Long) certificateQuery.getSingleResult();
    }
    
    public void removeFragaSvar(String certificateId) {
        Query removeQuery = entityManager.createQuery(DELETE_FRAGASVAR);
        removeQuery.setParameter("certId", certificateId);
        int res = removeQuery.executeUpdate();
        log.debug("removeFragaSvar deleted {} entities", res);
    }
    
    public void removeMigreratMedcertIntyg(String certificateId) {
        Query removeQuery = entityManager.createQuery(DELETE_MIGRERAT_MEDCERTINTYG);
        removeQuery.setParameter("certId", certificateId);
        int res = removeQuery.executeUpdate();
        log.debug("removeMigreratMedcertIntyg deleted {} entities", res);
    }
}
