package se.inera.certificate.mc2wc.converter;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import se.inera.certificate.mc2wc.dbunit.AbstractDbUnitSpringTest;
import se.inera.certificate.mc2wc.jpa.model.Certificate;
import se.inera.certificate.mc2wc.message.MigrationMessage;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import static org.junit.Assert.*;

@ContextConfiguration(locations = {"/spring/beans-context.xml"})
@DatabaseSetup({"/data/certificate_dataset_25.xml"})
public class CertificateConverterTest extends AbstractDbUnitSpringTest {

    private static final String CERT_WITH_ALL = "certificate010";
    private static final String CERT_WITH_NOTHING = "certificate001";
    private static final String CERT_WITH_JUST_CONTENT = "certificate009";
    private static final String CERT_WITH_JUST_QUESTION = "certificate020";
    private static final String CERT_WITH_QUESTIONS_ANSWERS = "certificate007";

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private MigrationMessageConverter converter;

    public CertificateConverterTest() {

    }

    @Test
    public void convertCertificate010() {

        Certificate certificate = getCertificateById(CERT_WITH_ALL);
        assertNotNull(certificate);

        MigrationMessage migrationMessage = converter.toMigrationMessage(certificate, true);
        assertNotNull(migrationMessage);
        assertNotNull(migrationMessage.getCertificate());
        assertEquals(1, migrationMessage.getQuestions().size());
    }

    @Test
    public void convertCertWithNothing() {

        Certificate certificate = getCertificateById(CERT_WITH_NOTHING);
        assertNotNull(certificate);

        MigrationMessage migrationMessage = converter.toMigrationMessage(certificate, true);
        assertNotNull(migrationMessage);
        assertNull(migrationMessage.getCertificate());
        assertTrue(migrationMessage.getQuestions().isEmpty());
    }

    @Test
    public void convertCertWithJustContent() {

        Certificate certificate = getCertificateById(CERT_WITH_JUST_CONTENT);
        assertNotNull(certificate);

        MigrationMessage migrationMessage = converter.toMigrationMessage(certificate, true);
        assertNotNull(migrationMessage);
        assertNotNull(migrationMessage.getCertificate());
        assertTrue(migrationMessage.getQuestions().isEmpty());
    }

    @Test
    public void convertCertWithJustQuestion() {

        Certificate certificate = getCertificateById(CERT_WITH_JUST_QUESTION);
        assertNotNull(certificate);

        MigrationMessage migrationMessage = converter.toMigrationMessage(certificate, true);
        assertNotNull(migrationMessage);
        assertNull(migrationMessage.getCertificate());
        assertEquals(1, migrationMessage.getQuestions().size());
    }

    @Test
    public void convertCertWithContentsQuestionsAnswers() {

        Certificate certificate = getCertificateById(CERT_WITH_QUESTIONS_ANSWERS);
        assertNotNull(certificate);

        MigrationMessage migrationMessage = converter.toMigrationMessage(certificate, true);
        assertNotNull(migrationMessage);
        assertNotNull(migrationMessage.getCertificate());
        assertEquals(4, migrationMessage.getQuestions().size());
    }

    private Certificate getCertificateById(String certId) {
        TypedQuery<Certificate> query = em.createQuery("SELECT c FROM Certificate c WHERE id = :certId", Certificate.class);
        query.setParameter("certId", certId);
        return query.getSingleResult();
    }
}
