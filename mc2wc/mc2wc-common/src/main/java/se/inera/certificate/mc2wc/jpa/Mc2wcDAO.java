package se.inera.certificate.mc2wc.jpa;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mc2wcDAO {

    private static final String COUNT_MIGRATED_CERTS = "SELECT COUNT(mc.id) FROM MigratedCertificate mc";
    
    private static final String COUNT_MIGRATED_CERTS_WITH_CONTENTS = "SELECT COUNT(mc.id) FROM MigratedCertificate mc WHERE mc.hasLegacyCertificate = true";
    
    private static final String COUNT_MIGRATED_CERTS_WITH_CONTENTS_WITHOUT_QUESTIONS = "SELECT COUNT(mc.id) FROM MigratedCertificate mc WHERE mc.hasLegacyCertificate = true"
            + " AND mc.nbrOfQuestions = 0";
    
    private static final String COUNT_MIGRATED_CERTS_WITH_CONTENTS_WITH_QUESTIONS = "SELECT COUNT(mc.id) FROM MigratedCertificate mc WHERE mc.hasLegacyCertificate = true"
            + " AND mc.nbrOfQuestions > 0";
    
    private static final String COUNT_MIGRATED_CERTS_WITHOUT_CONTENTS_WITH_QUESTIONS = "SELECT COUNT(mc.id) FROM MigratedCertificate mc WHERE mc.hasLegacyCertificate = false"
            + " AND mc.nbrOfQuestions > 0";

    private static final String CLEAR_MIGRATED_CERTS = "DELETE FROM MigratedCertificate mc WHERE mc.certificateId IS NOT NULL";
    
    private static final String SUM_MIGRATED_CERT_QUESTIONS = "SELECT SUM(mc.nbrOfQuestions) FROM MigratedCertificate mc";
    
    private static final String SUM_MIGRATED_CERT_ANSWERED_QUESTIONS = "SELECT SUM(mc.nbrOfAnsweredQuestions) FROM MigratedCertificate mc";

    public static Logger log = LoggerFactory.getLogger(Mc2wcDAO.class);
    
    @PersistenceContext(unitName = "jpa.migration.mc2wc")
    private EntityManager entityManager;

    public Long countMigratedCertificates() {
        Query certificateQuery = entityManager.createQuery(COUNT_MIGRATED_CERTS);
        return (Long) certificateQuery.getSingleResult();
    }
    
    public Long countMigratedCertificatesWithContentsWithoutQuestions() {
        Query certificateQuery = entityManager.createQuery(COUNT_MIGRATED_CERTS_WITH_CONTENTS_WITHOUT_QUESTIONS);
        return (Long) certificateQuery.getSingleResult();
    }
    
    public Long countMigratedCertificatesWithContentsWithQuestions() {
        Query certificateQuery = entityManager.createQuery(COUNT_MIGRATED_CERTS_WITH_CONTENTS_WITH_QUESTIONS);
        return (Long) certificateQuery.getSingleResult();
    }
    
    public Long countMigratedCertificatesWithoutContentsWithQuestions() {
        Query certificateQuery = entityManager.createQuery(COUNT_MIGRATED_CERTS_WITHOUT_CONTENTS_WITH_QUESTIONS);
        return (Long) certificateQuery.getSingleResult();
    }
    
    public Long sumNbrOfMigratedQuestions() {
        Query certificateQuery = entityManager.createQuery(SUM_MIGRATED_CERT_QUESTIONS);
        return (Long) certificateQuery.getSingleResult();
    }
    
    public Long countNbrOfMigratedLegacyCertificates() {
        Query certificateQuery = entityManager.createQuery(COUNT_MIGRATED_CERTS_WITH_CONTENTS);
        return (Long) certificateQuery.getSingleResult();
    }
    
    public Long sumNbrOfMigratedAnsweredQuestions() {
        Query certificateQuery = entityManager.createQuery(SUM_MIGRATED_CERT_ANSWERED_QUESTIONS);
        return (Long) certificateQuery.getSingleResult();
    }

    public void clearMigratedCertificates() {
        Query query = entityManager.createQuery(CLEAR_MIGRATED_CERTS);
        int res = query.executeUpdate();
        log.debug("Cleared {} items", res);
    }

    public void insertMigrationManifest(MigrationManifest manifest) {
        entityManager.persist(manifest);
    }
}
