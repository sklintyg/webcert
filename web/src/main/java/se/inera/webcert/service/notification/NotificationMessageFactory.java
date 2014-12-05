package se.inera.webcert.service.notification;

import org.joda.time.LocalDateTime;

import se.inera.webcert.notifications.message.v1.*;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.persistence.intyg.model.Intyg;

/**
 * Created by Magnus Ekstrand on 03/12/14
 */
public class NotificationMessageFactory {

    private static ObjectFactory objectFactory = new ObjectFactory();

    /* -- Create notification messages based on certificate -- */

    public static NotificationRequestType createNotificationFromRevokedCertificate(Intyg intyg) {

        NotificationRequestType nrt = createNotification(intyg, true);
        nrt.setHandelse(HandelseType.INTYG_MAKULERAT);

        return nrt;
    }

    public static NotificationRequestType createNotificationFromSentCertificate(Intyg intyg) {

        NotificationRequestType nrt = createNotification(intyg, true);
        nrt.setHandelse(HandelseType.INTYG_SKICKAT_FK);

        return nrt;
    }

    /* -- Create notification messages from certificate draft -- */

    public static NotificationRequestType createNotificationFromChangedCertificateDraft(Intyg utkast) {

        NotificationRequestType nrt = createNotification(utkast, true);
        nrt.setHandelse(HandelseType.INTYGSUTKAST_ANDRAT);

        return nrt;
    }

    public static NotificationRequestType createNotificationFromDeletedDraft(Intyg utkast) {

        NotificationRequestType nrt = createNotification(utkast, true);
        nrt.setHandelse(HandelseType.INTYGSUTKAST_RADERAT);

        return nrt;
    }

    public static NotificationRequestType createNotificationFromSignedDraft(Intyg utkast) {

        NotificationRequestType nrt = createNotification(utkast, true);
        nrt.setHandelse(HandelseType.INTYGSUTKAST_SIGNERAT);

        return nrt;
    }

    public static NotificationRequestType createNotificationFromCreatedDraft(Intyg utkast) {

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

    public static NotificationRequestType createNotificationFromManagedQuestionFromFK(FragaSvar fragaSvar) {

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

    public static NotificationRequestType createNotificationFromManagedAnswerFromFK(FragaSvar fragaSvar) {

        NotificationRequestType nrt = createNotification(fragaSvar);
        nrt.setHandelse(HandelseType.SVAR_FRAN_FK_HANTERAD);

        return nrt;
    }

    /* -- Non-public helper methods -- */

    static NotificationRequestType createNotification(Intyg intyg, boolean includeHsaPerson) {

        VardenhetType vt = getVardenhetType(intyg.getEnhetsId(), intyg.getEnhetsNamn());
        HoSPersonType hspt = getHoSPersonType(intyg.getSenastSparadAv().getNamn(), intyg.getSenastSparadAv().getHsaId(), vt);

        return getNotificationRequestType(intyg.getSenastSparadDatum(), hspt,
                intyg.getIntygsId(), intyg.getIntygsTyp());
    }

    static NotificationRequestType createNotification(FragaSvar fragaSvar) {


        VardenhetType vt = getVardenhetType(fragaSvar.getVardperson().getEnhetsId(), fragaSvar.getVardperson().getEnhetsnamn());
        HoSPersonType hspt = getHoSPersonType(fragaSvar.getVardperson().getNamn(), fragaSvar.getVardperson().getHsaId(), vt);

        return getNotificationRequestType(fragaSvar.getSenasteHandelse(), hspt,
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
