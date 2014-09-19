package se.inera.webcert.service.diagnos.repo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import se.inera.webcert.service.diagnos.model.Diagnos;

public class DiagnosRepositoryFactory {
    
    private static final String SPACE = " ";
    
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
            
            return diagnosRepoImpl;
            
        } catch (IOException e) {
            throw new RuntimeException("Exception occured when initiating repo", e);
        }
    }
    
    public void populateRepoFromDiagnosisCodeFile(String fileUrl, DiagnosRepositoryImpl diagnosRepository) throws IOException {

        Resource fileRes = new ClassPathResource(fileUrl);

        BufferedReader reader = new BufferedReader(new InputStreamReader(fileRes.getInputStream()));

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
