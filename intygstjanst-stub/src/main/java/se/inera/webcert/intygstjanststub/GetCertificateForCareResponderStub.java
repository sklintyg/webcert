package se.inera.webcert.intygstjanststub;

import org.springframework.beans.factory.annotation.Autowired;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.ifv.insuranceprocess.healthreporting.getcertificateforcare.v1.rivtabp20.GetCertificateForCareResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.getcertificateforcareresponder.v1.GetCertificateForCareRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.getcertificateforcareresponder.v1.GetCertificateForCareResponseType;



/**
 * Stub class for mocking Intygstjanstens {@link GetCertificateForCareResponderStub} WS interface
 * Uses a simple in memory store for complete  {@link GetCertificateForCareResponseType} responses
 * 
 * @author marced
 */
public class GetCertificateForCareResponderStub implements GetCertificateForCareResponderInterface {

    @Autowired
    private IntygStore intygStore;
    

    @Override
    public GetCertificateForCareResponseType getCertificateForCare(AttributedURIType logicalAddress, GetCertificateForCareRequestType request) {
        return intygStore.getAllIntyg().get(request.getCertificateId());
    }


   
}
