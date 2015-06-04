package se.inera.webcert.certificatesender.services.mock;

import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientResponseType;
import se.inera.webcert.client.SendCertificateServiceClient;
import se.riv.clinicalprocess.healthcond.certificate.v1.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultType;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by eriklupander on 2015-06-03.
 */
public class MockSendCertificateServiceClientImpl implements SendCertificateServiceClient {
    private AtomicInteger count = new AtomicInteger(0);

    @Override
    public SendCertificateToRecipientResponseType sendCertificate(String intygsId, String personId, String recipient, String logicalAddress) {
        count.incrementAndGet();
        return createResponse(ResultCodeType.OK, null);
    }

    public int getNumberOfReceivedMessages() {
        return count.get();
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
