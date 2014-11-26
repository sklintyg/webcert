package se.inera.webcert.notifications.process;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.UtlatandeType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.types.v1.TypAvUtlatandeTyp;
import se.inera.certificate.clinicalprocess.healthcond.certificate.types.v1.UtlatandeId;
import se.inera.webcert.notifications.message.v1.NotificationRequestType;


public class CreateAndInitCertificateStatusRequestProcessor implements Processor {
    
    private static final Logger LOG = LoggerFactory.getLogger(CreateAndInitCertificateStatusRequestProcessor.class);

    private static final String INTYGSID_ROOT = "acb";

    @Override
    public void process(Exchange exchange) throws Exception {
        
        Message inMsg = exchange.getIn();
        
        NotificationRequestType request = inMsg.getBody(NotificationRequestType.class);
        String intygsId = inMsg.getHeader("intygsId", String.class);
        
        LOG.debug("Creating CertificateStatusUpdate for certificate {}, event {}", intygsId, request.getHandelse());
        
        UtlatandeId utlatandeId = new UtlatandeId();
        utlatandeId.setRoot(INTYGSID_ROOT);
        utlatandeId.setExtension(intygsId);
        
        TypAvUtlatandeTyp typAvUtlatande = new TypAvUtlatandeTyp();
        typAvUtlatande.setCode(request.getIntygsTyp());
        
        UtlatandeType utlatandeType = new UtlatandeType();
        utlatandeType.setUtlatandeId(utlatandeId);
        utlatandeType.setTypAvUtlatande(typAvUtlatande);
        
        CertificateStatusUpdateForCareType statusUpdateType = new CertificateStatusUpdateForCareType();
        statusUpdateType.setUtlatande(utlatandeType);
        
        exchange.getIn().setHeader("intygsId", intygsId);
        exchange.getIn().setHeader("event", request.getHandelse());
        
        exchange.getIn().setBody(statusUpdateType);
    }

}
