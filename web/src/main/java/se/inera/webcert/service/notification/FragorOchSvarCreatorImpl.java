package se.inera.webcert.service.notification;

import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.certificate.modules.support.api.notification.FragorOchSvar;
import se.inera.webcert.notifications.routes.RouteHeaders;
import se.inera.webcert.persistence.fragasvar.model.Status;
import se.inera.webcert.persistence.fragasvar.repository.FragaSvarRepository;

public class FragorOchSvarCreatorImpl implements FragorOchSvarCreator {
    
    private static final String FK = "FK";
    private static final String WC = "WC";
    
    private static final Logger LOG = LoggerFactory.getLogger(FragorOchSvarCreatorImpl.class);
    
    @Autowired
    private FragaSvarRepository fragaSvarRepository;

    /* (non-Javadoc)
     * @see se.inera.webcert.service.notification.FragorOchSvarCreator#createFragorOchSvar(java.lang.String)
     */
    @Override
    public FragorOchSvar createFragorOchSvar(String intygsId) {
        
        int antalFragor = countNbrOfQuestionsForIntyg(intygsId);
        int antalSvar = countNbrOfAnsweredQuestionsForIntyg(intygsId);
        int antalHanteradeFragor = countNbrOfHandledQuestionsForIntyg(intygsId);
        int antalHanteradeSvar = countNbrOfHandledAndAnsweredQuestionsForIntyg(intygsId);
        
        FragorOchSvar fs = new FragorOchSvar(antalFragor, antalSvar, antalHanteradeFragor, antalHanteradeSvar);
        
        LOG.debug("Created FragorOchSvar ({}) for intyg {}", fs.toString(), intygsId);
        
        return fs;
    }

    public int countNbrOfQuestionsForIntyg(String intygsId) {
        Long res = fragaSvarRepository.countByIntygAndFragestallare(intygsId, FK);
        return res.intValue();
    }

    public int countNbrOfAnsweredQuestionsForIntyg(String intygsId) {
        Long res = fragaSvarRepository.countByIntygAndStatusAndFragestallare(intygsId, Status.ANSWERED, WC);
        return res.intValue();
    }

    public int countNbrOfHandledQuestionsForIntyg(String intygsId) {
        Long res = fragaSvarRepository.countByIntygAndStatusAndFragestallare(intygsId, Status.CLOSED, FK);
        return res.intValue();
    }

    public int countNbrOfHandledAndAnsweredQuestionsForIntyg(@Header(RouteHeaders.INTYGS_ID) String intygsId) {
        Long res = fragaSvarRepository.countByIntygAndStatusAndFragestallare(intygsId, Status.CLOSED, WC);
        return res.intValue();
    }
}
