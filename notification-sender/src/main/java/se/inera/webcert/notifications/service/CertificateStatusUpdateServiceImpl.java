package se.inera.webcert.notifications.service;

import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareResponderInterface;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareType;
import se.inera.webcert.notifications.routes.RouteHeaders;

public class CertificateStatusUpdateServiceImpl implements CertificateStatusUpdateService {
    
    private static final Logger LOG = LoggerFactory.getLogger(CertificateStatusUpdateServiceImpl.class);
    
    @Value("${service.logicaladdress}")
    private String logicalAddress;
    
    @Autowired
    private CertificateStatusUpdateForCareResponderInterface statusUpdateForCareClient;

    /* (non-Javadoc)
     * @see se.inera.webcert.notifications.service.CertificateStatusUpdateService#sendStatusUpdate(java.lang.String, se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareType)
     */
    @Override
    public void sendStatusUpdate(@Header(RouteHeaders.INTYGS_ID) String intygsId, CertificateStatusUpdateForCareType request) throws Exception {
        
        LOG.debug("Sending status update for intyg '{}'", intygsId);
        
        CertificateStatusUpdateForCareResponseType response = statusUpdateForCareClient.certificateStatusUpdateForCare(logicalAddress, request);
        
    }
}
