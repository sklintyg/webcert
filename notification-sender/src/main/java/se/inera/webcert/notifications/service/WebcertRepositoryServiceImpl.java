package se.inera.webcert.notifications.service;

import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.webcert.notifications.routes.RouteHeaders;
import se.inera.webcert.persistence.fragasvar.model.Status;
import se.inera.webcert.persistence.fragasvar.repository.FragaSvarRepository;
import se.inera.webcert.persistence.integreradenhet.repository.IntegreradEnhetRepository;
import se.inera.webcert.persistence.utkast.model.Utkast;
import se.inera.webcert.persistence.utkast.repository.UtkastRepository;

/**
 * Simple facade for the Intyg respository so header values from Camel
 * routes can be used as parameters.
 *
 * @author npet
 *
 */
public class WebcertRepositoryServiceImpl implements WebcertRepositoryService {

    private static final String FK = "FK";
    private static final String WC = "WC";

    private static final Logger LOG = LoggerFactory.getLogger(WebcertRepositoryService.class);

    @Autowired
    private UtkastRepository utkastRepository;

    @Autowired
    private FragaSvarRepository fragaSvarRepository;

    @Autowired
    private IntegreradEnhetRepository integreradEnhetRepository;

    public Long countNbrOfQuestionsForIntyg(@Header(RouteHeaders.INTYGS_ID) String intygsId) {
        Long res = fragaSvarRepository.countByIntygAndFragestallare(intygsId, FK);
        LOG.debug("antalFragor = {}", res);
        return res;
    }

    public Long countNbrOfAnsweredQuestionsForIntyg(@Header(RouteHeaders.INTYGS_ID) String intygsId) {
        Long res = fragaSvarRepository.countByIntygAndStatusAndFragestallare(intygsId, Status.ANSWERED, WC);
        LOG.debug("antalSvar = {}", res);
        return res;
    }

    public Long countNbrOfHandledQuestionsForIntyg(@Header(RouteHeaders.INTYGS_ID) String intygsId) {
        Long res = fragaSvarRepository.countByIntygAndStatusAndFragestallare(intygsId, Status.CLOSED, FK);
        LOG.debug("antalHanteradeFragor = {}", res);
        return res;
    }

    public Long countNbrOfHandledAndAnsweredQuestionsForIntyg(@Header(RouteHeaders.INTYGS_ID) String intygsId) {
        Long res = fragaSvarRepository.countByIntygAndStatusAndFragestallare(intygsId, Status.CLOSED, WC);
        LOG.debug("antalHanteradeSvar = {}", res);
        return res;
    }

    /*
     * (non-Javadoc)
     *
     * @see se.inera.webcert.notifications.service.IntygRepositoryService#getIntygsUtkast(java.lang.String)
     */
    @Override
    public Utkast getUtkast(@Header(RouteHeaders.INTYGS_ID) String intygsId) {

        LOG.debug("Retrieveing Utkast '{}'", intygsId);

        return utkastRepository.findOne(intygsId);
    }

    @Override
    public String getModelFromUtkast(@Header(RouteHeaders.INTYGS_ID) String intygsId) {

        LOG.debug("Retrieveing model from Utkast '{}'", intygsId);

        Utkast intygsUtkast = utkastRepository.findOne(intygsId);

        return (intygsUtkast != null) ? intygsUtkast.getModel() : null;
    }

    public boolean isUtkastPresent(@Header(RouteHeaders.INTYGS_ID) String intygsId) {

        if (utkastRepository.exists(intygsId)) {
            return true;
        }

        LOG.debug("Intyg '{}' is not present in IntygRepository", intygsId);
        return false;
    }

    public boolean isVardenhetIntegrerad(@Header(RouteHeaders.VARDENHET_HSA_ID) String vardenhetHsaId) {

        if (integreradEnhetRepository.exists(vardenhetHsaId)) {
            return true;
        }
        LOG.debug("Vardenhet '{}' is not present in IntegreradEnhetRepository", vardenhetHsaId);
        return false;
    }

}
