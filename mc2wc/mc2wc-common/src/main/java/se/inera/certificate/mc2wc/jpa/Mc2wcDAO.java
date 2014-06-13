package se.inera.certificate.mc2wc.jpa;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

public class Mc2wcDAO {

    private static final String COUNT_MIGRATED_CERTS = "SELECT COUNT(mc.id) FROM MigratedCertificate mc";

    private static final String CLEAR_MIGRATED_CERTS = "DELETE FROM MigratedCertificate mc WHERE mc.certificateId IS NOT NULL";
    
    private static final String SUM_MIGRATED_CERT_QUESTIONS = "SELECT SUM(mc.nbrOfQuestions) FROM MigratedCertificate mc";
    private static final String SUM_MIGRATED_CERT_ANSWERED_QUESTIONS = "SELECT SUM(mc.nbrOfAnsweredQuestions) FROM MigratedCertificate mc";

    @PersistenceContext(unitName = "jpa.migration.mc2wc")
    private EntityManager entityManager;

    public Long countMigratedCertificates() {
        Query certificateQuery = entityManager.createQuery(COUNT_MIGRATED_CERTS);
        return (Long) certificateQuery.getSingleResult();
    }
    
    public Long sumNbrOfMigratedQuestions() {
        Query certificateQuery = entityManager.createQuery(SUM_MIGRATED_CERT_QUESTIONS);
        return (Long) certificateQuery.getSingleResult();
    }
    
    public Long sumNbrOfMigratedAnsweredQuestions() {
        Query certificateQuery = entityManager.createQuery(SUM_MIGRATED_CERT_ANSWERED_QUESTIONS);
        return (Long) certificateQuery.getSingleResult();
    }

    public void clearMigratedCertificates() {

        Query query = entityManager.createQuery(CLEAR_MIGRATED_CERTS);
        query.executeUpdate();
    }

    public void insertMigrationManifest(MigrationManifest manifest) {
        entityManager.persist(manifest);
    }
}
