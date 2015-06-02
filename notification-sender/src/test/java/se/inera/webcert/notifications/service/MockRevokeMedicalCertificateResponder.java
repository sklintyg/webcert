package se.inera.webcert.notifications.service;

import org.w3.wsaddressing10.AttributedURIType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificate.rivtabp20.v1.RevokeMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateResponseType;

public class MockRevokeMedicalCertificateResponder implements RevokeMedicalCertificateResponderInterface {
    @Override
    public RevokeMedicalCertificateResponseType revokeMedicalCertificate(AttributedURIType attributedURIType, RevokeMedicalCertificateRequestType revokeMedicalCertificateRequestType) {
        return null;
    }
}
