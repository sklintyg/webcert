package se.inera.webcert.certificatesender.services.converter;

import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateRequestType;

import javax.xml.bind.JAXBException;

/**
 * Created by eriklupander on 2015-05-21.
 */
public interface RevokeRequestConverter {

    String toXml(RevokeMedicalCertificateRequestType request) throws JAXBException;

    RevokeMedicalCertificateRequestType fromXml(String xml) throws JAXBException;
}
