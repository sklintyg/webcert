package se.inera.webcert.service.diagnos;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import se.inera.webcert.service.diagnos.dto.DiagnosResponse;
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
    
    /**
     * A regular expression for validating a 'swedish' ICD-10 code. 
     * 
     * The code should start with an upper-case letter 
     * and should be followed by two digits, 
     * then an optional '.', 
     * followed by an optional digit,
     * finishing with an optional upper-case letter.
     * 
     * Tested with: A11, A11.1, A11.1X, A111, A111X
     */
    private static final String ICD10_CODE_REGEXP = "^[A-Z]\\d{2}\\.{0,1}\\d{0,1}[A-Z]{0,1}$";

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
    public DiagnosResponse getDiagnosisByCode(String code) {
        
        if(!validateDiagnosisCode(code)) {
            return DiagnosResponse.invalidCode();
        }
        
        Diagnos diagnos = diagnosRepo.getDiagnosByCode(code);
        
        if (diagnos != null) {
            return DiagnosResponse.ok(diagnos);
        }
        
        return DiagnosResponse.notFound();
    }

    /*
     * (non-Javadoc)
     * 
     * @see se.inera.webcert.service.diagnos.DiagnosService#searchDiagnosisByCode(java.lang.String, int)
     */
    public DiagnosResponse searchDiagnosisByCode(String codeFragment, int nbrOfResults) {
        
        Assert.isTrue((nbrOfResults > 0), "nbrOfResults must be larger that 0");
        
        if(!validateDiagnosisCode(codeFragment)) {
            return DiagnosResponse.invalidCode();
        }
        
        List<Diagnos> matches = diagnosRepo.searchDiagnosisByCode(codeFragment);

        if (matches.isEmpty()) {
            return DiagnosResponse.notFound(); 
        }
            
        if (matches.size() <= nbrOfResults) {
            return DiagnosResponse.ok(matches);
        }

        LOG.debug("Returning {} diagnosises out of a match of {}", nbrOfResults, matches.size());

        return DiagnosResponse.ok(matches.subList(0, nbrOfResults));
    }

    /*
     * (non-Javadoc)
     * 
     * @see se.inera.webcert.service.diagnos.DiagnosService#searchDiagnosisByCode(java.lang.String)
     */
    @Override
    public DiagnosResponse searchDiagnosisByCode(String codeFragment) {
        
        if(!validateDiagnosisCode(codeFragment)) {
            return DiagnosResponse.invalidCode();
        }
        
        List<Diagnos> matches = diagnosRepo.searchDiagnosisByCode(codeFragment);
        
        if (matches.isEmpty()) {
            return DiagnosResponse.notFound(); 
        }
        
        return DiagnosResponse.ok(matches);
    }

    
    /**
     * Perform a regex validation on the diagnosis code, the code
     * is trimmed before checked.
     * 
     * @param diagnosisCode
     * @return true if the diagnosisCode matches the regexp
     */
    public boolean validateDiagnosisCode(String diagnosisCode) {
        
        if (StringUtils.isNotBlank(diagnosisCode)) {           
            Pattern p = Pattern.compile(ICD10_CODE_REGEXP);
            Matcher m = p.matcher(diagnosisCode.trim());
            return m.matches();
        }
        
        return false;
    }
}
