package se.inera.intyg.webcert.web.service.notification;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.inera.intyg.common.support.modules.support.api.notification.FragorOchSvar;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvarStatus;
import se.inera.intyg.webcert.persistence.fragasvar.repository.FragaSvarRepository;

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
     * @see se.inera.intyg.webcert.web.service.notification.FragorOchSvarCreator#
     * createFragorOchSvar(java.lang.String)
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

            // WEBCERT-2001: Vi vill endast öka antalet hanteradeSvar på
            // faktiska svar från FK.
            if (isFromWebcert(fsStatus)) {
                if (fsStatus.hasAnswerSet()) {
                    antalSvar++;
                    if (fsStatus.isClosed()) {
                        antalHanteradeSvar++;
                    }
                }
            } else if (isFromFK(fsStatus)) {
                antalFragor++;
                if (fsStatus.isClosed()) {
                    antalHanteradeFragor++;
                }
            }
        }

        return new FragorOchSvar(antalFragor, antalSvar, antalHanteradeFragor,
                antalHanteradeSvar);
    }

    public boolean isFromFK(FragaSvarStatus fsStatus) {
        return fsStatus.getFrageStallare().equalsIgnoreCase(FRAGESTALLARE_FK);
    }

    public boolean isFromWebcert(FragaSvarStatus fsStatus) {
        return fsStatus.getFrageStallare().equalsIgnoreCase(
                FRAGESTALLARE_WEBCERT);
    }
}
