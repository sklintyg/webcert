package se.inera.webcert.service.notification;

import se.inera.webcert.notifications.message.v1.HandelseType;
import se.inera.webcert.notifications.message.v1.HoSPersonType;
import se.inera.webcert.notifications.message.v1.VardenhetType;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.notifications.message.v1.NotificationRequestType;

/**
 * Created by Magnus Ekstrand on 03/12/14.
 */
public class NotificationFactory {

    /* -- Create notification messages based on certificate -- */

    public static NotificationRequestType createNotificationFromRevokedCertificate(Intyg intyg) {

        NotificationRequestType nrt = createNotification(intyg);
        nrt.setHandelse(HandelseType.INTYG_MAKULERAT);

        return nrt;
    }

    public static NotificationRequestType createNotificationFromSentCertificate(Intyg intyg) {

        NotificationRequestType nrt = createNotification(intyg);
        nrt.setHandelse(HandelseType.INTYG_SKICKAT_FK);

        return nrt;
    }

    /* -- Create notification messages from certificate draft -- */

    public static NotificationRequestType createNotificationFromChangedCertificateDraft(Intyg utkast) {

        NotificationRequestType nrt = createNotification(utkast);
        nrt.setHandelse(HandelseType.INTYGSUTKAST_ANDRAT);

        return nrt;
    }

    public static NotificationRequestType createNotificationFromDeletedDraft(Intyg utkast) {

        NotificationRequestType nrt = createNotification(utkast);
        nrt.setHandelse(HandelseType.INTYGSUTKAST_RADERAT);

        return nrt;
    }

    public static NotificationRequestType createNotificationFromSignedDraft(Intyg utkast) {

        NotificationRequestType nrt = createNotification(utkast);
        nrt.setHandelse(HandelseType.INTYGSUTKAST_SIGNERAT);

        return nrt;
    }

    public static NotificationRequestType createNotificationFromCreatedDraft(Intyg utkast) {

        NotificationRequestType nrt = createNotification(utkast);
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

    static NotificationRequestType createNotification(Intyg intyg) {

        VardenhetType vt = new VardenhetType();
        vt.setHsaId(intyg.getEnhetsId());
        vt.setEnhetsNamn(intyg.getEnhetsNamn());

        HoSPersonType hspt = new HoSPersonType();
        hspt.setFullstandigtNamn(intyg.getSkapadAv().getNamn());
        hspt.setHsaId(intyg.getSkapadAv().getHsaId());
        hspt.setVardenhet(vt);

        NotificationRequestType nrt = new NotificationRequestType();
        nrt.setHandelseTidpunkt(intyg.getSenastSparadDatum());
        nrt.setHoSPerson(hspt);
        nrt.setIntygsId(intyg.getIntygsId());
        nrt.setIntygsTyp(intyg.getIntygsTyp());

        return nrt;
    }

    static NotificationRequestType createNotification(FragaSvar fragaSvar) {

        VardenhetType vt = new VardenhetType();
        vt.setHsaId(fragaSvar.getVardperson().getEnhetsId());
        vt.setEnhetsNamn(fragaSvar.getVardperson().getEnhetsnamn());

        HoSPersonType hspt = new HoSPersonType();
        hspt.setFullstandigtNamn(fragaSvar.getVardperson().getNamn());
        hspt.setHsaId(fragaSvar.getVardperson().getHsaId());
        hspt.setVardenhet(vt);

        NotificationRequestType nrt = new NotificationRequestType();
        nrt.setHandelseTidpunkt(fragaSvar.getSenasteHandelse());
        nrt.setHoSPerson(hspt);
        nrt.setIntygsId(fragaSvar.getIntygsReferens().getIntygsId());
        nrt.setIntygsTyp(fragaSvar.getIntygsReferens().getIntygsTyp());

        return nrt;
    }

}
