package se.inera.certificate.mc2wc.converter;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import se.inera.certificate.mc2wc.dbunit.AbstractDbUnitSpringTest;
import se.inera.certificate.mc2wc.dbunit.CustomFlatXmlDataSetLoader;
import se.inera.certificate.mc2wc.medcert.jpa.model.Certificate;
import se.inera.certificate.mc2wc.message.MigrationMessage;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import static org.junit.Assert.*;

@DbUnitConfiguration(databaseConnection = "medcertDataSource", dataSetLoader = CustomFlatXmlDataSetLoader.class)
@DatabaseSetup({"/data/certificate_dataset_25.xml"})
@ActiveProfiles({"export","export-unittest"})
public class CertificateConverterTest extends AbstractDbUnitSpringTest {

    private static final String CERT_WITH_ALL = "certificate010";
    private static final String CERT_WITH_NOTHING = "certificate001";
    private static final String CERT_WITH_JUST_CONTENT = "certificate009";
    private static final String CERT_WITH_JUST_QUESTION = "certificate020";
    private static final String CERT_WITH_QUESTIONS_ANSWERS = "certificate007";

    @PersistenceContext(unitName="jpa.migration.medcert")
    private EntityManager em;

    @Autowired
    private MigrationMessageConverter converter;

    @Test
    public void convertCertificate010() {

        Certificate certificate = getCertificateById(CERT_WITH_ALL);
        assertNotNull(certificate);

        MigrationMessage migrationMessage = converter.toMigrationMessage(certificate, "aa");
        assertNotNull(migrationMessage);
        assertNotNull(migrationMessage.getCertificate());
        assertEquals(1, migrationMessage.getQuestions().size());
    }

    @Test
    public void convertCertWithNothing() {

        Certificate certificate = getCertificateById(CERT_WITH_NOTHING);
        assertNotNull(certificate);

        MigrationMessage migrationMessage = converter.toMigrationMessage(certificate, "aa");
        assertNotNull(migrationMessage);
        assertNull(migrationMessage.getCertificate());
        assertTrue(migrationMessage.getQuestions().isEmpty());
    }

    @Test
    public void convertCertWithJustContent() {

        Certificate certificate = getCertificateById(CERT_WITH_JUST_CONTENT);
        assertNotNull(certificate);

        MigrationMessage migrationMessage = converter.toMigrationMessage(certificate, "aa");
        assertNotNull(migrationMessage);
        assertNotNull(migrationMessage.getCertificate());
        assertTrue(migrationMessage.getQuestions().isEmpty());
    }

    @Test
    public void convertCertWithJustQuestion() {

        Certificate certificate = getCertificateById(CERT_WITH_JUST_QUESTION);
        assertNotNull(certificate);

        MigrationMessage migrationMessage = converter.toMigrationMessage(certificate, "aa");
        assertNotNull(migrationMessage);
        assertNull(migrationMessage.getCertificate());
        assertEquals(1, migrationMessage.getQuestions().size());
    }

    @Test
    public void convertCertWithContentsQuestionsAnswers() {

        Certificate certificate = getCertificateById(CERT_WITH_QUESTIONS_ANSWERS);
        assertNotNull(certificate);

        MigrationMessage migrationMessage = converter.toMigrationMessage(certificate, "aa");
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
