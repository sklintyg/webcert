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
public class DiagnosServiceImpl implements DiagnosService {

    @Value("${diagnos.code.files}")
    private String[] diagnosKodFiler;
    
    private DiagnosRepository diagnosRepo;

    @PostConstruct
    public void initDiagnosRepository() {
        List<String> fileList = Arrays.asList(diagnosKodFiler);
        DiagnosRepositoryFactory repoFactory = new DiagnosRepositoryFactory(fileList);
        this.diagnosRepo = repoFactory.createAndInitDiagnosRepository();
    }

    /* (non-Javadoc)
     * @see se.inera.webcert.service.diagnos.DiagnosService#getDiagnosisByCode(java.lang.String)
     */
    @Override
    public Diagnos getDiagnosisByCode(String code) {
        return diagnosRepo.getDiagnosByCode(code);
    }
    
    /* (non-Javadoc)
     * @see se.inera.webcert.service.diagnos.DiagnosService#searchDiagnosisByCode(java.lang.String)
     */
    @Override
    public List<Diagnos> searchDiagnosisByCode(String codeFragment) {
        return diagnosRepo.searchDiagnosisByCode(codeFragment);
    }
    
}
