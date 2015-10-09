package se.inera.webcert.intygstjanststub;

import java.util.List;

import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificate.rivtabp20.v1.RevokeMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ErrorIdEnum;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultOfCall;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponseType;
import se.inera.webcert.intygstjanststub.mode.StubModeAware;
import se.riv.clinicalprocess.healthcond.certificate.v1.StatusType;
import se.riv.clinicalprocess.healthcond.certificate.v1.UtlatandeStatus;

public class RevokeMedicalCertificateResponderStub implements RevokeMedicalCertificateResponderInterface {

    @Autowired
    private IntygStore intygStore;

    @Override
    @StubModeAware
    public RevokeMedicalCertificateResponseType revokeMedicalCertificate(AttributedURIType attributedURIType, RevokeMedicalCertificateRequestType revokeMedicalCertificateRequestType) {
        GetCertificateForCareResponseType certResponseType = intygStore.getIntygForCertificateId(revokeMedicalCertificateRequestType.getRevoke().getLakarutlatande().getLakarutlatandeId());

        RevokeMedicalCertificateResponseType responseType = new RevokeMedicalCertificateResponseType();
        ResultOfCall resultOfCall = new ResultOfCall();

        if (certResponseType == null) {
            resultOfCall.setResultCode(ResultCodeEnum.ERROR);
            resultOfCall.setErrorId(ErrorIdEnum.APPLICATION_ERROR);
            responseType.setResult(resultOfCall);
            return responseType;
        }

        if (!isRevoked(certResponseType.getMeta().getStatus())) {
            UtlatandeStatus revokedStatus = new UtlatandeStatus();
            revokedStatus.setTimestamp(LocalDateTime.now());
            revokedStatus.setType(StatusType.CANCELLED);
            revokedStatus.setTarget(attributedURIType.getValue());
            intygStore.addStatus(certResponseType.getCertificate().getUtlatandeId().getExtension(), revokedStatus);
        }


        resultOfCall.setResultCode(ResultCodeEnum.OK);
        responseType.setResult(resultOfCall);
        return responseType;
    }

    private boolean isRevoked(List<UtlatandeStatus> statuses) {
        for (UtlatandeStatus status : statuses) {
            if (status.getType() == StatusType.CANCELLED) {
                return true;
            }
        }
        return false;
    }
}
