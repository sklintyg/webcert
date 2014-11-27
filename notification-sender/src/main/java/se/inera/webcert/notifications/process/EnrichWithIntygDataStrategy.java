package se.inera.webcert.notifications.process;

import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;

import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.EnhetType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.HosPersonalType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.UtlatandeType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.types.v1.HsaId;
import se.inera.certificate.clinicalprocess.healthcond.certificate.types.v1.TypAvUtlatandeTyp;
import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.model.VardpersonReferens;

public class EnrichWithIntygDataStrategy implements AggregationStrategy {
    
    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
                
        CertificateStatusUpdateForCareType statusUpdateType = oldExchange.getIn().getBody(CertificateStatusUpdateForCareType.class);
        UtlatandeType utlatandeType = statusUpdateType.getUtlatande();       
        
        Intyg intygsUtkast = newExchange.getIn().getBody(Intyg.class);
                
        //utlatandeType.setSigneringsdatum(intygsUtkast.get);
                
        TypAvUtlatandeTyp typAvUtlatande = new TypAvUtlatandeTyp();
        typAvUtlatande.setCode(intygsUtkast.getIntygsTyp());
        utlatandeType.setTypAvUtlatande(typAvUtlatande);
                
        /*HosPersonalType hoSPerson = createHosPersonal(intygsUtkast);
        utlatandeType.setSkapadAv(hoSPerson);*/
        
        // content that has to be extracted from the certificate model
        //utlatandeType.setDiagnos(value);
        //utlatandeType.getArbetsformaga()    
        
        return oldExchange;
    }
    
    private HosPersonalType createHosPersonal(Intyg intygsUtkast) {
        
        VardpersonReferens vardpersonReferens = intygsUtkast.getSkapadAv();
        
        HosPersonalType hoSPersonal = new HosPersonalType();
        hoSPersonal.setFullstandigtNamn(vardpersonReferens.getNamn());
        
        HsaId personHsaId = createHsaId(vardpersonReferens.getHsaId());
        hoSPersonal.setPersonalId(personHsaId);
                
        EnhetType vardEnhet = new EnhetType();
        vardEnhet.setEnhetsnamn(intygsUtkast.getEnhetsNamn());
        
        HsaId vardEnhetHsaId = createHsaId(vardpersonReferens.getHsaId());
        vardEnhet.setEnhetsId(vardEnhetHsaId);
                
        hoSPersonal.setEnhet(vardEnhet);
                
        return hoSPersonal;
    }
    
    private HsaId createHsaId(String id) {
        HsaId hsaId = new HsaId();
        hsaId.setRoot("adf");
        hsaId.setExtension(id);
        return hsaId;
    }
}
