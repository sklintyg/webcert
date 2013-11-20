package se.inera.webcert.intygstjanststub;

import org.springframework.beans.factory.annotation.Autowired;
import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareRequestType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponderInterface;
import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponseType;

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
    public GetCertificateForCareResponseType getCertificateForCare(String logicalAddress, GetCertificateForCareRequestType request) {

        return intygStore.getAllIntyg().get(request.getCertificateId());
    }


}
