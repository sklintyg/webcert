package se.inera.webcert.service.notification;

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

    @Autowired
    private FragorOchSvarCreator fragorOchSvarCreator;

    @Autowired
    private UtkastRepository utkastRepository;

    /* (non-Javadoc)
     * @see se.inera.webcert.service.notification.NotificationMessageFactory#createNotificationMessage(se.inera.webcert.persistence.utkast.model.Utkast, se.inera.certificate.modules.support.api.notification.HandelseType)
     */
    @Override
    public NotificationMessage createNotificationMessage(Utkast utkast, HandelseType handelse) {

        String intygsId = utkast.getIntygsId();
        String intygsTyp = utkast.getIntygsTyp();

        LocalDateTime handelseTid = LocalDateTime.now();
        String logiskAdress = utkast.getEnhetsId();

        FragorOchSvar fragaSvar = FragorOchSvar.getEmpty();

        String utkastJson = utkast.getModel();

        return new NotificationMessage(intygsId, intygsTyp, handelseTid, handelse, logiskAdress, utkastJson, fragaSvar);
    }

    /* (non-Javadoc)
     * @see se.inera.webcert.service.notification.NotificationMessageFactory#createNotificationMessage(se.inera.webcert.persistence.fragasvar.model.FragaSvar, se.inera.certificate.modules.support.api.notification.HandelseType)
     */
    @Override
    public NotificationMessage createNotificationMessage(FragaSvar fragaSvar, HandelseType handelse) {

        String intygsId = fragaSvar.getIntygsReferens().getIntygsId();
        String intygsTyp = fragaSvar.getIntygsReferens().getIntygsTyp();

        Utkast utkast = utkastRepository.findOne(intygsId);
        
        if (utkast == null) {
            LOG.error("Could not retrieve utkast with id {}", intygsId);
            return null;
        }
        
        String logiskAdress = utkast.getEnhetsId();
        String utkastJson = utkast.getModel();

        LocalDateTime handelseTid = LocalDateTime.now();

        FragorOchSvar fragorOchSvar = fragorOchSvarCreator.createFragorOchSvar(intygsId);

        return new NotificationMessage(intygsId, intygsTyp, handelseTid, handelse, logiskAdress, utkastJson, fragorOchSvar);
    }

}
