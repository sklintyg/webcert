package se.inera.webcert.certificatesender.services.mock;

import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientResponseType;
import se.inera.webcert.client.SendCertificateServiceClient;
import se.riv.clinicalprocess.healthcond.certificate.v1.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultType;

/**
 * Created by eriklupander on 2015-06-03.
 */
public class MockSendCertificateServiceClientImpl implements SendCertificateServiceClient {
    private int count = 0;

    @Override
    public SendCertificateToRecipientResponseType sendCertificate(String s, String s1, String s2, String s3) {
        count++;
        return createResponse(ResultCodeType.OK, null);
    }

    public int getNumberOfReceivedMessages() {
        return count;
    }

    private SendCertificateToRecipientResponseType createResponse(ResultCodeType resultCodeType, ErrorIdType errorType) {
        ResultType resultType = new ResultType();
        resultType.setResultCode(resultCodeType);
        if (errorType != null) {
            resultType.setErrorId(errorType);
        }
        SendCertificateToRecipientResponseType responseType = new SendCertificateToRecipientResponseType();

        responseType.setResult(resultType);
        return responseType;
    }


}
