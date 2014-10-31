package se.inera.webcert.service.diagnos.repo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import se.inera.webcert.service.diagnos.model.Diagnos;

/**
 * Factory responsible for creating the DiagnosRepository out of supplied code files.
 * 
 * @author npet
 *
 */
public class DiagnosRepositoryFactory {
    
    private static final String SPACE = " ";

    private static final String UTF_8 = "UTF-8";
    
    private static Logger LOG = LoggerFactory.getLogger(DiagnosRepositoryFactory.class);
    
    private List<String> diagnosCodeFiles;
    
    public DiagnosRepositoryFactory(List<String> diagnosCodeFiles) {
        this.diagnosCodeFiles = diagnosCodeFiles;
    }
    
    public DiagnosRepository createAndInitDiagnosRepository() {
        try {
            
            DiagnosRepositoryImpl diagnosRepoImpl = new DiagnosRepositoryImpl();

            for (String kodfile : diagnosCodeFiles) {
                populateRepoFromDiagnosisCodeFile(kodfile, diagnosRepoImpl);
            }
            
            LOG.info("Created DiagnosRepository containing {} diagnoses", diagnosRepoImpl.nbrOfDiagosis());
            
            return diagnosRepoImpl;
            
        } catch (IOException e) {
            LOG.error("Exception occured when initiating DiagnosRepository");
            throw new RuntimeException("Exception occured when initiating repo", e);
        }
    }
    
    public void populateRepoFromDiagnosisCodeFile(String fileUrl, DiagnosRepositoryImpl diagnosRepository) throws IOException {

        if (StringUtils.isBlank(fileUrl)) {
            return;
        }
        
        LOG.debug("Loading diagnosis file {}", fileUrl);
        
        Resource fileRes = new ClassPathResource(fileUrl);

        BufferedReader reader = new BufferedReader(new InputStreamReader(fileRes.getInputStream(), UTF_8));

        while (reader.ready()) {
            String line = reader.readLine();
            Diagnos diagnos = createDiagnosFromString(line);
            diagnosRepository.addDiagnos(diagnos);
        }
        
        reader.close();
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
}
