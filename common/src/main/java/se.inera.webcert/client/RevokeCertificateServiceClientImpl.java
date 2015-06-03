package se.inera.webcert.client;

import javax.xml.bind.JAXBException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.w3.wsaddressing10.AttributedURIType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificate.rivtabp20.v1.RevokeMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateResponseType;
import se.inera.webcert.client.converter.RevokeRequestConverter;
import se.inera.webcert.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.webcert.service.exception.WebCertServiceException;

/**
 * Created by eriklupander on 2015-06-03.
 */
@Component
public class RevokeCertificateServiceClientImpl implements RevokeCertificateServiceClient {

    @Autowired
    private RevokeMedicalCertificateResponderInterface revokeService;

    @Autowired
    private RevokeRequestConverter revokeRequestConverter;

    @Override
    public RevokeMedicalCertificateResponseType revokeCertificate(String xml, String logicalAddress) {

        if (logicalAddress == null || logicalAddress.trim().length() == 0) {
            throw new IllegalArgumentException("Logical address missing, cannot invoke revokeMedicalCertificate service");
        }

        try {
            RevokeMedicalCertificateRequestType request = revokeRequestConverter.fromXml(xml);

            AttributedURIType uri = new AttributedURIType();
            uri.setValue(logicalAddress);

            return revokeService.revokeMedicalCertificate(uri, request);
        } catch (JAXBException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, e);
        }
    }

    private void validateArgument(String arg, String msg) {
        if (arg == null || arg.trim().length() == 0) {
            throw new IllegalArgumentException(msg);
        }
    }
}
