package se.inera.webcert.service.notification;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.inera.certificate.modules.support.api.notification.FragorOchSvar;
import se.inera.webcert.persistence.fragasvar.model.FragaSvarStatus;
import se.inera.webcert.persistence.fragasvar.model.Status;
import se.inera.webcert.persistence.fragasvar.repository.FragaSvarRepository;

@Component
public class FragorOchSvarCreatorImpl implements FragorOchSvarCreator {

    private static final String FRAGESTALLARE_FK = "FK";
    private static final String FRAGESTALLARE_WEBCERT = "WC";

    private static final Logger LOG = LoggerFactory.getLogger(FragorOchSvarCreatorImpl.class);

    @Autowired
    private FragaSvarRepository fragaSvarRepository;

    /*
     * (non-Javadoc)
     * 
     * @see se.inera.webcert.service.notification.FragorOchSvarCreator#createFragorOchSvar(java.lang.String)
     */
    @Override
    public FragorOchSvar createFragorOchSvar(String intygsId) {

        List<FragaSvarStatus> fsStatuses = fragaSvarRepository.findFragaSvarStatusesForIntyg(intygsId);

        FragorOchSvar fs = performCount(fsStatuses);

        LOG.debug("Created FragorOchSvar ({}) for intyg {}", fs.toString(), intygsId);

        return fs;
    }

    public FragorOchSvar performCount(List<FragaSvarStatus> fsStatuses) {

        int antalFragor = 0;
        int antalSvar = 0;
        int antalHanteradeFragor = 0;
        int antalHanteradeSvar = 0;

        for (FragaSvarStatus fsStatus : fsStatuses) {
            antalFragor++;

            if (fsStatus.hasAnswerSet()) {
                antalSvar++;
                antalHanteradeFragor++;
                if (Status.CLOSED.equals(fsStatus.getStatus())) {
                    antalHanteradeSvar++;
                }
            } else {
                if (Status.CLOSED.equals(fsStatus.getStatus()) || Status.ANSWERED.equals(fsStatus.getStatus())) {
                    antalHanteradeFragor++;
                }
            }

        }

        return new FragorOchSvar(antalFragor, antalSvar, antalHanteradeFragor, antalHanteradeSvar);
    }

}
