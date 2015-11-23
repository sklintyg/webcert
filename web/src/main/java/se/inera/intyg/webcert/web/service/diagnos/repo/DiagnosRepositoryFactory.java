package se.inera.intyg.webcert.web.service.diagnos.repo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import se.inera.intyg.webcert.web.service.diagnos.model.Diagnos;

/**
 * Factory responsible for creating the DiagnosRepository out of supplied code files.
 *
 * @author npet
 *
 */
@Component
public class DiagnosRepositoryFactory implements InitializingBean {

    private static final String SPACE = " ";

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosRepositoryFactory.class);

    /**
     * Diagnosis files are usually encoded as ISO-8859-1.
     */
    @Value("${diagnos.code.encoding:ISO-8859-1}")
    private String fileEncoding;

    @Autowired
    private ResourceLoader resourceLoader;

    public DiagnosRepository createAndInitDiagnosRepository(List<String> filesList) {
        try {

            DiagnosRepositoryImpl diagnosRepository = new DiagnosRepositoryImpl();

            LOG.info("Creating DiagnosRepository from {} files using encoding '{}'", filesList.size(), fileEncoding);

            for (String file : filesList) {
                populateRepoFromDiagnosisCodeFile(file, diagnosRepository);
            }

            diagnosRepository.openLuceneIndexReader();

            LOG.info("Created DiagnosRepository containing {} diagnoses", diagnosRepository.nbrOfDiagosis());

            return diagnosRepository;

        } catch (IOException e) {
            LOG.error("Exception occured when initiating DiagnosRepository");
            throw new RuntimeException("Exception occured when initiating repo", e);
        }
    }

    public void populateRepoFromDiagnosisCodeFile(String fileUrl, DiagnosRepositoryImpl diagnosRepository) {

        if (StringUtils.isBlank(fileUrl)) {
            return;
        }

        LOG.debug("Loading diagnosis file '{}'", fileUrl);

        try {
            Resource resource = resourceLoader.getResource(fileUrl);

            if (!resource.exists()) {
                LOG.error("Could not load diagnosis file since the resource '{}' does not exists", fileUrl);
                return;
            }

            IndexWriterConfig idxWriterConfig = new IndexWriterConfig(Version.LUCENE_4_10_2, new StandardAnalyzer());
            IndexWriter idxWriter = new IndexWriter(diagnosRepository.getLuceneIndex(), idxWriterConfig);

            BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), fileEncoding));

            while (reader.ready()) {
                String line = reader.readLine();
                Diagnos diagnos = createDiagnosFromString(line);

                Document doc = new Document();
                doc.add(new StringField(DiagnosRepository.CODE, diagnos.getKod(), Field.Store.YES));
                doc.add(new TextField(DiagnosRepository.DESC, diagnos.getBeskrivning(), Field.Store.YES));
                idxWriter.addDocument(doc);
            }

            reader.close();
            idxWriter.close();

        } catch (IOException ioe) {
            LOG.error("IOException occured when loading diagnosis file '{}'", fileUrl);
            throw new RuntimeException("Error occured when loading diagnosis file", ioe);
        }
    }

    public Diagnos createDiagnosFromString(String diagnosStr) {

        if (StringUtils.isBlank(diagnosStr)) {
            return null;
        }

        // remove excess space in the string
        diagnosStr = StringUtils.normalizeSpace(diagnosStr);

        int firstSpacePos = diagnosStr.indexOf(SPACE);

        if (firstSpacePos == -1) {
            return null;
        }

        String kodStr = diagnosStr.substring(0, firstSpacePos);
        String beskStr = diagnosStr.substring(firstSpacePos + 1);

        Diagnos d = new Diagnos();
        d.setKod(kodStr.toUpperCase());
        d.setBeskrivning(beskStr);

        return d;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.hasText(fileEncoding, "File-encoding for diagnos code files not set!");
    }
}
