package se.inera.webcert.notifications.service;

import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientType;

public class MockSendCertificateToRecipientResponder implements SendCertificateToRecipientResponderInterface {
    @Override
    public SendCertificateToRecipientResponseType sendCertificateToRecipient(String logicalAddress, SendCertificateToRecipientType parameters) {
        return null;
    }
}
