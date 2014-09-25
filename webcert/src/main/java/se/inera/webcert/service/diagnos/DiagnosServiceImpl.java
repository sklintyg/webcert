package se.inera.webcert.service.diagnos;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import se.inera.webcert.service.diagnos.model.Diagnos;
import se.inera.webcert.service.diagnos.repo.DiagnosRepository;
import se.inera.webcert.service.diagnos.repo.DiagnosRepositoryFactory;

/**
 * Implementation of DiagnosService. Supplies services for getting a diagnosis by code
 * or searching for diagnosies.
 * 
 * @author npet
 *
 */
@Service
public class DiagnosServiceImpl implements DiagnosService {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosServiceImpl.class);

    @Value("${diagnos.code.files}")
    private String[] diagnosKodFiler;

    private DiagnosRepository diagnosRepo;

    public DiagnosServiceImpl() {

    }

    public DiagnosServiceImpl(String[] diagnosKodFiler) {
        this.diagnosKodFiler = diagnosKodFiler;
    }

    @PostConstruct
    public void initDiagnosRepository() {
        List<String> fileList = Arrays.asList(diagnosKodFiler);
        DiagnosRepositoryFactory repoFactory = new DiagnosRepositoryFactory(fileList);
        this.diagnosRepo = repoFactory.createAndInitDiagnosRepository();
    }

    /*
     * (non-Javadoc)
     * 
     * @see se.inera.webcert.service.diagnos.DiagnosService#getDiagnosisByCode(java.lang.String)
     */
    @Override
    public Diagnos getDiagnosisByCode(String code) {
        return diagnosRepo.getDiagnosByCode(code);
    }

    /*
     * (non-Javadoc)
     * 
     * @see se.inera.webcert.service.diagnos.DiagnosService#searchDiagnosisByCode(java.lang.String, int)
     */
    public List<Diagnos> searchDiagnosisByCode(String codeFragment, int nbrOfResults) {
        List<Diagnos> matches = diagnosRepo.searchDiagnosisByCode(codeFragment);

        if (matches.isEmpty() || nbrOfResults == -1 || matches.size() <= nbrOfResults) {
            return matches;
        }

        LOG.debug("Returning {} diagnosises out of a match of {}", nbrOfResults, matches.size());

        return matches.subList(0, nbrOfResults);
    }

    /*
     * (non-Javadoc)
     * 
     * @see se.inera.webcert.service.diagnos.DiagnosService#searchDiagnosisByCode(java.lang.String)
     */
    @Override
    public List<Diagnos> searchDiagnosisByCode(String codeFragment) {
        return diagnosRepo.searchDiagnosisByCode(codeFragment);
    }

    /* (non-Javadoc)
     * @see se.inera.webcert.service.diagnos.DiagnosService#validateDiagnosisCode(java.lang.String, int)
     */
    public boolean validateDiagnosisCode(String diagnosisCode, int minimumLength) {

        if (StringUtils.isBlank(diagnosisCode)) {
            return false;
        }

        if (diagnosisCode.length() >= minimumLength) {
            List<Diagnos> results = searchDiagnosisByCode(diagnosisCode);
            return (!results.isEmpty());
        }

        return false;
    }

}
