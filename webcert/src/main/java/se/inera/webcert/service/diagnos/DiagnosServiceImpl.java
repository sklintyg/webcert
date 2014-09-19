package se.inera.webcert.service.diagnos;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import se.inera.webcert.service.diagnos.model.Diagnos;
import se.inera.webcert.service.diagnos.repo.DiagnosRepository;
import se.inera.webcert.service.diagnos.repo.DiagnosRepositoryFactory;

@Service
public class DiagnosServiceImpl {

    @Value("${diagnso}")
    private String[] diagnosKodFiler;
    
    private DiagnosRepository diagnosRepo;

    @PostConstruct
    public void initDiagnosRepository() {
        List<String> fileList = Arrays.asList(diagnosKodFiler);
        DiagnosRepositoryFactory repoFactory = new DiagnosRepositoryFactory(fileList);
        this.diagnosRepo = repoFactory.createAndInitDiagnosRepository();
    }

    public Diagnos getDiagnosByCode(String code) {
        return diagnosRepo.getDiagnosByCode(code);
    }
    
    public List<Diagnos> searchDiagnosisByCode(String codeFragment) {
        return diagnosRepo.searchDiagnosisByCode(codeFragment);
    }
    
}
