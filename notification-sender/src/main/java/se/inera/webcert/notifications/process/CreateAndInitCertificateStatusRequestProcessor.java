package se.inera.webcert.notifications.process;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.EnhetType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.FragorOchSvarType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.HandelseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.HosPersonalType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.UtlatandeType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.types.v1.HandelsekodCodeRestrictionType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.types.v1.HsaId;
import se.inera.certificate.clinicalprocess.healthcond.certificate.types.v1.TypAvUtlatandeTyp;
import se.inera.certificate.clinicalprocess.healthcond.certificate.types.v1.UtlatandeId;
import se.inera.webcert.notifications.message.v1.HoSPersonType;
import se.inera.webcert.notifications.message.v1.NotificationRequestType;
import se.inera.webcert.notifications.routes.RouteHeaders;

public class CreateAndInitCertificateStatusRequestProcessor implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(CreateAndInitCertificateStatusRequestProcessor.class);

    private static final String INTYGSID_ROOT = "1.2.752.129.2.1.2.1";

    private static final String HSAID_ROOT = "1.2.752.129.2.1.4.1";

    private static final String TYPAVUTLATANDE_CODESYSTEM = "f6fb361a-e31d-48b8-8657-99b63912dd9b";

    private static final String TYPAVUTLATANDE_CODESYSTEM_NAME = "kv_utlåtandetyp_intyg";

    private static final String TYPAVUTLATANDE_DISPLAYNAME = "Läkarintyg enligt 3 kap. 8 § lagen (1962:381) om allmän försäkring";

    @Override
    public void process(Exchange exchange) throws Exception {

        Message inMsg = exchange.getIn();

        NotificationRequestType request = inMsg.getBody(NotificationRequestType.class);
        String intygsId = inMsg.getHeader(RouteHeaders.INTYGS_ID, String.class);

        LOG.debug("Creating CertificateStatusUpdate for certificate {}, event {}", intygsId, request.getHandelse());

        UtlatandeType utlatandeType = new UtlatandeType();

        UtlatandeId utlatandeId = createUtlatandeId(intygsId);
        utlatandeType.setUtlatandeId(utlatandeId);

        TypAvUtlatandeTyp typAvUtlatande = createTypAvUtlatande(request);
        utlatandeType.setTypAvUtlatande(typAvUtlatande);

        HandelseType handelseType = new HandelseType();
        handelseType.setHandelsekod(convertToHandelsekod(request.getHandelse()));
        handelseType.setHandelsetidpunkt(request.getHandelseTidpunkt());

        utlatandeType.setHandelse(handelseType);

        FragorOchSvarType fosType = createDefaultFragorOchSvarType();
        utlatandeType.setFragorOchSvar(fosType);

        if (se.inera.webcert.notifications.message.v1.HandelseType.INTYGSUTKAST_RADERAT.equals(request.getHandelse())) {
            LOG.debug("Decorating with HoSPerson from request");
            decorateWithHoSPersonFromRequest(utlatandeType, request.getHoSPerson());
        }

        CertificateStatusUpdateForCareType statusUpdateType = new CertificateStatusUpdateForCareType();
        statusUpdateType.setUtlatande(utlatandeType);

        exchange.getIn().setBody(statusUpdateType);
    }

    private void decorateWithHoSPersonFromRequest(UtlatandeType utlatandeType, HoSPersonType hoSPerson) {

        HosPersonalType hoSPersonalType = new HosPersonalType();
        hoSPersonalType.setFullstandigtNamn(hoSPerson.getFullstandigtNamn());

        HsaId personHsaId = createHsaId(hoSPerson.getHsaId());
        hoSPersonalType.setPersonalId(personHsaId);

        EnhetType vardEnhet = new EnhetType();
        vardEnhet.setEnhetsnamn(hoSPerson.getVardenhet().getEnhetsNamn());

        HsaId vardEnhetHsaId = createHsaId(hoSPerson.getVardenhet().getHsaId());
        vardEnhet.setEnhetsId(vardEnhetHsaId);

        hoSPersonalType.setEnhet(vardEnhet);

        utlatandeType.setSkapadAv(hoSPersonalType);
    }

    private HsaId createHsaId(String id) {
        HsaId hsaId = new HsaId();
        hsaId.setRoot(HSAID_ROOT);
        hsaId.setExtension(id);
        return hsaId;
    }

    private UtlatandeId createUtlatandeId(String intygsId) {
        UtlatandeId utlatandeId = new UtlatandeId();
        utlatandeId.setRoot(INTYGSID_ROOT);
        utlatandeId.setExtension(intygsId);
        return utlatandeId;
    }

    private TypAvUtlatandeTyp createTypAvUtlatande(NotificationRequestType request) {
        TypAvUtlatandeTyp typAvUtlatande = new TypAvUtlatandeTyp();
        typAvUtlatande.setCode(request.getIntygsTyp());
        typAvUtlatande.setCodeSystem(TYPAVUTLATANDE_CODESYSTEM);
        typAvUtlatande.setCodeSystemName(TYPAVUTLATANDE_CODESYSTEM_NAME);
        typAvUtlatande.setDisplayName(TYPAVUTLATANDE_DISPLAYNAME);
        return typAvUtlatande;
    }

    private FragorOchSvarType createDefaultFragorOchSvarType() {
        FragorOchSvarType fosType = new FragorOchSvarType();
        fosType.setAntalFragor(0);
        fosType.setAntalHanteradeFragor(0);
        fosType.setAntalHanteradeSvar(0);
        fosType.setAntalSvar(0);
        return fosType;
    }

    private HandelsekodCodeRestrictionType convertToHandelsekod(se.inera.webcert.notifications.message.v1.HandelseType handelse) {
        switch (handelse) {
        case FRAGA_FRAN_FK:
            return HandelsekodCodeRestrictionType.HAN_6;
        case FRAGA_TILL_FK:
            return HandelsekodCodeRestrictionType.HAN_8;
        case FRAGA_FRAN_FK_HANTERAD:
            return HandelsekodCodeRestrictionType.HAN_9;
        case INTYG_MAKULERAT:
            return HandelsekodCodeRestrictionType.HAN_5;
        case INTYG_SKICKAT_FK:
            return HandelsekodCodeRestrictionType.HAN_3;
        case INTYGSUTKAST_ANDRAT:
            return HandelsekodCodeRestrictionType.HAN_11;
        case INTYGSUTKAST_RADERAT:
            return HandelsekodCodeRestrictionType.HAN_4;
        case INTYGSUTKAST_SIGNERAT:
            return HandelsekodCodeRestrictionType.HAN_2;
        case INTYGSUTKAST_SKAPAT:
            return HandelsekodCodeRestrictionType.HAN_1;
        case SVAR_FRAN_FK:
            return HandelsekodCodeRestrictionType.HAN_7;
        case SVAR_FRAN_FK_HANTERAD:
            return HandelsekodCodeRestrictionType.HAN_10;
        default:
            return null;
        }
    }

}
