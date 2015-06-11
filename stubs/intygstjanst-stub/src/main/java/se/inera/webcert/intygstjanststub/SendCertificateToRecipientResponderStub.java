package se.inera.webcert.intygstjanststub;

import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ErrorIdEnum;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultOfCall;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientType;
import se.inera.webcert.intygstjanststub.mode.StubLatencyAware;
import se.inera.webcert.intygstjanststub.mode.StubModeAware;
import se.riv.clinicalprocess.healthcond.certificate.v1.*;

/**
 * Created by eriklupander on 2015-06-10.
 */
public class SendCertificateToRecipientResponderStub implements SendCertificateToRecipientResponderInterface {

    @Autowired
    private IntygStore intygStore;

    @Override
    @StubLatencyAware
    @StubModeAware
    public SendCertificateToRecipientResponseType sendCertificateToRecipient(String logicalAddress, SendCertificateToRecipientType parameters) {
        GetCertificateForCareResponseType fromStore = intygStore.getIntygForCertificateId(parameters.getUtlatandeId());

        SendCertificateToRecipientResponseType responseType = new SendCertificateToRecipientResponseType();

        if (fromStore == null) {
            ResultType resultOfCall = new ResultType();
            resultOfCall.setResultCode(ResultCodeType.ERROR);
            resultOfCall.setErrorId(ErrorIdType.APPLICATION_ERROR);
            responseType.setResult(resultOfCall);
            return responseType;
        }

        Utlatande intyg = fromStore.getCertificate();
        intyg.setSkickatdatum(LocalDateTime.now());
        intygStore.updateUtlatande(intyg);

        UtlatandeStatus sentStatus = new UtlatandeStatus();
        sentStatus.setTarget("FK");
        sentStatus.setTimestamp(LocalDateTime.now());
        sentStatus.setType(StatusType.SENT);
        intygStore.addStatus(parameters.getUtlatandeId(), sentStatus);

        ResultType resultType = new ResultType();
        resultType.setResultCode(ResultCodeType.OK);
        responseType.setResult(resultType);
        return responseType;
    }
}
