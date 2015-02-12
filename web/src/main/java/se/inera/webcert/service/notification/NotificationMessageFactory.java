package se.inera.webcert.service.notification;

import org.joda.time.LocalDateTime;

import se.inera.webcert.notifications.message.v1.HandelseType;
import se.inera.webcert.notifications.message.v1.HoSPersonType;
import se.inera.webcert.notifications.message.v1.NotificationRequestType;
import se.inera.webcert.notifications.message.v1.ObjectFactory;
import se.inera.webcert.notifications.message.v1.VardenhetType;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.persistence.utkast.model.Utkast;

/**
 * Factory class exposing methods which creates notifications messages.
 *
 * Created by Magnus Ekstrand on 03/12/14
 */
public class NotificationMessageFactory {

    private static ObjectFactory objectFactory = new ObjectFactory();

    /* -- Create notification messages based on certificate -- */

    public static NotificationRequestType createNotificationFromRevokedCertificate(Utkast utkast) {

        NotificationRequestType nrt = createNotification(utkast, true);
        nrt.setHandelse(HandelseType.INTYG_MAKULERAT);

        return nrt;
    }

    public static NotificationRequestType createNotificationFromSentCertificate(Utkast utkast) {

        NotificationRequestType nrt = createNotification(utkast, true);
        nrt.setHandelse(HandelseType.INTYG_SKICKAT_FK);

        return nrt;
    }

    /* -- Create notification messages from certificate draft -- */

    public static NotificationRequestType createNotificationFromChangedCertificateDraft(Utkast utkast) {

        NotificationRequestType nrt = createNotification(utkast, true);
        nrt.setHandelse(HandelseType.INTYGSUTKAST_ANDRAT);

        return nrt;
    }

    /**
     * The INTYGSUTKAST_RADERAT is a bit special since we must set utfardandeEnhetsId.
     * 
     * @param utkast
     * @return
     */
    public static NotificationRequestType createNotificationFromDeletedDraft(Utkast utkast) {

        NotificationRequestType nrt = createNotification(utkast, true);
        nrt.setHandelse(HandelseType.INTYGSUTKAST_RADERAT);
        nrt.setUtfardandeEnhetsId(utkast.getEnhetsId());
        
        return nrt;
    }

    public static NotificationRequestType createNotificationFromSignedDraft(Utkast utkast) {

        NotificationRequestType nrt = createNotification(utkast, true);
        nrt.setHandelse(HandelseType.INTYGSUTKAST_SIGNERAT);

        return nrt;
    }

    public static NotificationRequestType createNotificationFromCreatedDraft(Utkast utkast) {

        NotificationRequestType nrt = createNotification(utkast, true);
        nrt.setHandelse(HandelseType.INTYGSUTKAST_SKAPAT);

        return nrt;
    }

    /* -- Create notification messages from question and answers -- */

    public static NotificationRequestType createNotificationFromQuestionFromFK(FragaSvar fragaSvar) {

        NotificationRequestType nrt = createNotification(fragaSvar);
        nrt.setHandelse(HandelseType.FRAGA_FRAN_FK);

        return nrt;
    }

    public static NotificationRequestType createNotificationFromClosedQuestionFromFK(FragaSvar fragaSvar) {

        NotificationRequestType nrt = createNotification(fragaSvar);
        nrt.setHandelse(HandelseType.FRAGA_FRAN_FK_HANTERAD);

        return nrt;
    }

    public static NotificationRequestType createNotificationFromQuestionToFK(FragaSvar fragaSvar) {

        NotificationRequestType nrt = createNotification(fragaSvar);
        nrt.setHandelse(HandelseType.FRAGA_TILL_FK);

        return nrt;
    }

    public static NotificationRequestType createNotificationFromAnswerFromFK(FragaSvar fragaSvar) {

        NotificationRequestType nrt = createNotification(fragaSvar);
        nrt.setHandelse(HandelseType.SVAR_FRAN_FK);

        return nrt;
    }

    public static NotificationRequestType createNotificationFromClosedAnswerFromFK(FragaSvar fragaSvar) {

        NotificationRequestType nrt = createNotification(fragaSvar);
        nrt.setHandelse(HandelseType.SVAR_FRAN_FK_HANTERAD);

        return nrt;
    }

    /* -- Non-public helper methods -- */

    static NotificationRequestType createNotification(Utkast utkast, boolean includeHsaPerson) {

        VardenhetType vt = getVardenhetType(utkast.getEnhetsId(), utkast.getEnhetsNamn());
        HoSPersonType hspt = getHoSPersonType(utkast.getSenastSparadAv().getNamn(), utkast.getSenastSparadAv().getHsaId(), vt);

        return getNotificationRequestType(LocalDateTime.now(), hspt,
                utkast.getIntygsId(), utkast.getIntygsTyp());
    }

    static NotificationRequestType createNotification(FragaSvar fragaSvar) {

        VardenhetType vt = getVardenhetType(fragaSvar.getVardperson().getEnhetsId(), fragaSvar.getVardperson().getEnhetsnamn());
        HoSPersonType hspt = getHoSPersonType(fragaSvar.getVardperson().getNamn(), fragaSvar.getVardperson().getHsaId(), vt);

        return getNotificationRequestType(LocalDateTime.now(), hspt,
                fragaSvar.getIntygsReferens().getIntygsId(), fragaSvar.getIntygsReferens().getIntygsTyp());
    }

    private static VardenhetType getVardenhetType(String hsaId, String enhetsNamn) {

        VardenhetType vt = objectFactory.createVardenhetType();
        vt.setHsaId(hsaId);
        vt.setEnhetsNamn(enhetsNamn);

        return vt;
    }

    private static HoSPersonType getHoSPersonType(String namn, String hsaId, VardenhetType vt) {

        HoSPersonType hspt = objectFactory.createHoSPersonType();
        hspt.setFullstandigtNamn(namn);
        hspt.setHsaId(hsaId);
        hspt.setVardenhet(vt);

        return hspt;
    }

    static NotificationRequestType getNotificationRequestType(LocalDateTime handelseTidpunkt, HoSPersonType hoSPersonType, String intygsId, String intygsTyp) {

        NotificationRequestType nrt = objectFactory.createNotificationRequestType();
        nrt.setHandelseTidpunkt(handelseTidpunkt);
        nrt.setHoSPerson(hoSPersonType);
        nrt.setIntygsId(intygsId);
        nrt.setIntygsTyp(intygsTyp);

        return nrt;
    }

}
