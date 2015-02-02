package se.inera.webcert.service.diagnos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    private static final String KSH97P = "KSH_97_P";

    private static final String ICD_10_SE = "ICD_10_SE";

    /**
     * A regular expression for validating a 'swedish' ICD-10 code.
     * 
     * The code should start with an upper-case letter
     * and should be followed by two digits,
     * then an optional '.',
     * followed by an optional digit,
     * finishing with an optional upper-case letter.
     * 
     * Tested with: A11, A11.1, A11.1X, A111, A111X, A1111
     */
    private static final String ICD10_CODE_REGEXP = "^[A-Z]\\d{2}\\.{0,1}\\d{0,1}[0-9A-Z]{0,1}$";

    /**
     * A regular expression for validating a specialization of ICD-10 code called KSH97P.
     * 
     * Tested with: A11, A11-P, A11-, A111, A111P
     */
    private static final String KSH97P_CODE_REGEXP = "^[A-Z]\\d{2}\\-{0,1}\\d{0,1}[P]{0,1}$";

    private static final String COMMA = ",";

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosServiceImpl.class);

    @Value("${icd10se.diagnos.code.files}")
    private String icd10seCodeFilesStr;

    @Value("${ksh97p.diagnos.code.files}")
    private String ksh97pCodeFilesStr;

    @Autowired
    private DiagnosRepositoryFactory diagnosRepositoryFactory;

    private DiagnosRepository icd10seDiagnosRepo;

    private DiagnosRepository ksh97pDiagnosRepo;

    public DiagnosServiceImpl() {

    }

    public DiagnosServiceImpl(String icd10seDiagnosKodFiler, String ksh97pDiagnosKodFiler) {
        this.icd10seCodeFilesStr = icd10seDiagnosKodFiler;
        this.ksh97pCodeFilesStr = ksh97pDiagnosKodFiler;
    }

    @PostConstruct
    public void initDiagnosRepository() {
        Assert.hasText(this.icd10seCodeFilesStr, "Can not populate DiagnosRepository since no diagnosis code files is supplied");
        Assert.hasText(this.ksh97pCodeFilesStr, "Can not populate DiagnosRepository since no diagnosis code files is supplied");

        String[] splittedCodeFilesStrIcd10se = StringUtils.split(icd10seCodeFilesStr, COMMA);
        List<String> fileListIcd10se = Arrays.asList(splittedCodeFilesStrIcd10se);
        this.icd10seDiagnosRepo = diagnosRepositoryFactory.createAndInitDiagnosRepository(fileListIcd10se);

        String[] splittedCodeFilesStrKsh97p = StringUtils.split(ksh97pCodeFilesStr, COMMA);
        List<String> fileListKsh97p = Arrays.asList(splittedCodeFilesStrKsh97p);
        this.ksh97pDiagnosRepo = diagnosRepositoryFactory.createAndInitDiagnosRepository(fileListKsh97p);
    }

    /*
     * (non-Javadoc)
     * 
     * @see se.inera.webcert.service.diagnos.DiagnosService#getDiagnosisByCode(java.lang.String)
     */
    @Override
    public DiagnosResponse getDiagnosisByCode(String code, String codeSystem) {

        if (!validateDiagnosisCode(code, codeSystem)) {
            return DiagnosResponse.invalidCode();
        }

        Diagnos diagnos = null;

        switch (codeSystem) {
        case ICD_10_SE:
            diagnos = icd10seDiagnosRepo.getDiagnosByCode(code);
            break;
        case KSH97P:
            diagnos = ksh97pDiagnosRepo.getDiagnosByCode(code);
            break;
        }

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
    @Override
    public DiagnosResponse searchDiagnosisByCode(String codeFragment, String codeSystem, int nbrOfResults) {

        Assert.isTrue((nbrOfResults > 0), "nbrOfResults must be larger that 0");

        if (!validateDiagnosisCode(codeFragment, codeSystem)) {
            return DiagnosResponse.invalidCode();
        }

        List<Diagnos> matches = new ArrayList<Diagnos>();

        switch (codeSystem) {
            case ICD_10_SE:
                matches = icd10seDiagnosRepo.searchDiagnosisByCode(codeFragment);
                break;
            case KSH97P:
                matches = ksh97pDiagnosRepo.searchDiagnosisByCode(codeFragment);
                break;
        }

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
     * @see se.inera.webcert.service.diagnos.DiagnosService#searchDiagnosisByCode(java.lang.String, int)
     */
    @Override
    public DiagnosResponse searchDiagnosisByDescription(String searchString, String codeSystem, int nbrOfResults) {

        Assert.isTrue((nbrOfResults > 0), "nbrOfResults must be larger that 0");

        if (searchString == null) {
            return DiagnosResponse.invalidSearchString();
        }
        searchString = searchString.trim();
        if (searchString.length() == 0) {
            return DiagnosResponse.invalidSearchString();
        }

        List<Diagnos> matches = new ArrayList<Diagnos>();

        switch (codeSystem) {
        case ICD_10_SE:
            matches = icd10seDiagnosRepo.searchDiagnosisByDescription(searchString, nbrOfResults);
            break;
        case KSH97P:
            matches = ksh97pDiagnosRepo.searchDiagnosisByDescription(searchString, nbrOfResults);
            break;
        }

        if (matches.isEmpty()) {
            return DiagnosResponse.notFound();
        }

        return DiagnosResponse.ok(matches);
    }

    /*
     * (non-Javadoc)
     * 
     * @see se.inera.webcert.service.diagnos.DiagnosService#searchDiagnosisByCode(java.lang.String)
     */
    @Override
    public DiagnosResponse searchDiagnosisByCode(String codeFragment, String codeSystem) {

        if (!validateDiagnosisCode(codeFragment, codeSystem)) {
            return DiagnosResponse.invalidCode();
        }

        List<Diagnos> matches = new ArrayList<Diagnos>();

        switch (codeSystem) {
        case ICD_10_SE:
            matches = icd10seDiagnosRepo.searchDiagnosisByCode(codeFragment);
            break;
        case KSH97P:
            matches = ksh97pDiagnosRepo.searchDiagnosisByCode(codeFragment);
            break;
    }
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
    @Override
    public boolean validateDiagnosisCode(String diagnosisCode, String codeSystem) {
        if (StringUtils.isNotBlank(diagnosisCode)) {
            if (codeSystem.equals(ICD_10_SE)) {
                Pattern p = Pattern.compile(ICD10_CODE_REGEXP);
                Matcher m = p.matcher(diagnosisCode.trim());
                return m.matches();
            }
            if (codeSystem.equals(KSH97P)) {
                Pattern p = Pattern.compile(KSH97P_CODE_REGEXP);
                Matcher m = p.matcher(diagnosisCode.trim());
                return m.matches();
            }
        }
        return false;
    }
}
