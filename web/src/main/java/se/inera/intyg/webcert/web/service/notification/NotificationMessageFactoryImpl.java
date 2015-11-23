package se.inera.intyg.webcert.web.service.notification;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.inera.certificate.modules.support.api.notification.FragorOchSvar;
import se.inera.certificate.modules.support.api.notification.HandelseType;
import se.inera.certificate.modules.support.api.notification.NotificationMessage;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.persistence.utkast.model.Utkast;
import se.inera.webcert.persistence.utkast.repository.UtkastRepository;

@Component
public class NotificationMessageFactoryImpl implements NotificationMessageFactory {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationMessageFactoryImpl.class);

    private static final List<HandelseType> USES_FRAGOR_OCH_SVAR = Arrays.asList(HandelseType.FRAGA_FRAN_FK,
            HandelseType.SVAR_FRAN_FK, HandelseType.FRAGA_TILL_FK, HandelseType.FRAGA_FRAN_FK_HANTERAD,
            HandelseType.SVAR_FRAN_FK_HANTERAD, HandelseType.INTYG_MAKULERAT);

    @Autowired
    private FragorOchSvarCreator fragorOchSvarCreator;

    @Autowired
    private UtkastRepository utkastRepository;

    @Override
    public NotificationMessage createNotificationMessage(String intygsId, HandelseType handelse) {

        Utkast utkast = utkastRepository.findOne(intygsId);

        if (utkast == null) {
            LOG.error("Could not retrieve utkast with id {}", intygsId);
            return null;
        }

        return createNotificationMessage(utkast, handelse);
    }
    /*
     * (non-Javadoc)
     *
     * @see se.inera.intyg.webcert.web.service.notification.NotificationMessageFactory#createNotificationMessage(se.inera.intyg.webcert.web.
     * persistence.utkast.model.Utkast, se.inera.certificate.modules.support.api.notification.HandelseType)
     */
    @Override
    public NotificationMessage createNotificationMessage(Utkast utkast, HandelseType handelse) {

        String intygsId = utkast.getIntygsId();
        String intygsTyp = utkast.getIntygsTyp();

        LocalDateTime handelseTid = LocalDateTime.now();
        String logiskAdress = utkast.getEnhetsId();

        FragorOchSvar fragaSvar = FragorOchSvar.getEmpty();

        // Add a count of questions to the message
        if (USES_FRAGOR_OCH_SVAR.contains(handelse)) {
            fragaSvar = fragorOchSvarCreator.createFragorOchSvar(intygsId);
        }

        String utkastJson = utkast.getModel();

        return new NotificationMessage(intygsId, intygsTyp, handelseTid, handelse, logiskAdress, utkastJson, fragaSvar);
    }

    /*
     * (non-Javadoc)
     *
     * @see se.inera.intyg.webcert.web.service.notification.NotificationMessageFactory#createNotificationMessage(se.inera.intyg.webcert.web.
     * persistence.fragasvar.model.FragaSvar, se.inera.certificate.modules.support.api.notification.HandelseType)
     */
    @Override
    public NotificationMessage createNotificationMessage(FragaSvar fragaSvar, HandelseType handelse) {
        String intygsId = fragaSvar.getIntygsReferens().getIntygsId();
        return createNotificationMessage(intygsId, handelse);
    }

}
