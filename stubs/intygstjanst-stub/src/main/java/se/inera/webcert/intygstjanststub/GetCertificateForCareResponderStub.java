package se.inera.webcert.intygstjanststub;

import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareRequestType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.v1.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultType;

/**
 * Stub class for mocking Intygstjanstens {@link GetCertificateForCareResponderStub} WS interface Uses a simple in
 * memory store for complete {@link GetCertificateForCareResponseType} responses.
 *
 * @author marced
 */
public class GetCertificateForCareResponderStub implements GetCertificateForCareResponderInterface {

    @Autowired
    private IntygStore intygStore;

    @Override
    public GetCertificateForCareResponseType getCertificateForCare(String logicalAddress,
                                                                   GetCertificateForCareRequestType request) {

        GetCertificateForCareResponseType intygResponse = intygStore.getAllIntyg().get(request.getCertificateId());
        if (intygResponse != null) {
            return intygResponse;
        } else {
            return buildNotFoundResponse();
        }
    }

    private GetCertificateForCareResponseType buildNotFoundResponse() {
        GetCertificateForCareResponseType response = new GetCertificateForCareResponseType();
        ResultType resultType = new ResultType();
        resultType.setResultCode(ResultCodeType.ERROR);
        resultType.setErrorId(ErrorIdType.VALIDATION_ERROR);
        resultType.setResultText("Intyg not found in stub");
        response.setResult(resultType);
        return response;

    }

}
