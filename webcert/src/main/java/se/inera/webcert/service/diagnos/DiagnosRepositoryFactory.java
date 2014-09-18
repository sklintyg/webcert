package se.inera.webcert.service.diagnos;

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

    public List<String> diagnosKodFiler;

    public DiagnosRepository createDiagnosRepository() {
        try {
            DiagnosRepository repository = new DiagnosRepository();

            for (String kodfile : diagnosKodFiler) {
                readFile(kodfile, repository);
            }

            return repository;
            
        } catch (IOException e) {
            throw new RuntimeException("Exception occured when initiating repo", e);
        }
    }

    public void readFile(String fileUrl, DiagnosRepository diagnosRepository) throws IOException {

        Resource fileRes = new ClassPathResource(fileUrl);

        BufferedReader reader = new BufferedReader(new InputStreamReader(fileRes.getInputStream()));

        while (reader.ready()) {
            String line = reader.readLine();
            Diagnos diagnos = createDiagnosFromString(line);
            diagnosRepository.addDiagnos(diagnos);
        }

    }

    public Diagnos createDiagnosFromString(String diagnosStr) {

        if (StringUtils.isBlank(diagnosStr)) {
            return null;
        }

        diagnosStr = StringUtils.normalizeSpace(diagnosStr);

        int firstSpacePos = diagnosStr.indexOf(SPACE);

        if (firstSpacePos == -1) {
            return null;
        }

        String kodStr = diagnosStr.substring(0, firstSpacePos);
        String beskStr = diagnosStr.substring(firstSpacePos + 1);

        Diagnos d = new Diagnos();
        d.setKod(kodStr);
        d.setBeskrivning(beskStr);

        return d;
    }

    public List<String> getDiagnosKodFiler() {
        return diagnosKodFiler;
    }

    public void setDiagnosKodFiler(List<String> diagnosKodFiler) {
        this.diagnosKodFiler = diagnosKodFiler;
    }

}
