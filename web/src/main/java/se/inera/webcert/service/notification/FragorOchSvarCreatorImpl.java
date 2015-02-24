package se.inera.webcert.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.inera.certificate.modules.support.api.notification.FragorOchSvar;
import se.inera.webcert.persistence.fragasvar.model.Status;
import se.inera.webcert.persistence.fragasvar.repository.FragaSvarRepository;

@Component
public class FragorOchSvarCreatorImpl implements FragorOchSvarCreator {
    
    private static final String FRAGESTALLARE_FK = "FK";
    private static final String FRAGESTALLARE_WEBCERT = "WC";
    
    private static final Logger LOG = LoggerFactory.getLogger(FragorOchSvarCreatorImpl.class);
    
    @Autowired
    private FragaSvarRepository fragaSvarRepository;

    /* (non-Javadoc)
     * @see se.inera.webcert.service.notification.FragorOchSvarCreator#createFragorOchSvar(java.lang.String)
     */
    @Override
    public FragorOchSvar createFragorOchSvar(String intygsId) {
        
        int antalFragor = antalFragor(intygsId);
        int antalSvar = antalSvar(intygsId);
        int antalHanteradeFragor = antalHanteradeFragor(intygsId);
        int antalHanteradeSvar = antalHanteradeSvar(intygsId);
        
        FragorOchSvar fs = new FragorOchSvar(antalFragor, antalSvar, antalHanteradeFragor, antalHanteradeSvar);
        
        LOG.debug("Created FragorOchSvar ({}) for intyg {}", fs.toString(), intygsId);
        
        return fs;
    }

    private int antalFragor(String intygsId) {
        Long res = fragaSvarRepository.countByIntygAndFragestallare(intygsId, FRAGESTALLARE_FK);
        return res.intValue();
    }

    private int antalHanteradeFragor(String intygsId) {
        Long res = fragaSvarRepository.countByIntygAndStatusAndFragestallare(intygsId, Status.CLOSED, FRAGESTALLARE_FK);
        return res.intValue();
    }

    private int antalSvar(String intygsId) {
        Long res = fragaSvarRepository.countByIntygAndStatusAndFragestallare(intygsId, Status.ANSWERED, FRAGESTALLARE_WEBCERT);
        return res.intValue();
    }

    private int antalHanteradeSvar(String intygsId) {
        Long res = fragaSvarRepository.countByIntygAndStatusAndFragestallare(intygsId, Status.CLOSED, FRAGESTALLARE_WEBCERT);
        return res.intValue();
    }
}
