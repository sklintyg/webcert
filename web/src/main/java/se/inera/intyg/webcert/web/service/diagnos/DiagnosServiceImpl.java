package se.inera.intyg.webcert.web.service.diagnos;

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

import se.inera.certificate.common.enumerations.Diagnoskodverk;
import se.inera.intyg.webcert.web.service.diagnos.dto.DiagnosResponse;
import se.inera.intyg.webcert.web.service.diagnos.model.Diagnos;
import se.inera.intyg.webcert.web.service.diagnos.repo.DiagnosRepository;
import se.inera.intyg.webcert.web.service.diagnos.repo.DiagnosRepositoryFactory;

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
        this.icd10seDiagnosRepo = createDiagnosRepo(icd10seCodeFilesStr, Diagnoskodverk.ICD_10_SE.getCodeSystemName());
        this.ksh97pDiagnosRepo = createDiagnosRepo(ksh97pCodeFilesStr, Diagnoskodverk.KSH_97_P.getCodeSystemName());
    }

    private DiagnosRepository createDiagnosRepo(String codeFilesStr, String repoName) {
        Assert.hasText(codeFilesStr, "Can not populate " + repoName + " DiagnosRepository since no diagnosis code files was supplied");
        String[] splittedcodeFiles = StringUtils.split(codeFilesStr, COMMA);
        return diagnosRepositoryFactory.createAndInitDiagnosRepository(Arrays.asList(splittedcodeFiles));
    }

    @Override
    public DiagnosResponse getDiagnosisByCode(String code, String codeSystemStr) {

        if (!validateDiagnosisCode(code, codeSystemStr)) {
            return DiagnosResponse.invalidCode();
        }

        Diagnoskodverk codeSystem = getDiagnoskodverk(codeSystemStr);

        return findDiagnosisesByCode(code, codeSystem);
    }

    @Override
    public DiagnosResponse getDiagnosisByCode(String code, Diagnoskodverk codeSystem) {

        if (!validateDiagnosisCode(code, codeSystem)) {
            return DiagnosResponse.invalidCode();
        }

        return findDiagnosisesByCode(code, codeSystem);
    }

    private DiagnosResponse findDiagnosisesByCode(String code, Diagnoskodverk codeSystem) {

        List<Diagnos> matches;

        switch (codeSystem) {
        case ICD_10_SE:
            matches = icd10seDiagnosRepo.getDiagnosesByCode(code);
            break;
        case KSH_97_P:
            matches = ksh97pDiagnosRepo.getDiagnosesByCode(code);
            break;
        default:
            LOG.warn("Unknown code system '{}'", codeSystem);
            return DiagnosResponse.invalidCodesystem();
        }

        if (matches.isEmpty()) {
            return DiagnosResponse.notFound();
        }

        return DiagnosResponse.ok(matches, false);
    }

    /*
     * (non-Javadoc)
     *
     * @see se.inera.intyg.webcert.web.service.diagnos.DiagnosService#searchDiagnosisByCode(java.lang.String, int)
     */
    @Override
    public DiagnosResponse searchDiagnosisByCode(String codeFragment, String codeSystemStr, int nbrOfResults) {

        Assert.isTrue((nbrOfResults > 0), "nbrOfResults must be larger that 0");

        Diagnoskodverk codeSystem = getDiagnoskodverk(codeSystemStr);

        if (!validateDiagnosisCode(codeFragment, codeSystem)) {
            return DiagnosResponse.invalidCode();
        }

        List<Diagnos> matches;

        switch (codeSystem) {
        case ICD_10_SE:
            matches = icd10seDiagnosRepo.searchDiagnosisByCode(codeFragment, nbrOfResults + 1);
            break;
        case KSH_97_P:
            matches = ksh97pDiagnosRepo.searchDiagnosisByCode(codeFragment, nbrOfResults + 1);
            break;
        default:
            LOG.warn("Unknown code system '{}'", codeSystem);
            return DiagnosResponse.invalidCodesystem();
        }

        if (matches.isEmpty()) {
            return DiagnosResponse.notFound();
        }

        if (matches.size() <= nbrOfResults) {
            return DiagnosResponse.ok(matches, false);
        }

        LOG.debug("Returning {} diagnosises out of a match of {}", nbrOfResults, matches.size());

        return DiagnosResponse.ok(matches.subList(0, nbrOfResults), true);
    }

    /*
     * (non-Javadoc)
     *
     * @see se.inera.intyg.webcert.web.service.diagnos.DiagnosService#searchDiagnosisByCode(java.lang.String, int)
     */
    @Override
    public DiagnosResponse searchDiagnosisByDescription(String searchString, String codeSystemStr, int nbrOfResults) {

        Assert.isTrue((nbrOfResults > 0), "nbrOfResults must be larger that 0");

        if (searchString == null) {
            return DiagnosResponse.invalidSearchString();
        }
        searchString = searchString.trim();
        if (searchString.length() == 0) {
            return DiagnosResponse.invalidSearchString();
        }

        Diagnoskodverk codeSystem = getDiagnoskodverk(codeSystemStr);
        List<Diagnos> matches;

        if (codeSystem == null) {
            LOG.warn("Code system is null");
            return DiagnosResponse.invalidCodesystem();
        }

        switch (codeSystem) {
        case ICD_10_SE:
            matches = icd10seDiagnosRepo.searchDiagnosisByDescription(searchString, nbrOfResults + 1);
            break;
        case KSH_97_P:
            matches = ksh97pDiagnosRepo.searchDiagnosisByDescription(searchString, nbrOfResults + 1);
            break;
        default:
            LOG.warn("Unknown code system '{}'", codeSystem);
            return DiagnosResponse.invalidCodesystem();
        }

        if (matches.isEmpty()) {
            return DiagnosResponse.notFound();
        }

        if (matches.size() <= nbrOfResults) {
            return DiagnosResponse.ok(matches, false);
        }

        LOG.debug("Returning {} diagnosises out of a match of {}", nbrOfResults, matches.size());

        return DiagnosResponse.ok(matches.subList(0, nbrOfResults), true);
    }

    /**
     * Perform a regex validation on the diagnosis code, the code
     * is trimmed before checked.
     *
     * @return true if the diagnosisCode matches the regexp
     */
    @Override
    public boolean validateDiagnosisCode(String diagnosisCode, String codeSystemStr) {

        if (StringUtils.isBlank(diagnosisCode)) {
            LOG.debug("Could not validate code since it is null or empty");
            return false;
        }

        Diagnoskodverk codeSystem = getDiagnoskodverk(codeSystemStr);

        return validateDiagnosisCode(diagnosisCode, codeSystem);
    }

    private boolean validateDiagnosisCode(String diagnosisCode, Diagnoskodverk codeSystem) {

        if (codeSystem == null) {
            LOG.warn("Tried to validate diagnosis code, but supplied Diagnoskodverk was null");
            return false;
        }

        String regExp;

        switch (codeSystem) {
        case ICD_10_SE:
            regExp = ICD10_CODE_REGEXP;
            break;
        case KSH_97_P:
            regExp = KSH97P_CODE_REGEXP;
            break;
        default:
            LOG.warn("Tried to validate diagnosis code using unknown code system '{}'", codeSystem);
            return false;
        }

        Pattern p = Pattern.compile(regExp);
        Matcher m = p.matcher(diagnosisCode.trim());
        return m.matches();
    }

    private Diagnoskodverk getDiagnoskodverk(String codeSystemStr) {

        if (StringUtils.isBlank(codeSystemStr)) {
            LOG.debug("Can not validate diagnosis code without code system");
            return null;
        }

        Diagnoskodverk codeSystem = Diagnoskodverk.valueOf(codeSystemStr);

        if (codeSystem == null) {
            LOG.warn("Can not validate diagnosis code, unknown code system '{}'", codeSystemStr);
            return null;
        }

        return codeSystem;
    }
}
